package com.example.dashboard.engine;

import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import org.neo4j.driver.*;
import java.util.Map;

@ApplicationScoped
public class Neo4jEngine implements QueryEngine {

    private Driver driver;
    private Map<String, Object> config;

    @Override
    public void init(Map<String, Object> config) {
        this.config = config;
        this.driver = GraphDatabase.driver(
            config.get("uri").toString(),
            AuthTokens.basic(
                config.get("username").toString(),
                config.get("password").toString()
            )
        );
    }

    @Override
    public JsonObject executeQuery(String query, Map<String, Object> params) {
        try (Session session = driver.session()) {
            Result result = session.run(query, params);
            return convertResult(result);
        } catch (Exception e) {
            throw new RuntimeException("Neo4j query failed", e);
        }
    }

    private JsonObject convertResult(Result result) {
        JsonObject json = new JsonObject();
        result.stream().forEach(record -> {
            record.fields().forEach(field -> {
                String key = field.key();
                Value value = field.value();
                json.put(key, convertValue(value));
            });
        });
        return json;
    }

    private Object convertValue(Value value) {
    if (value.type().name().equals("NODE")) { // Verificação corrigida
        return value.asNode().asMap();
    }
    return value.asObject();
	}
}