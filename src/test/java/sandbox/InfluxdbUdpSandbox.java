package sandbox;

import com.codahale.metrics.ScheduledReporter;
import com.google.common.collect.ImmutableMap;
import dropwizard.metrics.influxdb.InfluxDbReporter;
import dropwizard.metrics.influxdb.InfluxDbUdpSender;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;

public class InfluxdbUdpSandbox {

	public static void main(String[] args) {
		InfluxDbReporter influxDbReporter = null;
		ScheduledReporter consoleReporter = null;
		try {
			final MetricRegistry registry = new MetricRegistry();
			influxDbReporter = getInfluxdbReporter(registry);
			consoleReporter = getConsoleReporter(registry);
			influxDbReporter.start(3, TimeUnit.SECONDS);
			consoleReporter.start(3, TimeUnit.SECONDS);

			final Counter counter1 = registry.counter("counter1");
			final Map<String, String> tags = ImmutableMap.of("env", "test");
			influxDbReporter.putTagsForMetric("counter1", tags);

			final Counter counter2 = registry.counter("counter2");

			for (int i = 0; i < 20; ++i) {
				counter1.inc();
				counter2.inc();
				Thread.sleep(Math.round(Math.random()) * 1000);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		} finally {
			if (influxDbReporter != null) {
				influxDbReporter.report();
				influxDbReporter.stop();
			}
			if (consoleReporter != null) {
				consoleReporter.report();
				consoleReporter.stop();
			}
		}
	}

	private static InfluxDbReporter getInfluxdbReporter(MetricRegistry registry) throws Exception {
		final InfluxDbUdpSender influxDb = new InfluxDbUdpSender("127.0.0.1", 4444, "graphite");
		final Map<String, String> tags = ImmutableMap.of("host", "localhost");
		return InfluxDbReporter
				.forRegistry(registry)
				.withTags(tags)
				.convertRatesTo(TimeUnit.SECONDS)
				.convertDurationsTo(TimeUnit.MILLISECONDS)
				.filter(MetricFilter.ALL)
				.build(influxDb);
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	private static ConsoleReporter getConsoleReporter(MetricRegistry registry) throws Exception {
		final ConsoleReporter reporter = ConsoleReporter
				.forRegistry(registry)
				.convertRatesTo(TimeUnit.SECONDS)
				.convertDurationsTo(TimeUnit.MILLISECONDS)
				.build();
		reporter.start(1, TimeUnit.MINUTES);
		return reporter;
	}

}
