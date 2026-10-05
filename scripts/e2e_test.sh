#!/usr/bin/env bash
set -euo pipefail

BASE_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$BASE_DIR"

if [ ! -f "$BASE_DIR/.env" ]; then cp "$BASE_DIR/.env.example" "$BASE_DIR/.env"; fi
source "$BASE_DIR/.env"

for i in {1..30}; do
  if curl -fsS http://localhost:${EVENT_INGESTION_PORT}/actuator/health >/dev/null 2>&1 && \
     curl -fsS http://localhost:${RECOMMENDATION_PORT}/actuator/health >/dev/null 2>&1; then
    break
  fi
  sleep 2
done

before=$(docker compose exec -T postgres psql -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" -tAc "SELECT COALESCE((preferences->>'tech')::int,0) FROM users WHERE user_id='user001';" | tr -d '[:space:]')

curl -fsS -X POST http://localhost:${EVENT_INGESTION_PORT}/api/events/view \
  -H 'Content-Type: application/json' \
  -d '{"user_id":"user001","item_id":"item001","timestamp":"2026-10-05T08:00:00Z"}'

echo

echo "Waiting for Kafka consumer..."
for i in {1..20}; do
  after=$(docker compose exec -T postgres psql -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" -tAc "SELECT COALESCE((preferences->>'tech')::int,0) FROM users WHERE user_id='user001';" | tr -d '[:space:]')
  if [[ "$after" =~ ^[0-9]+$ ]] && (( after > before )); then
    break
  fi
  sleep 1
done

if ! [[ "$after" =~ ^[0-9]+$ ]] || (( after <= before )); then
  echo "E2E FAILED: preference vector was not updated (before=$before after=${after:-unknown})" >&2
  exit 1
fi

echo "Updated PostgreSQL preference: tech $before -> $after"

echo "Recommendation response:"
curl -fsS http://localhost:${RECOMMENDATION_PORT}/api/recommendations/user001
echo

echo "E2E PASSED"
