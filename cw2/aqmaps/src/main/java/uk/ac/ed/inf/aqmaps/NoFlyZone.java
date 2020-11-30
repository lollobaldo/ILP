package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class NoFlyZone {
    private Polygon polygon;
    private List<Point2D> coordinates;
    private List<Line2D> boundaries = new ArrayList<>();
    private String name;

    public NoFlyZone(Polygon polygon, String name) {
        this.polygon = Objects.requireNonNull(polygon);
        this.name = Objects.requireNonNull(name);
        this.coordinates = Utils.pointsToPoints2d(polygon.coordinates().get(0));
        for (int i=0; i < this.coordinates.size() - 1; i++) {
            var p1 = coordinates.get(i);
            var p2 = coordinates.get(i + 1);
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
        var directAngle = Utils.radiansBetween(start, target);
        var directAngleDegrees = Math.toDegrees(directAngle);
        var distanceToFurtherCorner = coordinates.stream().map(start::distance).max(Double::compare).orElse(0.0);
        Comparator<Integer> deltaFromDirectAngle = Comparator.comparingDouble(s -> Math.abs(Utils.normaliseAngle(s - directAngleDegrees)));
        Predicate<Integer> isGoodAngle = (angle) -> isLegalMove(Utils.getLine(start, angle, distanceToFurtherCorner));
        var result = IntStream.range(0, 36).map(a -> a*10).boxed()
                .filter(isGoodAngle)
                .min(deltaFromDirectAngle)
                .orElse(Utils.round10(directAngleDegrees));
        return result;
    }

    public List<Point2D> getCoordinates() {
        return coordinates;
    }
    public Polygon getPolygon() {
        return polygon;
    }

    @Override
    public String toString() {
        return "NoFlyZone: " + name + '.';
    }
}
