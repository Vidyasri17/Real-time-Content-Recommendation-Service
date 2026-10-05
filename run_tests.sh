#!/usr/bin/env bash
set -euo pipefail

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
if [ ! -f "$BASE_DIR/.env" ]; then cp "$BASE_DIR/.env.example" "$BASE_DIR/.env"; fi
source "$BASE_DIR/.env"

# Run unit tests in each service container image.
docker compose run --rm --no-deps --entrypoint mvn event-ingestion-api -q test
docker compose run --rm --no-deps --entrypoint mvn profile-update-service -q test
docker compose run --rm --no-deps --entrypoint mvn recommendation-api -q test

# Integration/E2E test across HTTP -> Kafka -> PostgreSQL -> Redis-backed Recommendation API.
docker compose up -d
trap 'docker compose down' EXIT
sleep 5
./scripts/e2e_test.sh
