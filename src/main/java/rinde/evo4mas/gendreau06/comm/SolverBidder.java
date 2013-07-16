/**
 * 
 */
package rinde.evo4mas.gendreau06.comm;

import rinde.sim.core.model.pdp.Parcel;
import rinde.solver.spdptw.Solver;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class SolverBidder extends AbstractBidder {

	protected final Solver solver;

	public SolverBidder(Solver sol) {
		solver = sol;
	}

	public double getBidFor(Parcel p, long time) {
		// TODO compute insertion cost

		// TODO investigate if a seperate heuristic can be created to compute
		// the insertion cost
		return 0;
	}

}
