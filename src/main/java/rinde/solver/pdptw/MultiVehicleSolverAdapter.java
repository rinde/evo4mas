/**
 * 
 */
package rinde.solver.pdptw;

import java.util.List;
import java.util.Queue;

import javax.measure.quantity.Duration;
import javax.measure.unit.Unit;

import rinde.sim.central.GlobalStateObject;
import rinde.sim.problem.common.DefaultParcel;
import rinde.solver.pdptw.ArraysSolvers.MVArraysObject;

import com.google.common.collect.ImmutableList;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class MultiVehicleSolverAdapter implements Solver {

    private final MultiVehicleArraysSolver solver;
    private final Unit<Duration> outputTimeUnit;

    public MultiVehicleSolverAdapter(MultiVehicleArraysSolver solver,
            Unit<Duration> outputTimeUnit) {
        this.solver = solver;
        this.outputTimeUnit = outputTimeUnit;
    }

    public List<? extends Queue<? extends DefaultParcel>> solve(
            GlobalStateObject state) {
        final MVArraysObject o = ArraysSolvers
                .toMultiVehicleArrays(state, outputTimeUnit);
        final SolutionObject[] sols = solver
                .solve(o.travelTime, o.releaseDates, o.dueDates, o.servicePairs, o.serviceTimes, o.vehicleTravelTimes, o.inventories, o.remainingServiceTimes);
        final ImmutableList.Builder<Queue<? extends DefaultParcel>> b = ImmutableList
                .builder();
        for (final SolutionObject sol : sols) {
            b.add(ArraysSolvers
                    .convertSolutionObject(sol, o.point2parcel, o.locations));
        }
        return b.build();
    }
}
