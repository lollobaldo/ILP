package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class Drone {
    private static final int ALLOWED_NUMBER_OF_MOVES = 150;
    private static final double STEP_LENGTH = 0.0003;
    private static final double STEP_ANGLE = 10;
    private static final double SENSOR_RANGE = 0.0002;

    private final Point2D startingPoint;
    private final NoFlyZonesManager noFlyZonesManager;
    private final HashSet<Sensor> sensors;
    private final int randomSeed;

    private int movesLeft = ALLOWED_NUMBER_OF_MOVES;
    private Point2D droneLocation;

    public Drone(Point2D startingPoint, NoFlyZonesManager noFlyZonesManager, Set<Sensor> sensors, int randomSeed) {
        this.startingPoint = Objects.requireNonNull(startingPoint);
        this.noFlyZonesManager = Objects.requireNonNull(noFlyZonesManager);
        this.sensors = new HashSet<>(Objects.requireNonNull(sensors));

        for (var sensor : sensors) {
            Objects.requireNonNull(sensor);
        }

        this.randomSeed = Objects.requireNonNull(randomSeed);

        droneLocation = startingPoint;
    }

    public FlightPlan planFlight() {
        var flightPlan = new FlightPlan(startingPoint);
        while (movesLeft != 0) {
            Sensor closestSensor = closestSensor();
            Point2D targetDestination;
            if (closestSensor != null) {
                targetDestination = closestSensor.getCoordinates();
            } else {
                targetDestination = startingPoint;
            }
            droneLocation = moveTowards(targetDestination);
            flightPlan.add(droneLocation);
            if (closestSensor != null && takeReading(closestSensor)) {
                flightPlan.read(closestSensor.getLocation());
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
        System.out.print("Optimal angle: ");
        System.out.println(angle);
        Point2D move;
        var newX = droneLocation.getX() + STEP_LENGTH * Math.cos(radians);
        var newY = droneLocation.getY() + STEP_LENGTH * Math.sin(radians);
        move = new Point2D.Double(newX, newY);
        return move;
    }

    public double getBestAngleTo(Point2D target) {
        var directAngle = Utils.radiansBetween(droneLocation, target);
        if (droneLocation.distance(target) < STEP_LENGTH) {
            var newX = droneLocation.getX() + STEP_LENGTH * Math.cos(directAngle);
            var newY = droneLocation.getY() + STEP_LENGTH * Math.sin(directAngle);
            target = new Point2D.Double(newX, newY);
        }
        var move = new Line2D.Double(droneLocation, target);
        for (var zone : noFlyZonesManager.getNoFlyZones()) {
            if (!zone.isLegalMove(move)) {
                var angle = getBestFlyAroundAngle(droneLocation, target, zone);
                return angle;
            };
        }
        var straightAngle = Utils.round10(Math.toDegrees(directAngle));
        return straightAngle;
    }

    public double getBestFlyAroundAngle(Point2D start, Point2D target, NoFlyZone noFlyZone) {
        var directAngle = Utils.radiansBetween(start, target);
        var directAngleDegrees = Math.toDegrees(directAngle);
        var zoneCoordinates = noFlyZone.getCoordinates();
        var distanceToFurtherCorner = zoneCoordinates.stream().map(start::distance).max(Double::compare).orElse(0.0);
        Comparator<Double> deltaFromDirectAngle = Comparator.comparingDouble(s -> Math.abs(Utils.normaliseAngle(s - directAngleDegrees)));
        Predicate<Double> avoidsNoFlyZone = (angle) -> noFlyZone.isLegalMove(Utils.getLine(start, angle, distanceToFurtherCorner));
        Predicate<Double> avoidsOtherZones = (angle) -> noFlyZonesManager.isLegalMove(Utils.getLine(start, angle, 0.0003));
        var result = legalAngles()
                .filter(avoidsNoFlyZone).filter(avoidsOtherZones)
                .min(deltaFromDirectAngle).get();
        return result;
    }

    private Stream<Double> legalAngles() {
        return DoubleStream.iterate(0, x -> x + STEP_ANGLE).boxed();
    }

    private boolean takeReading(Sensor sensor) {
        if (distanceToSensor(sensor) < SENSOR_RANGE) {
            sensor.visit();
            return sensors.remove(sensor);
        }
        return false;
    }

    private Sensor closestSensor() {
        if (sensors.size() == 0) {
            return null;
        }
        var comparingOnDistanceFromDrone = Comparator.comparingDouble(this::distanceToSensor);
        return Collections.min(sensors, comparingOnDistanceFromDrone);
    }

    private double distanceToSensor(Sensor sensor) {
        return droneLocation.distance(sensor.getCoordinates());
    }
}
