/**
 * This file is part of the source code and related artifacts for eGym Application.
 *
 * Copyright © 2013 eGym GmbH
 */
package dropwizard.metrics.influxdb;

import java.io.IOException;
import java.util.Map;

import javax.validation.constraints.NotNull;

import dropwizard.metrics.influxdb.data.InfluxDbPoint;

public interface InfluxDbSender {

	/**
	 * Flushes buffer, if applicable
	 *
	 * @throws IOException
	 */
	void flush() throws IOException;

	/**
	 * Returns true if ready to send data
	 *
	 */
	public boolean hasSeriesData();

	/**
	 * Adds this metric point to the buffer
	 *
	 * @param point
	 *            Metric point with tags and fields
	 */
	public void appendPoints(@NotNull final InfluxDbPoint point);

	/**
	 * Writes buffer data to InfluxDb
	 *
	 * @return the response code for the request sent to InfluxDb
	 * @throws Exception
	 */
	public int writeData() throws Exception;

	/**
	 * Set tags applicable for all the points.
	 *
	 * @param tags
	 */
	public void setTags(@NotNull final Map<String, String> tags);

}
