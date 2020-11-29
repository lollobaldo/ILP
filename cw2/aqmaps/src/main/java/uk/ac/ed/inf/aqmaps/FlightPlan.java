package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.MultiLineString;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FlightPlan {
    private List<Point2D> flightPlan = new ArrayList<Point2D>();

    public FlightPlan() {}
    public FlightPlan(Point2D startingPoint) {
        flightPlan.add(startingPoint);
    }

    public boolean add(Point2D point) {
        return flightPlan.add(point);
    }

    public MultiLineString toGeoJson() {
        return 	MultiLineString.fromLngLats(Arrays.asList(Utils.points2dToPoints(flightPlan)));
    }
}
