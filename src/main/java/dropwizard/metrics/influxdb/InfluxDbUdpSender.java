/**
 * This file is part of the source code and related artifacts for eGym Application.
 *
 * Copyright © 2013 eGym GmbH
 */
package dropwizard.metrics.influxdb;

import static org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;

import dropwizard.metrics.influxdb.data.InfluxDbPoint;
import dropwizard.metrics.influxdb.data.InfluxDbWriteObject;
import dropwizard.metrics.influxdb.utils.MapSerializer;

public class InfluxDbUdpSender implements InfluxDbSender {

	private final String host;

	private final int port;

	private final InfluxDbWriteObject influxDbWriteObject;

	public InfluxDbUdpSender(@NotNull final String host, final int port, @NotNull final String database) {
		this(host, port, database, TimeUnit.MILLISECONDS);
	}

	public InfluxDbUdpSender(@NotNull final String host, final int port, @NotNull final String database,
			@NotNull final TimeUnit timeUnit) {
		this.host = host;
		this.port = port;
		this.influxDbWriteObject = new InfluxDbWriteObject(database, timeUnit);
	}

	@Override
	public void flush() throws IOException {
		influxDbWriteObject.setPoints(new HashSet<InfluxDbPoint>());
	}

	@Override
	public boolean hasSeriesData() {
		return influxDbWriteObject.getPoints() != null && !influxDbWriteObject.getPoints().isEmpty();
	}

	@Override
	public void appendPoints(@NotNull final InfluxDbPoint point) {
		influxDbWriteObject.getPoints().add(point);
	}

	@Override
	public void setTags(@NotNull final Map<String, String> tags) {
		influxDbWriteObject.setTags(tags);
	}

	@Override
	public int writeData() throws Exception {
		DatagramChannel channel = null;

		try {
			channel = DatagramChannel.open();
			final InetSocketAddress socketAddress = new InetSocketAddress(host, port);

			final ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.setSerializationInclusion(Inclusion.NON_EMPTY);
			final SimpleModule module = new SimpleModule("SimpleModule", new Version(1, 0, 0, null));
			module.addSerializer(Map.class, new MapSerializer());
			objectMapper.registerModule(module);

			final String json = objectMapper.writeValueAsString(influxDbWriteObject);

			final ByteBuffer buffer = ByteBuffer.wrap(json.getBytes());
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
