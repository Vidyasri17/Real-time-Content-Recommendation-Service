package com.example.profile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ProfileRepository {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    public ProfileRepository(JdbcTemplate jdbc, ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    public List<String> findCategories(String itemId) {
        return jdbc.query("SELECT categories FROM items WHERE item_id = ?", ps -> ps.setString(1, itemId),
                (rs, rowNum) -> Arrays.asList((String[]) rs.getArray("categories").getArray()))
                .stream().findFirst().orElse(List.of());
    }

    public Map<String, Integer> findPreferences(String userId) {
        List<Map<String, Integer>> result = jdbc.query(
                "SELECT preferences FROM users WHERE user_id = ?",
                ps -> ps.setString(1, userId),
                (rs, rowNum) -> {
                    String json = rs.getString("preferences");
                    try {
                        return mapper.readValue(json, new TypeReference<Map<String, Integer>>() {});
                    } catch (Exception e) {
                        return new HashMap<>();
                    }
                });
        return result.isEmpty() ? null : new HashMap<>(result.get(0));
    }

    public void updatePreferences(String userId, Map<String, Integer> preferences) {
        try {
            PGobject json = new PGobject();
            json.setType("jsonb");
            json.setValue(mapper.writeValueAsString(preferences));
            jdbc.update("UPDATE users SET preferences = ? WHERE user_id = ?", json, userId);
        } catch (SQLException | RuntimeException e) {
            throw new IllegalStateException("Unable to serialize preference JSON", e);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to update preferences", e);
        }
    }
}
