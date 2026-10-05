package com.example.ingestion;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final KafkaTemplate<String, UserEvent> kafkaTemplate;
    private final String topic;

    public EventController(KafkaTemplate<String, UserEvent> kafkaTemplate,
                           @Value("${KAFKA_TOPIC:user_interactions}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @PostMapping("/view")
    public ResponseEntity<?> publish(@Valid @RequestBody UserEvent event) {
        try {
            kafkaTemplate.send(topic, event.user_id(), event).get(3, TimeUnit.SECONDS);
            return ResponseEntity.accepted().body(Map.of(
                    "status", "accepted",
                    "message", "event queued",
                    "topic", topic));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Kafka broker is unavailable or rejected the event"));
        }
    }
}
