/**
 * This file is part of the source code and related artifacts for eGym Application.
 *
 * Copyright © 2013 eGym GmbH
 */
package dropwizard.metrics.influxdb;

import static org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import com.google.common.base.Strings;
import dropwizard.metrics.influxdb.data.InfluxDbPoint;
import dropwizard.metrics.influxdb.data.InfluxDbWriteObject;
import dropwizard.metrics.influxdb.utils.MapSerializer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;

public class InfluxDbHttpSender implements InfluxDbSender {

	private MetricsBuilder metricsBuilder = new MetricsBuilder();

	private InfluxDbWriteObject influxDbWriteObject = new InfluxDbWriteObject();

	private static final Charset UTF_8 = Charset.forName("UTF-8");

	private final URL url;

	public InfluxDbHttpSender(String host, int port, String database, String username, String password) throws Exception {
		this(host, port, database, "default", username, password, TimeUnit.MILLISECONDS);
	}

	public InfluxDbHttpSender(String host, int port, String database, String retentionPolicy, String username, String password,
			TimeUnit timePrecision) throws Exception {
		if (!Strings.isNullOrEmpty(username) && !Strings.isNullOrEmpty(password)) {
			this.url = new URL("http", host, port, "/write?u=" + username + "&p=" + password);
		} else {
			this.url = new URL("http", host, port, "/write");
		}
		influxDbWriteObject.setDatabase(database);
		influxDbWriteObject.setRetentionPolicy(retentionPolicy);
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
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Inclusion.NON_EMPTY);
		SimpleModule module = new SimpleModule("SimpleModule", new Version(1, 0, 0, null));
		module.addSerializer(Map.class, new MapSerializer());
		objectMapper.registerModule(module);
		String json = objectMapper.writeValueAsString(metricsBuilder.getInfluxDbObject());

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		OutputStream wr = con.getOutputStream();
		wr.write(json.getBytes(UTF_8));
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_OK) {
			con.getInputStream().close();
		} else {
			throw new IOException("Server returned HTTP response code: " + responseCode + "for URL: " + url
					+ " with content :'" + con.getResponseMessage() + "'");
		}
		return responseCode;
	}

}
