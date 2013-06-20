/**
 * 
 */
package rinde.evo4mas.gendreau06;

import rinde.evo4mas.gendreau06.comm.Communicator;
import rinde.evo4mas.gendreau06.comm.Communicator.CommunicatorEventType;
import rinde.evo4mas.gendreau06.route.RoutePlanner;
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
import rinde.sim.problem.common.VehicleDTO;
import rinde.sim.util.fsm.AbstractState;
import rinde.sim.util.fsm.StateMachine;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class Truck extends DefaultVehicle implements Listener {

	protected enum TruckEvent {
		DONE;
	}

	protected final StateMachine<TruckEvent, Truck> stateMachine;
	protected final RoutePlanner routePlanner;
	protected final Communicator communicator;
	protected TimeLapse currentTime;
	protected boolean changed;

	/**
	 * @param pDto
	 */
	public Truck(VehicleDTO pDto, RoutePlanner rp, Communicator c) {
		super(pDto);
		routePlanner = rp;
		communicator = c;
		communicator.addUpdateListener(this);

		final AbstractTruckState wait = new Wait();
		final AbstractTruckState go = new Goto();
		final AbstractTruckState service = new Service();
		stateMachine = StateMachine.create(wait)/* */
		.addTransition(wait, TruckEvent.DONE, go)/* */
		.addTransition(go, TruckEvent.DONE, service)/* */
		.addTransition(service, TruckEvent.DONE, wait)/* */
		.build();
	}

	@Override
	public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {
		super.initRoadPDP(pRoadModel, pPdpModel);
		// insert GendreauContextBuilder if needed
		if (routePlanner instanceof GCBuilderReceiver) {
			((GCBuilderReceiver) routePlanner).receive(new GendreauContextBuilder(roadModel, pdpModel, this));
		}
		if (communicator instanceof GCBuilderReceiver) {
			((GCBuilderReceiver) communicator).receive(new GendreauContextBuilder(roadModel, pdpModel, this));
		}
		routePlanner.init(roadModel, pdpModel, this);
	}

	@Override
	protected void tickImpl(TimeLapse time) {
		currentTime = time;
		stateMachine.handle(this);

	}

	protected boolean isTooEarly(Parcel p) {
		final boolean isPickup = pdpModel.getParcelState(p) != ParcelState.IN_CARGO;
		final Point loc = isPickup ? ((DefaultParcel) p).dto.pickupLocation : p.getDestination();
		final long travelTime = (long) ((Point.distance(loc, roadModel.getPosition(this)) / 30d) * 3600000d);
		long timeUntilAvailable = (isPickup ? p.getPickupTimeWindow().begin : p.getDeliveryTimeWindow().begin)
				- currentTime.getStartTime();

		final long remainder = timeUntilAvailable % currentTime.getTimeStep();
		if (remainder > 0) {
			timeUntilAvailable += currentTime.getTimeStep() - remainder;
		}
		return timeUntilAvailable - travelTime > 0;
	}

	protected boolean isEndOfDay() {
		return currentTime.hasTimeLeft()
				&& currentTime.getTime() > dto.availabilityTimeWindow.end
						- ((Point.distance(roadModel.getPosition(this), dto.startPosition) / getSpeed()) * 3600000);
	}

	abstract class AbstractTruckState extends AbstractState<TruckEvent, Truck> {}

	class Wait extends AbstractTruckState {
		@Override
		public TruckEvent handle(TruckEvent event, Truck context) {
			if (changed) {
				changed = false;

				routePlanner.update(communicator.getParcels(), pdpModel.getContents(context), currentTime.getTime());
				communicator.waitFor(routePlanner.current());
			}

			if (routePlanner.current() != null && !isTooEarly(routePlanner.current())) {
				return TruckEvent.DONE;
			}

			if (routePlanner.current() == null && isEndOfDay()
					&& !roadModel.getPosition(context).equals(dto.startPosition)) {
				roadModel.moveTo(context, dto.startPosition, context.currentTime);
			}
			return null;
		}
	}

	class Goto extends AbstractTruckState {
		protected Point destination;

		@Override
		public void onEntry(TruckEvent event, Truck context) {
			if (pdpModel.getParcelState(routePlanner.current()) == ParcelState.IN_CARGO) {
				destination = routePlanner.current().getDestination();
			} else {
				communicator.claim(routePlanner.current());
				destination = roadModel.getPosition(routePlanner.current());
			}
		}

		@Override
		public TruckEvent handle(TruckEvent event, Truck context) {
			// move to service location
			roadModel.moveTo(context, destination, currentTime);
			if (roadModel.getPosition(context).equals(destination)) {
				return TruckEvent.DONE;
			}
			return null;
		}
	}

	class Service extends AbstractTruckState {
		@Override
		public void onEntry(TruckEvent event, Truck context) {
			if (pdpModel.getParcelState(routePlanner.current()) == ParcelState.IN_CARGO) {
				// deliver
				pdpModel.deliver(context, routePlanner.current(), currentTime);
			} else {
				pdpModel.pickup(context, routePlanner.current(), currentTime);
			}
			routePlanner.next(currentTime.getTime());
		}

		@Override
		public TruckEvent handle(TruckEvent event, Truck context) {
			if (context.currentTime.hasTimeLeft()) {
				return TruckEvent.DONE;
			}
			return null;
		}
	}

	public void handleEvent(Event e) {
		if (e.getEventType() == CommunicatorEventType.CHANGE) {
			changed = true;
		}

	}

}
