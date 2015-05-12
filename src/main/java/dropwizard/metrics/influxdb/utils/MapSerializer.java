/**
 * This file is part of the source code and related artifacts for eGym Application.
 *
 * Copyright © 2013 eGym GmbH
 */
package dropwizard.metrics.influxdb.utils;

import java.io.IOException;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

public class MapSerializer<P, Q> extends JsonSerializer<Map<P, Q>> {

	@Override
	public void serialize(@NotNull final Map<P, Q> influxDbMap, @NotNull final JsonGenerator jsonGenerator,
			@NotNull final SerializerProvider provider) throws IOException {
		jsonGenerator.writeStartObject();
		if (influxDbMap != null) {
			for (Map.Entry<P, Q> entry : influxDbMap.entrySet()) {
				jsonGenerator.writeFieldName((String) entry.getKey());
				jsonGenerator.writeObject(entry.getValue());
			}
		}
		jsonGenerator.writeEndObject();
	}
}
