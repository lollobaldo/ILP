package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

import java.awt.geom.Line2D;
import java.util.*;


/**
 * This class manages the {@link NoFlyZone}s.
 * Practically, it represents the map, with allowable fly zones.
 * It includes the confinement area.
 */
public class NoFlyZonesManager {

    /** Constant confinement area */
    private static final Point TOP_LEFT = Point.fromLngLat(-3.192473, 55.946233);
    private static final Point TOP_RIGHT = Point.fromLngLat(-3.184319, 55.946233);
    private static final Point BOTTOM_LEFT = Point.fromLngLat(-3.192473, 55.942617);
    private static final Point BOTTOM_RIGHT = Point.fromLngLat(-3.184319, 55.942617);
    /** Last point must be repeated, see {@link Polygon} */
    private static final NoFlyZone confinementArea = new NoFlyZone(Polygon.fromLngLats(Collections.singletonList(Arrays.asList(
            TOP_LEFT, BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT
    ))));

    /** The set of NoFlyZones for this specific instance */
    private final Set<NoFlyZone> zones;

    /**
     * Class constructor.
     * It generates a set of NoFlyZones from a GeoJson Polygon array
     *
     * @param geoJson The GeoJson array to extract NoFlyZones
     */
    public NoFlyZonesManager(String geoJson) {
        // Validate inputs and add the confinement area
        Objects.requireNonNull(geoJson);
        zones = new HashSet<>();
        zones.add(confinementArea);

        // Extract polygons, construct a NoFlyZone for each and add to the set
        var zonesFeatures = FeatureCollection.fromJson(geoJson).features();
        assert zonesFeatures != null;
        for (var zone : zonesFeatures) {
            if (!(zone.geometry() instanceof com.mapbox.geojson.Polygon)) {
                throw new IllegalArgumentException("List does not contain polygons");
            }
            zones.add(new NoFlyZone((Polygon) zone.geometry()));
        }
    }


    /**
     * Verifies a move is legal in the map.
     * This checks that it does not intersect with any NoFlyZone,
     * including the confinement area.
     *
     * @param move The move to be checked
     * @return boolean Whether the move intersect the NoFlyZone
     * @see NoFlyZone#isLegalMove
     */
    public boolean isLegalMove(Line2D move) {
        // For every zone in the map, call their isLegalMove.
        for (var zone : zones) {
            if (!zone.isLegalMove(move)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the confinement area of the map
     *
     * @return NoFlyZone The confinement area
     */
    public static NoFlyZone getConfinementArea() {
        return confinementArea;
    }


    /**
     * Get the NoFlyZones in the map
     *
     * @return <code>Set&lt;NoFlyZone&gt;</code> The collection of NoFlyZones
     */
    public Set<NoFlyZone> getNoFlyZones() {
        return zones;
    }
}
