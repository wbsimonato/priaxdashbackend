package com.example.dashboard.engine;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

@ApplicationScoped
public class ElasticsearchEngine implements QueryEngine {

    private ElasticsearchClient client;
    private Map<String, Object> config;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init(Map<String, Object> config) {
        this.config = config;
        try {
            // Configurar cliente Elasticsearch
            RestClient restClient = RestClient.builder(
                HttpHost.create(config.get("hosts").toString())
            ).build();

            ElasticsearchTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper(objectMapper)
            );

            this.client = new ElasticsearchClient(transport);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Elasticsearch client", e);
        }
    }

    @Override
    public JsonObject executeQuery(String queryTemplate, Map<String, Object> params) {
        try {
            String renderedQuery = renderTemplate(queryTemplate, params);
            
            SearchRequest request = SearchRequest.of(sr -> sr
                .index(config.get("index").toString())
                .withJson(new StringReader(renderedQuery))
            );

            SearchResponse<JsonObject> response = client.search(request, JsonObject.class);
            return convertResponse(response);
            
        } catch (IOException e) {
            throw new RuntimeException("Elasticsearch query failed", e);
        }
    }

    private String renderTemplate(String template, Map<String, Object> params) {
        // Substituição de parâmetros estilo Mustache
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            template = template.replace("{{" + entry.getKey() + "}}", entry.getValue().toString());
        }
        return template;
    }

    private JsonObject convertResponse(SearchResponse<JsonObject> response) {
        JsonObject result = new JsonObject();
        response.hits().hits().forEach(hit -> {
            if (hit.source() != null) {
                result.put(hit.id(), hit.source().encode());
            }
        });
        return result;
    }
}