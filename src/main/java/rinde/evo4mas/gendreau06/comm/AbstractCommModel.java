/**
 * 
 */
package rinde.evo4mas.gendreau06.comm;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import rinde.sim.core.model.Model;
import rinde.sim.core.model.ModelProvider;
import rinde.sim.core.model.ModelReceiver;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.PDPModel.PDPModelEvent;
import rinde.sim.core.model.pdp.PDPModel.PDPModelEventType;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.event.Event;
import rinde.sim.event.Listener;

/**
 * This class provides a common base for classes that implement a communication
 * strategy between a set of {@link Communicator}s. There are currently two
 * implementations, blackboard communication ({@link BlackboardCommModel}) and
 * auctioning ({@link AuctionCommModel}).
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public abstract class AbstractCommModel<T extends Communicator> implements ModelReceiver, Model<T> {
	protected List<T> communicators;

	protected AbstractCommModel() {
		communicators = newArrayList();
	}

	public void registerModelProvider(ModelProvider mp) {
		mp.getModel(PDPModel.class).getEventAPI().addListener(new Listener() {
			public void handleEvent(Event e) {
				final PDPModelEvent event = ((PDPModelEvent) e);
				receiveParcel(event.parcel, event.time);
			}
		}, PDPModelEventType.NEW_PARCEL);
	}

	protected abstract void receiveParcel(Parcel p, long time);

	public boolean register(final T communicator) {
		communicators.add(communicator);
		return true;
	}

	public boolean unregister(T element) {
		throw new UnsupportedOperationException();
	}

}
