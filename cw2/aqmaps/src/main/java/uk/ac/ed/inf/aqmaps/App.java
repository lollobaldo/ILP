package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Point2D;
import java.io.FileOutputStream;
import java.net.http.HttpClient;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.*;

public class App {
    /** Bounding-rect Points */
    private static final Point TOP_LEFT = Point.fromLngLat(-3.192473, 55.946233);
    private static final Point TOP_RIGHT = Point.fromLngLat(-3.184319, 55.946233);
    private static final Point BOTTOM_LEFT = Point.fromLngLat(-3.192473, 55.942617);
    private static final Point BOTTOM_RIGHT = Point.fromLngLat(-3.184319, 55.942617);
    private static final Polygon confinementArea = Polygon.fromLngLats(Arrays.asList(Arrays.asList(
            TOP_LEFT, BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT
    )));

    private static final HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) throws Exception {
        // Check the arguments provided
        if(args.length != 7) {
            System.out.println("Error: wrong argument types");
            System.exit(1);
        }

        var day = Objects.requireNonNull(args[0]);
        var month = Objects.requireNonNull(args[1]);
        var year = Objects.requireNonNull(args[2]);

        var initialLat = Double.parseDouble(Objects.requireNonNull(args[3]));
        var initialLng = Double.parseDouble(Objects.requireNonNull(args[4]));

        var randomSeed = Integer.parseInt(Objects.requireNonNull(args[5]));
        var serverPort = Objects.requireNonNull(args[6]);

        var startingPoint = new Point2D.Double(initialLng, initialLat);

        Loader.setServer("http://localhost:" + serverPort + "/");

        var sesorsData = Loader.loadDayData(day, month, year);
        var targetClassType = new TypeToken<Set<Sensor>>() {}.getType();
        Set<Sensor> sensors = new Gson().fromJson(sesorsData, targetClassType);

        for (var sensor : sensors) {
            var json = Loader.loadSensorDetails(sensor.getLocation());
            var detailsObj = (JsonObject) JsonParser.parseString(json);

            Double lng = detailsObj.getAsJsonObject("coordinates").get("lng").getAsDouble();
            Double lat = detailsObj.getAsJsonObject("coordinates").get("lat").getAsDouble();
            sensor.setCoordinates(new Point2D.Double(lng, lat));
        }

        var noFlyZonesData = Loader.loadNoFlyZones();
        var noFlyZonesManager = new NoFlyZonesManager(noFlyZonesData);
//        noFlyZonesManager.add(new NoFlyZone(confinementArea, "Confinement area"));

        var features = noFlyZonesManager.toGeoJsonFeatures();
        var startingPointMarker = Feature.fromGeometry(Utils.point2dToPoint(startingPoint));
        startingPointMarker.addStringProperty("marker-symbol", "star");
        startingPointMarker.addStringProperty("marker-color", "#0000ff");
        startingPointMarker.addStringProperty("rgb-string", "#0000ff");
        features.add(startingPointMarker);

//        features.add(Feature.fromGeometry(confinementArea));

        var drone = new Drone(startingPoint, noFlyZonesManager, sensors, randomSeed);
        var dronePath = drone.planFlight().toGeoJson();
        features.add(Feature.fromGeometry(dronePath));

        for (var sensor : sensors) {
            features.add(sensor.toGeoJsonFeature());
        }
        
        GeoJson geojson = FeatureCollection.fromFeatures(features);
        String outputJson = geojson.toJson();
        writeToOutput("res.txt", outputJson);

        Point2D p = new Point2D.Double(0,0);
        Point2D q = new Point2D.Double(1,-1);
        System.out.println(Utils.radiansBetween(p, q));
        System.out.println(Math.toDegrees(Utils.radiansBetween(p, q)));

        int aaa = Arrays.asList(10, 30).stream()
                .min(Comparator.comparingDouble(a -> Math.abs(14 - a)))
                .orElseThrow(() -> new NoSuchElementException("No value present"));
        System.out.println(aaa);
    }

    public static void writeToOutput(String path, String output) throws Exception {
        FileOutputStream outputStream = new FileOutputStream(path);
        byte[] strToBytes = output.getBytes();
        outputStream.write(strToBytes);
        outputStream.close();
    }
}
