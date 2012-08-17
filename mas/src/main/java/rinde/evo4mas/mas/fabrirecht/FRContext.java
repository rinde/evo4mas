/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.problem.fabrirecht.ParcelDTO;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class FRContext {

	public final PDPModel pdpModel;
	public final RoadModel roadModel;
	public final Truck truck;
	public final ParcelDTO parcel;
	public final boolean isInCargo;
	public final long time;

	public FRContext(RoadModel rm, PDPModel pm, Truck t, ParcelDTO p, long tm, boolean c) {
		roadModel = rm;
		pdpModel = pm;
		truck = t;
		parcel = p;
		time = tm;
		isInCargo = c;
	}

}
