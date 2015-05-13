/**
 * This file is part of the source code and related artifacts for eGym Application.
 *
 * Copyright © 2013 eGym GmbH
 */
package dropwizard.metrics.influxdb;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.SortedMap;

import javax.validation.constraints.NotNull;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Counting;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;

import dropwizard.metrics.influxdb.data.InfluxDbPoint;

public class InfluxDbReporter extends ScheduledReporter {

	public static Builder forRegistry(MetricRegistry registry) {
		return new Builder(registry);
	}

	public static class Builder {
		private final MetricRegistry registry;

		private Map<String, String> tags;

		private TimeUnit rateUnit;

		private TimeUnit durationUnit;

		private MetricFilter filter;

		private boolean skipIdleMetrics;

		private Builder(MetricRegistry registry) {
			this.registry = registry;
			this.tags = null;
			this.rateUnit = TimeUnit.SECONDS;
			this.durationUnit = TimeUnit.MILLISECONDS;
			this.filter = MetricFilter.ALL;
		}

		/**
		 * Add these tags to all metrics.
		 *
		 * @param tags
		 *            a map containing tag name and tag value.
		 *
		 * @return {@code this}
		 */
		public Builder withTags(Map<String, String> tags) {
			this.tags = tags;
			return this;
		}

		/**
		 * Convert rates to the given time unit.
		 *
		 * @param rateUnit
		 *            a unit of time
		 *
		 * @return {@code this}
		 */
		public Builder convertRatesTo(TimeUnit rateUnit) {
			this.rateUnit = rateUnit;
			return this;
		}

		/**
		 * Convert durations to the given time unit.
		 *
		 * @param durationUnit
		 *            a unit of time
		 *
		 * @return {@code this}
		 */
		public Builder convertDurationsTo(TimeUnit durationUnit) {
			this.durationUnit = durationUnit;
			return this;
		}

		/**
		 * Only report metrics which match the given filter.
		 *
		 * @param filter
		 *            a {@link MetricFilter}
		 *
		 * @return {@code this}
		 */
		public Builder filter(MetricFilter filter) {
			this.filter = filter;
			return this;
		}

		/**
		 * Only report metrics that have changed.
		 *
		 * @param skipIdleMetrics
		 *
		 * @return {@code this}
		 */
		public Builder skipIdleMetrics(boolean skipIdleMetrics) {
			this.skipIdleMetrics = skipIdleMetrics;
			return this;
		}

		public InfluxDbReporter build(@NotNull final InfluxDbSender influxdb) {
			return new InfluxDbReporter(registry, influxdb, tags, rateUnit, durationUnit, filter, skipIdleMetrics);
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(InfluxDbReporter.class);

	private final InfluxDbSender influxdb;

	private final boolean skipIdleMetrics;

	private final Map<String, Long> previousValues;

	private Map<String, Map<String, String>> metricTags = new HashMap<>();

	private InfluxDbReporter(@NotNull final MetricRegistry registry, @NotNull final InfluxDbSender influxdb,
			@NotNull final Map<String, String> tags, @NotNull final TimeUnit rateUnit, @NotNull final TimeUnit durationUnit,
			@NotNull final MetricFilter filter, final boolean skipIdleMetrics) {
		super(registry, "influxdb-reporter", filter, rateUnit, durationUnit);
		this.influxdb = influxdb;
		influxdb.setTags(tags);
		this.skipIdleMetrics = skipIdleMetrics;
		this.previousValues = new TreeMap<>();
	}

	@Override
	public void report(@NotNull final SortedMap<String, Gauge> gauges, @NotNull final SortedMap<String, Counter> counters,
			@NotNull final SortedMap<String, Histogram> histograms, @NotNull final SortedMap<String, Meter> meters,
			@NotNull final SortedMap<String, Timer> timers) {
		final DateTime timestamp = new DateTime();

		try {
			influxdb.flush();

			for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
				reportGauge(entry.getKey(), entry.getValue(), timestamp);
			}

			for (Map.Entry<String, Counter> entry : counters.entrySet()) {
				reportCounter(entry.getKey(), entry.getValue(), timestamp);
			}

			for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
				reportHistogram(entry.getKey(), entry.getValue(), timestamp);
			}

			for (Map.Entry<String, Meter> entry : meters.entrySet()) {
				reportMeter(entry.getKey(), entry.getValue(), timestamp);
			}

			for (Map.Entry<String, Timer> entry : timers.entrySet()) {
				reportTimer(entry.getKey(), entry.getValue(), timestamp);
			}

			if (influxdb.hasSeriesData()) {
				influxdb.writeData();
			}
		} catch (Exception e) {
			LOGGER.warn("Unable to report to InfluxDB. Discarding data.", e);
		}
	}

	private void reportTimer(String name, Timer timer, DateTime timestamp) {
		if (canSkipMetric(name, timer)) {
			return;
		}
		final Snapshot snapshot = timer.getSnapshot();
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("count", snapshot.size());
		fields.put("min", convertDuration(snapshot.getMin()));
		fields.put("max", convertDuration(snapshot.getMax()));
		fields.put("mean", convertDuration(snapshot.getMean()));
		fields.put("std-dev", convertDuration(snapshot.getStdDev()));
		fields.put("50-percentile", convertDuration(snapshot.getMedian()));
		fields.put("75-percentile", convertDuration(snapshot.get75thPercentile()));
		fields.put("95-percentile", convertDuration(snapshot.get95thPercentile()));
		fields.put("99-percentile", convertDuration(snapshot.get99thPercentile()));
		fields.put("999-percentile", convertDuration(snapshot.get999thPercentile()));
		fields.put("one-minute", convertRate(timer.getOneMinuteRate()));
		fields.put("five-minute", convertRate(timer.getFiveMinuteRate()));
		fields.put("fifteen-minute", convertRate(timer.getFifteenMinuteRate()));
		fields.put("mean-rate", convertRate(timer.getMeanRate()));
		fields.put("run-count", timer.getCount());
		Map<String, String> tags = metricTags.containsKey(name) ? metricTags.get(name) : null;
		influxdb.appendPoints(new InfluxDbPoint(name, tags, timestamp.toString(), fields));
	}

	private void reportHistogram(String name, Histogram histogram, DateTime timestamp) {
		if (canSkipMetric(name, histogram)) {
			return;
		}
		final Snapshot snapshot = histogram.getSnapshot();
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("count", snapshot.size());
		fields.put("min", snapshot.getMin());
		fields.put("max", snapshot.getMax());
		fields.put("mean", snapshot.getMean());
		fields.put("std-dev", snapshot.getStdDev());
		fields.put("50-percentile", snapshot.getMedian());
		fields.put("75-percentile", snapshot.get75thPercentile());
		fields.put("95-percentile", snapshot.get95thPercentile());
		fields.put("99-percentile", snapshot.get99thPercentile());
		fields.put("999-percentile", snapshot.get999thPercentile());
		fields.put("run-count", histogram.getCount());
		Map<String, String> tags = metricTags.get(name);
		influxdb.appendPoints(new InfluxDbPoint(name, tags, timestamp.toString(), fields));
	}

	private void reportCounter(String name, Counter counter, DateTime timestamp) {
		Map<String, Object> fields = new HashMap<>();
		fields.put("count", counter.getCount());
		Map<String, String> tags = metricTags.get(name);
		influxdb.appendPoints(new InfluxDbPoint(name, tags, timestamp.toString(), fields));
	}

	private void reportGauge(String name, Gauge<?> gauge, DateTime timestamp) {
		Map<String, Object> fields = new HashMap<>();
		fields.put("value", gauge.getValue());
		Map<String, String> tags = metricTags.get(name);
		influxdb.appendPoints(new InfluxDbPoint(name, tags, timestamp.toString(), fields));
	}

	private void reportMeter(String name, Metered meter, DateTime timestamp) {
		if (canSkipMetric(name, meter)) {
			return;
		}
		Map<String, Object> fields = new HashMap<>();
		fields.put("count", meter.getCount());
		fields.put("one-minute", convertRate(meter.getOneMinuteRate()));
		fields.put("five-minute", convertRate(meter.getFiveMinuteRate()));
		fields.put("fifteen-minute", convertRate(meter.getFifteenMinuteRate()));
		fields.put("mean-rate", convertRate(meter.getMeanRate()));
		Map<String, String> tags = metricTags.get(name);
		influxdb.appendPoints(new InfluxDbPoint(name, tags, timestamp.toString(), fields));
	}

	private boolean canSkipMetric(String name, Counting counting) {
		boolean isIdle = (calculateDelta(name, counting.getCount()) == 0);
		if (skipIdleMetrics && !isIdle) {
			previousValues.put(name, counting.getCount());
		}
		return skipIdleMetrics && isIdle;
	}

	private long calculateDelta(String name, long count) {
		Long previous = previousValues.get(name);
		if (previous == null) {
			return -1;
		}
		if (count < previous) {
			LOGGER.warn("Saw a non-monotonically increasing value for metric '{}'", name);
			return 0;
		}
		return count - previous;
	}

	/**
	 * Adds tags for the Metric with name
	 * 
	 * @param metricName
	 *            the name of the metric
	 * @param tags
	 *            Map containing the tag name and value.
	 */
	public void putTagsForMetric(@NotNull final String metricName, @NotNull final Map<String, String> tags) {
		metricTags.put(metricName, tags);
	}
}
