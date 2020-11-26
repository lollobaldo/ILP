package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Point;

import java.awt.geom.Point2D;

public class Utils {
    public static Point2D pointToPoint2d(Point p) {
        return new Point2D.Double(p.longitude(), p.latitude());
    }

    public static Point point2dToPoint(Point2D p) {
        return Point.fromLngLat(p.getX(), p.getY());
    }
}
