/**
 * 
 */
package rinde.solver.pdptw;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.asList;

import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import rinde.sim.central.Converter;
import rinde.sim.central.GlobalStateObject;
import rinde.sim.central.GlobalStateObject.VehicleState;
import rinde.sim.core.graph.Point;
import rinde.sim.problem.common.DefaultParcel;

import com.google.common.math.DoubleMath;
import com.google.common.primitives.Ints;

/**
 * Adapter for {@link SingleVehicleArraysSolver} to conform to the
 * {@link Solver} interface.
 * 
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public class SingleVehicleSolverAdapter implements Solver {

    private final SingleVehicleArraysSolver solver;

    public SingleVehicleSolverAdapter(SingleVehicleArraysSolver solver) {
        this.solver = solver;
    }

    public Queue<DefaultParcel> solve(GlobalStateObject state) {
        checkArgument(state.vehicles.size() == 1);

        final VehicleState v = state.vehicles.iterator().next();
        checkArgument(v.remainingServiceTime == 0, "This solver can not deal with remaining service time, it should be 0, it was %s.", v.remainingServiceTime);
        final Collection<DefaultParcel> inCargo = v.contents;

        // there are always two locations: the current vehicle location and
        // the depot
        final int numLocations = 2 + (state.availableParcels.size() * 2)
                + inCargo.size();

        if (numLocations == 2) {
            // there are no orders
            // route.clear();
            return newLinkedList();
        } else if (state.availableParcels.size() + inCargo.size() == 1) {
            // if there is only one order, the solution is trivial
            if (!state.availableParcels.isEmpty()) {
                // parcels on the map require two visits (one for pickup, one
                // for delivery)
                final Queue<DefaultParcel> route = newLinkedList(state.availableParcels);
                route.addAll(state.availableParcels);
                return route;
            } // else
            return newLinkedList(inCargo);
        }
        // else, we are going to look for the optimal solution

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
            // add pickup location and time window
            locations[index] = p.dto.pickupLocation;
            point2parcel.put(locations[index], p);
            releaseDates[index] = fixTWstart(p.getPickupTimeWindow().begin, state.time);
            dueDates[index] = fixTWend(p.getPickupTimeWindow().end, state.time);
            serviceTimes[index] = Ints
                    .checkedCast(p.getPickupDuration() / 1000);

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
            serviceTimes[index] = Ints
                    .checkedCast(p.getDeliveryDuration() / 1000);

            locations[index] = p.getDestination();
            point2parcel.put(locations[index], p);
            releaseDates[index] = fixTWstart(p.getDeliveryTimeWindow().begin, state.time);
            dueDates[index] = fixTWend(p.getDeliveryTimeWindow().end, state.time);
            index++;
        }
        checkState(index == numLocations - 1);

        // the start position of the truck is the depot
        locations[index] = v.vehicle.getDTO().startPosition;
        // end of the day
        dueDates[index] = fixTWend(v.vehicle.getDTO().availabilityTimeWindow.end, state.time);

        final int[][] travelTime = Converter
                .toTravelTimeMatrix(asList(locations), 3600 / v.vehicle
                        .getSpeed(), RoundingMode.CEILING);
        final SolutionObject sol = solver
                .solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);

        final Queue<DefaultParcel> newRoute = newLinkedList();
        // ignore first (current pos) and last (depot)
        for (int i = 1; i < sol.route.length - 1; i++) {
            newRoute.add(point2parcel.get(locations[sol.route[i]]));
        }
        return newRoute;
    }

    static int fixTWstart(long start, long time) {
        return Math
                .max((DoubleMath.roundToInt(Ints.checkedCast(start - time) / 1000d, RoundingMode.CEILING)), 0);
    }

    static int fixTWend(long end, long time) {
        return Math
                .max((DoubleMath.roundToInt(Ints.checkedCast(end - time) / 1000d, RoundingMode.FLOOR)), 0);
    }

}
