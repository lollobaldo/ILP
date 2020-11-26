package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;

import java.awt.geom.Point2D;

import static uk.ac.ed.inf.aqmaps.Utils.point2dToPoint;

public class Sensor {
    private Point2D coordinates;
    private String location;
    private Double battery;
    private String reading;
    private boolean visited;

    public Sensor(Point2D coordinates) {
        this.coordinates = coordinates;
    }

    public Feature toGeoJsonFeature() {
        var data = new DataPoint(reading.equals("null") ? 0 : Double.parseDouble(reading), battery <= 10, visited);
        var feature = Feature.fromGeometry(point2dToPoint(coordinates));
        feature.addStringProperty("location", location);
        feature.addStringProperty("marker-symbol", data.getMarkerSymbol());
        feature.addStringProperty("marker-color", data.getRgbString());
        feature.addStringProperty("rgb-string", data.getRgbString());
        return feature;
    }

    public void setCoordinates(Point2D coordinates) {
        this.coordinates = coordinates;
    }

    public Point2D getCoordinates() {
        return coordinates;
    }

    public String getLocation() {
        return location;
    }

    public Double getBattery() {
        return battery;
    }

    public String getReading() {
        return reading;
    }
}
