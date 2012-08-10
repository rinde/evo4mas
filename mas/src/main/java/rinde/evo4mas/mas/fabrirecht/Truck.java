package rinde.evo4mas.mas.fabrirecht;

import java.util.Collection;

import rinde.evo4mas.evo.gp.GPProgram;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.PDPModel.PDPModelEventType;
import rinde.sim.core.model.pdp.PDPModel.ParcelState;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.event.Event;
import rinde.sim.event.Listener;
import rinde.sim.problem.fabrirecht.FRVehicle;
import rinde.sim.problem.fabrirecht.VehicleDTO;

class Truck extends FRVehicle implements Listener {

	protected final GPProgram<FRContext> program;

	protected boolean hasChanged = true;
	protected Parcel currentTarget;

	/**
	 * @param pDto
	 */
	public Truck(VehicleDTO pDto, GPProgram<FRContext> p) {
		super(pDto);
		program = p;
	}

	@Override
	protected void tickImpl(TimeLapse time) {

		if ((hasChanged && time.hasTimeLeft()) || (currentTarget == null || !roadModel.containsObject(currentTarget))) {
			// && !(pdpModel.getParcelState(currentTarget) ==
			// ParcelState.AVAILABLE || pdpModel
			// .getParcelState(currentTarget) == ParcelState.ANNOUNCED)) {
			hasChanged = false;

			final Collection<Parcel> parcels = pdpModel.getParcels(ParcelState.AVAILABLE, ParcelState.ANNOUNCED);
			Parcel best = null;
			double bestValue = Double.POSITIVE_INFINITY;
			for (final Parcel p : parcels) {
				final double curr = program.execute(new FRContext(roadModel, pdpModel, this, p));
				if (curr < bestValue) {
					bestValue = curr;
					best = p;
				}
			}
			currentTarget = best;
		}

		// TODO what about parcels in the truck? they should be delivered as
		// well. and can be a valid target.

		if (currentTarget != null) {
			// System.out.println(pdpModel.getParcelState(currentTarget) + " " +
			// pdpModel.getVehicleState(this));
			roadModel.moveTo(this, currentTarget, time);
			// System.out.println(time.getTime() + " move");
			if (roadModel.equalPosition(currentTarget, this)
					&& pdpModel
							.getTimeWindowPolicy()
							.canPickup(currentTarget.getPickupTimeWindow(), time.getTime(), currentTarget.getPickupDuration())
					&& getCapacity() >= pdpModel.getContentsSize(this) + currentTarget.getMagnitude()) {
				pdpModel.pickup(this, currentTarget, time);
			}
		}

	}

	@Override
	public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {
		super.initRoadPDP(pRoadModel, pPdpModel);
		pdpModel.getEventAPI().addListener(this, PDPModelEventType.NEW_PARCEL, PDPModelEventType.START_PICKUP);
	}

	public void handleEvent(Event e) {
		hasChanged = true;

	}
}
