/**
 * 
 */
package rinde.evo4mas.gendreau06.comm;

import rinde.sim.core.model.pdp.Parcel;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public interface Communicator {

	// indicates that the truck is waiting for this parcel to become available
	void waitFor(Parcel p);

	// indicates that the truck is going to this parcel
	void claim(Parcel p);

}
