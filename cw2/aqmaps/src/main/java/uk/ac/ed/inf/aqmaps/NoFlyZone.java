package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Polygon;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;


public class NoFlyZone {
    private final Polygon polygon;
    private final List<Point2D> coordinates;
    private final List<Line2D> boundaries = new ArrayList<>();
    private final String name;

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

    
    /** 
     * @param move
     * @return boolean
     */
    public boolean isLegalMove(Line2D move) {
        for (var boundary : boundaries) {
            if (boundary.intersectsLine(move)) {
                return false;
            }
        }
        return true;
    }

    
    /** 
     * @return List<Point2D>
     */
    public List<Point2D> getCoordinates() {
        return coordinates;
    }
    
    /** 
     * @return Polygon
     */
    public Polygon getPolygon() {
        return polygon;
    }

    
    /** 
     * @return String
     */
    @Override
    public String toString() {
        return "NoFlyZone: " + name + '.';
    }
}
