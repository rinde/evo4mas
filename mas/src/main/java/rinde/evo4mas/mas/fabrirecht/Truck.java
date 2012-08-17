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
import rinde.sim.problem.fabrirecht.FRParcel;
import rinde.sim.problem.fabrirecht.FRVehicle;
import rinde.sim.problem.fabrirecht.ParcelDTO;
import rinde.sim.problem.fabrirecht.VehicleDTO;

class Truck extends FRVehicle implements Listener, CoordAgent {

	protected final GPProgram<FRContext> program;

	protected boolean hasChanged = true;
	protected Target currentTarget;

	protected CoordModel coordModel;

	/**
	 * @param pDto
	 */
	public Truck(VehicleDTO pDto, GPProgram<FRContext> p) {
		super(pDto);
		program = p;
	}

	protected Target findTarget(long time) {
		final Collection<Parcel> parcels = pdpModel.getParcels(ParcelState.AVAILABLE, ParcelState.ANNOUNCED);
		Parcel best = null;
		double bestValue = Double.POSITIVE_INFINITY;
		for (final Parcel p : parcels) {
			// TODO optimize: avoid creating all those objects in a loop
			final double curr = program.execute(new FRContext(roadModel, pdpModel, this, ((FRParcel) p).dto, time,
					false));
			if (curr < bestValue && coordModel.canServe(p, curr)) {
				bestValue = curr;
				best = p;
			}
		}

		final Collection<Parcel> contents = pdpModel.getContents(this);
		for (final Parcel p : contents) {
			final double curr = program
					.execute(new FRContext(roadModel, pdpModel, this, ((FRParcel) p).dto, time, true));
			if (curr < bestValue) {
				bestValue = curr;
				best = p;
			}
		}
		if (best == null) {
			return null;
		}
		return new Target(best, bestValue);
	}

	public boolean acceptNewParcel(ParcelDTO p) {
		final double v = program.execute(new FRContext(roadModel, pdpModel, this, p, p.orderArrivalTime, false));
		return v < 100;
	}

	@Override
	protected void tickImpl(TimeLapse time) {
		try {
			// TODO allow diversions?
			if ((hasChanged && time.hasTimeLeft())
					|| (currentTarget == null || !roadModel.containsObject(currentTarget.target))) {
				hasChanged = false;
				currentTarget = findTarget(time.getTime());
				if (currentTarget != null) {
					coordModel.doServe(currentTarget.target, this, currentTarget.heuristicValue);
				}
			}

			if (currentTarget != null && time.hasTimeLeft()) {
				if (pdpModel.getParcelState(currentTarget.target) == ParcelState.IN_CARGO) {
					roadModel.moveTo(this, currentTarget.target.getDestination(), time);
					if (roadModel.getPosition(this).equals(currentTarget.target.getDestination())
							&& pdpModel
									.getTimeWindowPolicy()
									.canDeliver(currentTarget.target.getDeliveryTimeWindow(), time.getTime(), currentTarget.target
											.getDeliveryDuration())) {
						pdpModel.deliver(this, currentTarget.target, time);
						currentTarget = null;
					}

				} else {
					roadModel.moveTo(this, currentTarget.target, time);

					// System.out.println(time.getTime() + " move");
					if (roadModel.equalPosition(currentTarget.target, this)
							&& pdpModel
									.getTimeWindowPolicy()
									.canPickup(currentTarget.target.getPickupTimeWindow(), time.getTime(), currentTarget.target
											.getPickupDuration())
							&& getCapacity() >= pdpModel.getContentsSize(this) + currentTarget.target.getMagnitude()
							&& pdpModel.getParcelState(currentTarget.target) == ParcelState.AVAILABLE) {
						pdpModel.pickup(this, currentTarget.target, time);
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

	class Target {
		public final Parcel target;
		public final double heuristicValue;

		public Target(Parcel t, double v) {
			target = t;
			heuristicValue = v;
		}
	}

	public void setCoordModel(CoordModel model) {
		coordModel = model;
	}

	public void notifyServiceChange() {
		hasChanged = true;
	}
}
