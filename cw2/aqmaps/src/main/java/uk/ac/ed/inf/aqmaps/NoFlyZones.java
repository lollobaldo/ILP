package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.GeoJson;
import com.mapbox.geojson.Polygon;

import java.util.*;

public class NoFlyZones {
    private static Set<NoFlyZone> zones = new HashSet<NoFlyZone>();

    public static Set<NoFlyZone> fromGeoJson(String geoJson) {
        var zonesFeatures = FeatureCollection.fromJson(geoJson).features();
        for (var zone : zonesFeatures) {
            if (!(zone.geometry() instanceof com.mapbox.geojson.Polygon)) {
                throw new IllegalArgumentException("List does not contain polygons");
            }
            zones.add(new NoFlyZone((Polygon) zone.geometry()));
        }
        return zones;
    }

    public static List<Feature> toGeoJsonFeatures() {
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
}
