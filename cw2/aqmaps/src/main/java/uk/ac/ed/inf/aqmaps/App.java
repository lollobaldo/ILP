package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Point2D;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.http.HttpClient;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.*;

public class App {
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

        var sensorsData = Loader.loadDayData(day, month, year);
        System.out.println(sensorsData);
        var targetClassType = new TypeToken<Set<Sensor>>() {}.getType();
        Set<Sensor> sensors = new Gson().fromJson(sensorsData, targetClassType);

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

        var drone = new Drone(startingPoint, noFlyZonesManager, sensors, randomSeed);
        var droneFlightPlan = drone.planFlight();
        features.add(Feature.fromGeometry(droneFlightPlan.toGeoJson()));

        for (var sensor : sensors) {
            features.add(sensor.toGeoJsonFeature());
        }
        
        GeoJson geojson = FeatureCollection.fromFeatures(features);
        String outputJson = geojson.toJson();
        writeToOutput("res.txt", outputJson);
        var flightPathFile = "flightpath-" + day + "-" + month + "-" + year + ".txt";
        var flightPath = droneFlightPlan.fileFlightPlan().collect(Collectors.joining("\n"));
        try (PrintWriter out = new PrintWriter(flightPathFile)) {
            out.println(flightPath);
        }
        writeToOutput("res.txt", outputJson);
    }

    public static void writeToOutput(String path, String output) throws Exception {
        FileOutputStream outputStream = new FileOutputStream(path);
        byte[] strToBytes = output.getBytes();
        outputStream.write(strToBytes);
        outputStream.close();
    }
}
