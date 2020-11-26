package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Polygon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class NoFlyZones {
    private static Set<NoFlyZone> zones = new HashSet<NoFlyZone>();

    public static ArrayList<Polygon> fromGeoJson(String geoJson) {
        var polygons = new ArrayList<Polygon>();
        var zonesFeatures = FeatureCollection.fromJson(geoJson).features();
        for (var zone : zonesFeatures) {
            if (!(zone.geometry() instanceof com.mapbox.geojson.Polygon)) {
                throw new IllegalArgumentException("List does not contain polygons");
            }
            zones.add(new NoFlyZone((Polygon) zone.geometry()));
        }
        return polygons;
    }
}
