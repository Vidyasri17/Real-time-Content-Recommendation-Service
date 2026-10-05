# Architecture

The implementation follows the supplied architecture:

- Client -> Event Ingestion API (`POST /api/events/view`)
- Event Ingestion API -> Kafka `user_interactions`
- Profile Update Service consumes Kafka asynchronously
- Profile Update Service -> PostgreSQL JSONB preference vector
- Recommendation API -> Redis cache first
- Recommendation API -> PostgreSQL on cache miss
- Recommendation API returns up to five category-based item IDs
- Recommendation API tracks already-returned item IDs per user for the current service session

The Profile Update Service also invalidates `user:{user_id}:prefs` after a successful PostgreSQL update. This small addition prevents the Redis cache from hiding a newly processed interaction for up to the full TTL, while preserving the cache-aside design.
