package com.example.profile;

import java.time.Instant;

public record UserEvent(String user_id, String item_id, Instant timestamp) {}
