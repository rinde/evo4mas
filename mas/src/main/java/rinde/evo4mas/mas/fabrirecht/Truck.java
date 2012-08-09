package rinde.evo4mas.mas.fabrirecht;

import rinde.evo4mas.evo.gp.GPProgram;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel.ParcelState;
import rinde.sim.core.model.road.RoadModels;
import rinde.sim.core.model.road.RoadUser;
import rinde.sim.problem.fabrirecht.FRParcel;
import rinde.sim.problem.fabrirecht.FRVehicle;
import rinde.sim.problem.fabrirecht.VehicleDTO;

import com.google.common.base.Predicate;

class Truck extends FRVehicle {

	protected final GPProgram<FRContext> program;

	/**
	 * @param pDto
	 */
	public Truck(VehicleDTO pDto, GPProgram<FRContext> p) {
		super(pDto);
		program = p;
	}

	@Override
	protected void tickImpl(TimeLapse time) {
		// final FRParcel closest =
		// RoadModels.findClosestObject(roadModel.getPosition(this), roadModel,
		// FRParcel.class);

		final FRParcel closest = (FRParcel) RoadModels
				.findClosestObject(roadModel.getPosition(this), roadModel, new Predicate<RoadUser>() {
					public boolean apply(RoadUser input) {
						return input instanceof FRParcel
								&& pdpModel.getParcelState(((FRParcel) input)) == ParcelState.AVAILABLE;
					}
				});

		if (closest != null) {
			roadModel.moveTo(this, closest, time);
			if (roadModel.equalPosition(closest, this)
					&& pdpModel.getTimeWindowPolicy()
							.canPickup(closest.getPickupTimeWindow(), time.getTime(), closest.getPickupDuration())) {
				pdpModel.pickup(this, closest, time);
			}
		}

	}
}
