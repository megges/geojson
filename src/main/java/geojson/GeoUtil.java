package geojson;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.geom.util.AffineTransformation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "WeakerAccess"})
public class GeoUtil {

    public static int WGS84_SRID = 4326;
    public static int GOOGLE_MERCATOR_SRID = 3857;

    private static GeometryFactory localGeometryFactory;
    private static GeometryFactory wgs84GeometryFactory;
    private static GeometryFactory googleMapsMercatorProjectionFactory;


    public static final double EarthRadius = 6378137;
    public static final double EarthRadiusPI = EarthRadius * Math.PI;
    public static final double EarthRadiusPI2 = EarthRadiusPI * 2;
    public static final double MinLatitude = -85.05112878;
    public static final double MaxLatitude = 85.05112878;
    public static final double MinLongitude = -180;
    public static final double MaxLongitude = 180;


    public static GeometryFactory getLocalGeometryFactory() {
        if (localGeometryFactory == null)
            localGeometryFactory = new GeometryFactory(new PrecisionModel(), 0);
        return localGeometryFactory;
    }

    public static GeometryFactory getWgs84GeometryFactory() {
        if (wgs84GeometryFactory == null)
            wgs84GeometryFactory = new GeometryFactory(new PrecisionModel(), WGS84_SRID);
        return wgs84GeometryFactory;
    }

    public static GeometryFactory getMercatorFactory() {
        if (googleMapsMercatorProjectionFactory == null)
            googleMapsMercatorProjectionFactory = new GeometryFactory(new PrecisionModel(), GOOGLE_MERCATOR_SRID);
        return googleMapsMercatorProjectionFactory;
    }

    /**
     * @param lon longitude
     * @param lat latitude
     * @return point referenced in LonLat coordinates
     */
    public static Point asWgs84(double lon, double lat) {
        return getWgs84GeometryFactory().createPoint(new Coordinate(lon, lat));
    }

    /**
     * @param c coordinate in LonLat coordinates
     * @return point referenced in LonLat coordinates
     */
    public static Point asWgs84(Coordinate c) {
        return getWgs84GeometryFactory().createPoint(c);
    }

    /**
     * Converts geometry to WGS84 point if not already in WGS84 coordinates
     *
     * @param geom  the geometry
     * @return geom referenced in LonLat coordinates
     */
    public static <T extends Geometry> T asWgs84(T geom) {
        if (geom.getSRID() != WGS84_SRID)
            return project(geom, WGS84_SRID);
        return geom;
    }

    /**
     * @param x in meters
     * @param y in meters
     * @return point referenced in metric coordinates
     */
    public static Point asMercator(double x, double y) {
        return getMercatorFactory().createPoint(new Coordinate(x, y));
    }

    /**
     * @param c metric coordinates
     * @return point referenced in metric coordinates
     */
    public static Point asMercator(Coordinate c) {
        return getMercatorFactory().createPoint(c);
    }

    /**
     * Converts geometry to Google Mercator point if not already in Mercator projected coordinates
     *
     * @param geom  the geometry
     * @return geometry referenced in metric coordinates
     */
    public static <T extends Geometry> T asMercator(T geom) {
        if (geom.getSRID() != GOOGLE_MERCATOR_SRID)
            return project(geom, GOOGLE_MERCATOR_SRID);
        return geom;
    }

    public static Point moveAngle(final Point origin, final double distanceMeters, final double angleDeg) {
        Point destination = (Point) translateFromPolar(distanceMeters, angleDeg).transform(asMercator(origin));
        return project(destination, origin.getSRID());
    }

    public static double angleDeg(final Point p1, final Point p2) {
        Point p1M = asMercator(p1);
        Point p2M = asMercator(p2);
        final double bearing = Math.toDegrees(Math.atan2(p2M.getY() - p1M.getY(), p2M.getX() - p1M.getX()));
        return (bearing + 360) % 360; // normalize
    }

    public static double angleDifference(double degA, double degB) {
        double diff = degA - degB;
        return (diff + 180) % 360 - 180;
    }

    public static Point destinationPoint(final Point origin, final double distanceMeters, final double bearingDeg) {
        final double dist = distanceMeters / EarthRadius;
        final Point pW = asWgs84(origin);

        final double lat1 = Math.toRadians(pW.getY());
        final double lon1 = Math.toRadians(pW.getX());

        final double bearingRad = Math.toRadians(bearingDeg);

        final double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dist) + Math.cos(lat1)
                * Math.sin(dist) * Math.cos(bearingRad));
        final double lon2 = lon1
                + Math.atan2(Math.sin(bearingRad) * Math.sin(dist) * Math.cos(lat1), Math.cos(dist)
                - Math.sin(lat1) * Math.sin(lat2));

        final double lat2deg = Math.toDegrees(lat2);
        final double lon2deg = Math.toDegrees(lon2);

        return GeoUtil.asWgs84(lon2deg, lat2deg);
    }

    public static double bearingDeg(@NotNull final Point p1, @NotNull final Point p2) {
        if (p1.equals(p2))
            return 0;
        Point p1W = asWgs84(p1);
        Point p2W = asWgs84(p2);

        final double lat1 = Math.toRadians(p1W.getY());
        final double long1 = Math.toRadians(p1W.getX());
        final double lat2 = Math.toRadians(p2W.getY());
        final double long2 = Math.toRadians(p2W.getX());
        final double delta_long = long2 - long1;
        final double a = Math.sin(delta_long) * Math.cos(lat2);
        final double b = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2)
                * Math.cos(delta_long);
        final double bearing = Math.toDegrees(Math.atan2(a, b));
        return (bearing + 360) % 360; // normalize
    }

    public static AffineTransformation translateFromPolar(final double distanceMeters, final double bearingDeg) {
        // to cartesian coordinates
        final double bearing = Math.toRadians(bearingDeg);
        final double dx = distanceMeters * Math.cos(bearing);
        final double dy = distanceMeters * Math.sin(bearing);
        return new AffineTransformation().setToTranslation(dx, dy);
    }

    public static double metricDistance(final Point p1, final Point p2) {
        Point p1W = asWgs84(p1);
        Point p2W = asWgs84(p2);

        final double a1 = Math.toRadians(p1W.getY());
        final double a2 = Math.toRadians(p1W.getX());
        final double b1 = Math.toRadians(p2W.getY());
        final double b2 = Math.toRadians(p2W.getX());

        final double cosa1 = Math.cos(a1);
        final double cosb1 = Math.cos(b1);

        final double t1 = cosa1 * Math.cos(a2) * cosb1 * Math.cos(b2);
        final double t2 = cosa1 * Math.sin(a2) * cosb1 * Math.sin(b2);
        final double t3 = Math.sin(a1) * Math.sin(b1);

        final double tt = Math.acos(t1 + t2 + t3);

        double distance = EarthRadius * tt;
        if(Double.isNaN(distance)) return 0;
        return distance;
    }

//    public static double metricDistance(final Geometry geom1, final Geometry geom2) {
//        Geometry g1 = geom1;
//        Geometry g2 = geom2;
//
//        if (g1.getSRID() != GOOGLE_MERCATOR_SRID)
//            g1 = project(g1, GOOGLE_MERCATOR_SRID);
//
//        if (g2.getSRID() != GOOGLE_MERCATOR_SRID)
//            g2 = project(g2, GOOGLE_MERCATOR_SRID);
//
//        return g1.distance(g2);
//    }


    public static <T extends Geometry> T project(T geom, int SRID) {
        if (geom == null)
            return null;

        if (geom.getSRID() == SRID)
            return geom;

        // project from WGS84 to Google Mercator
        if (geom.getSRID() == WGS84_SRID && SRID == GOOGLE_MERCATOR_SRID) {
            //noinspection unchecked
            T copy = (T) getMercatorFactory().createGeometry(geom);
            for (Coordinate coordinate : copy.getCoordinates()) {
                mercator(coordinate);
            }
            return copy;
        }

        if (geom.getSRID() == GOOGLE_MERCATOR_SRID && SRID == WGS84_SRID) {
            //noinspection unchecked
            T copy = (T) getWgs84GeometryFactory().createGeometry(geom);
            for (Coordinate coordinate : copy.getCoordinates()) {
                inverseMercator(coordinate);
            }
            return copy;
        }

        throw new IllegalArgumentException("Projection from " + geom.getSRID() + " to " + SRID + " not supported.");
    }

    /**
     * Projects WGS84 coordinate to metric coordinates
     *
     * @param c Wgs84 coordinate (LonLat)
     */
    public static void mercator(Coordinate c) {
        c.x = c.x * EarthRadiusPI / 180;
        double y = Math.log(Math.tan((90 + c.y) * Math.PI / 360)) / (Math.PI / 180);
        c.y = y * EarthRadiusPI / 180;
    }

    /**
     * metric Mercator coordinates to WGS84 coordinates
     *
     * @param c Google Mercator projection coordinates
     */
    public static void inverseMercator(Coordinate c) {
        c.x = (c.x / EarthRadiusPI) * 180;
        double lat = (c.y / EarthRadiusPI) * 180;
        c.y = 180 / Math.PI * (2 * Math.atan(Math.exp(lat * Math.PI / 180)) - Math.PI / 2);
    }


    /**
     * Calculate Latitude
     */
    public static double wgs84Distance(double metricDistance) {
        return (metricDistance / EarthRadiusPI) * 180;
    }

    /**
     * Calculate Longitude
     */
    public static double wgs84DistanceX(double metricDistance, double lat) {
        return (metricDistance / EarthRadiusPI) * 180 / Math.cos(Math.toRadians(lat));
    }


    public static Envelope expandEnvelope(Envelope envelope, double expandMeters) {
        Envelope expanded = new Envelope(envelope);
        expanded.expandBy(GeoUtil.wgs84DistanceX(expandMeters, envelope.centre().y),
                GeoUtil.wgs84Distance(expandMeters));
        return expanded;
    }

    public static void growEnvelope(Envelope envelope, double factor) {
        double lonRadius = 0.5 * (envelope.getMaxX() - envelope.getMinX());
        double latRadius = 0.5 * (envelope.getMaxY() - envelope.getMinY());

        double centerLon = envelope.getMinX() + lonRadius;
        double centerLat = envelope.getMinY() + latRadius;

        envelope.init(centerLon - factor * lonRadius, centerLon + factor * lonRadius,
                centerLat - factor * latRadius, centerLat + factor * latRadius);
    }

    public static LineString toLineString(Geometry g1, Geometry g2) {
        if (g1 == null || g2 == null)
            throw new IllegalArgumentException("Arguments may not be null");

        if (g1.getSRID() != g2.getSRID())
            throw new IllegalArgumentException("Geometries with different CRS");

        CoordinateSequence coordinateSequence = new CoordinateArraySequence(new Coordinate[]{
                g1.getCentroid().getCoordinate(),
                g2.getCentroid().getCoordinate()
        });

        return new LineString(coordinateSequence, g1.getFactory());
    }

    public static double angleDiff(double angleDegrees1, double angleDegrees2) {
        return 180.0 - Math.abs(Math.abs(angleDegrees1 - angleDegrees2) - 180.0);
    }

    public static boolean compareAngle(double angleDegrees1, double angleDegrees2, double tolerance) {
        return angleDiff(angleDegrees1, angleDegrees2) <= tolerance;
    }

    public static double angleToBearing(double angleDegrees) {
        return (450-angleDegrees)%360;
    }


    // TODO Refactor to PolygonBuilder?
//    public static LineString asGeometry(GeometryFactory factory, LineSegment seg) {
//        Coordinate[] coord = {new Coordinate(seg.p0), new Coordinate(seg.p1)};
//        return factory.createLineString(coord);
//    }

    public static double spatialDistance(Point p1, Point p2) {
        return Math.sqrt(Math.pow((p2.getX() - p1.getX()), 2) + Math.pow((p2.getY() - p1.getY()), 2));
    }

    public static double spatialDistance(double[] p1, double[] p2) {
        return Math.sqrt(Math.pow((p2[0] - p1[0]), 2) + Math.pow((p2[1] - p1[1]), 2));
    }

    public static class Circle {

        public double p;
        public double q;
        public double r;

        public Circle() {

        }

        public Circle(Point center, double radius) {
            p = center.getX();
            q = center.getY();
            r = radius;
        }

        public Circle(double[] center, double radius) {
            p = center[0];
            q = center[1];
            r = radius;
        }

        private double[] binom(double A, double B, double C) {
            double check = B * B - 4 * A * C;
            double x1;
            double x2;
            if (check < 0) {
                return null;
            } else {
                double num = Math.sqrt(check);
                x1 = (-B + num) / (2 * A);
                x2 = (-B - num) / (2 * A);
            }
            if (x1 == x2) {
                return new double[]{x1, x1};
            }
            return new double[]{x1, x2};
        }

        public double[] getX(double y) {
            double A = 1;
            double B = -2 * p;
            double C = p * p + y * y - 2 * q * y + q * q - r * r;

            return binom(A, B, C);
        }

        public double[] getY(double x) {
            float A = 1;
            double B = -2 * q;
            double C = x * x - 2 * p * x + p * p + q * q - r * r;

            return binom(A, B, C);
        }

        public List<Point> intersectionPoints(LineString ls) {
            Line l = new Line(ls);
            List<Point> intersections = intersectionPoints(l);
            List<Point> results = new ArrayList<>();
            for (Point p : intersections) {
                if (l.contains(p)) results.add(p);
            }
            return results;
        }


        public List<Point> intersectionPoints(Line l) {
            List<double[]> inters = intersections(l);
            List<Point> points = new ArrayList<>();
            for (double[] c : inters) {
                //points.add(new Point(c[0], c[1]));
                points.add(getWgs84GeometryFactory().createPoint(new Coordinate(c[0], c[1])));
            }
            return points;
        }

        public List<double[]> intersections(Line l) {

            double x1, y1, x2, y2;

            if (l.isHorizontal) {
                double[] x = getX(l.mY);
                if (x == null) return new ArrayList<>();
                x1 = x[0];
                x2 = x[1];
                y1 = l.mY;
                y2 = l.mY;

            } else if (l.isVertical) {
                double[] y = getY(l.mX);
                if (y == null) return new ArrayList<>();
                y1 = y[0];
                y2 = y[1];
                x1 = l.mX;
                x2 = l.mX;

            } else {
                double A = l.m * l.m + 1;
                //float B = 2 * l.m * (l.c - q) - 2 * p;
                double B = 2 * (l.m * l.b - l.m * q - p);
                //float C = p*p + (l.c - q) * (l.c - q) - r*r;
                double C = q * q - r * r + p * p - 2 * l.b * q + l.b * l.b;

                double[] x = binom(A, B, C);
                if (x == null) return new ArrayList<>();
                x1 = x[0];
                x2 = x[1];
                y1 = l.getY(x1);
                y2 = l.getY(x2);
            }

            List<double[]> inters = new ArrayList<>();
            inters.add(new double[]{x1, y1});
            inters.add(new double[]{x2, y2});
            return inters;
        }
    }

    public static class Line {

        public double A;
        public double B;
        public double C;
        public double m;
        public double b;
        public double c;

        public double mX = Double.NaN;
        public double mY = Double.NaN;

        public double[] lsP1;
        public double[] lsP2;

        private boolean isVertical = false;
        private boolean isHorizontal = false;

        private GeometryFactory geometryFactory = getWgs84GeometryFactory();

        public Line() {

        }

        public Line(Point p1, Point p2) {
            initLine(new double[]{p1.getX(), p1.getY()}, new double[]{p2.getX(), p2.getY()});
        }

        public Line(double[] p1, double[] p2) {
            initLine(p1, p2);
        }

        public Line(LineString ls) {
            Point p1 = ls.getStartPoint();
            Point p2 = ls.getEndPoint();
            initLine(new double[]{p1.getX(), p1.getY()}, new double[]{p2.getX(), p2.getY()});
        }

        public Point getStartPoint() {
            return geometryFactory.createPoint(new Coordinate(lsP1[0], lsP1[1]));
        }

        public Point getEndPoint() {
            return geometryFactory.createPoint(new Coordinate(lsP2[0], lsP2[1]));
        }

        private void initLine(double[] p1, double[] p2) {
            lsP1 = p1;
            lsP2 = p2;
            double x1 = p1[0];
            double x2 = p2[0];
            double y1 = p1[1];
            double y2 = p2[1];
            if ((x2 - x1) == 0) {
                m = 0;
                mX = x1;
                isVertical = true;
            } else if ((y2 - y1) == 0) {
                m = 0;
                mY = y1;
                isHorizontal = true;
            } else {
                m = (y2 - y1) / (x2 - x1);
                b = y1 - (m * x1);
                A = y2 - y1;
                B = x1 - x2;
                C = A * x1 + B * y1;
            }
        }

        public double getX(double y) {
            if (m != 0) {
                return (y - b) / m;
            } else {
                return mX;
            }
        }

        public double getY(double x) {
            if (m != 0) {
                return (m * x) + b;
            } else {
                return mY;
            }
        }

        public double length() {
            return spatialDistance(lsP1, lsP2);
        }

        public boolean contains(double[] p, double diffRatio) {
            double delta = spatialDistance(lsP1, lsP2);
            double deltaA = spatialDistance(lsP1, p);
            double deltaB = spatialDistance(p, lsP2);
            double diff = Math.abs(delta - (deltaA + deltaB));
            if (diff > delta) return false;
            double ratio = Math.abs(1 - (diff / delta));
            return ratio >= diffRatio;
        }

        public boolean contains(Point p) {
            return contains(new double[]{p.getX(), p.getY()}, 0.9999);
        }

        public boolean contains(Point p, double diffRatio) {
            return contains(new double[]{p.getX(), p.getY()}, diffRatio);
        }

		/*
        public boolean isOnSegment(float[] p) {
		    float epsilon = 0.0001f;
		    float[] a = lsP1;
		    float[] b = lsP2;
		    float[] c = p;
		    float crossproduct = (c[1] - a[1]) * (b[0] - a[0]) - (c[0] - a[0]) * (b[1] - a[1]);
		    if(Math.abs(crossproduct) > epsilon) {
		    	return false;
		    }
		    float dotproduct = (c[0] - a[0]) * (b[0] - a[0]) + (c[1] - a[1])*(b[1] - a[1]);
		    if(dotproduct < 0) {
		    	return false;
		    }
		    float squaredlengthba = (b[0] - a[0])*(b[0] - a[0]) + (b[1] - a[1])*(b[1] - a[1]);
		    if(dotproduct > squaredlengthba) {
		    	return false;
		    }
		    return true;
		}*/

        public double angle(Line l) {
            if (l.isVertical() && this.isVertical()) return 0;
            if (l.isVertical()) return Math.atan(1 / m);
            if (this.isVertical()) return Math.atan(1 / l.m);

            double tan = (m - l.m) / (1 + m * l.m);
            return Math.atan(tan);
        }

        public Line perpendicularLine(Point p) {
            return perpendicularLine(new double[]{p.getX(), p.getY()});
        }

        public Line perpendicularLine(double[] p) {
            double x = p[0];
            double y = p[1];
            Line l = new Line();
            if (m != 0) {
                l.m = -(1 / m);
                l.b = y - (l.m * x);
                l.c = -l.m * x + y;
                l.A = -l.m;
                l.B = 1;
                l.C = l.b;
            } else {
                l.m = 0;
                if (isVertical()) {
                    l.mY = y;
                    l.isHorizontal = true;
                } else if (isHorizontal()) {
                    l.mX = x;
                    l.isVertical = true;
                }
            }

            return l;
        }

        public double distance(Point p) {
            Line pp = perpendicularLine(p);
            Point xing = pp.crossingPoint(this);
            if (this.contains(xing)) {
                return spatialDistance(p, xing);
            }
            double d1 = spatialDistance(lsP1, new double[]{p.getX(), p.getY()});
            double d2 = spatialDistance(lsP2, new double[]{p.getX(), p.getY()});
            if (d1 < d2) {
                return d1;
            }
            return d2;
        }

        public Point getClosestPointOnLine(Point p) {
            Line pp = perpendicularLine(p);
            Point xing = pp.crossingPoint(this);
            if (this.contains(xing)) {
                return xing;
            }
            double d1 = spatialDistance(lsP1, new double[]{p.getX(), p.getY()});
            double d2 = spatialDistance(lsP2, new double[]{p.getX(), p.getY()});
            if (d1 < d2) {
                return geometryFactory.createPoint(new Coordinate(lsP1[0], lsP1[1]));
            }
            return geometryFactory.createPoint(new Coordinate(lsP2[0], lsP2[1]));
        }

        public Line parallelLine(Point p) {
            return parallelLine(new double[]{p.getX(), p.getY()});
        }

        public Line parallelLine(double[] p) {
            double x = p[0];
            double y = p[1];
            Line l = new Line();
            if (m != 0) {
                l.m = m;
                l.b = y - (m * x);
                l.A = -m;
                l.B = 1;
                l.C = l.b;
            } else {
                l.m = 0;
                if (isVertical()) {
                    l.mX = x;
                    l.isVertical = true;
                } else if (isHorizontal()) {
                    l.mY = y;
                    l.isHorizontal = true;
                }
            }
            return l;
        }

        public boolean isVertical() {
            return isVertical;
        }

        public boolean isHorizontal() {
            return isHorizontal;
        }


        public Point crossingPoint(Line l) {
            double[] xy = crossing(l);
            if (xy != null) {
                return geometryFactory.createPoint(new Coordinate(xy[0], xy[1]));
            }
            return null;
        }

        public double[] crossing(Line l) {
            if (m != 0 && l.m != 0) {
                double det = A * l.B - l.A * B;
                if (Math.abs(det) == 0) {
                    return null;
                } else {
                    double x = (l.B * C - B * l.C) / det;
                    double y = (A * l.C - l.A * C) / det;
                    return new double[]{x, y};
                }
            } else if (m == 0 && l.m != 0) {
                double x;
                double y;
                if (isVertical()) {
                    x = mX;
                    y = l.getY(x);
                } else {
                    y = mY;
                    x = l.getX(y);
                }
                return new double[]{x, y};
            } else if (m != 0 && l.m == 0) {
                double x;
                double y;
                if (l.isVertical()) {
                    x = l.mX;
                    y = getY(x);
                } else {
                    y = l.mY;
                    x = getX(y);
                }
                return new double[]{x, y};
            } else if (isHorizontal() && l.isVertical()) {
                return new double[]{l.mX, mY};
            } else if (isVertical() && l.isHorizontal()) {
                return new double[]{mX, l.mY};
            }
            return null;
        }
    }


}
