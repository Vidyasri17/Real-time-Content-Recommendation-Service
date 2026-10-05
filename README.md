# Real-Time Recommendation Service — Java/Spring Boot

A complete event-driven recommendation backend implemented with **Java 21 + Spring Boot 3.5**, Apache Kafka, PostgreSQL JSONB, Redis, and Docker Compose.

## Architecture

```text
Client / Frontend
      |
      | POST /api/events/view
      v
Event Ingestion API :8000
      |
      | publish user_interactions
      v
Apache Kafka
      |
      | consume
      v
Profile Update Service
      |
      | read item categories + update JSONB
      v
PostgreSQL

Client -- GET /api/recommendations/{user_id} --> Recommendation API :8001
                                                    |
                                                    | cache-aside
                                                    v
                                                  Redis
                                                    |
                                                    | miss
                                                    v
                                                PostgreSQL
```

## Services

1. **Event Ingestion API** — validates interaction events and publishes JSON to Kafka.
2. **Profile Update Service** — headless Kafka consumer that updates user preference vectors in PostgreSQL JSONB.
3. **Recommendation API** — cache-aside Redis + PostgreSQL recommendation serving layer with session-level viewed-item filtering.

## Requirements covered

- `POST /api/events/view` with validation and `202 Accepted`.
- Kafka topic `user_interactions` with producer retries and graceful broker errors.
- Separate Kafka consumer group `profile-update-group`.
- Malformed Kafka messages are logged and skipped without killing the consumer.
- PostgreSQL `users.preferences` uses JSONB.
- PostgreSQL `items.categories` uses a PostgreSQL `TEXT[]` array and `features` uses JSONB.
- Preference scores increment for every category on an interaction.
- `GET /api/recommendations/{user_id}` returns up to five IDs or `404`.
- Redis cache-aside with configurable TTL.
- In-memory per-user recommendation tracking prevents repeated recommendations during the service session.
- Docker Compose includes Kafka, PostgreSQL, Redis and all three application services.
- Kafka/PostgreSQL/Redis healthchecks and dependency ordering are defined.
- PostgreSQL auto-seeds 10 users and 20 items.
- Unit tests exist for all three services.
- `run_tests.sh` runs unit tests and an end-to-end HTTP → Kafka → PostgreSQL → Recommendation flow.
- No credentials or infrastructure URLs are hardcoded in Java code; environment variables are documented in `.env.example`.

## Prerequisites

- Docker Desktop / Docker Engine with Compose v2.
- Bash shell (Git Bash or WSL on Windows is recommended for `run_tests.sh`).

## Start

```bash
cp .env.example .env
docker compose up -d --build
```

Check:

```bash
docker compose ps
```

## API examples

### Publish a view event

```bash
curl -i -X POST http://localhost:8000/api/events/view \
  -H "Content-Type: application/json" \
  -d '{"user_id":"user001","item_id":"item001","timestamp":"2026-10-05T08:00:00Z"}'
```

Expected status: `202 Accepted`.

### Get recommendations

```bash
curl http://localhost:8001/api/recommendations/user001
```

Example shape:

```json
{
  "user_id": "user001",
  "recommendations": ["item001", "item002", "item003", "item004", "item019"]
}
```

The exact list changes as the user's preference vector and session-level viewed set change.

## Run all tests

```bash
./run_tests.sh
```

The script executes each service's Maven unit tests and then runs the E2E flow.

## Useful commands

```bash
docker compose logs -f event-ingestion-api
docker compose logs -f profile-update-service
docker compose logs -f recommendation-api
docker compose logs -f kafka

docker compose exec postgres psql -U recommendation_user -d recommendations
```

To reset the seeded database completely:

```bash
docker compose down -v
docker compose up -d --build
```

## Environment variables

Copy `.env.example` to `.env`. The application uses environment variables for Kafka, Redis, PostgreSQL credentials/host information, ports, topic/group names and Redis TTL.

## Notes for assessment

- The recommendation algorithm is intentionally rule-based. It ranks preference categories by score and returns items matching the top categories; no ML model is required by the specification.
- The Recommendation API is read-only with respect to user preferences. Only the Profile Update Service mutates `users.preferences`.
- The in-memory viewed-item set is deliberately session-scoped as permitted by the task. Restarting the Recommendation API resets that simulation.
- The Kafka consumer catches malformed JSON and database processing exceptions so one bad message cannot terminate the worker.
