package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

import java.util.List;

public class NoFlyZone {
    private static List<Point> coordinates;

    public NoFlyZone(Polygon polygon) {
        this.coordinates = polygon.coordinates().get(0);
    }
}
