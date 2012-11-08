/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import rinde.evo4mas.evo.gp.GPProgram;
import rinde.evo4mas.mas.common.TruckContext;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel.ParcelState;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.problem.common.VehicleDTO;
import rinde.sim.problem.fabrirecht.FabriRechtScenario;

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
	public SubTruck(VehicleDTO pDto, GPProgram<TruckContext> p, FabriRechtScenario scen) {
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
