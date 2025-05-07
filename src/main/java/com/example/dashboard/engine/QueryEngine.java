package com.example.dashboard.engine;

import io.vertx.core.json.JsonObject;
import java.util.Map;

public interface QueryEngine {
    void init(Map<String, Object> config);
    JsonObject executeQuery(String query, Map<String, Object> params);
}