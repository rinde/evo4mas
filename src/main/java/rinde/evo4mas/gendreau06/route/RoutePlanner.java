/**
 * 
 */
package rinde.evo4mas.gendreau06.route;

import java.util.Collection;
import java.util.List;

import rinde.evo4mas.gendreau06.Truck;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;

/**
 * This is a route planner. It is unusual in the sense that it reveals its
 * future destinations one hop at a time.
 * 
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public interface RoutePlanner {

	void init(RoadModel rm, PDPModel pm, Truck t);

	/**
	 * indicates a change in data, this should update the route. should be
	 * called before next()
	 * @param onMap TODO Should be immutable!
	 * @param inCargo TODO
	 * @param time
	 */
	void update(Collection<Parcel> onMap, Collection<Parcel> inCargo, long time);

	/**
	 * Should give the current destination. Subsequent calls should always
	 * return the same destination. (no recomputation shoudl be done).
	 * @param change Indicates whether there has been a change in data (i.e. has
	 *            there been a new parcel assigned to the truck). In two
	 *            consecutive calls, if this value is false, the same parcel
	 *            should be returned.
	 * @return Should return null when there are no parcels to go to
	 */
	Parcel current();

	/**
	 * Indicates that current location has been visited. Computes next
	 * @param time
	 * @return The new current or null if there is no
	 */
	Parcel next(long time);

	/**
	 * Should return the previous destination
	 * @return
	 */
	Parcel prev();

	/**
	 * 
	 * @return A list of all visited parcels
	 */
	List<Parcel> getHistory();

	boolean hasNext();

	// void parcelIsDone(Parcel p);

}
