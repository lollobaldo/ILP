package uk.ac.ed.inf.aqmaps;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


/**
 * This class is responsible for the loading of the network resources.
 * It does not do any processing of the data nor exception handling,
 * to make its use more generalisable. It uses static methods as
 * network resources are expensive. This way, it ensures only one 
 * HTTP Client instance is created.
 */
public class Loader {
    /** Filenames from specification */
    private static final String dayDataFilename = "air-quality-data.json";
    private static final String noFlightZonesFilename = "no-fly-zones.geojson";
    private static final String sensorLocationFilename = "details.json";

    /** The HttpClient used to make the network requests */
    private static final HttpClient client = HttpClient.newHttpClient();

    /** The server for the request. {@link #setServer} */
    private static String server;


    /**
     * Sets the server to be used for the API calls.
     * Should include protocol, address and port.
     * E.g.: <code>setServer("https://localhost:80")</code>
     *
     * @param server The URL of the server.
     */
    public static void setServer(String server) {
        Loader.server = server + "/";
    }


    /**
     * Method to make a network request.
     *
     * @param path Path component of the URI for the resource to access
     * @return String The server response for the resource
     * @throws IOException If an IO error occurs when loading the data
     * @throws InterruptedException If the network operation is interrupted
     */
    private static String getServerData(String path) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(server + path))
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return(response.body());
    }


    /**
     * Method to load daily sensor data
     *
     * @param day The day (2 characters)
     * @param month The month (2 characters)
     * @param year The year (4 characters)
     * @return String The Json for the sensor data
     * @throws IOException If an IO error occurs when loading the data
     * @throws InterruptedException If the network operation is interrupted
     */
    public static String loadDayData(String day, String month, String year) throws IOException, InterruptedException {
        var path = "maps/" + year + "/" + month + "/" + day + "/" + dayDataFilename;
        return(getServerData(path));
    }


    /**
     * Method to load buildings data
     *
     * @return String The Json for the buildings data
     * @throws IOException If an IO error occurs when loading the data
     * @throws InterruptedException If the network operation is interrupted
     */
    public static String loadNoFlyZones() throws IOException, InterruptedException {
        var path = "buildings/" + noFlightZonesFilename;
        return(getServerData(path));
    }


    /**
     * Method to load a Sensor details
     *
     * @param location What3Words location of the sensor
     * @return String The Json for the sensor data
     * @throws IOException If an IO error occurs when loading the data
     * @throws InterruptedException If the network operation is interrupted
     */
    public static String loadSensorDetails(String location) throws IOException, InterruptedException {
        var path = "words/" + location.replaceAll("\\.","/") + "/" + sensorLocationFilename;
        return(getServerData(path));
    }
}
