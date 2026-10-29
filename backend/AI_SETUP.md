# AI Health Assistant Setup

The AI endpoint is `POST /ai/chat`. It requires the existing JWT header:

```text
token: <login response data>
```

Request body:

```json
{
  "message": "今天应该怎样安排运动？"
}
```

`POST /ai/daily-report` needs no request body. It generates a short report from the current user's records for today, and shares the same daily AI request limit as chat.

Before starting the backend, configure the model provider in the server environment. Do not put a real API key in `application.yml`, the frontend source, or version control.

PowerShell example for the current terminal session:

```powershell
$env:ARK_API_KEY = "your_volcengine_ark_api_key"
$env:ARK_MODEL = "your_endpoint_id"
$env:ARK_BASE_URL = "https://ark.cn-beijing.volces.com/api/v3"
.\mvnw.cmd spring-boot:run
```

Optional settings:

```powershell
$env:AI_DAILY_REQUEST_LIMIT = "10"
```

`OPENAI_BASE_URL` and `OPENAI_MODEL` make the integration usable with an OpenAI-compatible provider. The backend sends the current user's food, sport, and mood records only as context for that user's answer. The in-memory daily request limit is intended for demonstration; production should store usage records in the database or Redis.

The endpoint returns a reply, the configured model name, remaining request quota, and a medical disclaimer. It does not provide diagnosis, medication guidance, or emergency care instructions.

For Doubao via Volcengine Ark, \`ARK_MODEL\` is the Endpoint ID created in the Ark console. This backend uses the OpenAI-compatible Chat Completions API and function calling. Tools are allow-listed to read only the current user's last-seven-day food, sport, and mood summaries. Check the Ark console for current free quota, billing, and tool-calling availability. The in-memory daily limit resets on restart; production should persist usage in a database or Redis.
