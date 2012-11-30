package rinde.evo4mas.mas.gendreau06;

import static com.google.common.collect.Sets.newLinkedHashSet;

import java.util.Collection;
import java.util.Set;

import rinde.ecj.GPProgram;
import rinde.evo4mas.mas.common.TruckContext;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel.ParcelState;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.problem.common.DefaultParcel;
import rinde.sim.problem.common.DefaultVehicle;
import rinde.sim.problem.common.ParcelDTO;
import rinde.sim.problem.common.VehicleDTO;

class Truck extends DefaultVehicle {

	protected final GPProgram<TruckContext> program;
	protected boolean hasChanged = true;
	protected Parcel currentTarget;

	public Truck(VehicleDTO pDto, GPProgram<TruckContext> p) {
		super(pDto);
		program = p;
	}

	protected Parcel next(long time) {
		return next(program, dto, pdpModel.getAvailableParcels(), pdpModel.getContents(this), roadModel.getPosition(this), time);
	}

	protected static Parcel next(GPProgram<TruckContext> program, VehicleDTO dto, Collection<Parcel> todo,
			Collection<Parcel> contents, Point truckPos, long time) {
		Parcel best = null;
		double bestValue = Double.POSITIVE_INFINITY;

		final Collection<ParcelDTO> contentDTOs = convert(contents);
		for (final Parcel p : todo) {
			final double v = program.execute(new TruckContext(dto, truckPos, contentDTOs, ((DefaultParcel) p).dto,
					time, false));
			if (v < bestValue) {
				best = p;
				bestValue = v;
			}
		}
		for (final Parcel p : contents) {
			final double v = program.execute(new TruckContext(dto, truckPos, contentDTOs, ((DefaultParcel) p).dto,
					time, true));
			if (v < bestValue) {
				best = p;
				bestValue = v;
			}
		}
		return best;
	}

	@Override
	protected void tickImpl(TimeLapse time) {
		try {

			if (currentTarget == null && time.hasTimeLeft()) {
				currentTarget = next(time.getTime());
			}

			if (currentTarget != null && time.hasTimeLeft()) {
				if (pdpModel.getParcelState(currentTarget) == ParcelState.IN_CARGO) {
					roadModel.moveTo(this, currentTarget.getDestination(), time);
					if (roadModel.getPosition(this).equals(currentTarget.getDestination())
							&& pdpModel
									.getTimeWindowPolicy()
									.canDeliver(currentTarget.getDeliveryTimeWindow(), time.getStartTime(), currentTarget
											.getDeliveryDuration())) {
						pdpModel.deliver(this, currentTarget, time);
						currentTarget = null;
					}

				} else {
					roadModel.moveTo(this, currentTarget, time);

					// System.out.println(time.getTime() + " move");
					if (roadModel.equalPosition(this, currentTarget)
							&& pdpModel
									.getTimeWindowPolicy()
									.canPickup(currentTarget.getPickupTimeWindow(), time.getStartTime(), currentTarget.getPickupDuration())
							&& getCapacity() >= pdpModel.getContentsSize(this) + currentTarget.getMagnitude()) {

						if (time.getTime() > currentTarget.getPickupTimeWindow().end
								- currentTarget.getPickupDuration()) {
							System.err.println("pickup tardiness " + currentTarget);
						}
						pdpModel.pickup(this, currentTarget, time);
						currentTarget = null;
					}
				}
			} else if (time.hasTimeLeft()
					&& time.getTime() > dto.availabilityTimeWindow.end
							- ((Point.distance(roadModel.getPosition(this), dto.startPosition) / getSpeed()) * 3600000)) {
				// there is nothing left to do and the end of the day is
				// approaching so we are going back to base
				roadModel.moveTo(this, dto.startPosition, time);
			}
		} catch (final Exception e) {
			System.out.println(program.toString());
			throw new RuntimeException(e);
		}
	}

	public GPProgram<TruckContext> getProgram() {
		return program;
	}

	static Set<ParcelDTO> convert(Collection<Parcel> parcels) {
		final Set<ParcelDTO> dtos = newLinkedHashSet();
		for (final Parcel p : parcels) {
			dtos.add(((DefaultParcel) p).dto);
		}
		return dtos;
	}
}
