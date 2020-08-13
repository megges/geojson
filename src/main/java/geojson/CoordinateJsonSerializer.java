package geojson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.vividsolutions.jts.geom.Coordinate;

import java.io.IOException;

@SuppressWarnings("WeakerAccess")
public class CoordinateJsonSerializer extends JsonSerializer<Coordinate> {

    @Override
    public void serialize(Coordinate coordinate, JsonGenerator jgen,
                          SerializerProvider provider) throws IOException {

        jgen.writeStartArray();

        jgen.writeNumber(coordinate.x);
        jgen.writeNumber(coordinate.y);

        // currently just 2D supported
//        if (!Double.isNaN(coordinate.z))
//            jgen.writeNumber(coordinate.z);

        jgen.writeEndArray();

    }

}
