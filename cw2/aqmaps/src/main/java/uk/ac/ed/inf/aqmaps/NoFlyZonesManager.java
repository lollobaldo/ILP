package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Polygon;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;


public class NoFlyZonesManager {
    private Set<NoFlyZone> zones = new HashSet<>();

    public NoFlyZonesManager(String geoJson) {
        Objects.requireNonNull(geoJson);
        var zonesFeatures = FeatureCollection.fromJson(geoJson).features();
        for (var zone : zonesFeatures) {
            if (!(zone.geometry() instanceof com.mapbox.geojson.Polygon)) {
                throw new IllegalArgumentException("List does not contain polygons");
            }
            zones.add(new NoFlyZone((Polygon) zone.geometry(), 	zone.getStringProperty("name")));
        }
    }

    public boolean add(NoFlyZone noFlyZone) {
        return zones.add(noFlyZone);
    }

//    public boolean isLegalMove(Line2D move) {
//        for (var zone : zones) {
//            if (!zone.isLegalMove(move)) {
//                return false;
//            };
//        }
//        return true;
//    }

    public int getBestFlyAroundAngle(Point2D start, Point2D target) {
        var move = new Line2D.Double(start, target);
        for (var zone : zones) {
            if (!zone.isLegalMove(move)) {
                System.out.print("Crashing with: ");
                System.out.print(zone);
                System.out.print(" - resolution angle: ");
                var angle = zone.getBestFlyAroundAngle(start, target);
                return angle;
            };
        }
        var straightAngle = Utils.round10(Math.toDegrees(Utils.radiansBetween(start, target)));
        return straightAngle;
    }

    public List<Feature> toGeoJsonFeatures() {
        var features = new ArrayList<Feature>();
        for (var zone : zones) {
            System.out.println(zone);
            Polygon polygon = Polygon.fromLngLats(Arrays.asList(zone.getCoordinates()));
            Feature feature = Feature.fromGeometry(polygon);
            feature.addStringProperty("fill", "#ff0000");
            features.add(feature);
        }
        return features;
    }

    @Override
    public String toString() {
        return "NoFlyZones{}";
    }
}
