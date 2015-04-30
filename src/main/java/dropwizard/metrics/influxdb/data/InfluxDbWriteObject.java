/**
 * This file is part of the source code and related artifacts for eGym Application.
 *
 * Copyright © 2013 eGym GmbH
 */
package dropwizard.metrics.influxdb.data;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;

public class InfluxDbWriteObject {

	private String database;

	private String retentionPolicy;

	private String precision;

	private Set<InfluxDbPoint> points;

	private Map<String, String> tags;

	public InfluxDbWriteObject(@NotNull final String database, @NotNull final TimeUnit timeUnit) {
		this(database, "default", timeUnit);
	}

	public InfluxDbWriteObject(@NotNull final String database, @NotNull final String retentionPolicy, @NotNull final TimeUnit timeUnit) {
		this.points = new HashSet<InfluxDbPoint>();
		this.database = database;
		this.retentionPolicy = retentionPolicy;
		this.precision = toTimePrecision(timeUnit);
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

	private static String toTimePrecision(TimeUnit t) {
		switch (t) {
			case SECONDS:
				return "s";
			case MILLISECONDS:
				return "ms";
			case MICROSECONDS:
				return "u";
			default:
				throw new IllegalArgumentException("time precision should be SECONDS or MILLISECONDS or MICROSECONDS");
		}
	}
}
