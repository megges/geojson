package geojson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.vividsolutions.jts.geom.*;

import java.io.IOException;

@SuppressWarnings("WeakerAccess")
public class GeometryJsonSerializer extends JsonSerializer<Geometry> {

	private JsonGenerator jgen;

	@Override
	public void serialize(Geometry geometry, JsonGenerator jgen, SerializerProvider provider) throws IOException {
		this.jgen = jgen;

		String geometryType = geometry.getGeometryType();

		jgen.writeStartObject();
		jgen.writeStringField("type", geometryType);

		switch (geometryType) {
			case "Point":
				jgen.writeArrayFieldStart("coordinates");
				pointCoordinates((Point) geometry);
				jgen.writeEndArray();
				break;
			case "MultiPoint":
				jgen.writeArrayFieldStart("coordinates");
				for (int i = 0; i < geometry.getNumGeometries(); i++) {
					Point child = (Point) geometry.getGeometryN(i);
					jgen.writeStartArray();
					pointCoordinates(child);
					jgen.writeEndArray();
				}
				jgen.writeEndArray();
				break;
			case "LineString":
				jgen.writeArrayFieldStart("coordinates");
				lineStringCoordinates((LineString) geometry);
				jgen.writeEndArray();
				break;
			case "MultiLineString":
				jgen.writeArrayFieldStart("coordinates");
				for (int i = 0; i < geometry.getNumGeometries(); i++) {
					LineString child = (LineString) geometry.getGeometryN(i);
					jgen.writeStartArray();
					lineStringCoordinates(child);
					jgen.writeEndArray();
				}
				jgen.writeEndArray();
				break;
			case "Polygon":
				jgen.writeArrayFieldStart("coordinates");
				polygonCoordinates((Polygon) geometry);
				jgen.writeEndArray();
				break;
			case "MultiPolygon":
				jgen.writeArrayFieldStart("coordinates");
				for (int i = 0; i < geometry.getNumGeometries(); i++) {
					Polygon child = (Polygon) geometry.getGeometryN(i);
					jgen.writeStartArray();
					polygonCoordinates(child);
					jgen.writeEndArray();
				}
				jgen.writeEndArray();
				break;
			case "GeometryCollection":
				jgen.writeArrayFieldStart("geometries");
				for (int i = 0; i < geometry.getNumGeometries(); i++) {
					Geometry child = geometry.getGeometryN(i);
					serialize(child, jgen, provider);
				}
				jgen.writeEndArray();
				break;
			default:
				throw new IllegalArgumentException("Unknown geometry type " + geometry.getGeometryType());
		}

		jgen.writeEndObject();

	}

	void pointCoordinates(Point geometry) throws IOException {
		toJson(geometry.getCoordinate());
	}

	void lineStringCoordinates(LineString geometry) throws IOException {
		toJson(geometry.getCoordinates());
	}

	void polygonCoordinates(Polygon polygon) throws IOException {
		jgen.writeStartArray();
		toJson(polygon.getExteriorRing().getCoordinates());
		jgen.writeEndArray();

		if (polygon.getNumInteriorRing() > 0) {
			jgen.writeStartArray();
			for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
				toJson(polygon.getInteriorRingN(i).getCoordinates());
			}
			jgen.writeEndArray();
		}
	}

	void toJson(Coordinate[] coordinates) throws IOException {
		for (Coordinate coordinate : coordinates) {
			jgen.writeStartArray();
			toJson(coordinate);
			jgen.writeEndArray();
		}
	}

	void toJson(Coordinate coordinate) throws IOException {
		jgen.writeNumber(coordinate.x);
		jgen.writeNumber(coordinate.y);
	}

}
