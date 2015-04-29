/**
 * This file is part of the source code and related artifacts for eGym Application.
 *
 * Copyright © 2013 eGym GmbH
 */
package dropwizard.metrics.influxdb.utils;

import java.io.IOException;
import java.util.Map;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

public class MapSerializer extends JsonSerializer<Map> {
	@Override
	public void serialize(Map influxDbMap, JsonGenerator jgen, SerializerProvider provider) throws IOException {
		{
			jgen.writeStartObject();
			if(influxDbMap != null){
				for(Object key :  influxDbMap.keySet()){
					jgen.writeFieldName((String) key);
					jgen.writeObject(influxDbMap.get(key));
				}
			}
			jgen.writeEndObject();
		}
	}
}
