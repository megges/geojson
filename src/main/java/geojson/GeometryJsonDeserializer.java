package geojson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.vividsolutions.jts.geom.*;

import java.io.IOException;

@SuppressWarnings({"WeakerAccess", "DuplicateThrows"})
public class GeometryJsonDeserializer extends JsonDeserializer<Geometry> {

    private static final GeometryFactory factory = GeoUtil.getWgs84GeometryFactory();

    @Override
    public Geometry deserialize(JsonParser jsonParser, DeserializationContext arg1) throws IOException,
            JsonProcessingException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);
        return geometry(node);
    }

    Geometry geometry(JsonNode node) {
        if (node.get("type") == null)
            throw new IllegalArgumentException("no type specified");

        String type = node.get("type").textValue();
        ArrayNode coordinates = (ArrayNode) node.get("coordinates");

        switch (type) {
            case "Point":
                return point(coordinates);
            case "MultiPoint":
                return multiPoint(coordinates);
            case "LineString":
                return lineString(coordinates);
            case "MultiLineString":
                return multiLineString(coordinates);
            case "Polygon":
                return polygon(coordinates);
            case "MultiPolygon":
                return multiPolygon(coordinates);
            case "GeometryCollection":
                return geometryCollection((ArrayNode) node.get("geometries"));
            default:
                throw new IllegalArgumentException("invalid Feature type: " + type);
        }
    }

    Geometry point(ArrayNode coordinates) {
        Coordinate coordinate = toCoordinate(coordinates);
        return factory.createPoint(coordinate);
    }

    Geometry multiPoint(ArrayNode nodes) {
        Coordinate[] coordinates = toCoordinateArray(nodes);
        return factory.createMultiPoint(coordinates);
    }

    LineString lineString(ArrayNode nodes) {
        Coordinate[] coordinates = toCoordinateArray(nodes);
        return factory.createLineString(coordinates);
    }

    MultiLineString multiLineString(ArrayNode nodes) {
        LineString[] lineStrings = new LineString[nodes.size()];
        for (int i = 0; i < lineStrings.length; ++i) {
            lineStrings[i] = lineString((ArrayNode) nodes.get(i));
        }
        return factory.createMultiLineString(lineStrings);
    }

    Polygon polygon(ArrayNode nodes) {
        LinearRing outerRing = toLinearRing((ArrayNode) nodes.get(0));
        LinearRing[] innerRings = new LinearRing[nodes.size() - 1];
        for (int i = 0; i < innerRings.length; ++i) {
            innerRings[i] = toLinearRing((ArrayNode) nodes.get(i + 1));
        }
        return factory.createPolygon(outerRing, innerRings);
    }

    MultiPolygon multiPolygon(ArrayNode nodes) {
        Polygon[] polygons = new Polygon[nodes.size()];
        for (int i = 0; i < polygons.length; ++i) {
            polygons[i] = polygon((ArrayNode) nodes.get(i));
        }
        return factory.createMultiPolygon(polygons);
    }

    GeometryCollection geometryCollection(ArrayNode nodes) {
        Geometry[] geometries = new Geometry[nodes.size()];
        for (int i = 0; i < geometries.length; ++i) {
            geometries[i] = geometry(nodes.get(i));
        }
        return factory.createGeometryCollection(geometries);
    }

    LinearRing toLinearRing(ArrayNode nodes) {
        Coordinate[] coordinates = toCoordinateArray(nodes);
        return factory.createLinearRing(coordinates);
    }

    Coordinate[] toCoordinateArray(ArrayNode nodes) {
        Coordinate[] result = new Coordinate[nodes.size()];
        for (int i = 0; i < result.length; ++i) {
            result[i] = toCoordinate((ArrayNode) nodes.get(i));
        }
        return result;
    }

    Coordinate toCoordinate(ArrayNode node) {
        double x = 0, y = 0, z = Coordinate.NULL_ORDINATE;
        if (node.size() > 1) {
            x = node.get(0).asDouble();
            y = node.get(1).asDouble();
        }
        if (node.size() > 2) {
            z = node.get(1).asDouble();
        }
        return new Coordinate(x, y, z);
    }
}