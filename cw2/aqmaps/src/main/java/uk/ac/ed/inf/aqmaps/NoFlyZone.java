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
