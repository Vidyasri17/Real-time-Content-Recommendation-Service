package com.example.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Instant;
import java.util.Map;

import static org.mockito.Mockito.*;

class ProfileUpdateConsumerTest {
    @Test
    void malformedMessageDoesNotReachRepository() {
        ProfileRepository repository = mock(ProfileRepository.class);
        ProfileUpdateConsumer consumer = new ProfileUpdateConsumer(new ObjectMapper().findAndRegisterModules(), repository, mock(StringRedisTemplate.class));
        consumer.consume("not-json");
        verifyNoInteractions(repository);
    }

    @Test
    void validEventIncrementsEveryItemCategory() {
        ProfileRepository repository = mock(ProfileRepository.class);
        when(repository.findCategories("item001")).thenReturn(java.util.List.of("tech", "books"));
        when(repository.findPreferences("user001")).thenReturn(new java.util.HashMap<>(Map.of("tech", 2)));

        ProfileUpdateConsumer consumer = new ProfileUpdateConsumer(new ObjectMapper().findAndRegisterModules(), repository, mock(StringRedisTemplate.class));
        consumer.consume("{\"user_id\":\"user001\",\"item_id\":\"item001\",\"timestamp\":\"" + Instant.now() + "\"}");

        verify(repository).updatePreferences(eq("user001"), argThat(p -> p.get("tech") == 3 && p.get("books") == 1));
    }
}
