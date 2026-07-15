package org.csu.creditbank.dto;

import java.util.List;
import java.util.Map;

public class IntegrityDistribution {
    private List<Map<String, Object>> levelDistribution;
    private List<Map<String, Object>> byInstitution;

    public IntegrityDistribution() {}
    public IntegrityDistribution(List<Map<String, Object>> levelDistribution, List<Map<String, Object>> byInstitution) {
        this.levelDistribution = levelDistribution; this.byInstitution = byInstitution;
    }
    public List<Map<String, Object>> getLevelDistribution() { return levelDistribution; }
    public void setLevelDistribution(List<Map<String, Object>> levelDistribution) { this.levelDistribution = levelDistribution; }
    public List<Map<String, Object>> getByInstitution() { return byInstitution; }
    public void setByInstitution(List<Map<String, Object>> byInstitution) { this.byInstitution = byInstitution; }
}
