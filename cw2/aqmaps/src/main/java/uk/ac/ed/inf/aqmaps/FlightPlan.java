package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.LineString;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FlightPlan {
    private final Point2D startingPoint;
    private final Deque<FlightPlanComponent> flightPlan = new ArrayDeque<>();

    public FlightPlan(Point2D startingPoint) {
        this.startingPoint = startingPoint;
    }

    public void add(Point2D point) {
        var inProgress = new FlightPlanComponent();
        inProgress.index = flightPlan.size();
        if (flightPlan.peekLast() == null) {
            inProgress.start = startingPoint;
        } else {
            inProgress.start = flightPlan.peekLast().end;
        }
        inProgress.end = point;
        inProgress.angle = (int) Math.round(Utils.degreesBetween(inProgress.start, inProgress.end));
        assert(inProgress.angle % 10 == 0);
        flightPlan.add(inProgress);
//        flightPlan.add(point);
    }

    
    /** 
     * @param sensorLocation
     */
    public void read(String sensorLocation) {
        assert flightPlan.size() > 0;
        flightPlan.peekLast().sensor = sensorLocation;
    }

    public LineString toGeoJson() {
        var points = flightPlan.stream()
                .map(FlightPlanComponent::getEnd)
                .map(Utils::point2dToPoint)
                .collect(Collectors.toList());
        return 	LineString.fromLngLats(points);
    }

    
    /** 
     * @return Stream<String>
     */
    public String fileFlightPlan() {
        return flightPlan
                .stream()
                .map(FlightPlanComponent::toString)
                .collect(Collectors.joining("\n"));
    }

    private static class FlightPlanComponent {
        private int index;
        private Point2D start;
        private int angle;
        private Point2D end;
        private String sensor;

        public Point2D getEnd() {
            return end;
        }

        @Override
        public String toString() {
            return Stream.of(index, start.getX(), start.getY(), angle, end.getX(), end.getY(), sensor)
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        }
    }
}
