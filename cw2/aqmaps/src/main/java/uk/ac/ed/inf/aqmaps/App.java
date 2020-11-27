package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Point2D;
import java.io.FileOutputStream;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.GeoJson;
import com.mapbox.geojson.Point;

import static uk.ac.ed.inf.aqmaps.Utils.point2dToPoint;

public class App {
    /** Bounding-rect Points */
//    private static final Point TOP_LEFT = Point.fromLngLat(-3.192473, 55.946233);
//    private static final Point TOP_RIGHT = Point.fromLngLat(-3.184319, 55.946233);
//    private static final Point BOTTOM_LEFT = Point.fromLngLat(-3.192473, 55.942617);
//    private static final Point BOTTOM_RIGHT = Point.fromLngLat(-3.184319, 55.942617);

    private static final HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) throws Exception {
        System.out.println(args.toString());
        // Check arguments provided
        if(args.length != 7) {
            System.out.println("Error: wrong argument types");
            System.exit(1);
        }

        var day = args[0];
        var month = args[1];
        var year = args[2];
        var initialLat = Double.parseDouble(args[3]);
        var initialLng = Double.parseDouble(args[4]);
        var randomSeed = args[5];
        var serverPort = args[6];

        Loader.setServer("http://localhost:" + serverPort + "/");

        var sesorsData = Loader.loadDayData(day, month, year);
        // Deserialise JSON Array to an ArrayList of AirQualityData
        var targetClassType = new TypeToken<ArrayList<Sensor>>() { }.getType();
        ArrayList<Sensor> sensorList = new Gson().fromJson(sesorsData, targetClassType);

        var noFlyZonesData = Loader.loadNoFlyZones();
        var noFlyZones = NoFlyZones.fromGeoJson(noFlyZonesData);

        for (var sensor : sensorList) {
            var json = Loader.loadSensorDetails(sensor.getLocation());
            var detailsObj = (JsonObject) JsonParser.parseString(json);

            Double lng = detailsObj.getAsJsonObject("coordinates").get("lng").getAsDouble();
            Double lat = detailsObj.getAsJsonObject("coordinates").get("lat").getAsDouble();
            sensor.setCoordinates(new Point2D.Double(lng, lat));
        }

        var features = NoFlyZones.toGeoJsonFeatures();
        System.out.println(features.size());
        var startingPoint = Feature.fromGeometry(Point.fromLngLat(initialLng, initialLat));
        startingPoint.addStringProperty("marker-symbol", "star");
        startingPoint.addStringProperty("marker-color", "#0000ff");
        startingPoint.addStringProperty("rgb-string", "#0000ff");
        features.add(startingPoint);
        for (var sensor : sensorList) {
            features.add(sensor.toGeoJsonFeature());
        }

        GeoJson geojson = FeatureCollection.fromFeatures(features);
        String outputJson = geojson.toJson();
        writeToOutput("res.txt", outputJson);
    }

    public static void writeToOutput(String path, String output) throws Exception {
        FileOutputStream outputStream = new FileOutputStream(path);
        byte[] strToBytes = output.getBytes();
        outputStream.write(strToBytes);
        outputStream.close();
    }
}
