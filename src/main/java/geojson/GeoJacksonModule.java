package geojson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

@SuppressWarnings("WeakerAccess")
public class GeoJacksonModule extends SimpleModule {

    public GeoJacksonModule() {
        super("GeoJacksonModule", new Version(1, 0, 0, "SNAPSHOT", "GROUP", "ARTIFACT"));
    }

    @Override
    public void setupModule(SetupContext context) {
        SimpleSerializers serializers = new SimpleSerializers();
        SimpleDeserializers deserializers = new SimpleDeserializers();

        serializers.addSerializer(Geometry.class, new GeometryJsonSerializer());
        serializers.addSerializer(Coordinate.class, new CoordinateJsonSerializer());
        serializers.addSerializer(Envelope.class, new EnvelopeJsonSerializer());

        deserializers.addDeserializer(Geometry.class, new GeometryJsonDeserializer());
        deserializers.addDeserializer(Coordinate.class, new CoordinateJsonDeserializer());
        deserializers.addDeserializer(Envelope.class, new EnvelopeJsonDeserializer());

        context.addSerializers(serializers);
        context.addDeserializers(deserializers);
    }

}
