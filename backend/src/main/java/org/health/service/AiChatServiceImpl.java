package org.health.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.health.mapper.FoodRecordMapper;
import org.health.mapper.MoodRecordMapper;
import org.health.mapper.SportRecordMapper;
import org.health.pojo.AiChatResponse;
import org.health.pojo.FoodRecord;
import org.health.pojo.MoodRecord;
import org.health.pojo.SportRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Base64;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AiChatServiceImpl implements AiChatService {
    private static final String DISCLAIMER = "AI 建议仅供健康生活参考，不能替代医生诊断、治疗或紧急医疗帮助。";
    private static final List<String> SOURCES = List.of(
            "《中国居民膳食指南（2022）》｜中国营养学会｜https://www.cnsoc.org/",
            "《身体活动和久坐行为指南》｜世界卫生组织｜https://www.who.int/publications/i/item/9789240015128",
            "《健康中国行动（2019—2030年）》｜国家卫生健康委员会｜https://www.gov.cn/");

    private final FoodRecordMapper foodRecordMapper;
    private final MoodRecordMapper moodRecordMapper;
    private final SportRecordMapper sportRecordMapper;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().build();
    private final Map<String, Integer> dailyUsage = new ConcurrentHashMap<>();
    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final int dailyLimit;

    public AiChatServiceImpl(FoodRecordMapper foodRecordMapper,
                             MoodRecordMapper moodRecordMapper,
                             SportRecordMapper sportRecordMapper,
                             ObjectMapper objectMapper,
                             @Value("${ai.doubao.base-url}") String baseUrl,
                             @Value("${ai.doubao.api-key}") String apiKey,
                             @Value("${ai.doubao.model}") String model,
                             @Value("${ai.daily-request-limit}") int dailyLimit) {
        this.foodRecordMapper = foodRecordMapper;
        this.moodRecordMapper = moodRecordMapper;
        this.sportRecordMapper = sportRecordMapper;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.apiKey = apiKey;
        this.model = model;
        this.dailyLimit = dailyLimit;
    }

    @Override
    public AiChatResponse chat(Integer userId, String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("问题不能为空");
        }
        if (message.length() > 1000) {
            throw new IllegalArgumentException("单次问题不能超过 1000 个字符");
        }
        AiChatResponse safetyResponse = safetyResponse(message);
        if (safetyResponse != null) {
            return safetyResponse;
        }
        validateConfigured();
        String usageKey = ensureQuota(userId);
        List<String> workflow = new ArrayList<>();
        workflow.add("正在判断问题需要哪些健康数据");
        String responseText = runAgent(userId, message, workflow);
        workflow.add("已生成个性化建议");
        return new AiChatResponse(responseText, model, consumeRequest(usageKey), DISCLAIMER, workflow, SOURCES);
    }

    private AiChatResponse safetyResponse(String message) {
        String normalized = message.toLowerCase();
        boolean emergency = List.of("胸痛", "胸口痛", "呼吸困难", "喘不过气", "昏厥", "大量出血",
                "想自杀", "自杀", "自伤", "伤害自己", "不想活", "轻生").stream().anyMatch(normalized::contains);
        if (emergency) {
            String reply = "你描述的情况可能需要及时的专业帮助。若你或他人正处于立即危险中，请马上联系当地急救/报警服务，或前往最近的急诊；如果涉及自伤风险，请尽量不要独处，并立即联系可信任的人陪伴你。此处 AI 不能评估或处理紧急状况。";
            return new AiChatResponse(reply, "安全应答（未调用模型）", dailyLimit, DISCLAIMER,
                    List.of("检测到可能的紧急风险", "已跳过模型调用并提供紧急求助指引"), List.of());
        }
        boolean unsafeDiet = List.of("绝食", "催吐", "极端节食", "不吃饭减肥", "快速瘦", "一天只吃", "不吃东西").stream().anyMatch(normalized::contains);
        if (unsafeDiet) {
            String reply = "我不能帮助制定绝食、催吐或极端节食方案。这些做法可能伤害身体。更稳妥的方向是规律进餐、选择多样化食物，并结合适合自己的活动；如果你正被体重或进食困扰，建议和医生或注册营养师讨论个体化方案。";
            return new AiChatResponse(reply, "安全应答（未调用模型）", dailyLimit, DISCLAIMER,
                    List.of("检测到可能有害的节食请求", "已跳过模型调用并提供更安全的替代方向"), List.of());
        }
        return null;
    }

    @Override
    public AiChatResponse dailyReport(Integer userId) {
        validateConfigured();
        String usageKey = ensureQuota(userId);
        LocalDate today = LocalDate.now();
        List<FoodRecord> foods = foodRecordMapper.selectByUserId(userId).stream()
                .filter(item -> item.getEatTime() != null && item.getEatTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().equals(today))
                .toList();
        List<SportRecord> sports = sportRecordMapper.selectByUserId(userId).stream()
                .filter(item -> item.getRecordTime() != null && item.getRecordTime().toLocalDate().equals(today))
                .toList();
        List<MoodRecord> moods = moodRecordMapper.selectByUserId(userId).stream()
                .filter(item -> item.getRecordTime() != null && item.getRecordTime().toLocalDate().equals(today))
                .toList();

        String instructions = "你是健康伴侣应用中的中文健康助手。请基于用户今天的记录写一份不超过 220 字的日报。"
                + "必须使用三个小标题：今日概况、做得不错、明日一小步。"
                + "语气温和、具体、可执行；不诊断疾病、不推荐药物。"
                + "若当天没有足够数据，坦诚说明并建议补充记录。\n"
                + "日期：" + today + "\n"
                + "饮食：" + summarizeFoods(foods) + "\n"
                + "运动：" + summarizeSports(sports) + "\n"
                + "情绪：" + summarizeMoods(moods);
        String report = callChatCompletion(instructions, "请生成我的今日健康报告。", false).path("choices").path(0).path("message").path("content").asText();
        return new AiChatResponse(report, model, consumeRequest(usageKey), DISCLAIMER, List.of("已汇总今日饮食、运动与情绪记录", "已生成今日健康报告"), SOURCES);
    }

    @Override
    public AiChatResponse identifyFood(Integer userId, MultipartFile image) {
        if (image == null || image.isEmpty()) throw new IllegalArgumentException("请上传食物图片");
        if (image.getSize() > 8 * 1024 * 1024) throw new IllegalArgumentException("图片不能超过 8MB");
        validateConfigured();
        String type = image.getContentType() == null ? "image/jpeg" : image.getContentType();
        String dataUrl;
        try { dataUrl = "data:" + type + ";base64," + Base64.getEncoder().encodeToString(image.getBytes()); }
        catch (IOException e) { throw new IllegalStateException("图片读取失败"); }
        Map<String, Object> contentText = Map.of("type", "text", "text", "识别图片中的主要食物，估算一份常见分量的热量。只返回 JSON：{\"foodName\":\"\",\"calories\":0,\"note\":\"\"}，不要添加 Markdown。");
        Map<String, Object> contentImage = Map.of("type", "image_url", "image_url", Map.of("url", dataUrl));
        List<Map<String, Object>> messages = List.of(Map.of("role", "system", "content", "你是食物识别助手。热量是估算值，不能替代营养标签。"), Map.of("role", "user", "content", List.of(contentText, contentImage)));
        JsonNode result = callChatCompletion(messages, false).path("choices").path(0).path("message").path("content");
        String json = result.asText().replace("```json", "").replace("```", "").trim();
        return new AiChatResponse(json, model, dailyLimit, DISCLAIMER, List.of("正在识别食物图片", "已估算食物热量，请确认后保存"), SOURCES);
    }

    @Override
    public AiChatResponse identifyWorkout(Integer userId, MultipartFile image) {
        if (image == null || image.isEmpty()) throw new IllegalArgumentException("请上传运动手环截图");
        if (image.getSize() > 8 * 1024 * 1024) throw new IllegalArgumentException("图片不能超过 8MB");
        validateConfigured();
        String type = image.getContentType() == null ? "image/jpeg" : image.getContentType();
        String dataUrl;
        try { dataUrl = "data:" + type + ";base64," + Base64.getEncoder().encodeToString(image.getBytes()); }
        catch (IOException e) { throw new IllegalStateException("图片读取失败"); }
        Map<String, Object> contentText = Map.of("type", "text", "text", "识别运动手环或运动 App 截图中的运动信息。只提取截图清晰可见的数据，无法确认时用 null。只返回 JSON：{\"sportName\":\"\",\"duration\":null,\"calorie\":null,\"recordTime\":\"YYYY-MM-DDTHH:mm:ss 或 null\",\"summary\":\"\",\"suggestion\":\"\"}。duration 单位分钟，calorie 单位 kcal。不要编造数据，不要添加 Markdown。");
        Map<String, Object> contentImage = Map.of("type", "image_url", "image_url", Map.of("url", dataUrl));
        List<Map<String, Object>> messages = List.of(Map.of("role", "system", "content", "你是运动数据截图识别助手。运动建议仅为一般健康建议，不替代专业教练或医疗意见。"), Map.of("role", "user", "content", List.of(contentText, contentImage)));
        String json = callChatCompletion(messages, false).path("choices").path(0).path("message").path("content").asText().replace("```json", "").replace("```", "").trim();
        return new AiChatResponse(json, model, dailyLimit, DISCLAIMER, List.of("正在识别运动手环截图", "已提取运动数据与建议，请确认后导入"), SOURCES);
    }

    private void validateConfigured() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("豆包服务尚未配置，请在服务端设置 ARK_API_KEY");
        }
        if (model == null || model.isBlank()) {
            throw new IllegalStateException("豆包服务尚未配置，请在服务端设置 ARK_MODEL（火山方舟 Endpoint ID）");
        }
    }

    private String ensureQuota(Integer userId) {
        String usageKey = LocalDate.now() + ":" + userId;
        int used = dailyUsage.getOrDefault(usageKey, 0);
        if (used >= dailyLimit) {
            throw new IllegalStateException("今日 AI 问答次数已用完，请明天再试");
        }
        return usageKey;
    }

    private int consumeRequest(String usageKey) {
        int used = dailyUsage.merge(usageKey, 1, Integer::sum);
        return Math.max(0, dailyLimit - used);
    }

    private String runAgent(Integer userId, String message, List<String> workflow) {
        String instructions = "你是健康伴侣应用中的中文健康管理智能体。先判断是否需要调用工具获取用户近7天的饮食、运动或情绪数据；只调用与问题相关的工具。"
                + "拿到工具结果后结合数据回答，明确数据不足时不要臆测。建议简洁温和、可执行；不要诊断疾病、不要开药。"
                + "若用户描述胸痛、呼吸困难、自伤意图等紧急风险，明确建议立即联系当地急救服务或专业医疗人员。"
                + "工具结果只能用于当前用户个性化建议，不要声称工具未返回的信息。";
        JsonNode first = callChatCompletion(instructions, message, true);
        JsonNode responseMessage = first.path("choices").path(0).path("message");
        JsonNode toolCalls = responseMessage.path("tool_calls");
        List<Map<String, Object>> toolOutputs = new ArrayList<>();
        if (toolCalls.size() > 3) throw new IllegalStateException("AI 工具调用数量超出限制");
        for (JsonNode item : toolCalls) {
            String name = item.path("function").path("name").asText();
            String callId = item.path("id").asText();
            if (!List.of("get_food_summary", "get_sport_summary", "get_mood_summary").contains(name)) continue;
            String label = switch (name) {
                case "get_food_summary" -> "正在读取近 7 天饮食热量";
                case "get_sport_summary" -> "正在读取近 7 天运动时长与消耗";
                default -> "正在读取近 7 天情绪趋势";
            };
            workflow.add(label);
            Object result = executeTool(userId, name);
            toolOutputs.add(Map.of("role", "tool", "tool_call_id", callId, "content", objectMapper.writeValueAsString(result)));
        }
        if (toolOutputs.isEmpty()) return responseMessage.path("content").asText();
        workflow.add("正在综合健康数据并组织建议");
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", instructions));
        messages.add(objectMapper.convertValue(responseMessage, Map.class));
        messages.addAll(toolOutputs);
        return callChatCompletion(messages, false).path("choices").path(0).path("message").path("content").asText();
    }

    private Object executeTool(Integer userId, String name) {
        LocalDateTime since = LocalDate.now().minusDays(6).atStartOfDay();
        LocalDateTime until = LocalDate.now().plusDays(1).atStartOfDay();
        if ("get_food_summary".equals(name)) {
            List<FoodRecord> items = foodRecordMapper.selectByUserId(userId).stream().filter(x -> x.getEatTime() != null && !x.getEatTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isBefore(since.toLocalDate()) && x.getEatTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isBefore(until.toLocalDate())).toList();
            int total = items.stream().mapToInt(x -> x.getCal() == null ? 0 : x.getCal()).sum();
            return Map.of("period", "近7天", "recordCount", items.size(), "totalCaloriesKcal", total, "dailyAverageKcal", items.isEmpty() ? 0 : total / 7, "records", items.stream().limit(40).map(x -> Map.of("food", x.getFoodName() == null ? "未命名" : x.getFoodName(), "caloriesKcal", x.getCal() == null ? 0 : x.getCal(), "date", x.getEatTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString())).toList());
        }
        if ("get_sport_summary".equals(name)) {
            List<SportRecord> items = sportRecordMapper.selectByUserId(userId).stream().filter(x -> x.getRecordTime() != null && !x.getRecordTime().isBefore(since) && x.getRecordTime().isBefore(until)).toList();
            return Map.of("period", "近7天", "recordCount", items.size(), "totalDurationMinutes", items.stream().mapToInt(x -> x.getDuration() == null ? 0 : x.getDuration()).sum(), "totalCaloriesKcal", items.stream().mapToDouble(x -> x.getCalorie() == null ? 0 : x.getCalorie()).sum(), "records", items.stream().limit(40).map(x -> Map.of("sport", x.getSportName() == null ? "未命名" : x.getSportName(), "durationMinutes", x.getDuration() == null ? 0 : x.getDuration(), "caloriesKcal", x.getCalorie() == null ? 0 : x.getCalorie(), "date", x.getRecordTime().toLocalDate().toString())).toList());
        }
        List<MoodRecord> items = moodRecordMapper.selectByUserId(userId).stream().filter(x -> x.getRecordTime() != null && !x.getRecordTime().isBefore(since) && x.getRecordTime().isBefore(until)).toList();
        return Map.of("period", "近7天", "recordCount", items.size(), "averageScore", items.isEmpty() ? 0 : Math.round(items.stream().mapToInt(x -> x.getMoodScore() == null ? 0 : x.getMoodScore()).average().orElse(0) * 10.0) / 10.0, "records", items.stream().limit(40).map(x -> Map.of("mood", x.getMoodDesc() == null ? "未填写" : x.getMoodDesc(), "score", x.getMoodScore() == null ? 0 : x.getMoodScore(), "date", x.getRecordTime().toLocalDate().toString())).toList());
    }

    private JsonNode callChatCompletion(String instructions, String message, boolean withTools) {
        List<Map<String, Object>> messages = List.of(
                Map.of("role", "system", "content", instructions),
                Map.of("role", "user", "content", message));
        return callChatCompletion(messages, withTools);
    }

    private Map<String, Object> tool(String name, String description) {
        return Map.of("type", "function", "function", Map.of("name", name, "description", description, "parameters", Map.of("type", "object", "properties", Map.of(), "required", List.of(), "additionalProperties", false)));
    }

    private JsonNode callChatCompletion(List<Map<String, Object>> messages, boolean withTools) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("messages", messages);
            body.put("max_tokens", 600);
            if (withTools) {
                body.put("tools", List.of(
                        tool("get_food_summary", "查询当前用户近7天饮食记录与热量汇总"),
                        tool("get_sport_summary", "查询当前用户近7天运动时长与热量消耗汇总"),
                        tool("get_mood_summary", "查询当前用户近7天情绪评分趋势与记录")
                ));
                body.put("tool_choice", "auto");
            }
            String json = objectMapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/chat/completions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("AI 服务调用失败（HTTP " + response.statusCode() + "）");
            }
            return objectMapper.readTree(response.body());
        } catch (IOException e) {
            throw new IllegalStateException("AI 服务响应解析失败");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("AI 服务调用被中断");
        }
    }

    private String summarizeFoods(List<FoodRecord> records) {
        return records.stream().limit(10).map(item -> item.getFoodName() + "(" + item.getCal() + "kcal)").reduce((a, b) -> a + "，" + b).orElse("暂无记录");
    }

    private String summarizeSports(List<SportRecord> records) {
        return records.stream().limit(10).map(item -> item.getSportName() + "(" + item.getDuration() + "分钟, " + item.getCalorie() + "kcal)").reduce((a, b) -> a + "，" + b).orElse("暂无记录");
    }

    private String summarizeMoods(List<MoodRecord> records) {
        return records.stream().limit(10).map(item -> item.getMoodDesc() + "(" + item.getMoodScore() + "/10)").reduce((a, b) -> a + "，" + b).orElse("暂无记录");
    }
}
