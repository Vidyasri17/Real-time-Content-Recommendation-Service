package com.example.recommendation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.util.*;

@Repository
public class RecommendationRepository {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    public RecommendationRepository(JdbcTemplate jdbc, ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    public Optional<Map<String, Integer>> findPreferences(String userId) {
        return jdbc.query("SELECT preferences FROM users WHERE user_id = ?", ps -> ps.setString(1, userId),
                (rs, rowNum) -> {
                    try {
                        return mapper.readValue(rs.getString("preferences"), new TypeReference<Map<String, Integer>>() {});
                    } catch (Exception e) {
                        return new HashMap<String, Integer>();
                    }
                }).stream().findFirst();
    }

    public List<String> findItemsByCategories(List<String> categories) {
        if (categories.isEmpty()) return List.of();
        return jdbc.query(connection -> {
            var ps = connection.prepareStatement(
                    "SELECT item_id FROM items WHERE categories && ? ORDER BY item_id");
            Array array = connection.createArrayOf("text", categories.toArray());
            ps.setArray(1, array);
            return ps;
        }, (rs, rowNum) -> rs.getString("item_id"));
    }
}
