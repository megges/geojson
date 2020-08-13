package geojson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.vividsolutions.jts.geom.Envelope;

import java.io.IOException;

@SuppressWarnings("WeakerAccess")
public class EnvelopeJsonSerializer extends JsonSerializer<Envelope> {

    @Override
    public void serialize(Envelope envelope, JsonGenerator jgen,
                          SerializerProvider provider) throws IOException {

        jgen.writeStartArray();

        jgen.writeNumber(envelope.getMinX());
        jgen.writeNumber(envelope.getMinY());
        jgen.writeNumber(envelope.getMaxX());
        jgen.writeNumber(envelope.getMaxY());

        jgen.writeEndArray();

    }

}
