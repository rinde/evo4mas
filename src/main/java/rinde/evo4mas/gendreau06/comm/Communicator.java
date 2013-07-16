/**
 * 
 */
package rinde.evo4mas.gendreau06.comm;

import java.util.Collection;

import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.event.Listener;

/**
 * Interface of communications. Facade for communication system. acts on behalve
 * of an 'agent'. Implementations of this are added to and 'live' on a truck.
 * Via the communicator a truck receives updates about the environment with
 * regard to {@link Parcel}s.
 * 
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public interface Communicator {

	/**
	 * Event type for {@link Communicator}.
	 */
	public enum CommunicatorEventType {
		/**
		 * Indicates that the communicator received information indicating an
		 * environment change.
		 */
		CHANGE;
	}

	/**
	 * Add the {@link Listener} to this {@link Communicator}. The listener
	 * should from now receive all {@link CommunicatorEventType#CHANGE} events.
	 * @param l The listener to add.
	 */
	void addUpdateListener(Listener l);

	/**
	 * Indicates that the truck is waiting for this parcel to become available.
	 * Is used for intention spreading.
	 * @param p The parcel.
	 */
	void waitFor(Parcel p);

	/**
	 * Indicates that the truck is going to this parcel (this is final!)
	 * @param p The parcel.
	 */
	void claim(Parcel p);

	/**
	 * This method may only return {@link Parcel}s which are not yet picked up.
	 * @return All parcels which this communicator may handle.
	 */
	Collection<Parcel> getParcels();

}
