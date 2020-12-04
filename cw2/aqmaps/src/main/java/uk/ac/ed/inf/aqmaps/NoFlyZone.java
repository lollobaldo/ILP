package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Polygon;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;

/**
 * This class represents a single NoFlyZone.
 * It is represented as a list of {@link Line2D} boundaries.
 * A List of {@link Point2D} is also provided for quick access when needed by
 * the NoFlyZonesManager class.
 */
public class NoFlyZone {
    /** Instance attributes - see class description */
    private final List<Point2D> coordinates;
    private final List<Line2D> boundaries = new ArrayList<>();

    /**
     * Class constructor.
     *
     * @param polygon The GeoJson Polygon to build the NoFlyZone from
     */
    public NoFlyZone(Polygon polygon) {
        // Validate input, then extract corners, and generate Line2D boundaries
        Objects.requireNonNull(polygon);
        this.coordinates = Utils.pointsToPoints2d(polygon.coordinates().get(0));
        for (int i=0; i < this.coordinates.size() - 1; i++) {
            var p1 = coordinates.get(i);
            var p2 = coordinates.get(i + 1);
            boundaries.add(new Line2D.Double(p1, p2));
        }
    }


    /**
     * Checks if a Line2D move is legal (i.e. does not intersect any zone boundary)
     *
     * @param move The move to be checked
     * @return boolean Whether the move intersect the NoFlyZone
     */
    public boolean isLegalMove(Line2D move) {
        // For each boundary, check if the move intersects it.
        // If no match, then the move is legal
        for (var boundary : boundaries) {
            if (boundary.intersectsLine(move)) {
                return false;
            }
        }
        return true;
    }


    /**
     * Get coordinates
     *
     * @return List<Point2D> The list of vertices of the Zone
     */
    public List<Point2D> getCoordinates() {
        return coordinates;
    }
}
