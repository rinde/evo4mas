/**
 * 
 */
package rinde.solver.pdptw;

import java.util.List;
import java.util.Queue;

import rinde.sim.central.GlobalStateObject;
import rinde.sim.problem.common.DefaultParcel;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class MultiVehicleSolverAdapter implements Solver {

    private final MultiVehicleArraysSolver solver;

    public MultiVehicleSolverAdapter(MultiVehicleArraysSolver solver) {
        this.solver = solver;
    }

    public List<Queue<DefaultParcel>> solve(GlobalStateObject state) {

        // solver.solve(travelTime, releaseDates, dueDates, servicePairs,
        // serviceTimes, vehicleTravelTimes, inventories, remainingServiceTimes)
        return null;
    }

}
