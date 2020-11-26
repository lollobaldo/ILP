package uk.ac.ed.inf.heatmap;

import java.io.FileOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.mapbox.geojson.GeoJson;
import com.mapbox.geojson.Point;

import uk.ac.ed.inf.heatmap.DataPoint;
import uk.ac.ed.inf.heatmap.HeatMap;

public class App {
    /** Bounding-rect Points */
    private static final Point TOP_LEFT = Point.fromLngLat(-3.192473, 55.946233);
    private static final Point TOP_RIGHT = Point.fromLngLat(-3.184319, 55.946233);
    private static final Point BOTTOM_LEFT = Point.fromLngLat(-3.192473, 55.942617);
    private static final Point BOTTOM_RIGHT = Point.fromLngLat(-3.184319, 55.942617);

    /** Fill-opacity for HeatMap layer */
    private static final double FILL_OPACITY = 0.75;

    /** Output GeoJson filename */
    private static final String OUTPUT_FILENAME = "heatmap.geojson";


    public static void main(String[] args) throws Exception {
        // Check arguments provided
        if(args.length != 1) {
            System.out.println("Error: wrong argument types");
            System.exit(1);
        }

        // Load data into 2D array
        int[][] data = loadPredictions(args[0]);
        // Generate GeoJson object, then serialise and write to output file
        GeoJson geojson = getHeatMap(data);
        String outputJson = geojson.toJson();
        writeToOutput(OUTPUT_FILENAME, outputJson);
    }

    /**
     * Load data from CSV file to 2D array.
     *
     * @param filename  Filename for CSV file
     * @return data
     */
    public static int[][] loadPredictions(String filename) throws Exception {
        List<int[]> list = new ArrayList<>();
        Scanner sc = new Scanner(new File(filename));
        // Iterate through each line in file
        while (sc.hasNext()) {
            // Foreach line, split into column values, trim and parse
            String[] line = sc.nextLine().split(",");
            int[] row = Arrays.stream(line).mapToInt(s -> Integer.parseInt(s.trim())).toArray();
            list.add(row);
        }
        sc.close();
        // Convert to 2D array over List for performance and ease of use
        int[][] ret = list.toArray(new int[0][]);
        return ret;
    }

    /**
     * Method to get the GeoJson object from the HeatMap
     *
     * @param data  Data to fill the HeatMap
     * @return GeoJson
     */
    public static GeoJson getHeatMap(int[][] data) {
        HeatMap heatMap = new HeatMap(data, TOP_LEFT, BOTTOM_RIGHT);
        return heatMap.generateGeoJson(FILL_OPACITY);
    }

    /**
     * Method to write the Json to output
     *
     * @param path  Path for output file
     * @param output  Data to write to output
     * @return GeoJson
     */
    public static void writeToOutput(String path, String output) throws Exception {
        FileOutputStream outputStream = new FileOutputStream(path);
        byte[] strToBytes = output.getBytes();
        outputStream.write(strToBytes);
        outputStream.close();
    }
}
