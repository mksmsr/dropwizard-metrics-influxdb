/**
 * This file is part of the source code and related artifacts for eGym Application.
 *
 * Copyright © 2013 eGym GmbH
 */
package dropwizard.metrics.influxdb;

import dropwizard.metrics.influxdb.data.InfluxDbPoint;
import dropwizard.metrics.influxdb.data.InfluxDbWriteObject;
import java.util.HashSet;
import java.util.Map;

public class MetricsBuilder {

	private InfluxDbWriteObject influxDbObject;

	public boolean hasPoints() {
		return influxDbObject.getPoints() != null && influxDbObject.getPoints().size() > 0;
	}

	public void flush() {
		influxDbObject.setPoints(new HashSet<InfluxDbPoint>());
	}

	public void appendPoint(InfluxDbPoint point) {
		influxDbObject.getPoints().add(point);
	}

	public InfluxDbWriteObject getInfluxDbObject() {
		return influxDbObject;
	}

	public void setInfluxDbObject(InfluxDbWriteObject influxDbObject) {
		this.influxDbObject = influxDbObject;
	}

	public void setTags(Map<String, String> tags)
	{
		influxDbObject.setTags(tags);
	}
}
