package com.example.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ProfileUpdateConsumer {
    private static final Logger log = LoggerFactory.getLogger(ProfileUpdateConsumer.class);
    private final ObjectMapper mapper;
    private final ProfileRepository repository;
    private final StringRedisTemplate redis;

    public ProfileUpdateConsumer(ObjectMapper mapper, ProfileRepository repository, StringRedisTemplate redis) {
        this.mapper = mapper;
        this.repository = repository;
        this.redis = redis;
    }

    @KafkaListener(topics = "${KAFKA_TOPIC:user_interactions}", groupId = "${KAFKA_GROUP_ID:profile-update-group}")
    public void consume(String rawMessage) {
        try {
            UserEvent event = mapper.readValue(rawMessage, UserEvent.class);
            if (event.user_id() == null || event.item_id() == null) {
                log.warn("Ignoring event with missing user_id/item_id: {}", rawMessage);
                return;
            }

            var categories = repository.findCategories(event.item_id());
            if (categories.isEmpty()) {
                log.warn("Item {} not found; ignoring event", event.item_id());
                return;
            }

            Map<String, Integer> preferences = repository.findPreferences(event.user_id());
            if (preferences == null) {
                log.warn("User {} not found; ignoring event", event.user_id());
                return;
            }

            for (String category : categories) {
                preferences.merge(category, 1, Integer::sum);
            }
            repository.updatePreferences(event.user_id(), preferences);
            try {
                // Invalidate the read cache so the next recommendation request sees the real-time profile.
                redis.delete("user:" + event.user_id() + ":prefs");
            } catch (Exception cacheEx) {
                log.warn("Could not invalidate Redis cache for user {}", event.user_id(), cacheEx);
            }
            log.info("Updated preferences for user {} from item {} -> {}", event.user_id(), event.item_id(), preferences);
        } catch (Exception ex) {
            // Malformed messages and database errors are contained here so one message never kills the consumer.
            log.error("Failed to process Kafka message; continuing: {}", rawMessage, ex);
        }
    }
}
