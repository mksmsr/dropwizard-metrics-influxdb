/**
 * This file is part of the source code and related artifacts for eGym Application.
 *
 * Copyright © 2013 eGym GmbH
 */
package dropwizard.metrics.influxdb.data;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InfluxDbWriteObject {

	private String database;

	private String retentionPolicy;

	private Set<InfluxDbPoint> points;

	private Map<String, String> tags;

	public InfluxDbWriteObject() {
		points = new HashSet<InfluxDbPoint>();
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getRetentionPolicy() {
		return retentionPolicy;
	}

	public void setRetentionPolicy(String retentionPolicy) {
		this.retentionPolicy = retentionPolicy;
	}

	public Set<InfluxDbPoint> getPoints() {
		return points;
	}

	public void setPoints(Set<InfluxDbPoint> points) {
		this.points = points;
	}

	public Map<String, String> getTags() {
		return tags;
	}

	public void setTags(Map<String, String> tags) {
		this.tags = tags;
	}

}
