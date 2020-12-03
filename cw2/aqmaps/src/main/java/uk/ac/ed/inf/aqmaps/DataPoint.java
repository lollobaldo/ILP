package uk.ac.ed.inf.aqmaps;

/**
 * This class represents a DataPoint. This corresponds to a marker in the
 * output GeoJson, or simply a sensor reading.
 */
public class DataPoint {
    /** Array of upper bounds for the colors below */
    private final static int[] ranges = { 32, 64, 96, 128, 160, 192, 224, 256 };

    /** Given parameters for the markers */
    private final static String lowBatteryColor = "#000000";
    private final static String notVisitedColor = "#aaaaaa";
    private final static String[] readingColors = {
        "#00ff00", "#40ff00", "#80ff00", "#c0ff00",
        "#ffc000", "#ff8000", "#ff4000", "#ff0000"
    };
    private final static String lowBatteryMarker = "cross";
    private final static String notVisitedMarker = "";
    private final static String[] readingMarkers = {
        "lighthouse", "lighthouse", "lighthouse", "lighthouse",
        "danger", "danger", "danger", "danger"
    };


    /** Rgb color and Marker symbol for this instance of DataPoint */
    private String rgbString;
    private String markerSymbol;

    /**
     * Main constructor.
     *
     * @param reading The reading value
     * @param lowBattery Low-battery flag
     * @param visited Visited flag
     */
    public DataPoint(double reading, boolean lowBattery, boolean visited) {
        // If not visited or low battery, then assign appropriate marker
        if (!visited) {
            rgbString = notVisitedColor;
            markerSymbol = notVisitedMarker;
        } else if (lowBattery) {
            rgbString = lowBatteryColor;
            markerSymbol = lowBatteryMarker;
        } else {
            // Iterate through lower bounds to find the fitting one
            for(int i = 0; i < ranges.length; i++) {
                if (reading < ranges[i]) {
                    rgbString = readingColors[i];
                    markerSymbol = readingMarkers[i];
                    break;
                }
            }
        }
    }


    /**
     * Getter for rgbString
     *
     * @return String The String representation for the marker rgb color.
     */
    public String getRgbString() {
        return this.rgbString;
    }


    /**
     * Getter for markerSymbol
     *
     * @return String The String representation for the marker symbol.
     */
    public String getMarkerSymbol() {
        return this.markerSymbol;
    }
}
