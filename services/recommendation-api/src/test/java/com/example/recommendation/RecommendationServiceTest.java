package com.example.recommendation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecommendationServiceTest {
    @Test
    void sortsCategoriesByScoreAndLimitsToFive() {
        RecommendationRepository repo = mock(RecommendationRepository.class);
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        when(redis.opsForValue().get("user:u1:prefs")).thenReturn(null);
        when(repo.findPreferences("u1")).thenReturn(Optional.of(Map.of("sports", 1, "tech", 5, "books", 3)));
        when(repo.findItemsByCategories(List.of("tech", "books", "sports")))
                .thenReturn(List.of("item1", "item2", "item3", "item4", "item5", "item6"));

        RecommendationService service = new RecommendationService(repo, redis, new ObjectMapper(), 3600);
        List<String> result = service.recommend("u1").orElseThrow();

        assertEquals(5, result.size());
        assertEquals(List.of("item1", "item2", "item3", "item4", "item5"), result);
    }

    @Test
    void filtersPreviouslyReturnedItems() {
        RecommendationRepository repo = mock(RecommendationRepository.class);
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        when(redis.opsForValue().get("user:u1:prefs")).thenReturn("{\"tech\":2}");
        when(repo.findItemsByCategories(List.of("tech")))
                .thenReturn(List.of("item1", "item2", "item3"));

        RecommendationService service = new RecommendationService(repo, redis, new ObjectMapper(), 3600);
        assertEquals(List.of("item1", "item2", "item3"), service.recommend("u1").orElseThrow());
        assertEquals(List.of(), service.recommend("u1").orElseThrow());
    }
}
