/**
 * 
 */
package rinde.evo4mas.gendreau06.route;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.annotation.Nullable;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.problem.common.DefaultVehicle;
import rinde.solver.spdptw.SolutionObject;
import rinde.solver.spdptw.Solver;

import com.google.common.primitives.Ints;

/**
 * A {@link RoutePlanner} implementation that uses a {@link Solver} that
 * computes a complete route each time
 * {@link #update(Collection, Collection, long)} is called.
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public class SolverRoutePlanner extends AbstractRoutePlanner {

    protected final Solver solver;
    protected Queue<Parcel> route;
    @Nullable
    protected SolutionObject solutionObject;

    /**
     * Create a route planner that uses the specified {@link Solver} to compute
     * the best route.
     * @param s {@link Solver} used for route planning.
     */
    public SolverRoutePlanner(Solver s) {
        solver = s;
        route = newLinkedList();
    }

    @Override
    protected void doUpdate(Collection<Parcel> onMap,
            Collection<Parcel> inCargo, long time) {
        // there are always two locations: the current vehicle location and
        // the depot
        final int numLocations = 2 + (onMap.size() * 2) + inCargo.size();

        if (numLocations == 2) {
            // there are no orders
            route.clear();
            return;
        } else if (onMap.size() + inCargo.size() == 1) {
            // if there is only one order, the solution is trivial
            if (!onMap.isEmpty()) {
                // parcels on the map require two visits (one for pickup, one
                // for delivery)
                route = newLinkedList(onMap);
                route.addAll(onMap);
                return;
            } // else
            route = newLinkedList(inCargo);
            return;

        }
        // else, we are going to look for the optimal solution

        final RoadModel rm = roadModel;
        final PDPModel pm = pdpModel;
        final DefaultVehicle v = vehicle;
        checkState(rm != null && pm != null && v != null);

        final int[][] travelTime = new int[numLocations][numLocations];
        final int[] releaseDates = new int[numLocations];
        final int[] dueDates = new int[numLocations];
        final int[][] servicePairs = new int[onMap.size()][2];
        final int[] serviceTimes = new int[numLocations];

        final Map<Point, Parcel> point2parcel = newHashMap();
        final Point[] locations = new Point[numLocations];
        locations[0] = rm.getPosition(v);

        int index = 1;
        int spIndex = 0;
        for (final Parcel p : onMap) {
            // add pickup location and time window
            locations[index] = pm.getPosition(p);
            point2parcel.put(locations[index], p);
            releaseDates[index] = fixTWstart(p.getPickupTimeWindow().begin, time);
            dueDates[index] = fixTWend(p.getPickupTimeWindow().end, time);
            serviceTimes[index] = Ints
                    .checkedCast(p.getPickupDuration() / 1000);

            // link the pair with its delivery location (see next loop)
            servicePairs[spIndex++] = new int[] { index, index + onMap.size() };

            index++;
        }
        checkState(spIndex == onMap.size(), "%s %s", onMap.size(), spIndex);

        final List<Parcel> deliveries = newArrayListWithCapacity(onMap.size()
                + inCargo.size());
        deliveries.addAll(onMap);
        deliveries.addAll(inCargo);
        for (final Parcel p : deliveries) {
            serviceTimes[index] = Ints
                    .checkedCast(p.getDeliveryDuration() / 1000);

            locations[index] = p.getDestination();
            point2parcel.put(locations[index], p);
            releaseDates[index] = fixTWstart(p.getDeliveryTimeWindow().begin, time);
            dueDates[index] = fixTWend(p.getDeliveryTimeWindow().end, time);
            index++;
        }
        checkState(index == numLocations - 1);

        // the start position of the truck is the depot
        locations[index] = v.getDTO().startPosition;
        // end of the day
        dueDates[index] = fixTWend(v.getDTO().availabilityTimeWindow.end, time);

        // fill the distance matrix
        for (int i = 0; i < numLocations; i++) {
            for (int j = 0; j < i; j++) {
                if (i != j) {
                    final double dist = Point
                            .distance(locations[i], locations[j]);
                    // travel times are ceiled
                    final int tt = (int) Math
                            .ceil((dist / v.getDTO().speed) * 3600.0);
                    travelTime[i][j] = tt;
                    travelTime[j][i] = tt;
                }
            }
        }

        final SolutionObject sol = solver
                .solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);

        final Queue<Parcel> newRoute = newLinkedList();
        // ignore first (current pos) and last (depot)
        for (int i = 1; i < sol.route.length - 1; i++) {
            newRoute.add(point2parcel.get(locations[sol.route[i]]));
        }
        route = newRoute;
        solutionObject = sol;
    }

    /**
     * @return A copy of the {@link SolutionObject} that was used to find the
     *         current route. Or <code>null</code> if no {@link Solver} was used
     *         to find the current route (or there is no route).
     */
    @Nullable
    public SolutionObject getSolutionObject() {
        final SolutionObject so = solutionObject;
        if (so != null) {
            return new SolutionObject(so.route, so.arrivalTimes,
                    so.objectiveValue);
        }
        return null;
    }

    public boolean hasNext() {
        return !route.isEmpty();
    }

    static int fixTWstart(long start, long time) {
        return (int) Math
                .max((Math.ceil(Ints.checkedCast(start - time) / 1000)), 0);
    }

    static int fixTWend(long end, long time) {
        return (int) Math
                .max((Math.floor(Ints.checkedCast(end - time) / 1000)), 0);
    }

    @Nullable
    public Parcel current() {
        return route.peek();
    }

    @Override
    protected void nextImpl(long time) {
        route.poll();
    }

}
