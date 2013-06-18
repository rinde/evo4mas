/**
 * 
 */
package rinde.evo4mas.gendreau06.comm;

import java.util.Collection;

import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.event.Listener;

/**
 * Interface of communications. Facade for communication system. Implementations
 * of this are added to and 'live' on a truck. Via the communicator a truck
 * receives updates about the environment with regards to {@link Parcel}s.
 * 
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public interface Communicator {

	public enum CommunicatorEventType {
		CHANGE;
	}

	void addUpdateListener(Listener l);

	// indicates that the truck is waiting for this parcel to become available
	void waitFor(Parcel p);

	// indicates that the truck is going to this parcel (this is final!)
	void claim(Parcel p);

	/**
	 * 
	 * @return All parcels which this communicator may handle.
	 */
	Collection<Parcel> getParcels();

}
