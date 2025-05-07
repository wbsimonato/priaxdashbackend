package com.example.dashboard.engine;

import io.agroal.api.AgroalDataSource;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.*;
import java.util.Map;

@ApplicationScoped
public class MySqlEngine implements QueryEngine {
    
    @Inject
    AgroalDataSource dataSource;

    @Override
    public void init(Map<String, Object> config) {}

    @Override
    public JsonObject executeQuery(String query, Map<String, Object> params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            int index = 1;
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                stmt.setObject(index++, entry.getValue());
            }

            boolean isResultSet = stmt.execute();
            
            if (isResultSet) {
                return processResultSet(stmt.getResultSet());
            } else {
                return new JsonObject().put("rowsAffected", stmt.getUpdateCount());
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("MySQL query failed", e);
        }
    }

    private JsonObject processResultSet(ResultSet rs) throws SQLException {
        JsonArray results = new JsonArray();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (rs.next()) {
            JsonObject row = new JsonObject();
            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnName(i), rs.getObject(i));
            }
            results.add(row);
        }

        return new JsonObject().put("results", results);
    }
}