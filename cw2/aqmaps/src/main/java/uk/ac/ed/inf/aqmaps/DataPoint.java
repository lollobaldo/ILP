package uk.ac.ed.inf.aqmaps;

public class DataPoint {
    /** Array of upper bounds for the below colors */
    private final static int[] ranges = { 32, 64, 96, 128, 160, 192, 224, 256 };

    /** Color for low-battery points */
    private final static String lowBatteryColor = "#000000";

    /** Color for not-visited points */
    private final static String notVisitedColor = "#aaaaaa";

    /** Array of hex colors corresponding to the ranges above */
    private final static String[] readingColors = {
        "#00ff00", "#40ff00", "#80ff00", "#c0ff00",
        "#ffc000", "#ff8000", "#ff4000", "#ff0000"
    };

    /** Color for low-battery points */
    private final static String lowBatteryMarker = "cross";

    /** Color for not-visited points */
    private final static String notVisitedMarker = "";

    private final static String[] readingMarkers = {
        "lighthouse", "lighthouse", "lighthouse", "lighthouse",
        "danger", "danger", "danger", "danger"
    };

    private String rgbString;
    private String markerSymbol;

    /**
     * Main constructor.
     *
     * @param reading  the reading value
     * @param lowBattery  low-battery flag
     * @param visited  visited flag
     */
    public DataPoint(double reading, boolean lowBattery, boolean visited) {
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

    public String getRgbString() {
        return this.rgbString;
    }

    public String getMarkerSymbol() {
        return this.markerSymbol;
    }
}
