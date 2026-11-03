package org.health.service;

import org.health.mapper.UserProfileMapper;
import org.health.pojo.UserProfile;
import org.health.util.UserContext;
import org.springframework.stereotype.Service;
import java.util.Base64;
import java.util.Set;

@Service
public class UserProfileService {
    private final UserProfileMapper mapper;
    private volatile boolean initialized;

    public UserProfileService(UserProfileMapper mapper) { this.mapper = mapper; }

    private synchronized void initialize() {
        if (!initialized) { mapper.initialize(); initialized = true; }
    }

    public UserProfile get() {
        initialize();
        UserProfile profile = mapper.find(UserContext.getUserId());
        if (profile == null) profile = new UserProfile();
        return calculate(profile);
    }

    public UserProfile save(UserProfile profile) {
        profile.setUserId(UserContext.getUserId()); // Never accept another user's id from the client.
        String name = profile.getName() == null ? "" : profile.getName().trim();
        if (name.isEmpty() || name.length() > 50) throw new IllegalArgumentException("名字须为1至50个字符");
        profile.setName(name);
        if (profile.getGender() != null && !Set.of("male", "female", "unspecified").contains(profile.getGender()))
            throw new IllegalArgumentException("请选择有效的性别");
        if (profile.getAge() != null && (profile.getAge() < 1 || profile.getAge() > 120))
            throw new IllegalArgumentException("年龄须为1至120岁");
        checkRange(profile.getHeight(), 50, 250, "身高须为50至250厘米");
        checkRange(profile.getWeight(), 10, 350, "体重须为10至350千克");
        validateAvatar(profile.getAvatar());
        initialize();
        mapper.save(profile);
        return calculate(profile);
    }

    private void checkRange(Double value, double min, double max, String message) {
        if (value != null && (!Double.isFinite(value) || value < min || value > max)) throw new IllegalArgumentException(message);
    }

    private void validateAvatar(String avatar) {
        if (avatar == null || avatar.isEmpty()) return;
        if (avatar.length() > 750000 || !avatar.startsWith("data:image/")) throw new IllegalArgumentException("头像格式不正确或过大");
        int comma = avatar.indexOf(',');
        if (comma < 0 || !Set.of("data:image/jpeg;base64", "data:image/png;base64", "data:image/webp;base64").contains(avatar.substring(0, comma)))
            throw new IllegalArgumentException("头像仅支持 JPEG、PNG、WebP");
        try {
            byte[] bytes = Base64.getDecoder().decode(avatar.substring(comma + 1));
            boolean jpeg = bytes.length > 3 && (bytes[0] & 255) == 255 && (bytes[1] & 255) == 216 && (bytes[2] & 255) == 255;
            boolean png = bytes.length > 8 && bytes[0] == (byte)137 && bytes[1] == 80 && bytes[2] == 78 && bytes[3] == 71 && bytes[4] == 13 && bytes[5] == 10 && bytes[6] == 26 && bytes[7] == 10;
            boolean webp = bytes.length > 12 && bytes[0] == 82 && bytes[1] == 73 && bytes[2] == 70 && bytes[3] == 70 && bytes[8] == 87 && bytes[9] == 69 && bytes[10] == 66 && bytes[11] == 80;
            String type = avatar.substring(0, comma);
            if (!(jpeg && type.contains("jpeg") || png && type.contains("png") || webp && type.contains("webp"))) throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) { throw new IllegalArgumentException("头像文件无效"); }
    }

    public static UserProfile calculate(UserProfile profile) {
        profile.setBmi(null);
        profile.setBodyFat(null);
        if (profile.getHeight() != null && profile.getWeight() != null && profile.getHeight() > 0 && profile.getWeight() > 0) {
            double bmi = profile.getWeight() / Math.pow(profile.getHeight() / 100, 2);
            profile.setBmi(Math.round(bmi * 10) / 10.0);
            if (profile.getAge() != null && profile.getAge() >= 18 && ("male".equals(profile.getGender()) || "female".equals(profile.getGender()))) {
                double fat = 1.2 * bmi + 0.23 * profile.getAge() - ("male".equals(profile.getGender()) ? 10.8 : 0) - 5.4;
                if (fat > 0 && fat < 100) profile.setBodyFat(Math.round(fat * 10) / 10.0);
            }
        }
        return profile;
    }
}
