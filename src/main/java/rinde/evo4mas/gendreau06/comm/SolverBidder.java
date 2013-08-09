/**
 * 
 */
package rinde.evo4mas.gendreau06.comm;

import rinde.sim.problem.common.DefaultParcel;
import rinde.solver.pdptw.SingleVehicleArraysSolver;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class SolverBidder extends AbstractBidder {

    protected final SingleVehicleArraysSolver solver;

    public SolverBidder(SingleVehicleArraysSolver sol) {
        solver = sol;
    }

    public double getBidFor(DefaultParcel p, long time) {
        // TODO compute insertion cost

        // TODO investigate if a seperate heuristic can be created to compute
        // the insertion cost
        return 0;
    }

}
