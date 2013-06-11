/**
 * 
 */
package rinde.evo4mas.gendreau06;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import rinde.ecj.Heuristic;
import rinde.evo4mas.gendreau06.AuctionOptTruck.OptEvent;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel.ParcelState;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.problem.common.VehicleDTO;
import rinde.sim.util.fsm.State;
import rinde.sim.util.fsm.StateMachine;
import rinde.solver.spdptw.SolutionObject;
import rinde.solver.spdptw.Solver;
import rinde.solver.spdptw.SolverDebugger;
import rinde.solver.spdptw.SolverValidator;
import rinde.solver.spdptw.mip.MipSolver;

import com.google.common.primitives.Ints;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class AuctionOptTruck extends AbstractHeuristicTruck<OptEvent, AuctionOptTruck> implements Bidder {

	public enum OptEvent {
		DONE;
	}

	// TODO state machine should be simplified, this truck is mostly just
	// following its plan

	protected Solver solver;
	protected boolean changed;
	protected Queue<Parcel> route;
	protected Set<Parcel> todo;
	protected SolutionObject solutionObject;
	protected TimeLapse currentTime;

	/**
	 * @param pDto
	 * @param p
	 */
	public AuctionOptTruck(VehicleDTO pDto, Heuristic<GendreauContext> p) {
		super(pDto, p, createFSM());
		solver = SolverValidator.wrap(SolverDebugger.wrap(new MipSolver()));
		route = newLinkedList();
		todo = newHashSet();
	}

	static StateMachine<OptEvent, AuctionOptTruck> createFSM() {
		final State<OptEvent, AuctionOptTruck> wait = new Wait();
		final State<OptEvent, AuctionOptTruck> go = new Goto();
		final State<OptEvent, AuctionOptTruck> service = new Service();
		return StateMachine.create(wait)/* */
		.addTransition(wait, OptEvent.DONE, go)/* */
		.addTransition(go, OptEvent.DONE, service)/* */
		.addTransition(service, OptEvent.DONE, wait)/* */
		.build();
	}

	public double getBidFor(AuctionParcel ap, long time) {
		return program.compute(createFullContext(time, ap, false, false));
	}

	public void receiveParcel(AuctionParcel ap) {
		System.out.println("receive parcel");
		todo.add(ap);
		// super.receiveParcel(ap);
		changed = true;
	}

	@Override
	protected void tickImpl(TimeLapse time) {
		currentTime = time;
		stateMachine.handle(this);
	}

	void updateRoute(long time) {
		// if changed, recompute route
		changed = false;
		final Collection<Parcel> contents = pdpModel.getContents(this);
		// there are always two locations: the current vehicle location and
		// the depot
		final int numLocations = 2 + (todo.size() * 2) + contents.size();

		if (numLocations == 2) {
			// there are no orders
			route.clear();
			return;
		} else if (todo.size() + contents.size() == 1) {
			// if there is only one order, the solution is trivial
			if (!todo.isEmpty()) {
				route = newLinkedList(todo);
				return;
			} else {
				route = newLinkedList(contents);
				return;
			}
		}
		// else, we are going to look for the optimal solution

		final int[][] travelTime = new int[numLocations][numLocations];
		final int[] releaseDates = new int[numLocations];
		final int[] dueDates = new int[numLocations];
		final int[][] servicePairs = new int[todo.size()][2];
		int serviceTime = -1;

		final Map<Point, Parcel> point2parcel = newHashMap();
		final Point[] locations = new Point[numLocations];
		locations[0] = roadModel.getPosition(this);

		int index = 1;
		int spIndex = 0;
		for (final Parcel p : todo) {
			if (serviceTime < 0) {
				serviceTime = Ints.checkedCast(p.getDeliveryDuration() / 1000);
			}

			// add pickup location and time window
			locations[index] = pdpModel.getPosition(p);
			point2parcel.put(locations[index], p);
			releaseDates[index] = fixTWstart(p.getPickupTimeWindow().begin, time);
			dueDates[index] = fixTWend(p.getPickupTimeWindow().end, time);

			index++;

			// add delivery location and time window
			locations[index] = p.getDestination();
			point2parcel.put(locations[index], p);
			releaseDates[index] = fixTWstart(p.getDeliveryTimeWindow().begin, time);
			dueDates[index] = fixTWend(p.getDeliveryTimeWindow().end, time);

			servicePairs[spIndex++] = new int[] { index - 1, index };

			index++;
		}
		checkState(spIndex == todo.size(), "%s %s", todo.size(), spIndex);

		for (final Parcel p : contents) {
			if (serviceTime < 0) {
				serviceTime = Ints.checkedCast(p.getDeliveryDuration() / 1000);
			}

			// FIXME these four lines are identical to the second set of
			// four lines in the loop above :(
			locations[index] = p.getDestination();
			point2parcel.put(locations[index], p);
			releaseDates[index] = fixTWstart(p.getDeliveryTimeWindow().begin, time);
			dueDates[index] = fixTWend(p.getDeliveryTimeWindow().end, time);
			index++;
		}
		checkState(index == numLocations - 1);

		// the start position of the truck is the depot
		locations[index] = dto.startPosition;
		// end of the day
		dueDates[index] = fixTWend(dto.availabilityTimeWindow.end, time);

		// fill the distance matrix
		for (int i = 0; i < numLocations; i++) {
			for (int j = 0; j < i; j++) {
				if (i != j) {
					final double dist = Point.distance(locations[i], locations[j]);
					// travel times are ceiled
					final int tt = (int) Math.ceil((dist / dto.speed) * 3600.0);
					travelTime[i][j] = tt;
					travelTime[j][i] = tt;
				}
			}
		}

		final SolutionObject sol = solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTime);

		final Queue<Parcel> newRoute = newLinkedList();
		// ignore first (current pos) and last (depot)
		for (int i = 1; i < sol.route.length - 1; i++) {
			newRoute.add(point2parcel.get(locations[sol.route[i]]));
		}
		route = newRoute;
		solutionObject = sol;

		// when too early the next() method can be called repeatedly, parcels
		// which are not yet handled should not be removed until operation is
		// complete.

		// final Parcel cur = route.peek();
		// if (pdpModel.getParcelState(cur) == ParcelState.ANNOUNCED
		// || pdpModel.getParcelState(cur) == ParcelState.AVAILABLE) {
		// return cur;
		// }
		// else if( pdpModel.getParcelState(cur) ==)

		// returns and removes the Parcel that will be visited next, returns
		// null when empty
		// return route.poll();
	}

	/**
	 * 
	 * @return A list of Parcels that will be visited in that order. Parcels can
	 *         appear twice in the list, the first occurence is a pickup, the
	 *         second occurence is a delivery. The list is empty if there is no
	 *         route.
	 */
	// public List<Parcel> getRoute() {
	// final List<Parcel> list = newArrayList();
	// if (currentTarget != null) {
	// list.add(currentTarget);
	// }
	// if (!route.isEmpty()) {
	// list.addAll(route);
	// }
	// return unmodifiableList(list);
	// }

	/**
	 * @return A copy of the {@link SolutionObject} that was used to find the
	 *         current route. Or <code>null</code> if no {@link Solver} was used
	 *         to find the current route (or there is no route).
	 */
	public SolutionObject getSolutionObject() {
		if (solutionObject == null) {
			return null;
		}
		return new SolutionObject(solutionObject.route, solutionObject.arrivalTimes, solutionObject.objectiveValue);
	}

	static int fixTWstart(long start, long time) {
		return (int) Math.max((Math.ceil(Ints.checkedCast(start - time) / 1000)), 0);
	}

	static int fixTWend(long end, long time) {
		return (int) Math.max((Math.floor(Ints.checkedCast(end - time) / 1000)), 0);
	}

	public static class Wait extends AbstractTruckState<OptEvent, AuctionOptTruck> {
		public OptEvent handle(OptEvent event, AuctionOptTruck context) {

			if (context.changed) {
				context.changed = false;
				context.updateRoute(context.currentTime.getTime());
			}

			if (!context.route.isEmpty()
					&& !context
							.isTooEarly(context.route.peek(), context.roadModel.getPosition(context), context.currentTime)) {
				return OptEvent.DONE;
			}

			if (context.route.isEmpty() && context.isEndOfDay(context.currentTime)
					&& !context.roadModel.getPosition(context).equals(context.dto.startPosition)) {
				context.roadModel.moveTo(context, context.dto.startPosition, context.currentTime);
			}

			return null;
		}
	}

	public static class Goto extends AbstractTruckState<OptEvent, AuctionOptTruck> {
		public OptEvent handle(OptEvent event, AuctionOptTruck context) {
			if (context.pdpModel.getParcelState(context.route.peek()) == ParcelState.IN_CARGO) {
				// move to deliver location
				context.roadModel.moveTo(context, context.route.peek().getDestination(), context.currentTime);
				if (context.roadModel.getPosition(context).equals(context.route.peek().getDestination())) {
					return OptEvent.DONE;
				}
			} else {
				// move to pickup location
				context.roadModel.moveTo(context, context.route.peek(), context.currentTime);
				if (context.roadModel.equalPosition(context, context.route.peek())) {
					return OptEvent.DONE;
				}
			}
			return null;
		}
	}

	public static class Service extends AbstractTruckState<OptEvent, AuctionOptTruck> {
		private boolean isPickup;

		@Override
		public void onEntry(OptEvent event, AuctionOptTruck context) {
			if (context.pdpModel.getParcelState(context.route.peek()) == ParcelState.IN_CARGO) {
				// deliver
				context.pdpModel.deliver(context, context.route.peek(), context.currentTime);
			} else {
				context.pdpModel.pickup(context, context.route.peek(), context.currentTime);
			}
			context.route.remove();
		}

		public OptEvent handle(OptEvent event, AuctionOptTruck context) {
			if (context.currentTime.hasTimeLeft()) {
				return OptEvent.DONE;
			}
			return null;
		}
	}

}
