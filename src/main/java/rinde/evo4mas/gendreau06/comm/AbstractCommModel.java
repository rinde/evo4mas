/**
 * 
 */
package rinde.evo4mas.gendreau06.comm;

import rinde.sim.core.model.ModelProvider;
import rinde.sim.core.model.ModelReceiver;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.PDPModel.PDPModelEvent;
import rinde.sim.core.model.pdp.PDPModel.PDPModelEventType;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.event.Event;
import rinde.sim.event.Listener;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public abstract class AbstractCommModel implements ModelReceiver {

	public void registerModelProvider(ModelProvider mp) {
		mp.getModel(PDPModel.class).getEventAPI().addListener(new Listener() {
			public void handleEvent(Event e) {
				final PDPModelEvent event = ((PDPModelEvent) e);
				receiveParcel(event.parcel, event.time);
			}
		}, PDPModelEventType.NEW_PARCEL);
	}

	protected abstract void receiveParcel(Parcel p, long time);

}
