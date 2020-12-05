package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Point;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


/**
 * This class contains a collection of utility functions.
 * They mostly manage conversions between data types used in the application
 */
public class Utils {

    /**
     * Convert a {@link Point} to a {@link Point2D}
     *
     * @param point The Point to convert
     * @return Point2D The equivalent Point2D
     */
    public static Point2D pointToPoint2d(Point point) {
        return new Point2D.Double(point.longitude(), point.latitude());
    }


    /**
     * Convert a {@link Point2D} to a {@link Point}
     *
     * @param point2d The Point2D to convert
     * @return Point The equivalent Point
     */
    public static Point point2dToPoint(Point2D point2d) {
        return Point.fromLngLat(point2d.getX(), point2d.getY());
    }


    /**
     * Convert a Collection of {@link Point2D} to a List of {@link Point}
     *
     * @param points The Collection of Point to convert
     * @return <code>List&lt;Point2D&gt;</code> The equivalent List of Point2D
     * @see #pointToPoint2d
     */
    public static List<Point2D> pointsToPoints2d(Collection<Point> points) {
        // Convert the Collection to a stream, then convert each item individually
        // and return a list containing them.
        return points.stream()
                .map(Utils::pointToPoint2d)
                .collect(Collectors.toList());
    }


    /**
     * Method to get the angle in <b>radians</b> between two Point2D points
     *
     * @param point1 The first point
     * @param point2 The second point
     * @return double The angle in radians between the points
     */
    public static double radiansBetween(Point2D point1, Point2D point2) {
        // Apply trigonometry to get the angle
        var tan = Math.atan2(point2.getY() - point1.getY(), point2.getX() - point1.getX());
        return tan >= 0 ? tan : tan + 2*Math.PI;
    }


    /**
     * Method to get the angle in <b>degrees</b> between two Point2D points
     *
     * @param point1 The first point
     * @param point2 The second point
     * @return double The angle in radians between the points
     */
    public static double degreesBetween(Point2D point1, Point2D point2){
        // Calculate angle and convert to degrees
        return Math.toDegrees(radiansBetween(point1, point2));
    }


    /**
     * Method to calculate a Line2D starting from a point, with a given angle and length
     * The angle is represented in degrees.
     *
     * @param start The starting point of the line
     * @param degrees The angle in <b>degrees</b> of the line with the X-axis
     * @param length The length of the line
     * @return Line2D The Line2D object satisfying the requirements
     */
    public static Line2D getLine(Point2D start, double degrees, double length) {
        // Apply trigonometry to generate the end point, and create a line with it
        var endX = start.getX() + length * Math.cos(Math.toRadians(degrees));
        var endY = start.getY() + length * Math.sin(Math.toRadians(degrees));
        return new Line2D.Double(start, new Point2D.Double(endX, endY));
    }


    /**
     * Method to normalise an angle in degrees.
     * The angle returned is equivalent to the starting one,
     * and guaranteed to be in the range [0, 360)
     *
     * @param degrees The angle in degrees
     * @return double The normalised representation of the angle
     */
    public static double normaliseAngle(double degrees) {
        var mod = degrees % 360;
        return mod >= 0 ? mod : mod + 360;
    }


    /**
     * Method to find the difference of two angles.
     * The result is guaranteed to be in the range [0, 180)
     *
     * @param angle1 The first angle
     * @param angle2 The second angle
     * @return double The normalised difference
     */
    public static double angleDifference(double angle1, double angle2) {
        var diff = Math.abs(angle1 - angle2);
        return diff > 180 ? 360 - diff : diff;
    }


    /**
     * Method to round a number to a multiple of 10
     *
     * @param degrees The number to round
     * @return int The rounded number
     */
    public static int round10(double degrees) {
        return (int) Math.round(degrees/10.0) * 10;
    }
}
