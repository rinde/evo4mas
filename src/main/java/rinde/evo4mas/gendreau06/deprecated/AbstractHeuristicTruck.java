/**
 * 
 */
package rinde.evo4mas.gendreau06.deprecated;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rinde.ecj.Heuristic;
import rinde.evo4mas.gendreau06.GendreauContext;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel.ParcelState;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.Vehicle;
import rinde.sim.problem.common.DefaultParcel;
import rinde.sim.problem.common.DefaultVehicle;
import rinde.sim.problem.common.ParcelDTO;
import rinde.sim.problem.common.VehicleDTO;
import rinde.sim.util.fsm.State;
import rinde.sim.util.fsm.StateMachine;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
abstract class AbstractHeuristicTruck<E extends Enum<?>, T extends AbstractHeuristicTruck<E, T>> extends DefaultVehicle {

	protected final Heuristic<GendreauContext> program;
	protected final StateMachine<E, T> stateMachine;

	public AbstractHeuristicTruck(VehicleDTO pDto, Heuristic<GendreauContext> p, StateMachine<E, T> sm) {
		super(pDto);
		program = p;
		stateMachine = sm;
	}

	@Override
	protected abstract void tickImpl(TimeLapse time);

	// uses a generic context obj to create a parcel specific context
	protected GendreauContext createContext(GendreauContext gc, Parcel p, boolean isInCargo, boolean isAssignedToVehicle) {
		return new GendreauContext(gc.vehicleDTO, gc.truckPosition, gc.truckContents, ((DefaultParcel) p).dto, gc.time,
				isInCargo, isAssignedToVehicle, 0, gc.otherVehiclePositions, gc.todoList);

	}

	protected GendreauContext createGenericContext(long time) {
		final Collection<Parcel> contents = pdpModel.getContents(this);
		final List<Point> positions = newArrayList();
		final Set<Vehicle> vehicles = pdpModel.getVehicles();
		for (final Vehicle v : vehicles) {
			if (v != this) {
				positions.add(roadModel.getPosition(v));
			}
		}
		return new GendreauContext(dto, roadModel.getPosition(this), convert(contents), null, time, false, false, -1,
				positions, new HashSet<Parcel>());
	}

	protected GendreauContext createFullContext(long time, Parcel p, boolean isInCargo, boolean isAssignedToVehicle) {
		return createContext(createGenericContext(time), p, isInCargo, isAssignedToVehicle);
	}

	protected boolean isTooEarly(Parcel p, Point truckPos, TimeLapse time) {
		final boolean isPickup = pdpModel.getParcelState(p) != ParcelState.IN_CARGO;
		final Point loc = isPickup ? ((DefaultParcel) p).dto.pickupLocation : p.getDestination();
		final long travelTime = (long) ((Point.distance(loc, truckPos) / 30d) * 3600000d);
		long timeUntilAvailable = (isPickup ? p.getPickupTimeWindow().begin : p.getDeliveryTimeWindow().begin)
				- time.getStartTime();

		final long remainder = timeUntilAvailable % time.getTimeStep();
		if (remainder > 0) {
			timeUntilAvailable += time.getTimeStep() - remainder;
		}
		return timeUntilAvailable - travelTime > 0;
	}

	protected boolean isEndOfDay(TimeLapse time) {
		return time.hasTimeLeft()
				&& time.getTime() > dto.availabilityTimeWindow.end
						- ((Point.distance(roadModel.getPosition(this), dto.startPosition) / getSpeed()) * 3600000);
	}

	public static abstract class AbstractTruckState<E extends Enum<?>, T extends AbstractHeuristicTruck<E, T>>
			implements State<E, T> {

		public void onEntry(E event, T context) {}

		public void onExit(E event, T context) {}

		public String name() {
			return this.getClass().getSimpleName();
		}
	}

	protected static Set<ParcelDTO> convert(Collection<Parcel> parcels) {
		final Set<ParcelDTO> dtos = newLinkedHashSet();
		for (final Parcel p : parcels) {
			dtos.add(((DefaultParcel) p).dto);
		}
		return dtos;
	}

}
