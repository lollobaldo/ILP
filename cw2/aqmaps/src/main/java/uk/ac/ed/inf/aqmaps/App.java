package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.*;

public class App {
    private static String readingsFile(String day, String month, String year) {
        return "readings-" + day + "-" + month + "-" + year + ".txt";
    }

    private static String flightPathFile(String day, String month, String year) {
        return "flightpath-" + day + "-" + month + "-" + year + ".txt";
    }

    public static void main(String[] args) throws Exception {
        // Check that all arguments are provided
        if(args.length != 7) {
            System.err.println("Error: wrong arguments provided");
            System.exit(1);
        }

        var day = Objects.requireNonNull(args[0]);
        var month = Objects.requireNonNull(args[1]);
        var year = Objects.requireNonNull(args[2]);

        var initialLat = Double.parseDouble(Objects.requireNonNull(args[3]));
        var initialLng = Double.parseDouble(Objects.requireNonNull(args[4]));

        var serverPort = Objects.requireNonNull(args[6]);

        var startingPoint = new Point2D.Double(initialLng, initialLat);


        Loader.setServer("http://localhost:" + serverPort + "/");

        try {
            if (year.equals(0000)) {
                getSubmissionResults(startingPoint);
            } else {
                runDrone(day, month,year, startingPoint);
            }
        } catch(IOException | InterruptedException exception) {
            System.err.println("Network operation was interrupted.");
            System.err.println(exception);
            System.exit(1);
        }
    }

    private static void getSubmissionResults(Point2D startingPoint) throws IOException, InterruptedException {
        final String year = String.valueOf(2020);
        for (int i = 1; i <= 12; i++) {
            String day = String.valueOf(i);
            String month = day;
            runDrone(day, month, year, startingPoint);
        }
    }

    private static void runDrone(String day, String month, String year, Point2D startingPoint) throws IOException, InterruptedException {
        var sensors = loadSensorData(day, month, year);
        var noFlyZonesManager = new NoFlyZonesManager(Loader.loadNoFlyZones());

        var drone = new Drone(startingPoint, noFlyZonesManager, sensors);
        var droneFlightPlan = drone.planFlight();

        var readingsGeoJson = new ArrayList<Feature>();
        readingsGeoJson.add(Feature.fromGeometry(droneFlightPlan.toGeoJson()));

        for (var sensor : sensors) {
            readingsGeoJson.add(sensor.toGeoJsonFeature());
        }

        var readings = FeatureCollection.fromFeatures(readingsGeoJson).toJson();
        writeToOutput(readingsFile(day, month, year), readings);

        var flightPath = droneFlightPlan.fileFlightPlan();
        writeToOutput(flightPathFile(day, month, year), flightPath);
    }

    private static Set<Sensor> loadSensorData(String day, String month, String year) throws IOException, InterruptedException {
        var sensorsData = Loader.loadDayData(day, month, year);
        var targetClassType = new TypeToken<Set<Sensor>>() {}.getType();
        Set<Sensor> sensors = new Gson().fromJson(sensorsData, targetClassType);

        for (var sensor : sensors) {
            var json = Loader.loadSensorDetails(sensor.getLocation());
            var detailsObj = (JsonObject) JsonParser.parseString(json);

            var lng = detailsObj.getAsJsonObject("coordinates").get("lng").getAsDouble();
            var lat = detailsObj.getAsJsonObject("coordinates").get("lat").getAsDouble();
            sensor.setCoordinates(new Point2D.Double(lng, lat));
        }
        return sensors;
    }

    public static void writeToOutput(String fileName, String output) {
        try (PrintWriter out = new PrintWriter(fileName)) {
            out.println(output);
        } catch(FileNotFoundException exception) {
            System.err.println("Error while writing to file.");
            System.err.println("Filename: " + fileName);
            System.err.println("Content: " + output);
            System.err.println(exception);
        }
    }
}
