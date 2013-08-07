/**
 * 
 */
package rinde.evo4mas.gendreau06.comm;

import rinde.sim.core.model.pdp.Parcel;
import rinde.solver.spdptw.SingleVehicleSolver;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class SolverBidder extends AbstractBidder {

	protected final SingleVehicleSolver solver;

	public SolverBidder(SingleVehicleSolver sol) {
		solver = sol;
	}

	public double getBidFor(Parcel p, long time) {
		// TODO compute insertion cost

		// TODO investigate if a seperate heuristic can be created to compute
		// the insertion cost
		return 0;
	}

}
