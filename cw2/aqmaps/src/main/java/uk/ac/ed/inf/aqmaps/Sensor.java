package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Feature;

import java.awt.geom.Point2D;

import static uk.ac.ed.inf.aqmaps.Utils.point2dToPoint;

public class Sensor {
    private Point2D coordinates;
    private final String location;
    private final double battery;
    private final String reading;
    private boolean visited;

    public Sensor(Point2D coordinates, String location, double battery, String reading, boolean visited) {
        this.coordinates = coordinates;
        this.location = location;
        this.battery = battery;
        this.reading = reading;
        this.visited = visited;
    }

    public void visit() {
        this.visited = true;
    }

    
    /** 
     * @return Feature
     */
    public Feature toGeoJsonFeature() {
        var data = new DataPoint(reading.equals("null") ? 0 : Double.parseDouble(reading), battery <= 10, visited);
        var feature = Feature.fromGeometry(point2dToPoint(coordinates));
        feature.addStringProperty("location", location);
        feature.addStringProperty("marker-symbol", data.getMarkerSymbol());
        feature.addStringProperty("marker-color", data.getRgbString());
        feature.addStringProperty("rgb-string", data.getRgbString());
        return feature;
    }

    
    /** 
     * @param coordinates
     */
    public void setCoordinates(Point2D coordinates) {
        this.coordinates = coordinates;
    }

    
    /** 
     * @return Point2D
     */
    public Point2D getCoordinates() {
        return coordinates;
    }

    
    /** 
     * @return String
     */
    public String getLocation() {
        return location;
    }

    
    /** 
     * @return String
     */
    @Override
    public String toString() {
        return "Sensor{" +
                "coordinates=" + coordinates +
                ", location='" + location + '\'' +
                ", battery=" + battery +
                ", reading='" + reading + '\'' +
                ", visited=" + visited +
                '}';
    }
}
