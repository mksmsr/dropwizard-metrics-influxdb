/**
 * This file is part of the source code and related artifacts for eGym Application.
 *
 * Copyright © 2013 eGym GmbH
 */
package dropwizard.metrics.influxdb;

import static org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;

import com.google.common.base.Strings;

import dropwizard.metrics.influxdb.data.InfluxDbPoint;
import dropwizard.metrics.influxdb.data.InfluxDbWriteObject;
import dropwizard.metrics.influxdb.utils.MapSerializer;

public class InfluxDbHttpSender implements InfluxDbSender {

	private InfluxDbWriteObject influxDbWriteObject;

	private static final Charset UTF_8 = Charset.forName("UTF-8");

	private final URL url;

	public InfluxDbHttpSender(@NotNull final String host, final int port, @NotNull final String database, @NotNull final String username,
			@NotNull final String password) throws Exception {
		this(host, port, database, "default", username, password, TimeUnit.MILLISECONDS);
	}

	public InfluxDbHttpSender(@NotNull final String host, final int port, @NotNull final String database,
			@NotNull final String retentionPolicy, @NotNull final String username, @NotNull final String password,
			@NotNull final TimeUnit timePrecision) throws Exception {
		if (!Strings.isNullOrEmpty(username) && !Strings.isNullOrEmpty(password)) {
			this.url = new URL("http", host, port, "/write?u=" + username + "&p=" + password);
		} else {
			this.url = new URL("http", host, port, "/write");
		}
		this.influxDbWriteObject = new InfluxDbWriteObject(database, retentionPolicy, timePrecision);
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
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Inclusion.NON_EMPTY);
		final SimpleModule module = new SimpleModule("SimpleModule", new Version(1, 0, 0, null));
		module.addSerializer(Map.class, new MapSerializer());
		objectMapper.registerModule(module);
		final String json = objectMapper.writeValueAsString(influxDbWriteObject);

		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		final OutputStream wr = con.getOutputStream();
		wr.write(json.getBytes(UTF_8));
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_OK) {
			con.getInputStream().close();
		} else {
			throw new IOException("Server returned HTTP response code: " + responseCode + "for URL: " + url + " with content :'"
					+ con.getResponseMessage() + "'");
		}
		return responseCode;
	}

}
