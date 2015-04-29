/**
 * This file is part of the source code and related artifacts for eGym Application.
 *
 * Copyright © 2013 eGym GmbH
 */
package dropwizard.metrics.influxdb;

import static org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import dropwizard.metrics.influxdb.data.InfluxDbPoint;
import dropwizard.metrics.influxdb.data.InfluxDbWriteObject;
import dropwizard.metrics.influxdb.utils.MapSerializer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Map;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;

public class InfluxDbUdpSender implements InfluxDbSender {

	private final String host;

	private final int port;

	public MetricsBuilder metricsBuilder = new MetricsBuilder();

	InfluxDbWriteObject influxDbWriteObject = new InfluxDbWriteObject();

	public InfluxDbUdpSender(String host, int port, String database) {
		this.host = host;
		this.port = port;
		influxDbWriteObject.setDatabase(database);
		influxDbWriteObject.setRetentionPolicy("default");
		metricsBuilder.setInfluxDbObject(influxDbWriteObject);
	}

	@Override
	public void flush() throws IOException {
		metricsBuilder.flush();
	}

	@Override
	public boolean hasSeriesData() {
		return metricsBuilder.hasPoints();
	}

	@Override
	public void appendPoints(InfluxDbPoint point) {
		metricsBuilder.appendPoint(point);
	}

	@Override
	public void setTags(Map<String, String> tags) {
		metricsBuilder.setTags(tags);
	}

	@Override
	public int writeData() throws Exception {
		DatagramChannel channel = null;

		try {
			channel = DatagramChannel.open();
			InetSocketAddress socketAddress = new InetSocketAddress(host, port);

			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.setSerializationInclusion(Inclusion.NON_EMPTY);
			SimpleModule module = new SimpleModule("SimpleModule", new Version(1, 0, 0, null));
			module.addSerializer(Map.class, new MapSerializer());
			objectMapper.registerModule(module);

			String json = objectMapper.writeValueAsString(metricsBuilder.getInfluxDbObject());

			ByteBuffer buffer = ByteBuffer.wrap(json.getBytes());
			channel.send(buffer, socketAddress);
			buffer.clear();
		} catch (Exception e) {
			throw e;
		} finally {
			if (channel != null) {
				channel.close();
			}
		}

		return 0;
	}
}
