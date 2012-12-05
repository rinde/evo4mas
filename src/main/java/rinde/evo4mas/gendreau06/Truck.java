package rinde.evo4mas.gendreau06;

import static com.google.common.collect.Sets.newLinkedHashSet;

import java.util.Collection;
import java.util.Set;

import rinde.ecj.GPProgram;
import rinde.evo4mas.common.TruckContext;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.PDPModel.ParcelState;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.event.Event;
import rinde.sim.event.Listener;
import rinde.sim.problem.common.DefaultParcel;
import rinde.sim.problem.common.DefaultVehicle;
import rinde.sim.problem.common.ParcelDTO;
import rinde.sim.problem.common.VehicleDTO;
import rinde.sim.util.fsm.StateMachine;
import rinde.sim.util.fsm.StateMachine.DefaultState;
import rinde.sim.util.fsm.StateMachine.State;

class Truck extends DefaultVehicle implements Listener {

	protected final GPProgram<TruckContext> program;
	protected boolean hasChanged = true;
	protected Parcel currentTarget;

	protected CoordinationModel coordinationModel;

	// enum TruckState {
	// IDLE, DECIDE, EARLY_TARGET, GOTO_DELIVERY, GOTO_PICKUP, DELIVER, PICKUP,
	// GO_HOME;
	// }

	protected final StateMachine<Trigger> stateMachine;

	enum Trigger {
		CHANGE, END_OF_DAY, NO_TARGET, EARLY, READY, IN_CARGO, IS_AVAILABLE, MOVE, ARRIVE, DONE, CONTINUE;
	}

	public Truck(VehicleDTO pDto, GPProgram<TruckContext> p) {
		super(pDto);
		program = p;

		final State<Trigger> idle = new Idle();
		final State<Trigger> decide = new Decide();
		final State<Trigger> earlyTarget = new EarlyTarget();
		final State<Trigger> gotoDelivery = new GotoDelivery();
		final State<Trigger> gotoPickup = new GotoPickup();
		final State<Trigger> deliver = new Deliver();
		final State<Trigger> pickup = new Pickup();
		final State<Trigger> goHome = new GoHome();

		stateMachine = StateMachine.create(Trigger.class, idle)/* */
		.addTransition(idle, decide, Trigger.CHANGE) /* */
		.addTransition(idle, goHome, Trigger.END_OF_DAY) /* */
		.addTransition(decide, goHome, Trigger.END_OF_DAY) /* */
		.addTransition(decide, idle, Trigger.NO_TARGET) /* */
		.addTransition(decide, earlyTarget, Trigger.EARLY) /* */
		.addTransition(decide, gotoDelivery, Trigger.IN_CARGO) /* */
		.addTransition(decide, gotoPickup, Trigger.IS_AVAILABLE) /* */
		.addTransition(earlyTarget, decide, Trigger.CHANGE) /* */
		.addTransition(earlyTarget, decide, Trigger.READY) /* */
		.addTransition(earlyTarget, decide, Trigger.READY) /* */
		.addTransition(gotoDelivery, deliver, Trigger.ARRIVE) /* */
		.addTransition(gotoPickup, pickup, Trigger.ARRIVE) /* */
		.addTransition(deliver, decide, Trigger.DONE) /* */
		.addTransition(pickup, decide, Trigger.DONE) /* */
		.addTransition(goHome, decide, Trigger.CHANGE) /* */
		.addSelfTransitionToAll(Trigger.CONTINUE)

		.build();

		System.out.println(stateMachine.toDot());

	}

	protected Parcel next(long time) {
		return next(program, dto, pdpModel.getParcels(ParcelState.ANNOUNCED, ParcelState.AVAILABLE), pdpModel.getContents(this), coordinationModel
				.getClaims(), roadModel.getPosition(this), time);
	}

	protected static Parcel next(GPProgram<TruckContext> program, VehicleDTO dto, Collection<Parcel> todo,
			Collection<Parcel> contents, Set<Parcel> alreadyClaimed, Point truckPos, long time) {
		Parcel best = null;
		double bestValue = Double.POSITIVE_INFINITY;

		boolean isPickup = true;

		final Collection<ParcelDTO> contentDTOs = convert(contents);
		for (final Parcel p : todo) {
			// filter out the already claimed parcels
			if (!alreadyClaimed.contains(p)) {
				final double v = program.execute(new TruckContext(dto, truckPos, contentDTOs, ((DefaultParcel) p).dto,
						time, false));
				if (v < bestValue) {
					best = p;
					bestValue = v;
				}
			}
		}
		for (final Parcel p : contents) {
			final double v = program.execute(new TruckContext(dto, truckPos, contentDTOs, ((DefaultParcel) p).dto,
					time, true));
			if (v < bestValue) {
				best = p;
				bestValue = v;
				isPickup = false;
			}
		}
		return best;
	}

	protected boolean isTooEarly(Parcel p, Point truckPos, long time) {
		final boolean isPickup = pdpModel.getParcelState(currentTarget) == ParcelState.IN_CARGO;
		final Point loc = isPickup ? ((DefaultParcel) p).dto.pickupLocation : p.getDestination();
		final long travelTime = (long) ((Point.distance(loc, truckPos) / 30d) * 3600000d);
		final long timeToBegin = (isPickup ? p.getPickupTimeWindow().begin : p.getDeliveryTimeWindow().begin) - time;
		return timeToBegin >= 0 && timeToBegin - travelTime > 1000;
	}

	TimeLapse currentTime;
	Truck truck;

	@Override
	protected void tickImpl(TimeLapse time) {
		currentTime = time;
		truck = this;

		// if (hasChanged) {
		// stateMachine.fire(Trigger.CHANGE);
		// } else {
		stateMachine.fire(Trigger.CONTINUE);
		hasChanged = false;
		// }
		return;
		// try {
		//
		// if (((currentTarget == null && hasChanged) || (currentTarget != null
		// && !roadModel
		// .containsObject(currentTarget))) && time.hasTimeLeft()) {
		// currentTarget = next(time.getTime());
		// hasChanged = false;
		// if (currentTarget != null && pdpModel.getParcelState(currentTarget)
		// != ParcelState.IN_CARGO) {
		// coordinationModel.claim(currentTarget);
		// }
		// }
		//
		// if (currentTarget != null && time.hasTimeLeft()) {
		// if (pdpModel.getParcelState(currentTarget) == ParcelState.IN_CARGO) {
		// roadModel.moveTo(this, currentTarget.getDestination(), time);
		// if
		// (roadModel.getPosition(this).equals(currentTarget.getDestination())
		// && pdpModel
		// .getTimeWindowPolicy()
		// .canDeliver(currentTarget.getDeliveryTimeWindow(),
		// time.getStartTime(), currentTarget
		// .getDeliveryDuration())) {
		// pdpModel.deliver(this, currentTarget, time);
		// currentTarget = null;
		// }
		//
		// } else {
		// roadModel.moveTo(this, currentTarget, time);
		//
		// // System.out.println(time.getTime() + " move");
		// if (roadModel.equalPosition(this, currentTarget)
		// && pdpModel
		// .getTimeWindowPolicy()
		// .canPickup(currentTarget.getPickupTimeWindow(), time.getStartTime(),
		// currentTarget.getPickupDuration())
		// && getCapacity() >= pdpModel.getContentsSize(this) +
		// currentTarget.getMagnitude()) {
		//
		// if (time.getTime() > currentTarget.getPickupTimeWindow().end
		// - currentTarget.getPickupDuration()) {
		// System.err.println("pickup tardiness " + currentTarget);
		// }
		// pdpModel.pickup(this, currentTarget, time);
		// currentTarget = null;
		// }
		// }
		// } else if (time.hasTimeLeft()
		// && time.getTime() > dto.availabilityTimeWindow.end
		// - ((Point.distance(roadModel.getPosition(this), dto.startPosition) /
		// getSpeed()) * 3600000)) {
		// // there is nothing left to do and the end of the day is
		// // approaching so we are going back to base
		// roadModel.moveTo(this, dto.startPosition, time);
		// }
		// } catch (final Exception e) {
		// System.out.println(program.toString());
		// throw new RuntimeException(e);
		// }
	}

	protected boolean isEndOfDay(TimeLapse time) {
		return time.hasTimeLeft()
				&& time.getTime() > dto.availabilityTimeWindow.end
						- ((Point.distance(roadModel.getPosition(this), dto.startPosition) / getSpeed()) * 3600000);
	}

	class Idle extends DefaultState<Trigger> {

		@Override
		public void onEvent(Trigger e) {
			if (e == Trigger.CONTINUE && hasChanged) {
				hasChanged = false;
				stateMachine.fire(Trigger.CHANGE);
			}
		}

	}

	class Decide extends DefaultState<Trigger> {

		@Override
		public void onEntry(Trigger e) {
			currentTarget = next(currentTime.getTime());
			if (currentTarget == null) {
				if (isEndOfDay(currentTime)) {
					stateMachine.fire(Trigger.END_OF_DAY);
				} else {
					stateMachine.fire(Trigger.NO_TARGET);
				}
			} else if (isTooEarly(currentTarget, roadModel.getPosition(truck), currentTime.getTime())) {
				stateMachine.fire(Trigger.EARLY);
			} else if (pdpModel.getParcelState(currentTarget) == ParcelState.IN_CARGO) {
				stateMachine.fire(Trigger.IN_CARGO);
			} else {
				stateMachine.fire(Trigger.IS_AVAILABLE);
			}
		}
	}

	class EarlyTarget extends DefaultState<Trigger> {

		@Override
		public void onEvent(Trigger e) {
			if (hasChanged) {
				stateMachine.fire(Trigger.CHANGE);
			} else if (!isTooEarly(currentTarget, roadModel.getPosition(truck), currentTime.getTime())) {
				stateMachine.fire(Trigger.READY);
			}
		}
	}

	class GotoDelivery extends DefaultState<Trigger> {
		@Override
		public void onEntry(Trigger e) {
			move();
		}

		@Override
		public void onEvent(Trigger e) {
			move();
		}

		protected void move() {
			roadModel.moveTo(truck, currentTarget.getDestination(), currentTime);
			if (roadModel.getPosition(truck).equals(currentTarget.getDestination())) {
				stateMachine.fire(Trigger.ARRIVE);
			}
		}
	}

	class GotoPickup extends DefaultState<Trigger> {

		@Override
		public void onEntry(Trigger e) {
			coordinationModel.claim(currentTarget);
			move();
		}

		@Override
		public void onEvent(Trigger e) {
			move();
		}

		protected void move() {
			roadModel.moveTo(truck, currentTarget, currentTime);
			if (roadModel.equalPosition(truck, currentTarget)) {
				stateMachine.fire(Trigger.ARRIVE);
			}
		}

	}

	class Deliver extends DefaultState<Trigger> {
		@Override
		public void onEntry(Trigger t) {
			pdpModel.deliver(truck, currentTarget, currentTime);
			if (currentTime.hasTimeLeft()) {
				stateMachine.fire(Trigger.DONE);
			}
		}

		@Override
		public void onEvent(Trigger t) {
			if (currentTime.hasTimeLeft()) {
				stateMachine.fire(Trigger.DONE);
			}
		}
	}

	class Pickup extends DefaultState<Trigger> {
		@Override
		public void onEntry(Trigger t) {
			pdpModel.pickup(truck, currentTarget, currentTime);
			if (currentTime.hasTimeLeft()) {
				stateMachine.fire(Trigger.DONE);
			}
		}

		@Override
		public void onEvent(Trigger t) {
			if (currentTime.hasTimeLeft()) {
				stateMachine.fire(Trigger.DONE);
			}
		}

	}

	class GoHome extends DefaultState<Trigger> {
		@Override
		public void onEvent(Trigger e) {
			if (e == Trigger.CONTINUE && hasChanged) {
				stateMachine.fire(Trigger.CHANGE);
			} else {
				roadModel.moveTo(truck, dto.startPosition, currentTime);
			}
		}
	}

	public void setCoordinationModel(CoordinationModel cm) {
		coordinationModel = cm;
	}

	public GPProgram<TruckContext> getProgram() {
		return program;
	}

	@Override
	public void initRoadPDP(RoadModel rm, PDPModel pdp) {
		super.initRoadPDP(rm, pdp);
		pdpModel.getEventAPI().addListener(this, PDPModel.PDPModelEventType.NEW_PARCEL);
	}

	static Set<ParcelDTO> convert(Collection<Parcel> parcels) {
		final Set<ParcelDTO> dtos = newLinkedHashSet();
		for (final Parcel p : parcels) {
			dtos.add(((DefaultParcel) p).dto);
		}
		return dtos;
	}

	public void handleEvent(Event e) {
		hasChanged = true;
	}
}
