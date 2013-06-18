/**
 * 
 */
package rinde.evo4mas.gendreau06.route;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.unmodifiableList;

import java.util.Collection;
import java.util.List;

import rinde.evo4mas.gendreau06.Truck;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public abstract class AbstractRoutePlanner implements RoutePlanner {

	private final List<Parcel> history;
	private boolean initialized;
	private boolean updated;

	protected RoadModel roadModel;
	protected PDPModel pdpModel;
	protected Truck truck;

	protected AbstractRoutePlanner() {
		history = newArrayList();
	}

	public void init(RoadModel rm, PDPModel pm, Truck t) {
		checkState(!isInitialized(), "init shoud be called only once");
		initialized = true;
		roadModel = rm;
		pdpModel = pm;
		truck = t;
	}

	public final void update(Collection<Parcel> onMap, Collection<Parcel> inCargo, long time) {
		checkState(isInitialized(), "RoutePlanner should be initialized before it can be used, see init()");
		updated = true;
		doUpdate(onMap, inCargo, time);
	}

	protected abstract void doUpdate(Collection<Parcel> onMap, Collection<Parcel> inCargo, long time);

	public final Parcel next(long time) {
		checkState(isInitialized(), "RoutePlanner should be initialized before it can be used, see init()");
		checkState(updated, "RoutePlanner should be udpated before it can be used, see update()");
		history.add(current());
		nextImpl(time);
		return current();
	}

	protected abstract void nextImpl(long time);

	public Parcel prev() {
		if (history.isEmpty()) {
			return null;
		}
		return history.get(history.size() - 1);
	}

	public List<Parcel> getHistory() {
		return unmodifiableList(history);
	}

	protected boolean isInitialized() {
		return initialized;
	}

	protected boolean isUpdated() {
		return updated;
	}

}
