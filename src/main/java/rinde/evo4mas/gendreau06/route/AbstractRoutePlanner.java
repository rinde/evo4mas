/**
 * 
 */
package rinde.evo4mas.gendreau06.route;

import java.util.Set;

import rinde.evo4mas.gendreau06.Truck;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public abstract class AbstractRoutePlanner implements RoutePlanner {

	protected RoadModel roadModel;
	protected PDPModel pdpModel;
	protected Truck truck;

	public void init(RoadModel rm, PDPModel pm, Truck t) {
		roadModel = rm;
		pdpModel = pm;
		truck = t;
	}

	public abstract void update(Set<Parcel> parcels, long time);

	public abstract Parcel peek();

	public abstract void remove();

	public abstract boolean hasNext();

}
