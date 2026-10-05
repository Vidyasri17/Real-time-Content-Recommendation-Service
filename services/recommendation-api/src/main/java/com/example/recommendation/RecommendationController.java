package com.example.recommendation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final RecommendationService service;

    public RecommendationController(RecommendationService service) {
        this.service = service;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> recommendations(@PathVariable String userId) {
        return service.recommend(userId)
                .<ResponseEntity<?>>map(items -> ResponseEntity.ok(Map.of(
                        "user_id", userId,
                        "recommendations", items)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
