package rinde.evo4mas.gendreau06;

import rinde.ecj.Heuristic;
import rinde.evo4mas.gendreau06.GendreauFunctions.TimeUntilAvailable;
import rinde.evo4mas.gendreau06.HeuristicTruck.TruckEvent;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.PDPModel.ParcelState;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.event.Event;
import rinde.sim.event.Listener;
import rinde.sim.problem.common.VehicleDTO;
import rinde.sim.util.fsm.State;
import rinde.sim.util.fsm.StateMachine;

public abstract class HeuristicTruck extends AbstractHeuristicTruck<TruckEvent, HeuristicTruck> implements Listener {

	protected final TimeUntilAvailable<GendreauContext> tua;
	protected boolean hasChanged = true;
	protected Parcel currentTarget;

	public enum TruckEvent {
		CHANGE, END_OF_DAY, READY, ARRIVE, DONE, CONTINUE, YES, NO;
	}

	protected HeuristicTruck(VehicleDTO pDto, Heuristic<GendreauContext> p, StateMachine<TruckEvent, HeuristicTruck> sm) {
		super(pDto, p, sm);
		tua = new TimeUntilAvailable<GendreauContext>();
	}

	static StateMachine<TruckEvent, HeuristicTruck> createStateMachine(State<TruckEvent, HeuristicTruck> earlyTarget,
			State<TruckEvent, HeuristicTruck> gotoPickup, State<TruckEvent, HeuristicTruck> pickup) {

		final State<TruckEvent, HeuristicTruck> idle = new Idle();
		final State<TruckEvent, HeuristicTruck> hasTarget = new HasTarget();
		final State<TruckEvent, HeuristicTruck> isEarly = new IsEarly();
		final State<TruckEvent, HeuristicTruck> isInCargo = new IsInCargo();
		final State<TruckEvent, HeuristicTruck> gotoDelivery = new GotoDelivery();
		final State<TruckEvent, HeuristicTruck> deliver = new Deliver();
		final State<TruckEvent, HeuristicTruck> goHome = new GoHome();

		return StateMachine.create(idle)/* */
		.addTransition(idle, TruckEvent.CHANGE, hasTarget) /* */
		.addTransition(idle, TruckEvent.END_OF_DAY, goHome) /* */
		.addTransition(hasTarget, TruckEvent.YES, isEarly) /* */
		.addTransition(hasTarget, TruckEvent.NO, idle) /* */
		.addTransition(isEarly, TruckEvent.YES, earlyTarget) /* */
		.addTransition(isEarly, TruckEvent.NO, isInCargo) /* */
		.addTransition(isInCargo, TruckEvent.YES, gotoDelivery) /* */
		.addTransition(isInCargo, TruckEvent.NO, gotoPickup) /* */
		.addTransition(earlyTarget, TruckEvent.CHANGE, hasTarget) /* */
		.addTransition(earlyTarget, TruckEvent.READY, hasTarget) /* */
		.addTransition(gotoDelivery, TruckEvent.ARRIVE, deliver) /* */
		.addTransition(gotoPickup, TruckEvent.ARRIVE, pickup) /* */
		.addTransition(deliver, TruckEvent.DONE, hasTarget) /* */
		.addTransition(pickup, TruckEvent.DONE, hasTarget) /* */
		.addTransition(goHome, TruckEvent.CHANGE, hasTarget) /* */
		.addTransition(goHome, TruckEvent.ARRIVE, idle) /* */
		.build();
	}

	protected abstract Parcel next(long time);

	TimeLapse currentTime;
	HeuristicTruck truck;

	@Override
	protected void tickImpl(TimeLapse time) {
		currentTime = time;
		truck = this;

		if (hasChanged && stateMachine.isSupported(TruckEvent.CHANGE)) {
			stateMachine.handle(TruckEvent.CHANGE, this);
			hasChanged = false;
		} else {
			stateMachine.handle(this);
		}
		// if (time.hasTimeLeft() && !stateMachine.stateIsOneOf(TruckState.IDLE,
		// TruckState.EARLY_TARGET)) {
		// System.err.println(time.getTimeLeft() + " not used");
		// }
	}

	@Override
	protected RoadModel getRoadModel() {
		return roadModel;
	}

	public static class Idle extends AbstractTruckState<TruckEvent, HeuristicTruck> {
		public TruckEvent handle(TruckEvent event, HeuristicTruck context) {
			if (context.isEndOfDay(context.currentTime)
					&& !context.roadModel.getPosition(context).equals(context.dto.startPosition)) {
				return TruckEvent.END_OF_DAY;
			}
			return null;
		}
	}

	public static class HasTarget extends AbstractTruckState<TruckEvent, HeuristicTruck> {
		public TruckEvent handle(TruckEvent event, HeuristicTruck context) {
			context.currentTarget = context.next(context.currentTime.getTime());
			return context.currentTarget != null ? TruckEvent.YES : TruckEvent.NO;
		}
	}

	public static class IsEarly extends AbstractTruckState<TruckEvent, HeuristicTruck> {
		public TruckEvent handle(TruckEvent event, HeuristicTruck context) {
			return context
					.isTooEarly(context.currentTarget, context.roadModel.getPosition(context), context.currentTime) ? TruckEvent.YES
					: TruckEvent.NO;
		}
	}

	public static class EarlyTarget extends AbstractTruckState<TruckEvent, HeuristicTruck> {
		public TruckEvent handle(TruckEvent event, HeuristicTruck context) {
			if (!context
					.isTooEarly(context.currentTarget, context.getRoadModel().getPosition(context.truck), context.currentTime)) {
				return TruckEvent.READY;
			}
			return null;
		}
	}

	public static class IsInCargo extends AbstractTruckState<TruckEvent, HeuristicTruck> {
		public TruckEvent handle(TruckEvent event, HeuristicTruck context) {
			return context.pdpModel.getParcelState(context.currentTarget) == ParcelState.IN_CARGO ? TruckEvent.YES
					: TruckEvent.NO;
		}
	}

	public static class GotoDelivery extends AbstractTruckState<TruckEvent, HeuristicTruck> {
		public TruckEvent handle(TruckEvent event, HeuristicTruck context) {
			context.roadModel.moveTo(context.truck, context.currentTarget.getDestination(), context.currentTime);
			if (context.roadModel.getPosition(context.truck).equals(context.currentTarget.getDestination())) {
				return TruckEvent.ARRIVE;
			}
			return null;
		}
	}

	public static class Deliver extends AbstractTruckState<TruckEvent, HeuristicTruck> {
		@Override
		public void onEntry(TruckEvent event, HeuristicTruck context) {
			context.pdpModel.deliver(context.truck, context.currentTarget, context.currentTime);
		}

		public TruckEvent handle(TruckEvent event, HeuristicTruck context) {
			if (context.currentTime.hasTimeLeft()) {
				return TruckEvent.DONE;
			}
			return null;
		}
	}

	public static class GotoPickup extends AbstractTruckState<TruckEvent, HeuristicTruck> {
		public TruckEvent handle(TruckEvent event, HeuristicTruck context) {
			context.getRoadModel().moveTo(context.truck, context.currentTarget, context.currentTime);
			if (context.getRoadModel().equalPosition(context.truck, context.currentTarget)) {
				return TruckEvent.ARRIVE;
			}
			return null;
		}
	}

	public static class Pickup extends AbstractTruckState<TruckEvent, HeuristicTruck> {
		@Override
		public void onEntry(TruckEvent event, HeuristicTruck context) {
			if (context.pdpModel.getParcelState(context.currentTarget) == ParcelState.AVAILABLE) {
				context.pdpModel.pickup(context.truck, context.currentTarget, context.currentTime);
			} else {
				// if it is not available, we assume it will be next tick, we
				// consume all time to ensure that we stay in this state
				context.currentTime.consumeAll();
			}
		}

		public TruckEvent handle(TruckEvent event, HeuristicTruck context) {
			if (context.currentTime.hasTimeLeft()) {
				return TruckEvent.DONE;
			}
			return null;
		}
	}

	public static class GoHome extends AbstractTruckState<TruckEvent, HeuristicTruck> {
		public TruckEvent handle(TruckEvent event, HeuristicTruck context) {
			context.roadModel.moveTo(context.truck, context.dto.startPosition, context.currentTime);
			if (context.roadModel.getPosition(context.truck).equals(context.dto.startPosition)) {
				return TruckEvent.ARRIVE;
			}
			return null;
		}
	}

	public Heuristic<GendreauContext> getProgram() {
		return program;
	}

	@Override
	public void initRoadPDP(RoadModel rm, PDPModel pdp) {
		super.initRoadPDP(rm, pdp);
		pdpModel.getEventAPI().addListener(this, PDPModel.PDPModelEventType.NEW_PARCEL);
	}

	public void handleEvent(Event e) {
		hasChanged = true;
	}
}
