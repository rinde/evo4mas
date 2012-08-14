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
		try {

			// TODO allow diversions?
			if ((hasChanged && time.hasTimeLeft())
					|| (currentTarget == null || !roadModel.containsObject(currentTarget))) {
				// && !(pdpModel.getParcelState(currentTarget) ==
				// ParcelState.AVAILABLE || pdpModel
				// .getParcelState(currentTarget) == ParcelState.ANNOUNCED)) {
				hasChanged = false;

				// TODO optimize: avoid creating all those objects in a loop

				final Collection<Parcel> parcels = pdpModel.getParcels(ParcelState.AVAILABLE, ParcelState.ANNOUNCED);
				Parcel best = null;
				double bestValue = Double.POSITIVE_INFINITY;
				for (final Parcel p : parcels) {
					final double curr = program.execute(new FRContext(roadModel, pdpModel, this, p, time.getTime(),
							false));
					if (curr < bestValue) {
						bestValue = curr;
						best = p;
					}
				}

				final Collection<Parcel> contents = pdpModel.getContents(this);
				for (final Parcel p : contents) {
					final double curr = program.execute(new FRContext(roadModel, pdpModel, this, p, time.getTime(),
							true));
					if (curr < bestValue) {
						bestValue = curr;
						best = p;
					}
				}

				currentTarget = best;
			}

			if (currentTarget != null && time.hasTimeLeft()) {
				// System.out.println(pdpModel.getParcelState(currentTarget) +
				// " " +
				// pdpModel.getVehicleState(this));

				if (pdpModel.getParcelState(currentTarget) == ParcelState.IN_CARGO) {

					roadModel.moveTo(this, currentTarget.getDestination(), time);

					if (roadModel.getPosition(this).equals(currentTarget.getDestination())
							&& pdpModel
									.getTimeWindowPolicy()
									.canDeliver(currentTarget.getDeliveryTimeWindow(), time.getTime(), currentTarget.getDeliveryDuration())) {
						pdpModel.deliver(this, currentTarget, time);
					}
					currentTarget = null;

				} else {
					roadModel.moveTo(this, currentTarget, time);

					// System.out.println(time.getTime() + " move");
					if (roadModel.equalPosition(currentTarget, this)
							&& pdpModel
									.getTimeWindowPolicy()
									.canPickup(currentTarget.getPickupTimeWindow(), time.getTime(), currentTarget.getPickupDuration())
							&& getCapacity() >= pdpModel.getContentsSize(this) + currentTarget.getMagnitude()
							&& pdpModel.getParcelState(currentTarget) == ParcelState.AVAILABLE) {
						pdpModel.pickup(this, currentTarget, time);
						currentTarget = null;
					}
				}

			}
		} catch (final Exception e) {
			System.out.println(program.toString());
			throw new RuntimeException(e);
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
