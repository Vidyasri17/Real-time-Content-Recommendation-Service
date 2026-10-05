package com.example.recommendation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    private final RecommendationRepository repository;
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;
    private final long ttlSeconds;
    private final Map<String, Set<String>> viewedItems = new ConcurrentHashMap<>();

    public RecommendationService(RecommendationRepository repository,
                                  StringRedisTemplate redis,
                                  ObjectMapper mapper,
                                  @Value("${REDIS_TTL_SECONDS:3600}") long ttlSeconds) {
        this.repository = repository;
        this.redis = redis;
        this.mapper = mapper;
        this.ttlSeconds = ttlSeconds;
    }

    public Optional<List<String>> recommend(String userId) {
        Optional<Map<String, Integer>> preferences = loadPreferences(userId);
        if (preferences.isEmpty()) return Optional.empty();

        List<String> topCategories = preferences.get().entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry::getKey))
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();

        List<String> candidates = repository.findItemsByCategories(topCategories);
        Set<String> seen = viewedItems.computeIfAbsent(userId, ignored -> ConcurrentHashMap.newKeySet());
        List<String> recommendations = candidates.stream()
                .filter(item -> !seen.contains(item))
                .limit(5)
                .toList();

        seen.addAll(recommendations);
        return Optional.of(recommendations);
    }

    private Optional<Map<String, Integer>> loadPreferences(String userId) {
        String key = "user:" + userId + ":prefs";
        try {
            String cached = redis.opsForValue().get(key);
            if (cached != null) {
                return Optional.of(mapper.readValue(cached, new TypeReference<Map<String, Integer>>() {}));
            }
        } catch (Exception ignored) {
            // Redis failure should not make recommendations unavailable; PostgreSQL is the fallback.
        }

        Optional<Map<String, Integer>> fromDb = repository.findPreferences(userId);
        if (fromDb.isEmpty()) return Optional.empty();

        try {
            redis.opsForValue().set(key, mapper.writeValueAsString(fromDb.get()), Duration.ofSeconds(ttlSeconds));
        } catch (Exception ignored) {
            // DB response remains valid even if cache population fails.
        }
        return fromDb;
    }
}
