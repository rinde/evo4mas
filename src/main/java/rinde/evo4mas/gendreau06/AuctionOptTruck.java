/**
 * 
 */
package rinde.evo4mas.gendreau06;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.unmodifiableList;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import rinde.ecj.Heuristic;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.problem.common.VehicleDTO;
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
public class AuctionOptTruck extends AuctionTruck {

	// TODO state machine should be simplified, this truck is mostly just
	// following its plan

	protected Solver solver;
	protected boolean changed;
	protected Queue<Parcel> route;
	protected SolutionObject solutionObject;

	/**
	 * @param pDto
	 * @param p
	 */
	public AuctionOptTruck(VehicleDTO pDto, Heuristic<GendreauContext> p) {
		super(pDto, p);
		solver = SolverValidator.wrap(SolverDebugger.wrap(new MipSolver()));
		route = newLinkedList();
	}

	@Override
	public void receiveParcel(AuctionParcel ap) {
		System.out.println("receive parcel");
		super.receiveParcel(ap);
		changed = true;
	}

	@Override
	protected Parcel next(long time) {

		// if changed, recompute route
		if (changed) {
			changed = false;
			final Collection<Parcel> contents = pdpModel.getContents(this);
			// there are always two locations: the current vehicle location and
			// the depot
			final int numLocations = 2 + (todo.size() * 2) + contents.size();

			if (numLocations == 2) {
				// there are no orders
				return null;
			} else if (todo.size() + contents.size() == 1) {
				// if there is only one order, the solution is trivial
				if (!todo.isEmpty()) {
					return todo.iterator().next();
				} else {
					return contents.iterator().next();
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
		}

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
		return route.poll();
	}

	/**
	 * 
	 * @return A list of Parcels that will be visited in that order. Parcels can
	 *         appear twice in the list, the first occurence is a pickup, the
	 *         second occurence is a delivery. The list is empty if there is no
	 *         route.
	 */
	public List<Parcel> getRoute() {
		final List<Parcel> list = newArrayList();
		if (currentTarget != null) {
			list.add(currentTarget);
		}
		if (!route.isEmpty()) {
			list.addAll(route);
		}
		return unmodifiableList(list);
	}

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
}
