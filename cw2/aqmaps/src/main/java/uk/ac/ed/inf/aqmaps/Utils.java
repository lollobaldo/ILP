package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Point;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {
    
    /** 
     * @param p
     * @return Point2D
     */
    public static Point2D pointToPoint2d(Point p) {
        return new Point2D.Double(p.longitude(), p.latitude());
    }

    
    /** 
     * @param p
     * @return Point
     */
    public static Point point2dToPoint(Point2D p) {
        return Point.fromLngLat(p.getX(), p.getY());
    }
    
    /** 
     * @param points
     * @return List<Point2D>
     */
    public static List<Point2D> pointsToPoints2d(Collection<Point> points) {
        return points.stream()
                .map(Utils::pointToPoint2d)
                .collect(Collectors.toList());
    }

    /** 
     * @param start
     * @param end
     * @return double
     */
    public static double radiansBetween(Point2D start, Point2D end) {
        var tan = Math.atan2(end.getY() - start.getY(), end.getX() - start.getX());
        return tan >= 0 ? tan : tan + 2*Math.PI;
    }

    
    /** 
     * @param start
     * @param end
     * @return double
     */
    public static double degreesBetween(Point2D start, Point2D end){
        return Math.toDegrees(radiansBetween(start, end));
    }

    
    /** 
     * @param start
     * @param angle
     * @param length
     * @return Line2D
     */
    public static Line2D getLine(Point2D start, double angle, double length) {
        var endX = start.getX() + length * Math.cos(Math.toRadians(angle));
        var endY = start.getY() + length * Math.sin(Math.toRadians(angle));
        return new Line2D.Double(start, new Point2D.Double(endX, endY));
    }

    
    /** 
     * @param degrees
     * @return double
     */
    public static double normaliseAngle(double degrees) {
        var mod = degrees % 360;
        return mod >= 0 ? mod : mod + 360;
    }

    
    /** 
     * @param degrees
     * @return int
     */
    public static int round10(double degrees) {
        return (int) Math.round(degrees/10.0) * 10;
    }
}
