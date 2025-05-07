package com.example.dashboard.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class QueryRequest {
    @JsonProperty("datasourceId")
    private String datasourceId;
    
    @JsonProperty("filters")
    private Map<String, Object> filters;

    public QueryRequest() {}

    public String getDatasourceId() {
        return datasourceId;
    }

    public void setDatasourceId(String datasourceId) {
        this.datasourceId = datasourceId;
    }

    public Map<String, Object> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, Object> filters) {
        this.filters = filters;
    }
}