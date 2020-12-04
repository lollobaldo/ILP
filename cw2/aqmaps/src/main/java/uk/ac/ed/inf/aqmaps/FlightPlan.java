package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class represents a full Flight Plan.
 * It makes use of an inner class to represent a single component (i.e. step) in the plan.
 * Provides methods to generate maps as a result.
 */
public class FlightPlan {
    /** Instance attributes */
    private final Point2D startingPoint;
    // This is a Deque, as it is an efficient data structure when operating on the ends only
    private final Deque<FlightPlanComponent> flightPlan = new ArrayDeque<>();

    /**
     * Constructor for the class
     *
     * @param startingPoint Starting point for the Flight Plan
     */
    public FlightPlan(Point2D startingPoint) {
        this.startingPoint = startingPoint;
    }


    /**
     * Add a destination Point to a flight plan.
     * Generates the angle based on the destination.
     *
     * @param point The point to add
     */
    public void add(Point2D point) {
        // Create a new Component
        var inProgress = new FlightPlanComponent();
        inProgress.index = flightPlan.size();

        // Generate start based on last destination
        if (flightPlan.peekLast() == null) {
            inProgress.start = startingPoint;
        } else {
            inProgress.start = flightPlan.peekLast().end;
        }

        // Calculate angle and assert it's valid as a sanity check
        inProgress.end = point;
        inProgress.angle = (int) Math.round(Utils.degreesBetween(inProgress.start, inProgress.end));
        assert inProgress.angle % 10 == 0;
        flightPlan.add(inProgress);
    }


    /**
     * Add the reading of a sensor to a FlightPlanComponent
     *
     * @param sensorLocation The sensor to be read.
     */
    public void read(String sensorLocation) {
        // Sanity check: assert we moved before reading a sensor
        assert flightPlan.size() > 0;
        flightPlan.peekLast().sensor = sensorLocation;
    }


    /**
     * Method to generate a GeoJson LineString for the FlightPlan
     *
     * @return LineString The generated drone path
     */
    public LineString toGeoJson() {
        // Get all end-points, add the starting point, and generate a LineString
        var points = flightPlan.stream()
                .map(FlightPlanComponent::getEnd)
                .map(Utils::point2dToPoint);
        // Add the starting point, and generate a LineString
        var line = Stream.concat(Stream.of(Utils.point2dToPoint(startingPoint)), points)
                .collect(Collectors.toList());
        return LineString.fromLngLats(line);
    }


    /**
     * Method to generate a String representation of a FlightPlan.
     * This is a format according to {@link FlightPlanComponent::toString}
     *
     * @return String The resulting FlightPlan
     */
    public String fileFlightPlan() {
        // Map all components to toString(), then join with a line-break
        return flightPlan
                .stream()
                .map(FlightPlanComponent::toString)
                .collect(Collectors.joining("\n"));
    }


    /**
     * This is a helper class for the FlightPlanComponent.
     * It represents one single entry of a FlightPlan. It has all the information
     * needed to fill a flightpath file as required by coursework specification.
     */
    private static class FlightPlanComponent {
        // Private instance attribute
        private int index;
        private Point2D start;
        private int angle;
        private Point2D end;
        private String sensor;

        /**
         * Method to get the End point of a flight step. Used to generate the LineString
         *
         * @return Point2D The end point
         */
        public Point2D getEnd() {
            return end;
        }

        /**
         * Generates a String representation of the Component.
         * This is in the formats from the given coursework specification
         *
         * @return String The formatted string representation
         */
        @Override
        public String toString() {
            // Get all the needed elements, make them strings and concatenate with commas
            return Stream.of(index, start.getX(), start.getY(), angle, end.getX(), end.getY(), sensor)
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        }
    }
}
