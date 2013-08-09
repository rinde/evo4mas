/**
 * 
 */
package rinde.solver.pdptw;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newLinkedList;

import java.math.RoundingMode;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import javax.measure.quantity.Duration;
import javax.measure.unit.Unit;

import rinde.sim.central.GlobalStateObject;
import rinde.sim.central.GlobalStateObject.VehicleState;
import rinde.sim.problem.common.DefaultParcel;
import rinde.solver.pdptw.ArraysSolvers.ArraysObject;

import com.google.common.collect.ImmutableList;
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
    private final Unit<Duration> outputTimeUnit;

    public SingleVehicleSolverAdapter(SingleVehicleArraysSolver solver,
            Unit<Duration> outputTimeUnit) {
        this.solver = solver;
        this.outputTimeUnit = outputTimeUnit;
    }

    public ImmutableList<? extends Queue<? extends DefaultParcel>> solve(
            GlobalStateObject state) {
        checkArgument(state.vehicles.size() == 1, "This solver can only deal with one vehicle.");

        final VehicleState v = state.vehicles.iterator().next();
        checkArgument(v.remainingServiceTime == 0, "This solver can not deal with remaining service time, it should be 0, it was %s.", v.remainingServiceTime);
        final Collection<DefaultParcel> inCargo = v.contents;

        // there are always two locations: the current vehicle location and
        // the depot
        final int numLocations = 2 + (state.availableParcels.size() * 2)
                + inCargo.size();

        if (numLocations == 2) {
            // there are no orders
            return ImmutableList.of(new LinkedList<DefaultParcel>());
        } else if (state.availableParcels.size() + inCargo.size() == 1) {
            // if there is only one order, the solution is trivial
            if (!state.availableParcels.isEmpty()) {
                // parcels on the map require two visits (one for pickup, one
                // for delivery)
                final Queue<DefaultParcel> route = newLinkedList(state.availableParcels);
                route.addAll(state.availableParcels);
                return ImmutableList.of(route);
            } // else
            return ImmutableList.of(new LinkedList<DefaultParcel>(inCargo));
        }
        // else, we are going to look for the optimal solution

        final ArraysObject ao = ArraysSolvers.toArrays(state, outputTimeUnit);

        final SolutionObject sol = solver
                .solve(ao.travelTime, ao.releaseDates, ao.dueDates, ao.servicePairs, ao.serviceTimes);

        final Queue<DefaultParcel> newRoute = newLinkedList();
        // ignore first (current pos) and last (depot)
        for (int i = 1; i < sol.route.length - 1; i++) {
            newRoute.add(ao.point2parcel.get(ao.locations.get(sol.route[i])));
        }
        return ImmutableList.of(newRoute);
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
