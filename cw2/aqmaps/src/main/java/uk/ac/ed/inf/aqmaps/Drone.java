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
 * restrictions imposed by the {@link NoFlyZonesManager}.
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
                sensors.remove(closestSensor.get());
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
            targetDestination = step(directAngle);
        }

        // Generate a naive move (possibly illegal angle), then validate it.
        var move = new Line2D.Double(droneLocation, targetDestination);
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
        // Get the straight-line angle as optimal value
        var directAngle = Utils.radiansBetween(start, targetDestination);
        var directAngleDegrees = Math.toDegrees(directAngle);

        // Get the corners of the zone, and find the furthest one
        var zoneCoordinates = noFlyZone.getCoordinates();
        var distanceToFurtherCorner = zoneCoordinates.stream().map(start::distance).max(Double::compare).orElse(0.0);

        // Define predicates to avoid the noFlyZone
        Comparator<Double> deltaFromDirectAngle = Comparator.comparingDouble(s -> Math.abs(Utils.normaliseAngle(s - directAngleDegrees)));
        Predicate<Double> avoidsNoFlyZone = (angle) -> noFlyZone.isLegalMove(Utils.getLine(start, angle, distanceToFurtherCorner));
        Predicate<Double> avoidsOtherZones = (angle) -> noFlyZonesManager.isLegalMove(Utils.getLine(start, angle, STEP_LENGTH));
        
        // Out of all the legal angles, keep those which do not intersect with the NFZ,
        // then get the closest one to the straight-line angle.
        //noinspection OptionalGetWithoutIsPresent
        return legalAngles()
                .filter(avoidsNoFlyZone).filter(avoidsOtherZones)
                .min(deltaFromDirectAngle).get();
    }


    /**
     * Generate a stream of all legal angles for the drone to fly
     * This is generalised on the STEP_ANGLE, in this case it returns all multiples of 10 up to 360
     *
     * @return Stream<Double> The stream of legal angles
     */
    private Stream<Double> legalAngles() {
        return DoubleStream.iterate(0, angle -> angle < 360, x -> x + STEP_ANGLE).boxed();
    }


    /**
     * Tries to take a reading of a sensor. Returns whether the sensor was in range
     * and the reading was performed correctly.
     *
     * @param sensor The sensor to read
     * @return boolean Whether the sensor was read appropriately (i.e: within range)
     */
    private boolean takeReading(Sensor sensor) {
        if (distanceToSensor(sensor) < SENSOR_RANGE) {
            sensor.visit();
            return true;
        }
        return false;
    }


    /**
     * Get the closest sensor to the drone.
     *
     * @return Optional<Sensor> The closest sensor. Empty if no sensor is present
     */
    private Optional<Sensor> closestSensor() {
        // If no sensor is present, return an empty container
        if (sensors.size() == 0) {
            return Optional.empty();
        }
        // Else find the closest one by comparing the distance and taking the minimum.
        var comparingOnDistanceFromDrone = Comparator.comparingDouble(this::distanceToSensor);
        return Optional.of(Collections.min(sensors, comparingOnDistanceFromDrone));
    }


    /**
     * Make one step of STEP_LENGTH in the specified direction
     * @param radians The angle for the direction
     * @return Point2D The resulting position
     */
    private Point2D step(double radians) {
        var newX = droneLocation.getX() + STEP_LENGTH * Math.cos(radians);
        var newY = droneLocation.getY() + STEP_LENGTH * Math.sin(radians);
        return new Point2D.Double(newX, newY);
    }


    /**
     * Find the distance to a sensor
     *
     * @param sensor The sensor to find the distance to
     * @return double The distance to the sensor
     */
    private double distanceToSensor(Sensor sensor) {
        return droneLocation.distance(sensor.getCoordinates());
    }
}
