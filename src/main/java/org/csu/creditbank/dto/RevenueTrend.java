package org.csu.creditbank.dto;

import java.util.List;
import java.util.Map;

public class RevenueTrend {
    private List<Map<String, Object>> daily;
    private List<Map<String, Object>> statusDistribution;

    public RevenueTrend() {}
    public RevenueTrend(List<Map<String, Object>> daily, List<Map<String, Object>> statusDistribution) {
        this.daily = daily; this.statusDistribution = statusDistribution;
    }
    public List<Map<String, Object>> getDaily() { return daily; }
    public void setDaily(List<Map<String, Object>> daily) { this.daily = daily; }
    public List<Map<String, Object>> getStatusDistribution() { return statusDistribution; }
    public void setStatusDistribution(List<Map<String, Object>> statusDistribution) { this.statusDistribution = statusDistribution; }
}
