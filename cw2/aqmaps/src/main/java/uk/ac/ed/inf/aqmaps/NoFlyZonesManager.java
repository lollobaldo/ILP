package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

import java.awt.geom.Line2D;
import java.util.*;


public class NoFlyZonesManager {
    private static final Point TOP_LEFT = Point.fromLngLat(-3.192473, 55.946233);
    private static final Point TOP_RIGHT = Point.fromLngLat(-3.184319, 55.946233);
    private static final Point BOTTOM_LEFT = Point.fromLngLat(-3.192473, 55.942617);
    private static final Point BOTTOM_RIGHT = Point.fromLngLat(-3.184319, 55.942617);
    private static final Polygon confinementArea = Polygon.fromLngLats(Collections.singletonList(Arrays.asList(
            TOP_LEFT, BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT
    )));

    private final Set<NoFlyZone> zones;

    public NoFlyZonesManager(String geoJson) {
        Objects.requireNonNull(geoJson);
        zones = new HashSet<>();
        zones.add(new NoFlyZone(confinementArea, "Confinement area"));
        var zonesFeatures = FeatureCollection.fromJson(geoJson).features();
        assert zonesFeatures != null;
        for (var zone : zonesFeatures) {
            if (!(zone.geometry() instanceof com.mapbox.geojson.Polygon)) {
                throw new IllegalArgumentException("List does not contain polygons");
            }
            zones.add(new NoFlyZone((Polygon) zone.geometry(), 	zone.getStringProperty("name")));
        }
    }

    
    /** 
     * @param move
     * @return boolean
     */
    public boolean isLegalMove(Line2D move) {
        for (var zone : zones) {
            if (!zone.isLegalMove(move)) {
                return false;
            }
        }
        return true;
    }

    
    /** 
     * @return List<Feature>
     */
    public List<Feature> toGeoJsonFeatures() {
        var features = new ArrayList<Feature>();
        for (var zone : zones) {
            System.out.println(zone);
            Polygon polygon = zone.getPolygon();
            Feature feature = Feature.fromGeometry(polygon);
            feature.addStringProperty("fill", "#ff0000");
            features.add(feature);
        }
        return features;
    }

    
    /** 
     * @return Collection<NoFlyZone>
     */
    public Collection<NoFlyZone> getNoFlyZones() {
        return zones;
    }
}
