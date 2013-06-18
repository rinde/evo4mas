/**
 * 
 */
package rinde.evo4mas.gendreau06.comm;

import rinde.ecj.Heuristic;
import rinde.evo4mas.gendreau06.GCBuilderReceiver;
import rinde.evo4mas.gendreau06.GendreauContext;
import rinde.evo4mas.gendreau06.GendreauContextBuilder;
import rinde.sim.core.model.pdp.Parcel;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class EvoHeuristicBidder extends AbstractBidder implements GCBuilderReceiver {

	protected final Heuristic<GendreauContext> heuristic;
	protected GendreauContextBuilder gendreauContextBuilder;

	public EvoHeuristicBidder(Heuristic<GendreauContext> h) {
		heuristic = h;
	}

	public double getBidFor(Parcel p, long time) {
		return heuristic.compute(gendreauContextBuilder.buildFromScatch(time, p, false, false));
	}

	public void receive(GendreauContextBuilder gcb) {
		gendreauContextBuilder = gcb;
	}

}
