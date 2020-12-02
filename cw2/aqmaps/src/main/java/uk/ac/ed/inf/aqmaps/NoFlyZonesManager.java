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
    private static final Polygon confinementArea = Polygon.fromLngLats(Arrays.asList(Arrays.asList(
            TOP_LEFT, BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT
    )));

    private Set<NoFlyZone> zones;

    public NoFlyZonesManager(String geoJson) {
        Objects.requireNonNull(geoJson);
        zones = new HashSet<>();
        zones.add(new NoFlyZone(confinementArea, "Confinement area"));
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

    public boolean isLegalMove(Line2D move) {
        for (var zone : zones) {
            if (!zone.isLegalMove(move)) {
                return false;
            };
        }
        return true;
    }

//    public int getBestFlyAroundAngle(Point2D start, Point2D target) {
//        var move = new Line2D.Double(start, target);
//        for (var zone : zones) {
//            if (!zone.isLegalMove(move)) {
//                System.out.print("Crashing with: ");
//                System.out.print(zone);
//                System.out.print(" - resolution angle: ");
//                var angle = getBestFlyAroundAngle(start, target, zone);
//                return angle;
//            };
//        }
//        var straightAngle = Utils.round10(Math.toDegrees(Utils.radiansBetween(start, target)));
//        return straightAngle;
//    }
//
//    public int getBestFlyAroundAngle(Point2D start, Point2D target, NoFlyZone noFlyZone) {
//        var directAngle = Utils.radiansBetween(start, target);
//        var directAngleDegrees = Math.toDegrees(directAngle);
//        var zoneCoordinates = noFlyZone.getCoordinates();
//        var distanceToFurtherCorner = zoneCoordinates.stream().map(start::distance).max(Double::compare).orElse(0.0);
//        Comparator<Integer> deltaFromDirectAngle = Comparator.comparingDouble(s -> Math.abs(Utils.normaliseAngle(s - directAngleDegrees)));
//        Predicate<Integer> avoidsNoFlyZone = (angle) -> noFlyZone.isLegalMove(Utils.getLine(start, angle, distanceToFurtherCorner));
//        Predicate<Integer> avoidsOtherZones = (angle) -> isLegalMove(Utils.getLine(start, angle, 0.0003));
//        var result = IntStream.range(0, 36).map(a -> a*10).boxed()
//                .filter(avoidsNoFlyZone)
//                .filter(avoidsOtherZones)
//                .min(deltaFromDirectAngle)
//                .get();
//        return result;
//    }

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

    public Collection<NoFlyZone> getNoFlyZones() {
        return zones;
    }

    @Override
    public String toString() {
        return "NoFlyZones{}";
    }
}
