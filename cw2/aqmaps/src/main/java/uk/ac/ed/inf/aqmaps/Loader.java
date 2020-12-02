package uk.ac.ed.inf.aqmaps;

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

    public static void setServer(String server) {
        Loader.server = server;
    }

    private static String getServerData(String path) throws Exception {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(server + path))
                .build();
        System.out.println(URI.create(server + path));
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return(response.body());
    }

    public static String loadDayData(String day, String month, String year) throws Exception {
        var path = "maps/" + year + "/" + month + "/" + day + "/" + dayDataFilename;
        return(getServerData(path));
    }

    public static String loadNoFlyZones() throws Exception {
        var path = "buildings/" + noFlightZonesFilename;
        return(getServerData(path));
    }

    public static String loadSensorDetails(String location) throws Exception {
        var path = "words/" + location.replaceAll("\\.","/") + "/" + sensorLocationFilename;
        return(getServerData(path));
    }
}
