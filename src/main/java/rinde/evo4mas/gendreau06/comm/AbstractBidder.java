/**
 * 
 */
package rinde.evo4mas.gendreau06.comm;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Collections.unmodifiableSet;

import java.util.Collection;
import java.util.Set;

import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.event.Event;
import rinde.sim.event.EventDispatcher;
import rinde.sim.event.Listener;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public abstract class AbstractBidder implements Bidder {

	protected final Set<Parcel> assignedParcels;
	protected final EventDispatcher eventDispatcher;

	public AbstractBidder() {
		assignedParcels = newLinkedHashSet();
		eventDispatcher = new EventDispatcher(CommunicatorEventType.values());
	}

	public void addUpdateListener(Listener l) {
		eventDispatcher.addListener(l, CommunicatorEventType.CHANGE);
	}

	// ignore
	public void waitFor(Parcel p) {}

	public void claim(Parcel p) {
		checkArgument(assignedParcels.contains(p));
		assignedParcels.remove(p);
	}

	public final Collection<Parcel> getParcels() {
		return unmodifiableSet(assignedParcels);
	}

	public void receiveParcel(Parcel p) {
		assignedParcels.add(p);
		eventDispatcher.dispatchEvent(new Event(CommunicatorEventType.CHANGE, this));
	}

}
