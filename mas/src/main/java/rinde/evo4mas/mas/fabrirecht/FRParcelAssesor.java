/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import java.util.Set;

import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Vehicle;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.problem.fabrirecht.ParcelAssesor;
import rinde.sim.problem.fabrirecht.ParcelDTO;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class FRParcelAssesor implements ParcelAssesor {
	protected final RoadModel roadModel;
	protected final PDPModel pdpModel;

	public FRParcelAssesor(RoadModel rm, PDPModel pm) {
		roadModel = rm;
		pdpModel = pm;
	}

	public boolean acceptParcel(ParcelDTO dto) {

		final Set<Vehicle> vehicles = pdpModel.getVehicles();

		for (final Vehicle v : vehicles) {
			if (((Truck) v).acceptNewParcel(dto)) {
				return true;
			}
		}

		return false;
	}
}
