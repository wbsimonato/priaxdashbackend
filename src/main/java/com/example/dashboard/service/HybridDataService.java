package com.example.dashboard.service;

import com.example.dashboard.config.HybridDataSource;
import com.example.dashboard.di.EngineType;
import com.example.dashboard.engine.QueryEngine;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.enterprise.inject.Instance;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.yaml.snakeyaml.LoaderOptions;

@ApplicationScoped
public class HybridDataService {

    @Inject
    @EngineType
    Instance<QueryEngine> engines;

    @ConfigProperty(name = "datasources.config-file")
    String configFile;

    private final Map<String, HybridDataSource> dataSources = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        loadConfig();
        startConfigWatcher();
    }

    private void loadConfig() {
        try (InputStream inputStream = Files.newInputStream(Paths.get(configFile))) {
			Yaml yaml = new Yaml(new Constructor(HybridDataSource.class, new LoaderOptions()));
            Iterable<Object> configs = yaml.loadAll(inputStream);
            
            dataSources.clear();
            configs.forEach(config -> {
                HybridDataSource ds = (HybridDataSource) config;
                dataSources.put(ds.id, ds);
            });
            
        } catch (IOException e) {
        throw new RuntimeException("Error loading datasources config", e);
        }
    }

    private void startConfigWatcher() {
    Thread.startVirtualThread(() -> {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            Path configDir = Paths.get(configFile).getParent();
            
            configDir.register(watchService,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_CREATE);

            while (true) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path changedFile = (Path) event.context();
                    if (changedFile.endsWith(Paths.get(configFile).getFileName())) {
                        loadConfig();
                    }
                }
                key.reset();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    });
	}

	public JsonObject executeHybridQuery(String datasourceId, Map<String, Object> filters) {
    HybridDataSource config = dataSources.get(datasourceId);
    validateConfig(config, datasourceId);

    JsonObject finalResult = new JsonObject();
    
    for (int i = 0; i < config.phases.size(); i++) {
        HybridDataSource.PhaseConfig phase = config.phases.get(i);
        QueryEngine engine = getEngine(phase.engine);
        engine.init(phase.config);
        
        Map<String, Object> phaseParams = prepareParams(filters, finalResult);
        JsonObject phaseResult = engine.executeQuery(phase.query, phaseParams);
        
        finalResult.put("phase" + (i + 1), phaseResult);
    }
    
    return finalResult;
	}

    private QueryEngine getEngine(String engineType) {
        return engines.select(new EngineType.Literal(engineType)).get();
    }

    private Map<String, Object> prepareParams(Map<String, Object> filters, JsonObject previousResults) {
        Map<String, Object> params = new HashMap<>(filters);
        previousResults.getMap().forEach(params::put);
        return params;
    }

    private void validateConfig(HybridDataSource config, String datasourceId) {
        if (config == null) {
            throw new WebApplicationException(
                "Datasource '" + datasourceId + "' not configured",
                Response.Status.NOT_FOUND
            );
        }
        if (config.phases == null || config.phases.isEmpty()) {
            throw new WebApplicationException(
                "No phases configured for datasource: " + datasourceId,
                Response.Status.BAD_REQUEST
            );
        }
    }
}