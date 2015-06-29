/**
 * This file is part of the source code and related artifacts for eGym Application.
 *
 * Copyright © 2013 eGym GmbH
 */
package dropwizard.metrics.influxdb.data;

import java.util.Map;

import javax.validation.constraints.NotNull;

public class InfluxDbPoint {

	private String measurement;

	private Map<String, String> tags;

	private String timestamp;

	private Map<String, Object> fields;


	public InfluxDbPoint(@NotNull final String name, @NotNull final String timestamp, @NotNull final Map<String, Object> fields) {
		this.measurement = name;
		this.timestamp = timestamp;
		this.fields = fields;
	}

	public InfluxDbPoint(String name, Map<String, String> tags, String timestamp, Map<String, Object> fields) {
		this.measurement = name;
		this.tags = tags;
		this.timestamp = timestamp;
		this.fields = fields;
	}

	public String getMeasurement() {
        return measurement;
    }

    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }

    public Map<String, String> getTags() {
		return tags;
	}

	public void setTags(Map<String, String> tags) {
		this.tags = tags;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public Map<String, Object> getFields() {
		return fields;
	}

	public void setFields(Map<String, Object> fields) {
		this.fields = fields;
	}
}
