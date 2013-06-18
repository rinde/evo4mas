/**
 * 
 */
package rinde.evo4mas.gendreau06.comm;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;

import java.util.Iterator;
import java.util.List;

import rinde.sim.core.model.Model;
import rinde.sim.core.model.pdp.Parcel;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class AuctionCommModel extends AbstractCommModel implements Model<Bidder> {

	protected final List<Bidder> bidders;

	public AuctionCommModel() {
		bidders = newArrayList();
	}

	@Override
	protected void receiveParcel(Parcel p, long time) {
		checkState(!bidders.isEmpty(), "there are no bidders..");
		final Iterator<Bidder> it = bidders.iterator();
		Bidder bestBidder = it.next();
		// if there are no other bidders, there is no need to organize an
		// auction at all (mainly used in test cases)
		if (it.hasNext()) {
			double bestValue = bestBidder.getBidFor(p, time);

			while (it.hasNext()) {
				final Bidder cur = it.next();
				final double curValue = cur.getBidFor(p, time);
				if (curValue < bestValue) {
					bestValue = curValue;
					bestBidder = cur;
				}
			}
		}
		bestBidder.receiveParcel(p);
	}

	public boolean register(Bidder element) {
		bidders.add(element);
		return true;
	}

	public boolean unregister(Bidder element) {
		throw new UnsupportedOperationException();
	}

	public Class<Bidder> getSupportedType() {
		return Bidder.class;
	}
}
