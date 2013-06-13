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
public interface RoutePlanner {

	void init(RoadModel rm, PDPModel pm, Truck t);

	/**
	 * indicates a change in data, this should update the route.
	 * @param parcels TODO Should be immutable!
	 * @param time
	 */
	void update(Set<Parcel> parcels, long time);

	/**
	 * Should give the next destination.
	 * @param change Indicates whether there has been a change in data (i.e. has
	 *            there been a new parcel assigned to the truck). In two
	 *            consecutive calls, if this value is false, the same parcel
	 *            should be returned.
	 * @param time The current time.
	 * @return Should return null when there are no parcels to go to
	 */
	// Parcel nextParcel(boolean change, long time);
	Parcel peek();

	void remove();

	boolean hasNext();

	// void parcelIsDone(Parcel p);

}
