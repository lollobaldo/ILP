package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;
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
    private FlightPlan flightPlan;

    public Drone(Point2D startingPoint, NoFlyZonesManager noFlyZonesManager, Set<Sensor> sensors, int randomSeed) {
        this.startingPoint = Objects.requireNonNull(startingPoint);
        this.noFlyZonesManager = Objects.requireNonNull(noFlyZonesManager);
        this.sensors = new HashSet<Sensor>(Objects.requireNonNull(sensors));

        for (var sensor : sensors) {
            Objects.requireNonNull(sensor);
        }

        this.randomSeed = Objects.requireNonNull(randomSeed);

        droneLocation = startingPoint;
        flightPlan = new FlightPlan(startingPoint);
    }

    public FlightPlan planFlight() {
        while (movesLeft != 0) {
            Sensor closestSensor = closestSensor();
            Point2D targetDestination;
            if (closestSensor != null) {
                System.out.print("Going to ");
                System.out.println(closestSensor.getLocation());
                targetDestination = closestSensor.getCoordinates();
            } else {
                targetDestination = startingPoint;
            }
            droneLocation = moveTowards(targetDestination);
            flightPlan.add(droneLocation);
            if (closestSensor != null) {
                takeReading(closestSensor);
            }
            movesLeft -= 1;
            if (sensors.size() == 0 && droneLocation.distance(startingPoint) < SENSOR_RANGE) {
                break;
            }
        }
        return flightPlan;
    }

    private Point2D moveTowards(Point2D targetDestination) {
        var angle = noFlyZonesManager.getBestFlyAroundAngle(droneLocation, targetDestination);
        var radians = Math.toRadians(angle);
        System.out.print("Optimal angle: ");
        System.out.println(angle);
        Point2D move;
        var newX = droneLocation.getX() + STEP_LENGTH * Math.cos(radians);
        var newY = droneLocation.getY() + STEP_LENGTH * Math.sin(radians);
        move = new Point2D.Double(newX, newY);
        return move;
    }

    private double getValidAngleTo(Point2D destination) {
        var degrees = Math.toDegrees(radiansTo(destination));
        var validDegrees = Math.round(degrees/10.0) * 10;
        return Math.toRadians(validDegrees);
    }

//    private boolean isLegalMove(Point2D targetDestination) {
//        return isLegalMove(droneLocation, targetDestination);
//    }
//
//    private boolean isLegalMove(Point2D startingPoint, Point2D targetDestination) {
//        return noFlyZonesManager.isLegalMove(new Line2D.Double(startingPoint, targetDestination));
//    }

    private boolean takeReading(Sensor sensor) {
        if (distanceToSensor(sensor) < SENSOR_RANGE) {
            sensor.visit();
            sensors.remove(sensor);
        }
        return false;
    }

    private double radiansTo(Point2D destination) {
        return Math.atan2(destination.getY() - droneLocation.getY(), destination.getX() - droneLocation.getX());
    }

    private Sensor closestSensor() {
        if (sensors.size() == 0) {
            return null;
        }
        return Collections.min(sensors, Comparator.comparingDouble(this::distanceToSensor));
    }

    private double distanceToSensor(Sensor sensor) {
        return droneLocation.distance(sensor.getCoordinates());
    }
}
