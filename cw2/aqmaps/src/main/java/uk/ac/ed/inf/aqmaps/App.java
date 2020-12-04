package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.*;


/**
 * This class is the entry point of the application
 */
public class App {
    /**
     * Generates an appropriate readings filename for a given date
     *
     * @param day The day (2 characters)
     * @param month The month (2 characters)
     * @param year The year (4 characters)
     * @return String The filename for a readings file
     */
    private static final String readingsFile(String day, String month, String year) {
        return "readings-" + day + "-" + month + "-" + year + ".geojson";
    }

    /** Constant confinement area */
    private static final Point TOP_LEFT = Point.fromLngLat(-3.192473, 55.946233);
    private static final Point TOP_RIGHT = Point.fromLngLat(-3.184319, 55.946233);
    private static final Point BOTTOM_LEFT = Point.fromLngLat(-3.192473, 55.942617);
    private static final Point BOTTOM_RIGHT = Point.fromLngLat(-3.184319, 55.942617);
    /** Last point must be repeated, {@see Polygon} */
    private static final Polygon confinementArea = Polygon.fromLngLats(Collections.singletonList(Arrays.asList(
            TOP_LEFT, BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT
    )));


    /**
     * Generates an appropriate flightpath filename for a given date
     *
     * @param day The day (2 characters)
     * @param month The month (2 characters)
     * @param year The year (4 characters)
     * @return String The filename for a flightpath file
     */
    private static String flightPathFile(String day, String month, String year) {
        return "flightpath-" + day + "-" + month + "-" + year + ".txt";
    }


    /**
     * The main class.
     * Handles inputs, server startup and calls the appropriate function to
     * run the drone.
     * 
     * If the year is "0000", then all output files required for the submission
     * are generated (12 dates x 2 files)
     *
     * @param args
     */
    public static void main(String[] args) {
        // Check that all arguments are provided
        if(args.length != 7) {
            System.err.println("Error: wrong arguments provided");
            System.exit(1);
        }

        // Validate and parse the inputs
        var day = Objects.requireNonNull(args[0]);
        var month = Objects.requireNonNull(args[1]);
        var year = Objects.requireNonNull(args[2]);

        var initialLat = Double.parseDouble(Objects.requireNonNull(args[3]));
        var initialLng = Double.parseDouble(Objects.requireNonNull(args[4]));

        var serverPort = Objects.requireNonNull(args[6]);

        // Generate a starting point given Lng and Lat
        var startingPoint = new Point2D.Double(initialLng, initialLat);

        // Startup the Loader with the appropriate server / port
        Loader.setServer("http://localhost:" + serverPort + "/");

        // Try to run the drone -- catches network exceptions
        try {
            if (year.equals("0000")) {
                getSubmissionResults(startingPoint);
            } else {
                runDrone(day, month,year, startingPoint);
            }
        } catch (IOException | InterruptedException exception) {
            // If the operation was interrupted, exit with code 1.
            // Prints the stack trace to stderr.
            System.err.println("Network operation was interrupted.");
            System.err.println(exception);
            System.exit(1);
        }
    }

    
    /**
     * Generates the 12x2 files for the submission.
     * Runs the drone with all 12 possible dates in 2020, where day==month
     *
     * @param startingPoint The starting location for the drone.
     * @throws IOException If an IO error occurs when loading the data
     * @throws InterruptedException If the network operation is interrupted
     */
    private static void getSubmissionResults(Point2D startingPoint) throws IOException, InterruptedException {
        // Sets the year to 2020
        final String year = String.valueOf(2020);
        // Loop through possible days
        for (int i = 1; i <= 12; i++) {
            // Pad the day/month so it can be used as input
            String dayMonth = i < 10 ? "0" + i : String.valueOf(i);
            runDrone(dayMonth, dayMonth, year, startingPoint);
        }
    }

    
    /**
     * Run the drone on a given date, starting at the given point.
     * Calls the drone class, then generates the outputs and writes them to file
     *
     * @param day The day (2 characters)
     * @param month The month (2 characters)
     * @param year The year (4 characters)
     * @param startingPoint The starting point
     * @throws IOException If an IO error occurs when loading the data
     * @throws InterruptedException If the network operation is interrupted
     */
    private static void runDrone(String day, String month, String year, Point2D startingPoint) throws IOException, InterruptedException {
        // Load sensors and buildings data
        var sensors = loadSensorData(day, month, year);
        var noFlyZonesManager = new NoFlyZonesManager(Loader.loadNoFlyZones());

        // Start the drone and generate a FlightPlan
        var drone = new Drone(startingPoint, noFlyZonesManager, sensors);
        var droneFlightPlan = drone.planFlight();

        // Generates the readings GeoJson
        var readingsGeoJson = new ArrayList<Feature>();
        readingsGeoJson.add(Feature.fromGeometry(droneFlightPlan.toGeoJson()));
        for (var sensor : sensors) {
            readingsGeoJson.add(sensor.toGeoJsonFeature());
        }

        // Writes output files
        var flightPath = droneFlightPlan.fileFlightPlan();
        writeToOutput(flightPathFile(day, month, year), flightPath);
        var readings = FeatureCollection.fromFeatures(readingsGeoJson).toJson();
        writeToOutput(readingsFile(day, month, year), readings);
    }

    
    /**
     * Get the sensors information for a given date.
     * Uses the Loader class to make the network request.
     *
     * @param day The day (2 characters)
     * @param month The month (2 characters)
     * @param year The year (4 characters)
     * @return Set<Sensor> The Set of sensors to visit on that day
     * @throws IOException If an IO error occurs when loading the data
     * @throws InterruptedException If the network operation is interrupted
     */
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

    
    /**
     * Writes a String to an output file.
     *
     * @param fileName The filename of the file to be written
     * @param output The string to write
     */
    public static void writeToOutput(String fileName, String output) {
        try (PrintWriter out = new PrintWriter(fileName)) {
            out.println(output);
        } catch(FileNotFoundException exception) {
            System.err.println("Error while writing to file.");
            System.err.println("Filename: " + fileName);
            System.err.println("Content: " + output);
        }
    }
}
