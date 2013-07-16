/**
 * 
 */
package rinde.evo4mas.gendreau06.comm;

import javax.annotation.Nullable;

import rinde.ecj.Heuristic;
import rinde.evo4mas.gendreau06.GCBuilderReceiver;
import rinde.evo4mas.gendreau06.GendreauContext;
import rinde.evo4mas.gendreau06.GendreauContextBuilder;
import rinde.sim.core.model.pdp.Parcel;

/**
 * This is a {@link Bidder} implementation that uses an evolved heuristic to
 * compute its bids.
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public class EvoHeuristicBidder extends AbstractBidder implements GCBuilderReceiver {

	protected final Heuristic<GendreauContext> heuristic;
	@Nullable
	protected GendreauContextBuilder gendreauContextBuilder;

	/**
	 * Create the bidder using the specified {@link Heuristic}.
	 * @param h The heuristic to use for computing bids.
	 */
	public EvoHeuristicBidder(Heuristic<GendreauContext> h) {
		heuristic = h;
	}

	public double getBidFor(Parcel p, long time) {
		final GendreauContextBuilder gcb = gendreauContextBuilder;
		if (gcb == null) {
			throw new RuntimeException("The GendreauContextBuilder has not been set.");
		}
		return heuristic.compute(gcb.buildFromScatch(time, p, false, false));
	}

	public void receive(GendreauContextBuilder gcb) {
		gendreauContextBuilder = gcb;
	}

}
