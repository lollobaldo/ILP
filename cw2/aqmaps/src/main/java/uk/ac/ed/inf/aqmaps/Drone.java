package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;


public class Drone {
    private static final int ALLOWED_NUMBER_OF_MOVES = 150;
    private static final double STEP_LENGTH = 0.0003;
    private static final double STEP_ANGLE = 10;
    private static final double SENSOR_RANGE = 0.0002;

    private final Point2D startingPoint;
    private final NoFlyZonesManager noFlyZonesManager;
    private final HashSet<Sensor> sensors;

    private int movesLeft = ALLOWED_NUMBER_OF_MOVES;
    private Point2D droneLocation;

    public Drone(Point2D startingPoint, NoFlyZonesManager noFlyZonesManager, Set<Sensor> sensors) {
        this.startingPoint = Objects.requireNonNull(startingPoint);
        this.noFlyZonesManager = Objects.requireNonNull(noFlyZonesManager);
        this.sensors = new HashSet<>(Objects.requireNonNull(sensors));

        for (var sensor : sensors) {
            Objects.requireNonNull(sensor);
        }

        droneLocation = startingPoint;
    }

    public FlightPlan planFlight() {
        var flightPlan = new FlightPlan(startingPoint);
        while (movesLeft != 0) {
            var closestSensor = closestSensor();
            Point2D targetDestination;
            if (closestSensor.isPresent()) {
                targetDestination = closestSensor.get().getCoordinates();
            } else {
                targetDestination = startingPoint;
            }
            droneLocation = moveTowards(targetDestination);
            flightPlan.add(droneLocation);
            if (closestSensor.isPresent() && takeReading(closestSensor.get())) {
                flightPlan.read(closestSensor.get().getLocation());
            }
            movesLeft -= 1;
            if (sensors.size() == 0 && droneLocation.distance(startingPoint) < SENSOR_RANGE) {
                break;
            }
        }
        return flightPlan;
    }


    private Point2D moveTowards(Point2D targetDestination) {
        var angle = getBestAngleTo(targetDestination);
        var radians = Math.toRadians(angle);
        return step(radians);
    }

    public double getBestAngleTo(Point2D target) {
        var directAngle = Utils.radiansBetween(droneLocation, target);
        if (droneLocation.distance(target) < STEP_LENGTH) {
            target = step(directAngle);
        }
        var move = new Line2D.Double(droneLocation, target);
        for (var zone : noFlyZonesManager.getNoFlyZones()) {
            if (!zone.isLegalMove(move)) {
                return getBestFlyAroundAngle(droneLocation, target, zone);
            }
        }
        return Utils.round10(Math.toDegrees(directAngle));
    }

    public double getBestFlyAroundAngle(Point2D start, Point2D target, NoFlyZone noFlyZone) {
        var directAngle = Utils.radiansBetween(start, target);
        var directAngleDegrees = Math.toDegrees(directAngle);
        var zoneCoordinates = noFlyZone.getCoordinates();
        var distanceToFurtherCorner = zoneCoordinates.stream().map(start::distance).max(Double::compare).orElse(0.0);
        Comparator<Double> deltaFromDirectAngle = Comparator.comparingDouble(s -> Math.abs(Utils.normaliseAngle(s - directAngleDegrees)));
        Predicate<Double> avoidsNoFlyZone = (angle) -> noFlyZone.isLegalMove(Utils.getLine(start, angle, distanceToFurtherCorner));
        Predicate<Double> avoidsOtherZones = (angle) -> noFlyZonesManager.isLegalMove(Utils.getLine(start, angle, STEP_LENGTH));
        //noinspection OptionalGetWithoutIsPresent
        return legalAngles()
                .filter(avoidsNoFlyZone).filter(avoidsOtherZones)
                .min(deltaFromDirectAngle).get();
    }

    private Stream<Double> legalAngles() {
        return DoubleStream.iterate(0, angle -> angle < 360, x -> x + STEP_ANGLE).boxed();
    }

    private boolean takeReading(Sensor sensor) {
        if (distanceToSensor(sensor) < SENSOR_RANGE) {
            sensor.visit();
            return sensors.remove(sensor);
        }
        return false;
    }

    private Optional<Sensor> closestSensor() {
        if (sensors.size() == 0) {
            return Optional.empty();
        }
        var comparingOnDistanceFromDrone = Comparator.comparingDouble(this::distanceToSensor);
        return Optional.of(Collections.min(sensors, comparingOnDistanceFromDrone));
    }

    private Point2D step(double radians) {
        var newX = droneLocation.getX() + STEP_LENGTH * Math.cos(radians);
        var newY = droneLocation.getY() + STEP_LENGTH * Math.sin(radians);
        return new Point2D.Double(newX, newY);
    }

    private double distanceToSensor(Sensor sensor) {
        return droneLocation.distance(sensor.getCoordinates());
    }
}
