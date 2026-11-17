# EventHub AI chatbot

The chatbot runs in the Spring Boot backend and calls [OpenRouter Chat Completions](https://openrouter.ai/docs/api/api-reference/chat/create-a-chat-completion). It does not require the separate recommendation service in `eventhub-ai`.

## Local setup

Add these settings to the ignored **backend root** `application-local.properties`, then restart the backend:

```properties
app.ai.api-key=YOUR_OPENROUTER_API_KEY
# Choose any OpenRouter model compatible with Chat Completions.
app.ai.model=nvidia/nemotron-3-ultra-550b-a55b:free
app.ai.max-tokens=1800
app.ai.temperature=0.3
# Optional override for the model above. Empty means provider default.
app.ai.reasoning-enabled=false
app.ai.timeout=45s
```

Model selection is entirely configuration-driven: there is no model ID or model-specific branch in Java. Change `app.ai.model` and restart the backend; no Java edit/rebuild or frontend change is needed. The current local configuration selects [NVIDIA Nemotron 3 Ultra (free)](https://openrouter.ai/nvidia/nemotron-3-ultra-550b-a55b:free); this is a configurable value, not a fixed model in the client. To let OpenRouter select among free models, set `app.ai.model=openrouter/free`. To choose another specific free model, use its available ID ending in `:free`. Do not use `openrouter/auto` for free-only usage: it can route to paid models. No alternate or paid fallback model is configured.

`app.ai.reasoning-enabled` is an optional setting for any selected model: empty/omitted uses the provider default, `false` requests direct answers, and `true` enables reasoning. The client never infers it from a model name. The current local value is `false` because Nemotron supports disabling reasoning. When switching models, clear this override unless the new model supports the chosen value; models with mandatory reasoning must not receive `false`. Reasoning tokens share the output budget, so reasoning models may need a larger `app.ai.max-tokens` and timeout. The chatbot only displays visible answer content, not reasoning traces.

Generation options are also configurable: `app.ai.max-tokens` accepts 1–65,536, `app.ai.temperature` accepts 0–2 (empty omits it), and `app.ai.timeout` accepts 5–90 seconds. The selected provider can impose stricter limits or support fewer parameters. API key and model must both be configured for AI to be available; an omitted model disables AI without preventing other features from starting.

The NVIDIA free endpoint's [provider notice](https://openrouter.ai/nvidia/nemotron-3-ultra-550b-a55b:free) states that requests are logged for security and product improvement and asks users not to upload confidential or personal data. Use demo/non-sensitive data with this endpoint; scoped authorization alone does not make private booking or wallet information suitable for this provider.

Settings can live in `src/main/resources/application.properties`, the ignored root `application-local.properties`, or environment variables `OPENROUTER_API_KEY`, `OPENROUTER_MODEL`, `OPENROUTER_MAX_TOKENS`, `OPENROUTER_TEMPERATURE`, `OPENROUTER_REASONING_ENABLED`, and `OPENROUTER_TIMEOUT`. The shared application file has no default model ID. Local values override the shared file and its environment placeholders: change the local model setting, or remove it if you want the shared file/environment to select the model. The key and model are never supplied by the browser.

Free usage has [provider/account rate limits](https://openrouter.ai/docs/api_reference/limits): accounts without purchased credits currently have 50 free requests per day and free models allow up to 20 requests/minute, with additional provider availability limits possible. Because this backend uses one OpenRouter account, its users share the account quota across all four EventHub roles. Free providers may be slower or temporarily unavailable; a 429 should be retried later. These limits can change.

Run the backend and the existing Vite frontend as usual. Flyway applies additive migration **V16**: message content becomes `TEXT`, existing conversations are assigned the owner's current role, and indexes are added. Restart an already running backend so it loads the new endpoints and migration. Sign in with any of the four roles; use **Hỏi EventHub AI** in the bottom right. Without a key the application still starts, history stays readable, and sending is disabled with an explanation.

## Data available to each role

| Role | Context sent to the model |
| --- | --- |
| CUSTOMER | Public events and ticket prices/availability; their own 10 latest bookings, ticket counts and refund status |
| ORGANIZER | Their own events, paid booking amounts and ticket/check-in counts; their wallet and 10 latest withdrawals |
| STAFF | Events with their own ACTIVE assignment, ticket/check-in counts; their personal bookings |
| ADMIN | Global user/event counts, gross revenue/refunds, pending queue counts and oldest pending events |

Event search ranks words in the user's question against event name, category and city and returns up to 12 relevant events. Ticket types are capped at 60. List limits are explicitly included in the model instructions: absent rows do not establish that data does not exist. Aggregates have the authenticated user's scope. Ask for a specific event name when needed. Amounts are VND; DB timestamps are UTC and the assistant is instructed to display UTC+7.

The server uses fixed, parameterized, read-only SQL and derives the role and user ID from the authenticated session. The model cannot run SQL, choose another account, or perform business mutations. Passwords, email/phone lists, bank account numbers, ticket codes and QR tokens are excluded from context. Messages and this scoped snapshot are sent to OpenRouter when the user asks a question; server conversation history is stored in PostgreSQL. No chat content is stored in browser storage.

## API

All endpoints require the existing authenticated cookie. POST/DELETE also require `X-CSRF-Protection: 1`.

| Method | Endpoint | Result |
| --- | --- | --- |
| GET | `/api/ai/status` | `{ "available": true }` when a server key and model are set (does not probe the provider) |
| GET | `/api/ai/conversations` | Up to 50 latest conversations for the current user and role |
| GET | `/api/ai/conversations/{id}` | Conversation metadata and ordered messages |
| POST | `/api/ai/chat` | `{ "message": "...", "conversationId": null }`; omit/null ID for a new conversation |
| DELETE | `/api/ai/conversations/{id}` | Deletes the owned conversation and its messages, 204 |

Chat response: `conversation`, `userMessage`, `assistantMessage`, `sources` (server-selected related pages), `contextAt`. Sources are navigation to the scoped data pages, not a claim that every sentence has a citation. Opening old history returns stored messages; data is refreshed on each new question. Ownership failures return 404, including cross-role history access after a role change.

Questions are limited to 2,000 characters and conversations to 50 exchanges. The model receives up to 20 recent messages and a fresh scoped snapshot. A user can make at most 10 requests/minute and one simultaneous request; the process permits eight concurrent AI calls. These are per-process limits; use a shared limiter if deploying multiple instances.

Successful user/assistant messages are saved atomically after the provider responds. Provider failures do not create empty conversations or partial turns. No DB lock/transaction is held during the provider request. A short persistence lock detects concurrent changes before saving. Upstream authentication/billing failures become a generic 503 without expiring the EventHub session or leaking provider diagnostics; rate limits are 429 and connection/timeouts are 504. A timeout after server completion can still leave a saved answer; reopen history before retrying if the result is uncertain.

## Validation

Backend tests: `AiChatIntegrationTests` (real isolated PostgreSQL: all roles, owner isolation, scoped rows, long answers, deletion, validation and failure rollback), `OpenRouterClientTest` (HTTP format and provider failures), and existing application-property tests. Use the repository's `PostgresTestConfiguration`/test database instructions; do not run tests against the business database.

Frontend: `npm run build`, `npm run lint`, `npm run test:e2e -- tests/chatbot.spec.ts`. Set `PLAYWRIGHT_CHANNEL=chrome` to use installed Chrome, or install the Playwright Chromium binary. Tests cover desktop/mobile, all role portals, history, retry, disabled provider, busy state and safe Markdown rendering. Provider responses are mocked; these tests do not consume OpenRouter credits.
