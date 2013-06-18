/**
 * 
 */
package rinde.evo4mas.gendreau06.comm;

import rinde.ecj.Heuristic;
import rinde.evo4mas.gendreau06.GendreauContext;
import rinde.sim.core.model.pdp.Parcel;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class EvoHeuristicBidder extends AbstractBidder {

	protected final Heuristic<GendreauContext> heuristic;

	public EvoHeuristicBidder(Heuristic<GendreauContext> h) {
		heuristic = h;
	}

	public double getBidFor(Parcel p, long time) {
		// TODO Auto-generated method stub
		return 0;
	}

}
