/**
 * 
 */
package rinde.solver.pdptw;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.asList;

import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.measure.Measure;
import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import rinde.sim.central.GlobalStateObject;
import rinde.sim.central.GlobalStateObject.VehicleState;
import rinde.sim.core.graph.Point;
import rinde.sim.problem.common.DefaultParcel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.math.DoubleMath;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public final class ArraysSolvers {

    private ArraysSolvers() {}

    // convenience method using default units
    public static int[][] toTravelTimeMatrix(List<Point> points, double speed,
            RoundingMode rm) {
        return toTravelTimeMatrix(points, SI.METER, Measure.valueOf(speed, SI.METERS_PER_SECOND), SI.SECOND, rm);
    }

    /**
     * Converts the list of points on a plane into a travel time matrix. For
     * distance between two points the euclidean distance is used, i.e. no
     * obstacles or graph structure. This method is unit agnostic, i.e. it can
     * be used with any unit of time and space.
     * @param points The set of points which will be converted to a travel time
     *            matrix.
     * @param distToTimeFactor The factor which with every distance will be
     *            multiplied to obtain the travel time.
     * @param rm The result of the multiplication needs to be rounded. The
     *            rounding mode indicates how numbers are rounded, see
     *            {@link RoundingMode} for the available options.
     * @return A <code>n x n</code> travel time matrix, where <code>n</code> is
     *         the size of the <code>points</code> list.
     */
    public static int[][] toTravelTimeMatrix(List<Point> points,
            Unit<Length> distUnit, Measure<Double, Velocity> speed,
            Unit<Duration> outputTimeUnit, RoundingMode rm) {
        checkArgument(points.size() >= 2);
        final int[][] matrix = new int[points.size()][points.size()];
        for (int i = 0; i < points.size(); i++) {
            for (int j = 0; j < i; j++) {
                if (i != j) {
                    // compute distance
                    final Measure<Double, Length> dist = Measure.valueOf(Point
                            .distance(points.get(i), points.get(j)), distUnit);
                    // calculate duration in desired unit
                    final double duration = convertSpeed(speed, dist, outputTimeUnit);
                    // round duration
                    final int tt = DoubleMath.roundToInt(duration, rm);
                    matrix[i][j] = tt;
                    matrix[j][i] = tt;
                }
            }
        }
        return matrix;
    }

    // input units
    // time: ms, distance: km, speed: km/h
    // output units
    // time: seconds

    public static ArraysObject toArrays(GlobalStateObject state,
            Unit<Duration> outputTimeUnit) {

        final UnitConverter timeConverter = state.timeUnit
                .getConverterTo(outputTimeUnit);

        final VehicleState v = state.vehicles.iterator().next();
        final Collection<DefaultParcel> inCargo = v.contents;
        // there are always two locations: the current vehicle location and
        // the depot
        final int numLocations = 2 + (state.availableParcels.size() * 2)
                + inCargo.size();

        final int[] releaseDates = new int[numLocations];
        final int[] dueDates = new int[numLocations];
        final int[][] servicePairs = new int[state.availableParcels.size()][2];
        final int[] serviceTimes = new int[numLocations];

        final Map<Point, DefaultParcel> point2parcel = newHashMap();
        final Point[] locations = new Point[numLocations];
        locations[0] = v.location;

        int index = 1;
        int spIndex = 0;
        for (final DefaultParcel p : state.availableParcels) {
            serviceTimes[index] = DoubleMath.roundToInt(timeConverter.convert(p
                    .getPickupDuration()), RoundingMode.CEILING);
            // add pickup location and time window
            locations[index] = p.dto.pickupLocation;
            point2parcel.put(locations[index], p);
            releaseDates[index] = fixTWstart(p.getPickupTimeWindow().begin, state.time, timeConverter);
            dueDates[index] = fixTWend(p.getPickupTimeWindow().end, state.time, timeConverter);

            // link the pair with its delivery location (see next loop)
            servicePairs[spIndex++] = new int[] { index,
                    index + state.availableParcels.size() };

            index++;
        }
        checkState(spIndex == state.availableParcels.size(), "%s %s", state.availableParcels
                .size(), spIndex);

        final List<DefaultParcel> deliveries = newArrayListWithCapacity(state.availableParcels
                .size() + inCargo.size());
        deliveries.addAll(state.availableParcels);
        deliveries.addAll(inCargo);
        for (final DefaultParcel p : deliveries) {
            serviceTimes[index] = DoubleMath.roundToInt(timeConverter.convert(p
                    .getDeliveryDuration()), RoundingMode.CEILING);

            locations[index] = p.getDestination();
            point2parcel.put(locations[index], p);
            releaseDates[index] = fixTWstart(p.getDeliveryTimeWindow().begin, state.time, timeConverter);
            dueDates[index] = fixTWend(p.getDeliveryTimeWindow().end, state.time, timeConverter);
            index++;
        }
        checkState(index == numLocations - 1);

        // the start position of the truck is the depot
        locations[index] = v.vehicle.getDTO().startPosition;
        // end of the day
        dueDates[index] = fixTWend(v.vehicle.getDTO().availabilityTimeWindow.end, state.time, timeConverter);

        final Measure<Double, Velocity> speed = Measure.valueOf(v.vehicle
                .getSpeed(), state.speedUnit);

        final int[][] travelTime = ArraysSolvers
                .toTravelTimeMatrix(asList(locations), state.distUnit, speed, outputTimeUnit, RoundingMode.CEILING);

        return new ArraysObject(travelTime, releaseDates, dueDates,
                servicePairs, serviceTimes, locations, point2parcel);
    }

    // computes duration which is required to travel length with the given
    // velocity
    static double convertSpeed(Measure<Double, Velocity> speed,
            Measure<Double, Length> distance, Unit<Duration> outputTimeUnit) {
        return Measure.valueOf(distance.doubleValue(SI.METER)// meters
                / speed.doubleValue(SI.METERS_PER_SECOND), // divided by m/s
        SI.SECOND) // gives seconds
                .doubleValue(outputTimeUnit); // convert to desired unit
    }

    public static class ArraysObject {
        public final int[][] travelTime;
        public final int[] releaseDates;
        public final int[] dueDates;
        public final int[][] servicePairs;
        public final int[] serviceTimes;

        public final ImmutableList<Point> locations;
        public final ImmutableMap<Point, DefaultParcel> point2parcel;

        ArraysObject(int[][] travelTime, int[] releaseDates, int[] dueDates,
                int[][] servicePairs, int[] serviceTimes, Point[] locations,
                Map<Point, DefaultParcel> point2parcel) {
            this.travelTime = travelTime;
            this.releaseDates = releaseDates;
            this.dueDates = dueDates;
            this.servicePairs = servicePairs;
            this.serviceTimes = serviceTimes;
            this.locations = ImmutableList.copyOf(asList(locations));
            this.point2parcel = ImmutableMap.copyOf(point2parcel);
        }
    }

    static int fixTWstart(long start, long time, UnitConverter timeConverter) {
        return Math.max((DoubleMath.roundToInt(timeConverter.convert(start
                - time), RoundingMode.CEILING)), 0);
    }

    static int fixTWend(long end, long time, UnitConverter timeConverter) {
        return Math.max((DoubleMath.roundToInt(timeConverter
                .convert(end - time), RoundingMode.FLOOR)), 0);
    }
}
