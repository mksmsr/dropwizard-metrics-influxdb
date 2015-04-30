/**
 * This file is part of the source code and related artifacts for eGym Application.
 *
 * Copyright © 2013 eGym GmbH
 */
package sandbox;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import com.google.common.collect.ImmutableMap;
import dropwizard.metrics.influxdb.InfluxDbHttpSender;
import dropwizard.metrics.influxdb.InfluxDbReporter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SendToLocalInfluxDB {

	public static void main(String[] args) {
		InfluxDbReporter influxDbReporter = null;
		ScheduledReporter consoleReporter = null;
		Timer.Context context = null;
		try {
			final MetricRegistry registry = new MetricRegistry();
			consoleReporter = startConsoleReporter(registry);
			influxDbReporter = startInfluxdbReporter(registry);

			final Meter myMeter = registry.meter("testMetric");
			final Map<String, String> tags = ImmutableMap.of("env", "test");
			influxDbReporter.putTagsForMetric("testMetric", tags);

			final Timer myTimer = registry.timer("testTimer");
			context = myTimer.time();
			for (int i = 0; i < 5; i++) {
				myMeter.mark();
				myMeter.mark(Math.round(Math.random() * 100.0));
				Thread.sleep(2000);
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			System.exit(1);
		} finally {
			context.stop();
			if (influxDbReporter != null) {
				influxDbReporter.report();
				influxDbReporter.stop();
			}
			if (consoleReporter != null) {
				consoleReporter.report();
				consoleReporter.stop();
			}
			System.out.println("Finished");
		}
	}

	private static InfluxDbReporter startInfluxdbReporter(MetricRegistry registry) throws Exception {
		final InfluxDbHttpSender influxDb = new InfluxDbHttpSender("127.0.0.1", 8086, "graphite", "root", "root");
		final Map<String, String> tags = ImmutableMap.of("host", "localhost");
		final InfluxDbReporter reporter = InfluxDbReporter
				.forRegistry(registry)
				.withTags(tags)
				.convertRatesTo(TimeUnit.SECONDS)
				.convertDurationsTo(TimeUnit.MILLISECONDS)
				.filter(MetricFilter.ALL)
				.build(influxDb);
		reporter.start(10, TimeUnit.SECONDS);
		return reporter;
	}

	private static ConsoleReporter startConsoleReporter(MetricRegistry registry) throws Exception {
		final ConsoleReporter reporter = ConsoleReporter
				.forRegistry(registry)
				.convertRatesTo(TimeUnit.SECONDS)
				.convertDurationsTo(TimeUnit.MILLISECONDS)
				.build();
		reporter.start(1, TimeUnit.MINUTES);
		return reporter;
	}
}
