/**
 * 
 */
package rinde.evo4mas.gendreau06.comm;

import rinde.sim.core.model.pdp.Parcel;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public interface Bidder extends Communicator {

	double getBidFor(Parcel p, long time);

	void receiveParcel(Parcel p);

}
