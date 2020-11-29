package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.stream.IntStream;

public class NoFlyZone {
    private List<Point> coordinates;
    private List<Line2D> boundaries = new ArrayList<>();
    private String name;

    public NoFlyZone(Polygon polygon, String name) {
        Objects.requireNonNull(polygon);
        this.name = Objects.requireNonNull(name);
        this.coordinates = polygon.coordinates().get(0);
        for (int i=0; i < this.coordinates.size() - 1; i++) {
            var p1 = Utils.pointToPoint2d(coordinates.get(i));
            var p2 = Utils.pointToPoint2d(coordinates.get(i + 1));
            boundaries.add(new Line2D.Double(p1, p2));
        }
    }

    public boolean isLegalMove(Line2D move) {
        for (var boundary : boundaries) {
            if (boundary.intersectsLine(move)) {
                return false;
            };
        }
        return true;
    }

    public int getBestFlyAroundAngle(Point2D start, Point2D target) {
        var straightAngle = Utils.radiansBetween(start, target);
//        var clockwiseCorner = coordinates.stream()
//                .map(s -> Utils.radiansBetween(start, s))
//                .min(Double::compare).orElse(straightAngle);
//        var antiClockwiseCorner = coordinates.stream()
//                .map(s -> Utils.radiansBetween(start, s))
//                .max(Double::compare).orElse(straightAngle);

        var angleToCorners = coordinates.stream().map(s -> Utils.radiansBetween(start, s));

        Comparator<Double> normalise = Comparator.comparingDouble(s -> s - straightAngle);
        var clockwiseCorner = coordinates.stream().map(s -> Utils.radiansBetween(start, s)).min(normalise).orElse(straightAngle);
        var antiClockwiseCorner = coordinates.stream().map(s -> Utils.radiansBetween(start, s)).max(normalise).orElse(straightAngle);


        var straightAngleDegrees = Math.toDegrees(straightAngle);
        var clockwiseDegrees = (int) Math.floor(Math.toDegrees(clockwiseCorner)/ 10) * 10;
        var antiClockwiseDegrees = (int) Math.ceil(Math.toDegrees(antiClockwiseCorner) / 10) * 10;
        var res = Arrays.asList(clockwiseDegrees, antiClockwiseDegrees).stream()
                .min(Comparator.comparingDouble(a -> Math.abs(straightAngleDegrees - a)))
                .orElseThrow(() -> new NoSuchElementException("No value present"));
        System.out.print("Given angle: ");
        System.out.print(Math.toDegrees(straightAngle));
        System.out.print(", choose ");
        System.out.print(clockwiseDegrees);
        System.out.print(" or ");
        System.out.print(antiClockwiseDegrees);
        System.out.print(": ");
        System.out.println(res);
        return res;
    }

    public List<Point> getCoordinates() {
        return coordinates;
    }

    @Override
    public String toString() {
        return "NoFlyZone: " + name + '.';
    }
}
