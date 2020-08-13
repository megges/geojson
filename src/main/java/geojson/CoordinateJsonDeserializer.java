package geojson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.vividsolutions.jts.geom.Coordinate;

import java.io.IOException;
import java.util.Iterator;

/**
 * Deserializes a JSON location String to a Coordinate. Lon/Lat (x/y) is expected for Wgs84 coordinates.
 * expected format: [x,y,z] (z is optional)
 *
 * @author marcus
 */
@SuppressWarnings({"DuplicateThrows", "WeakerAccess"})
public class CoordinateJsonDeserializer extends JsonDeserializer<Coordinate> {

    @Override
    public Coordinate deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        if (jp.nextToken() == JsonToken.VALUE_NULL)
            return null;

        Coordinate coordinate = new Coordinate();
        Iterator<Double> doubleIterator = jp.readValuesAs(Double.class);
        int ordinate = 0;
        while (doubleIterator.hasNext())
            coordinate.setOrdinate(ordinate++, doubleIterator.next());

        return coordinate;
    }
}
