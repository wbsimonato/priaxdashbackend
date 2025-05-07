package com.example.dashboard.di;

import com.example.dashboard.engine.ElasticsearchEngine;
import com.example.dashboard.engine.MySqlEngine;
import com.example.dashboard.engine.Neo4jEngine;
import com.example.dashboard.engine.QueryEngine;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class EngineProducer {
    
    @Produces
    @EngineType("mysql")
    public QueryEngine produceMySql() {
        return new MySqlEngine();
    }
    
    @Produces
    @EngineType("elasticsearch")
    public QueryEngine produceElasticsearch() {
        return new ElasticsearchEngine();
    }
    
    @Produces
    @EngineType("neo4j")
    public QueryEngine produceNeo4j() {
        return new Neo4jEngine();
    }
}