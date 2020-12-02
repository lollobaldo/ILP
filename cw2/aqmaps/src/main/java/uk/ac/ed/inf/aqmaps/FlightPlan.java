package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.LineString;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FlightPlan {
    private final Point2D startingPoint;
    private final List<Point2D> flightPlan = new ArrayList<>();
    private final Deque<FlightPlanComponent> flightPlan1 = new ArrayDeque<>();

    public FlightPlan(Point2D startingPoint) {
        this.startingPoint = startingPoint;
    }

    public void add(Point2D point) {
        var inProgress = new FlightPlanComponent();
        inProgress.index = flightPlan1.size();
        if (flightPlan1.peekLast() == null) {
            inProgress.start = startingPoint;
        } else {
            inProgress.start = flightPlan1.peekLast().end;
        }
        inProgress.end = point;
        inProgress.angle = (int) Math.round(Utils.degreesBetween(inProgress.start, inProgress.end));
        assert(inProgress.angle % 10 == 0);
        flightPlan1.add(inProgress);
        flightPlan.add(point);
    }

    public void read(String sensorLocation) {
        assert flightPlan1.size() > 0;
        flightPlan1.peekLast().sensor = sensorLocation;
    }

    public LineString toGeoJson() {
        return 	LineString.fromLngLats(Utils.points2dToPoints(flightPlan));
    }

    public Stream<String> fileFlightPlan() {
        return flightPlan1.stream().map(FlightPlanComponent::toString);
    }

    private static class FlightPlanComponent {
        public int index;
        public Point2D start;
        public int angle;
        public Point2D end;
        public String sensor;

        @Override
        public String toString() {
            return Stream.of(index, start.getX(), start.getY(), angle, end.getX(), end.getY(), sensor)
                    .map(String::valueOf).collect(Collectors.joining(","));
        }
    }
}