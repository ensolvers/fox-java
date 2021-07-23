package com.ensolvers.fox.quality.model;

import java.util.List;

/**
 * Represents a response for a metric history request against SonarQube
 *
 * @author josematiasrivero
 */
public class SonarQubeMetricHistoryResponse {

    private List<SonarQubeMetricHistory> measures;

    protected SonarQubeMetricHistoryResponse() {
    }

    public SonarQubeMetricHistoryResponse(List<SonarQubeMetricHistory> measures) {
        this.measures = measures;
    }

    public List<SonarQubeMetricHistory> getMeasures() {
        return measures;
    }
}
