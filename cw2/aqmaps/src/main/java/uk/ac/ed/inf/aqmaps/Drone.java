package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

/**
 * This class represents the Drone.
 * It is a stateful drone, aiming to visit all sensors provided, with the
 * restrictions imposed by the {@link noFlyZonesManager}.
 */
public class Drone {
    /** Given parameters for the drone */
    private static final int ALLOWED_NUMBER_OF_MOVES = 150;
    private static final double STEP_LENGTH = 0.0003;
    private static final double STEP_ANGLE = 10;
    private static final double SENSOR_RANGE = 0.0002;

    /** Instance attributes */
    private final Point2D startingPoint;
    private final NoFlyZonesManager noFlyZonesManager;
    private final HashSet<Sensor> sensors;

    private int movesLeft;
    private Point2D droneLocation;

    /**
     * Constructor for the Drone.
     *
     * @param startingPoint The starting point for the Drone
     * @param noFlyZonesManager The NoFlyZonesManager
     * @param sensors The Set of sensors to be visited
     */
    public Drone(Point2D startingPoint, NoFlyZonesManager noFlyZonesManager, Set<Sensor> sensors) {
        // Validate input and initialise attributes
        this.startingPoint = Objects.requireNonNull(startingPoint);
        this.noFlyZonesManager = Objects.requireNonNull(noFlyZonesManager);
        this.sensors = new HashSet<>(Objects.requireNonNull(sensors));
        this.movesLeft = ALLOWED_NUMBER_OF_MOVES;
        this.droneLocation = startingPoint;
        for (var sensor : sensors) {
            Objects.requireNonNull(sensor);
        }
    }

    /**
     * Generates a valid {@link FlightPlan} for the Drone, with requirements
     * and restrictions given in the constructor.
     *
     * @return FlightPlan The valid FlightPlan visiting all sensors
     */
    public FlightPlan planFlight() {
        // Initialise an empty FlightPlan at <code>startingPoint</code>
        var flightPlan = new FlightPlan(startingPoint);

        // While there are moves left, iterate
        while (movesLeft != 0) {
            var closestSensor = closestSensor();
            Point2D targetDestination;

            // If there's a sensor to visit, try to go there.
            // Else go back to starting point
            if (closestSensor.isPresent()) {
                targetDestination = closestSensor.get().getCoordinates();
            } else {
                targetDestination = startingPoint;
            }

            // Make a valid move towards the target destination, and add it to the flight plan.
            droneLocation = moveTowards(targetDestination);
            flightPlan.add(droneLocation);

            // Read sensor if possible, and add to flight plan.
            if (closestSensor.isPresent() && takeReading(closestSensor.get())) {
                flightPlan.read(closestSensor.get().getLocation());
            }

            // If no sensor is left to read, and we're close to the starting point, break.
            if (sensors.size() == 0 && droneLocation.distance(startingPoint) < STEP_LENGTH) {
                break;
            }
            movesLeft -= 1;
        }
        return flightPlan;
    }


    /**
     * Make a move towards the target destination
     *
     * @param targetDestination The target destination
     * @return Point2D The drone location after the move
     */
    private Point2D moveTowards(Point2D targetDestination) {
        var angle = getBestAngleTo(targetDestination);
        var radians = Math.toRadians(angle);
        return step(radians);
    }

    /**
     * Get the best angle to go to the target location.
     * Note this is not always the straight-line angle if there is a
     * NoFlyZone in the path, or if the angle is not legal for the drone to fly.
     *
     * @param targetDestination The target destination
     * @return double The optimal <b>legal<b> angle to fly to the destination.
     * @see #legalAngles
     * @see #getBestFlyAroundAngle
     */
    public double getBestAngleTo(Point2D targetDestination) {
        // This is the straght-line angle to the target destination
        var directAngle = Utils.radiansBetween(droneLocation, targetDestination);

        // If the target destination is too close, generate an alternative target
        // in the same direction
        if (droneLocation.distance(targetDestination) < STEP_LENGTH) {
            target = step(directAngle);
        }

        // Generate a naive move (possibly illegal angle), then validate it.
        var move = new Line2D.Double(droneLocation, target);
        for (var zone : noFlyZonesManager.getNoFlyZones()) {
            if (!zone.isLegalMove(move)) {
                // If the move hits a NoFlyZone, then get a fly-around angle.
                return getBestFlyAroundAngle(droneLocation, targetDestination, zone);
            }
        }

        // Legalise the move by rounding to 10
        return Utils.round10(Math.toDegrees(directAngle));
    }

    
    /**
     * Get the best <b>legal</b> angle to <b>fully</b> fly around a given no fly zone.
     * It returns the closest angle to a straight-line path, that never intersects the no-fly zone.
     * This assures the least number of steps are needed to fly around it.
     * 
     * @param start The starting point
     * @param targetDestination The target destination
     * @param noFlyZone The NoFlyZone to circumvent
     * @return double
     */
    public double getBestFlyAroundAngle(Point2D start, Point2D targetDestination, NoFlyZone noFlyZone) {
        var directAngle = Utils.radiansBetween(start, targetDestination);
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

    
    /** 
     * @return Stream<Double>
     */
    private Stream<Double> legalAngles() {
        return DoubleStream.iterate(0, angle -> angle < 360, x -> x + STEP_ANGLE).boxed();
    }

    
    /** 
     * @param sensor
     * @return boolean
     */
    private boolean takeReading(Sensor sensor) {
        if (distanceToSensor(sensor) < SENSOR_RANGE) {
            sensor.visit();
            return sensors.remove(sensor);
        }
        return false;
    }

    
    /** 
     * @return Optional<Sensor>
     */
    private Optional<Sensor> closestSensor() {
        if (sensors.size() == 0) {
            return Optional.empty();
        }
        var comparingOnDistanceFromDrone = Comparator.comparingDouble(this::distanceToSensor);
        return Optional.of(Collections.min(sensors, comparingOnDistanceFromDrone));
    }

    
    /** 
     * @param radians
     * @return Point2D
     */
    private Point2D step(double radians) {
        var newX = droneLocation.getX() + STEP_LENGTH * Math.cos(radians);
        var newY = droneLocation.getY() + STEP_LENGTH * Math.sin(radians);
        return new Point2D.Double(newX, newY);
    }

    
    /** 
     * @param sensor
     * @return double
     */
    private double distanceToSensor(Sensor sensor) {
        return droneLocation.distance(sensor.getCoordinates());
    }
}
