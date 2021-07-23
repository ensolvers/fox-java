package com.ensolvers.fox.quality.model;

import java.time.Instant;
import java.util.Date;

/**
 * Represents a specific measure in the context of a SonarQube metric
 *
 * @author josematiasrivero
 */
public class SonarQubeMetricMeasure {

    private String value;
    private Date date;

    protected SonarQubeMetricMeasure() {
    }

    public SonarQubeMetricMeasure(String value, Date date) {
        this.value = value;
        this.date = date;
    }

    public String getValue() {
        return value;
    }

    public Date getDate() {
        return date;
    }
}
