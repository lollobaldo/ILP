package uk.ac.ed.inf.aqmaps;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Loader {
    private static final String dayDataFilename = "air-quality-data.json";
    private static final String noFlightZonesFilename = "no-fly-zones.geojson";
    private static final String sensorLocationFilename = "details.json";

    private static final HttpClient client = HttpClient.newHttpClient();

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
     * @param path
     * @return String
     * @throws Exception
     */
    private static String getServerData(String path) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(server + path))
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return(response.body());
    }

    
    /** 
     * @param day
     * @param month
     * @param year
     * @return String
     * @throws Exception
     */
    public static String loadDayData(String day, String month, String year) throws IOException, InterruptedException {
        var path = "maps/" + year + "/" + month + "/" + day + "/" + dayDataFilename;
        return(getServerData(path));
    }

    
    /** 
     * @return String
     * @throws Exception
     */
    public static String loadNoFlyZones() throws IOException, InterruptedException {
        var path = "buildings/" + noFlightZonesFilename;
        return(getServerData(path));
    }

    
    /** 
     * @param location
     * @return String
     * @throws Exception
     */
    public static String loadSensorDetails(String location) throws IOException, InterruptedException {
        var path = "words/" + location.replaceAll("\\.","/") + "/" + sensorLocationFilename;
        return(getServerData(path));
    }
}
