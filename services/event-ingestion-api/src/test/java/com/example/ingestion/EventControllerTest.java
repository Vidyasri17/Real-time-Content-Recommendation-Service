package com.example.ingestion;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventControllerTest {
    @Test
    void validEventIsAccepted() throws Exception {
        KafkaTemplate<String, UserEvent> template = mock(KafkaTemplate.class);
        CompletableFuture<SendResult<String, UserEvent>> future = new CompletableFuture<>();
        future.complete(null);
        when(template.send(anyString(), anyString(), any(UserEvent.class))).thenReturn(future);

        EventController controller = new EventController(template, "user_interactions");
        ResponseEntity<?> response = controller.publish(new UserEvent("user001", "item001", Instant.now()));

        assertEquals(202, response.getStatusCode().value());
        verify(template).send(eq("user_interactions"), eq("user001"), any(UserEvent.class));
    }
}
