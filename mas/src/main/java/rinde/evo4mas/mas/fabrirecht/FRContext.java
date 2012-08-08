/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class FRContext {

	public final PDPModel pdpModel;
	public final RoadModel roadModel;
	public final Truck truck;

	public FRContext(RoadModel rm, PDPModel pm, Truck t) {
		roadModel = rm;
		pdpModel = pm;
		truck = t;
	}

}
