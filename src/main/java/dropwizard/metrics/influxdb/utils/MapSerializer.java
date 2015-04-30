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

public class MapSerializer extends JsonSerializer<Map> {
	@Override
	public void serialize(@NotNull final Map influxDbMap, @NotNull final JsonGenerator jsonGenerator,
			@NotNull final SerializerProvider provider) throws IOException {
		{
			jsonGenerator.writeStartObject();
			if (influxDbMap != null) {
				for (Object key : influxDbMap.keySet()) {
					jsonGenerator.writeFieldName((String) key);
					jsonGenerator.writeObject(influxDbMap.get(key));
				}
			}
			jsonGenerator.writeEndObject();
		}
	}
}
