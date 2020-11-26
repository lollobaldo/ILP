package uk.ac.ed.inf.aqmaps;

public class DataPoint {
    /** Array of upper bounds for the below colors */
    private static int[] ranges = { 32, 64, 96, 128, 160, 192, 224, 256 };

    /** Color for low-battery points */
    private static String lowBatteryColor = "#000000";

    /** Color for not-visited points */
    private static String notVisitedColor = "#aaaaaa";

    /** Array of hex colors corresponding to the ranges above */
    private static String[] readingColors = {
        "#00ff00", "#40ff00", "#80ff00", "#c0ff00",
        "#ffc000", "#ff8000", "#ff4000", "#ff0000"
    };

    /** Color for low-battery points */
    private static String lowBatteryMarker = "cross";

    /** Color for not-visited points */
    private static String notVisitedMarker = "";

    private static String[] readingMarkers = {
        "lighthouse", "lighthouse", "lighthouse", "lighthouse",
        "danger", "danger", "danger", "danger"
    };

    private double reading;
    private String rgbString;
    private String colorName;
    private String markerSymbol;

    /**
     * Constructor.
     * Assumes battery is good.
     *
     * @param reading  the reading value
     */
    public DataPoint(double reading) {
        this(reading, false, true);
    }

    /**
     * Constructor.
     *
     * @param reading  the reading value
     * @param lowBattery  low-battery flag
     */
    public DataPoint(double reading, boolean lowBattery) {
        this(0, true, true);
    }

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

    public double getReading() {
        return this.reading;
    }

    public String getRgbString() {
        return this.rgbString;
    }

    public String getColorName() {
        return this.colorName;
    }

    public String getMarkerSymbol() {
        return this.markerSymbol;
    }

    @Override
    public String toString() {
        return "{" +
            " reading='" + getReading() + "'" +
            ", rgbString='" + getRgbString() + "'" +
            ", colorName='" + getColorName() + "'" +
            ", markerSymbol='" + getMarkerSymbol() + "'" +
            "}";
    }
}
