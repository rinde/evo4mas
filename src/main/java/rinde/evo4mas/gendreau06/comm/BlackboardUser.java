/**
 * 
 */
package rinde.evo4mas.gendreau06.comm;

import java.util.Collection;

import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.event.Event;
import rinde.sim.event.EventDispatcher;
import rinde.sim.event.Listener;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class BlackboardUser implements Communicator {

	protected BlackboardCommModel bcModel;
	protected final EventDispatcher eventDispatcher;

	public BlackboardUser() {
		eventDispatcher = new EventDispatcher(CommunicatorEventType.values());
	}

	public void init(BlackboardCommModel model) {
		bcModel = model;
	}

	public void waitFor(Parcel p) {}

	public void claim(Parcel p) {
		// forward call to model
		bcModel.claim(this, p);
	}

	public void update() {
		eventDispatcher.dispatchEvent(new Event(CommunicatorEventType.CHANGE, this));
	}

	public void addUpdateListener(Listener l) {
		eventDispatcher.addListener(l, CommunicatorEventType.CHANGE);
	}

	public Collection<Parcel> getParcels() {
		return bcModel.getUnclaimedParcels();
	}

}
