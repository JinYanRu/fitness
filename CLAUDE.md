# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Fitness OCR — a nutrition tracking app that uses OCR + LLM to identify food nutrition labels and track daily dietary intake. Chinese-language codebase (comments, UI, API docs are in Chinese).

## Architecture

Monorepo with two independent sub-projects:

```
fitness/
├── fitness-backend/    # Spring Boot 3.2 REST API (Java 17, Maven)
├── fitness-frontend/   # Vue 3 SPA (Vite, Pinia, Vue Router)
├── doc/                # Development plan & design docs
└── start.sh            # Interactive startup script (both services)
```

**Data flow**: Frontend → Vite proxy (`/api` → `localhost:8080`) → Spring Boot → MySQL / RapidOCR / Doubao LLM

### Backend (fitness-backend)

Standard Spring Boot layered architecture under `com.fitness.ocr`:
- **controller/** — REST endpoints (OcrController, AiController, AuthController, NutritionController, UserFoodController, CommonFoodController, UserRecipeController)
- **service/** — Business logic (OcrService calls RapidOCR, LlmService calls Doubao/豆包 LLM, AuthService handles JWT auth)
- **repository/** — Spring Data JPA interfaces
- **entity/** — JPA entities mapped to MySQL tables
- **dto/** — Request/response DTOs; `Result<T>` is the unified API response wrapper (`{code, message, data}`)
- **config/** — CorsConfig, OcrProperties, LlmProperties, JwtProperties, WebMvcConfig (JWT interceptor registration)
- **security/** — JwtAuthenticationInterceptor + JwtUtils; JWT token in `Authorization: Bearer <token>` header
- **exception/** — GlobalExceptionHandler

**JWT auth**: Interceptor covers `/api/**`, excludes `/api/auth/login`, `/api/auth/register`, `/api/ocr/**`, `/api/food/common/**`. Authenticated user ID/username stored as request attributes (`userId`, `username`).

**External services**:
- RapidOCR (image OCR) — configured via `ocr.service.rapid-url`
- Doubao LLM (nutrition parsing from OCR text) — configured via `llm.service.api-url`, `llm.service.api-key`, `llm.service.model`

### Frontend (fitness-frontend)

Vue 3 SPA with hash-based routing:
- **pages/** — Route pages: index (home/stats), auth/login, auth/register, food/library, record/nutrition-record, history/history, profile/profile
- **services/api/** — API modules (nutrition.js, ai.js, auth.js, food.js) using `request.js` wrapper
- **services/ocr/** — Strategy pattern for OCR providers (base.js, backend.js, rapidocr.js, baidu.js, tencent.js, utools.js, mock.js, init.js manager)
- **store/** — Pinia stores (nutrition.js, user.js)
- **utils/request.js** — Fetch-based HTTP client with auto JWT injection and 401 redirect to login
- **router.js** — Route guards: `requiresAuth` pages redirect to login if no token; `guest` pages redirect away if already logged in

**API proxy**: Vite dev server proxies `/api` → `http://localhost:8080` and `/ocr-proxy` → RapidOCR server.

### Database

MySQL 8.0, database `fitness_ocr`. Init script at `fitness-backend/src/main/resources/db/init.sql`. JPA `ddl-auto: update` so Hibernate syncs entities on startup. Key tables: `user`, `user_profile`, `ocr_record`, `food_nutrition`, `nutrition_record`, `user_food`, `common_food`, `user_recipe`, `recipe_ingredient`.

## Build & Run Commands

### Backend
```bash
cd fitness-backend
mvn clean install              # Build
mvn spring-boot:run            # Run (port 8080)
mvn test                       # Run tests (uses H2 in-memory DB via application-test.yml)
```

### Frontend
```bash
cd fitness-frontend
npm install                    # Install dependencies
npm run dev:h5                 # Dev server (port 5173, proxies /api to backend)
npm run build:h5               # Production build
```

### Both at once
```bash
./start.sh                     # Interactive script, prompts for each service
```

### Prerequisites
- JDK 17+, Maven 3.6+
- Node.js 16+, npm
- MySQL 8.0+ (remote DB at `111.228.49.250:3306/fitness_ocr` currently configured)
- RapidOCR service (Python, port 8000) — needed for OCR features

## Key Conventions

- **Unified API response**: All backend endpoints return `Result<T>` with `{code: int, message: string, data: T}`. Success is `code=200`.
- **Auth flow**: Register/login → receive JWT → store in `localStorage.token` → auto-attached by `request.js` → backend interceptor validates.
- **OCR strategy pattern**: Frontend `services/ocr/` uses strategy pattern; `init.js` is the manager, `base.js` is the abstract base. Switch providers via `ocrManager.use('backend'|'rapidocr'|'baidu'|'tencent'|'utools'|'mock')`.
- **Lombok**: All backend entities and DTOs use Lombok (`@Data`, `@RequiredArgsConstructor`, etc.).
- **Chinese language**: UI text, comments, API descriptions, and database column comments are in Chinese.

## Known Issues (from doc/开发计划.md)

- Sensitive credentials (MySQL password, LLM API key) are hardcoded in `application.yml` — should use environment variables
- Frontend API paths and backend endpoints have historical mismatches being resolved
- `OcrRecordRepository` exists but OCR results are not fully persisted
- Test coverage is minimal (only NutritionControllerTest and NutritionServiceTest exist)
