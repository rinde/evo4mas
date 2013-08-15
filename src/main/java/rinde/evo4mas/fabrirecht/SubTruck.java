/**
 * 
 */
package rinde.evo4mas.fabrirecht;

import rinde.ecj.Heuristic;
import rinde.evo4mas.common.TruckContext;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel.ParcelState;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.pdptw.common.VehicleDTO;
import rinde.sim.pdptw.fabrirecht.FabriRechtScenario;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class SubTruck extends Truck {

	Parcel firstSubject;

	/**
	 * @param pDto
	 * @param p
	 * @param scen
	 */
	public SubTruck(VehicleDTO pDto, Heuristic<TruckContext> p, FabriRechtScenario scen) {
		super(pDto, p, scen);
	}

	public void setFirstActionSubject(Parcel p) {
		firstSubject = p;
	}

	@Override
	protected void tickImpl(TimeLapse time) {
		if (firstSubject != null) {
			if (pdpModel.getParcelState(firstSubject) == ParcelState.IN_CARGO) {
				pdpModel.deliver(this, firstSubject, time);
			} else {
				pdpModel.pickup(this, firstSubject, time);
			}
			firstSubject = null;
		}
		if (time.hasTimeLeft()) {
			super.tickImpl(time);
		}
	}

}
