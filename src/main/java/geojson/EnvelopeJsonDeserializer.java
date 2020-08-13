package geojson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.vividsolutions.jts.geom.Envelope;

import java.io.IOException;

@SuppressWarnings({"DuplicateThrows", "WeakerAccess"})
public class EnvelopeJsonDeserializer extends JsonDeserializer<Envelope> {

    @Override
    public Envelope deserialize(JsonParser jsonParser, DeserializationContext arg1) throws IOException,
            JsonProcessingException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);
        return envelope((ArrayNode) node);
    }

    Envelope envelope(ArrayNode node) {
        return new Envelope(node.get(0).asDouble(), node.get(2).asDouble(),
                node.get(1).asDouble(), node.get(3).asDouble());
    }

}