package geojson;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTReader;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GeoUtilTest {

    private static Point MARIENPLATZ_STUTTGART = GeoUtil.asWgs84(9.1679004, 48.76456);

    @Test
    public void testProjectToMercatorAndInverse() throws Exception {
        Point pM = GeoUtil.project(MARIENPLATZ_STUTTGART, GeoUtil.GOOGLE_MERCATOR_SRID);

        assertEquals(GeoUtil.GOOGLE_MERCATOR_SRID, pM.getSRID());
        assertEquals("POINT (1020566.0041714492 6235006.16571537)", pM.toString());

        Point pW = GeoUtil.project(pM, GeoUtil.WGS84_SRID);
        assertEquals(GeoUtil.WGS84_SRID, pW.getSRID());
        assertEquals(MARIENPLATZ_STUTTGART.getX(), pW.getX(), 0.000001);
        assertEquals(MARIENPLATZ_STUTTGART.getY(), pW.getY(), 0.000001);

        pW = GeoUtil.asWgs84(pM); // shortcut for above
        assertEquals(GeoUtil.WGS84_SRID, pW.getSRID());
        assertEquals(MARIENPLATZ_STUTTGART.getX(), pW.getX(), 0.000001);
        assertEquals(MARIENPLATZ_STUTTGART.getY(), pW.getY(), 0.000001);

    }

    @Test
    public void testMetricDistance() throws Exception {
        Point destinationPoint = GeoUtil.destinationPoint(MARIENPLATZ_STUTTGART, 10, 33);
        assertEquals(GeoUtil.WGS84_SRID, destinationPoint.getSRID());

        double distance;

        // two wgs84 points
        distance = GeoUtil.metricDistance(MARIENPLATZ_STUTTGART, destinationPoint);
        assertEquals(10, distance, 0.001);
        // first wgs84, second mercator
        distance = GeoUtil.metricDistance(MARIENPLATZ_STUTTGART, GeoUtil.asMercator(destinationPoint));
        assertEquals(10, distance, 0.001);
        // first mercator, second wgs84
        distance = GeoUtil.metricDistance(GeoUtil.asMercator(MARIENPLATZ_STUTTGART), destinationPoint);
        assertEquals(10, distance, 0.001);
    }

    @Test
    public void testWgs84Distance() throws Exception {
        Point destinationPoint = GeoUtil.asWgs84(10, 70);

        Point other = (Point) destinationPoint.clone();
        other.getCoordinate().x = other.getCoordinate().x + GeoUtil.wgs84DistanceX(10, other.getCoordinate().y);

        assertEquals(10, GeoUtil.metricDistance(destinationPoint, other), 0.001);

        other = (Point) destinationPoint.clone();
        other.getCoordinate().y = other.getCoordinate().y + GeoUtil.wgs84Distance(10);

        assertEquals(10, GeoUtil.metricDistance(destinationPoint, other), 0.001);
    }


    @Test
    public void testExpandEnvelopeMetric() throws Exception {
        Point destinationPoint = GeoUtil.asWgs84(10, 48);
        Envelope envelope = GeoUtil.expandEnvelope(destinationPoint.getEnvelopeInternal(), 5);

        Point p1 = GeoUtil.asWgs84(envelope.getMinX(), envelope.getMinY());
        Point p2 = GeoUtil.asWgs84(envelope.getMaxX(), envelope.getMinY());
        Point p3 = GeoUtil.asWgs84(envelope.getMaxX(), envelope.getMaxY());

        assertEquals(10, GeoUtil.metricDistance(p1, p2), 0.001);
        assertEquals(10, GeoUtil.metricDistance(p2, p3), 0.001);
        assertEquals(Math.sqrt(2.0) * 10, GeoUtil.metricDistance(p1, p3), 0.001);
    }


    @Test
    public void testAngle() throws Exception {
        Point destinationPoint;
        double bearingDeg;

        bearingDeg = 33.0;
        destinationPoint = GeoUtil.destinationPoint(MARIENPLATZ_STUTTGART, 100, bearingDeg);
        assertEquals(bearingDeg, GeoUtil.bearingDeg(MARIENPLATZ_STUTTGART, destinationPoint), 0.000001);

        bearingDeg = 102.0;
        destinationPoint = GeoUtil.destinationPoint(MARIENPLATZ_STUTTGART, 100, bearingDeg);
        assertEquals(bearingDeg, GeoUtil.bearingDeg(MARIENPLATZ_STUTTGART, destinationPoint), 0.000001);

        bearingDeg = 344.34;
        destinationPoint = GeoUtil.destinationPoint(MARIENPLATZ_STUTTGART, 100, bearingDeg);
        assertEquals(bearingDeg, GeoUtil.bearingDeg(MARIENPLATZ_STUTTGART, destinationPoint), 0.000001);

        bearingDeg = 410;
        destinationPoint = GeoUtil.destinationPoint(MARIENPLATZ_STUTTGART, 100, bearingDeg);
        assertEquals(50, GeoUtil.bearingDeg(MARIENPLATZ_STUTTGART, destinationPoint), 0.000001);
    }

    @Test
    public void testBearing() throws Exception {
        Point destinationPoint;
        double bearingDeg;

        bearingDeg = 33.0;
        destinationPoint = GeoUtil.destinationPoint(MARIENPLATZ_STUTTGART, 100, bearingDeg);
        assertEquals(57, GeoUtil.angleDeg(MARIENPLATZ_STUTTGART, destinationPoint), 0.001);

        bearingDeg = 102.0;
        destinationPoint = GeoUtil.destinationPoint(MARIENPLATZ_STUTTGART, 100, bearingDeg);
        assertEquals(348, GeoUtil.angleDeg(MARIENPLATZ_STUTTGART, destinationPoint), 0.001);

        bearingDeg = 344.34;
        destinationPoint = GeoUtil.destinationPoint(MARIENPLATZ_STUTTGART, 100, bearingDeg);
        assertEquals(105.66, GeoUtil.angleDeg(MARIENPLATZ_STUTTGART, destinationPoint), 0.001);

        bearingDeg = 410;
        destinationPoint = GeoUtil.destinationPoint(MARIENPLATZ_STUTTGART, 100, bearingDeg);
        assertEquals(40, GeoUtil.angleDeg(MARIENPLATZ_STUTTGART, destinationPoint), 0.001);
    }

    @Test
    public void testConvertFromWktToGeoJson() throws Exception {

        String wkt = "POLYGON ((11.4102462461803 48.7876309782145, 11.410299873577426 48.78760522758016, 11.410357588912674 48.78766314220658, 11.41030379846691 48.78768736704519, 11.4102462461803 48.7876309782145))";
        WKTReader reader = new WKTReader();
        Geometry geom = reader.read(wkt);

        String geoJson = Mapper.get().writeValueAsString(geom);
        System.out.println(geoJson);
    }


    @Test
    public void testCompareAngle() throws Exception {
        assertTrue(GeoUtil.compareAngle(31.2, 44.2, 30));
        assertTrue(GeoUtil.compareAngle(-12, 13, 30));
        assertTrue(GeoUtil.compareAngle(356.1, 13, 30));
        assertTrue(GeoUtil.compareAngle(180, 190, 30));
        assertTrue(GeoUtil.compareAngle(-190, 181, 30));
    }

    @Test
    public void testAngleToBearing() throws Exception {
        assertEquals(0, GeoUtil.angleToBearing(Math.toDegrees(new LineSegment(0, 0, 0, 1).angle())), 0);
        assertEquals(90, GeoUtil.angleToBearing(Math.toDegrees(new LineSegment(0, 0, 1, 0).angle())), 0);
        assertEquals(180, GeoUtil.angleToBearing(Math.toDegrees(new LineSegment(0, 0, 0, -1).angle())), 0);
        assertEquals(270, GeoUtil.angleToBearing(Math.toDegrees(new LineSegment(0, 0, -1, 0).angle())), 0);

    }
}