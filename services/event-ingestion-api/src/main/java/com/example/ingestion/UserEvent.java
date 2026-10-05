package com.example.ingestion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record UserEvent(
        @NotBlank(message = "user_id is required") String user_id,
        @NotBlank(message = "item_id is required") String item_id,
        @NotNull(message = "timestamp is required") Instant timestamp) {
}
