package com.example.dashboard.config;

import java.util.List;
import java.util.Map;

public class HybridDataSource {
    public String id;
    public List<PhaseConfig> phases;
    
    public static class PhaseConfig {
        public int phase;
        public String engine;
        public Map<String, Object> config;
        public String query;
    }
}