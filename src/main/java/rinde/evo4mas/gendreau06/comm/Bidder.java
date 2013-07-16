/**
 * 
 */
package rinde.evo4mas.gendreau06.comm;

import rinde.sim.core.model.pdp.Parcel;

/**
 * Implementations of this interface can participate in auctions.
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public interface Bidder extends Communicator {

	/**
	 * Should compute the 'bid value' for the specified {@link Parcel}. It can
	 * be assumed that this method is called only once for each {@link Parcel},
	 * the caller is responsible for any caching if necessary.
	 * @param p The {@link Parcel} that needs to be handled.
	 * @param time The current time.
	 * @return The bid value, the lower the better (i.e. cheaper).
	 */
	double getBidFor(Parcel p, long time);

	/**
	 * When an auction has been won by this {@link Bidder}, the {@link Parcel}
	 * is received via this method.
	 * @param p The {@link Parcel} that is won.
	 */
	void receiveParcel(Parcel p);

}
