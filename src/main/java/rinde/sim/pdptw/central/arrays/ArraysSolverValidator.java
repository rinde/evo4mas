package rinde.sim.pdptw.central.arrays;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ranges;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

/**
 * Provides methods for validating input to and output from
 * {@link SingleVehicleArraysSolver}s and {@link MultiVehicleArraysSolver}. Also
 * provides <code>wrap(..)</code> methods which decorates any solver such that
 * both inputs and outputs are validated every time <code>solve(..)</code> is
 * called.
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public final class ArraysSolverValidator {

    private ArraysSolverValidator() {}

    /**
     * Decorates the original {@link SingleVehicleArraysSolver} such that both
     * the inputs to the solver and the outputs from the solver are validated.
     * When an invalid input or output is detected a
     * {@link IllegalArgumentException is thrown}.
     * @param delegate The {@link SingleVehicleArraysSolver} that will be used
     *            for the actual solving.
     * @return The wrapped solver.
     */
    public static SingleVehicleArraysSolver wrap(
            SingleVehicleArraysSolver delegate) {
        return new SingleValidator(delegate);
    }

    /**
     * Decorates the original {@link MultiVehicleArraysSolver} such that both
     * the inputs to the solver and the outputs from the solver are validated.
     * When an invalid input or output is detected a
     * {@link IllegalArgumentException is thrown}.
     * @param delegate The {@link MultiVehicleArraysSolver} that will be used
     *            for the actual solving.
     * @return The wrapped solver.
     */
    public static MultiVehicleArraysSolver wrap(
            MultiVehicleArraysSolver delegate) {
        return new MultiValidator(delegate);
    }

    /**
     * Validates the inputs for the {@link SingleVehicleArraysSolver}. This
     * method checks all properties as defined in
     * {@link SingleVehicleArraysSolver#solve(int[][], int[], int[], int[][], int[])}
     * . If the inputs are not correct an {@link IllegalArgumentException} is
     * thrown.
     * @param travelTime Parameter as specified by
     *            {@link SingleVehicleArraysSolver#solve(int[][], int[], int[], int[][], int[])}
     *            .
     * @param releaseDates Parameter as specified by
     *            {@link SingleVehicleArraysSolver#solve(int[][], int[], int[], int[][], int[])}
     *            .
     * @param dueDates Parameter as specified by
     *            {@link SingleVehicleArraysSolver#solve(int[][], int[], int[], int[][], int[])}
     *            .
     * @param servicePairs Parameter as specified by
     *            {@link SingleVehicleArraysSolver#solve(int[][], int[], int[], int[][], int[])}
     *            .
     * @param serviceTimes Parameter as specified by
     *            {@link SingleVehicleArraysSolver#solve(int[][], int[], int[], int[][], int[])}
     *            .
     */
    public static void validateInputs(int[][] travelTime, int[] releaseDates,
            int[] dueDates, int[][] servicePairs, int[] serviceTimes) {

        final int n = travelTime.length;
        checkArgument(n > 0, "Travel time matrix cannot be empty");
        // check that matrix is n x n
        for (int i = 0; i < n; i++) {
            checkArgument(travelTime[i].length == n, "row %s has invalid length %s", i, travelTime[i].length);
        }
        checkArgument(releaseDates.length == n, "ReleaseDates array has incorrect length (%s) should be %s", releaseDates.length, n);
        checkArgument(dueDates.length == n, "dueDates array has incorrect length (%s) should be %s", dueDates.length, n);
        checkArgument(serviceTimes.length == n, "serviceTimes has incorrect length (%s) should be %s", serviceTimes.length, n);
        for (int i = 0; i < n; i++) {
            checkArgument(serviceTimes[i] >= 0, "serviceTimes should be >= 0");
        }

        // check time windows validity
        for (int i = 0; i < n; i++) {
            checkArgument(releaseDates[i] <= dueDates[i], "Index %s, release date (%s) should always be before the due date (%s)", i, releaseDates[i], dueDates[i]);
        }

        checkArgument(releaseDates[0] == 0 && dueDates[0] == 0, "Start location should have release date and due date 0");
        checkArgument(releaseDates[n - 1] == 0, "Depot should have release date 0");

        // check that every pair consists of valid ids and that a location is in
        // only one pair
        final Set<Integer> set = newHashSet();
        for (int i = 0; i < servicePairs.length; i++) {
            checkArgument(servicePairs[i].length == 2, "Each pair entry should consist of exactly two locations.");
            for (int j = 0; j < 2; j++) {
                checkArgument(servicePairs[i][j] > 0
                        && servicePairs[i][j] < n - 1, "Pair consists of an invalid location (start location and depot are not allowed), index is %s, location is %s", i, servicePairs[i][j]);
                checkArgument(!set.contains(servicePairs[i][j]), "Location can be part of only one pair, duplicate location: %s (index %s,%s)", servicePairs[i][j], i, j);
                set.add(servicePairs[i][j]);
            }
        }
    }

    /**
     * Validates the inputs for the {@link MultiVehicleArraysSolver}. This
     * method checks all properties as defined in
     * {@link MultiVehicleArraysSolver#solve(int[][], int[], int[], int[][], int[], int[][], int[][], int[])}
     * . If the inputs are not correct an {@link IllegalArgumentException} is
     * thrown.
     * @param travelTime Parameter as specified by
     *            {@link MultiVehicleArraysSolver#solve(int[][], int[], int[], int[][], int[], int[][], int[][], int[])}
     *            .
     * @param releaseDates Parameter as specified by
     *            {@link MultiVehicleArraysSolver#solve(int[][], int[], int[], int[][], int[], int[][], int[][], int[])}
     *            .
     * @param dueDates Parameter as specified by
     *            {@link MultiVehicleArraysSolver#solve(int[][], int[], int[], int[][], int[], int[][], int[][], int[])}
     *            .
     * @param servicePairs Parameter as specified by
     *            {@link MultiVehicleArraysSolver#solve(int[][], int[], int[], int[][], int[], int[][], int[][], int[])}
     *            .
     * @param serviceTimes Parameter as specified by
     *            {@link MultiVehicleArraysSolver#solve(int[][], int[], int[], int[][], int[], int[][], int[][], int[])}
     *            .
     * @param vehicleTravelTimes Parameter as specified by
     *            {@link MultiVehicleArraysSolver#solve(int[][], int[], int[], int[][], int[], int[][], int[][], int[])}
     *            .
     * @param inventories Parameter as specified by
     *            {@link MultiVehicleArraysSolver#solve(int[][], int[], int[], int[][], int[], int[][], int[][], int[])}
     *            .
     * @param remainingServiceTimes Parameter as specified by
     *            {@link MultiVehicleArraysSolver#solve(int[][], int[], int[], int[][], int[], int[][], int[][], int[])}
     *            .
     */
    public static void validateInputs(int[][] travelTime, int[] releaseDates,
            int[] dueDates, int[][] servicePairs, int[] serviceTimes,
            int[][] vehicleTravelTimes, int[][] inventories,
            int[] remainingServiceTimes) {

        validateInputs(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);

        // number of vehicles v
        final int v = vehicleTravelTimes.length;
        final int n = travelTime.length;
        checkArgument(v > 0, "At least one vehicle is required.");

        for (int i = 0; i < v; i++) {
            checkArgument(n == vehicleTravelTimes[i].length, "We expected vehicleTravelTimes matrix of size v x %s, but we found v x %s at index %s.", n, vehicleTravelTimes[i].length, i);
            for (int j = 0; j < n; j++) {
                checkArgument(vehicleTravelTimes[i][j] >= 0, "Found an invalid vehicle travel time (%s) at position %s,%s. All times must be >= 0.", vehicleTravelTimes[i][j], i, j);
            }
        }

        final ImmutableSet.Builder<Integer> b = ImmutableSet.builder();
        for (int i = 0; i < servicePairs.length; i++) {
            b.add(servicePairs[i][0]);
            b.add(servicePairs[i][1]);
        }
        final Set<Integer> availLocs = b.build();

        final int m = n - 2 - (servicePairs.length * 2);
        checkArgument(inventories.length == m, "Invalid number of inventory entries, expected %s found %s.", m, servicePairs.length);

        final Set<Integer> parcelsInInventory = newHashSet();
        for (int i = 0; i < m; i++) {
            checkArgument(2 == inventories[i].length, "We expected inventories matrix of size m x 2, but we found m x %s at index %s.", inventories[i].length, i);
            checkArgument(inventories[i][0] >= 0 && inventories[i][0] < v, "Found a reference to a non-existing vehicle (%s) at row %s.", inventories[i][0], i);
            checkArgument(inventories[i][1] >= 1 && inventories[i][1] < n - 1, "Found a reference to a non-existing location (%s) at row %s.", inventories[i][1], i);
            checkArgument(!availLocs.contains(inventories[i][1]), "Found a reference to a location (%s) at row %s which is available, as such, it can not be in the inventory.", inventories[i][1], i);
            checkArgument(!parcelsInInventory.contains(inventories[i][1]), "Found a duplicate inventory entry, first duplicate at row %s.", i);
            parcelsInInventory.add(inventories[i][1]);
        }

        checkArgument(v == remainingServiceTimes.length, "We expected remainingServiceTimes array of size %s, but we found %s.", v, remainingServiceTimes.length);
        for (int i = 0; i < v; i++) {
            checkArgument(remainingServiceTimes[i] >= 0, "Remaining service time must be >= 0, found %s.", remainingServiceTimes[i]);
        }
    }

    /**
     * Validates the {@link SolutionObject} that is produced by a
     * {@link SingleVehicleArraysSolver} . If the {@link SolutionObject} is
     * infeasible, an {@link IllegalArgumentException} is thrown.
     * @param sol The {@link SolutionObject} that is validated.
     * @param travelTime Parameter as specified by
     *            {@link SingleVehicleArraysSolver#solve(int[][], int[], int[], int[][], int[])}
     *            .
     * @param releaseDates Parameter as specified by
     *            {@link SingleVehicleArraysSolver#solve(int[][], int[], int[], int[][], int[])}
     *            .
     * @param dueDates Parameter as specified by
     *            {@link SingleVehicleArraysSolver#solve(int[][], int[], int[], int[][], int[])}
     *            .
     * @param servicePairs Parameter as specified by
     *            {@link SingleVehicleArraysSolver#solve(int[][], int[], int[], int[][], int[])}
     *            .
     * @param serviceTimes Parameter as specified by
     *            {@link SingleVehicleArraysSolver#solve(int[][], int[], int[], int[][], int[])}
     *            .
     * @return The solution as is supplied, used for method chaining.
     */
    public static SolutionObject validateOutputs(SolutionObject sol,
            int[][] travelTime, int[] releaseDates, int[] dueDates,
            int[][] servicePairs, int[] serviceTimes) {
        // convert single vehicle version to multi vehicle version for checking
        // of inputs
        final int n = travelTime.length;
        final int[][] vehicleTravelTimes = new int[1][n];
        // copy first row
        for (int i = 0; i < n; i++) {
            vehicleTravelTimes[0][i] = travelTime[0][i];
        }
        final Set<Integer> locationSet = newHashSet(Ranges.closedOpen(1, n - 1)
                .asSet(DiscreteDomains.integers()));
        for (int i = 0; i < servicePairs.length; i++) {
            locationSet.remove(servicePairs[i][0]);
            locationSet.remove(servicePairs[i][1]);
        }

        final int[][] inventories = new int[locationSet.size()][2];
        final Iterator<Integer> locationSetIterator = locationSet.iterator();
        for (int i = 0; i < locationSet.size(); i++) {
            inventories[i][0] = 0;// vehicle 0
            inventories[i][1] = locationSetIterator.next();
        }

        final int[] remainingServiceTimes = new int[] { 0 };
        // check inputs again since we just modified them
        validateInputs(travelTime, releaseDates, dueDates, servicePairs, serviceTimes, vehicleTravelTimes, inventories, remainingServiceTimes);

        final SolutionObject[] sols = new SolutionObject[] { sol };
        validateOutputs(sols, travelTime, releaseDates, dueDates, servicePairs, serviceTimes, vehicleTravelTimes, inventories, remainingServiceTimes);
        return sol;
    }

    /**
     * Validates the {@link SolutionObject}s that are produced by a
     * {@link MultiVehicleArraysSolver}. If any of the {@link SolutionObject}s
     * is infeasible, an {@link IllegalArgumentException} is thrown.
     * @param sols The {@link SolutionObject}s that are validated.
     * @param travelTime Parameter as specified by
     *            {@link MultiVehicleArraysSolver#solve(int[][], int[], int[], int[][], int[], int[][], int[][], int[])}
     *            .
     * @param releaseDates Parameter as specified by
     *            {@link MultiVehicleArraysSolver#solve(int[][], int[], int[], int[][], int[], int[][], int[][], int[])}
     *            .
     * @param dueDates Parameter as specified by
     *            {@link MultiVehicleArraysSolver#solve(int[][], int[], int[], int[][], int[], int[][], int[][], int[])}
     *            .
     * @param servicePairs Parameter as specified by
     *            {@link MultiVehicleArraysSolver#solve(int[][], int[], int[], int[][], int[], int[][], int[][], int[])}
     *            .
     * @param serviceTimes Parameter as specified by
     *            {@link MultiVehicleArraysSolver#solve(int[][], int[], int[], int[][], int[], int[][], int[][], int[])}
     *            .
     * @param vehicleTravelTimes Parameter as specified by
     *            {@link MultiVehicleArraysSolver#solve(int[][], int[], int[], int[][], int[], int[][], int[][], int[])}
     *            .
     * @param inventories Parameter as specified by
     *            {@link MultiVehicleArraysSolver#solve(int[][], int[], int[], int[][], int[], int[][], int[][], int[])}
     *            .
     * @param remainingServiceTimes Parameter as specified by
     *            {@link MultiVehicleArraysSolver#solve(int[][], int[], int[], int[][], int[], int[][], int[][], int[])}
     *            .
     * @return The solution as is supplied, used for method chaining.
     */
    public static SolutionObject[] validateOutputs(SolutionObject[] sols,
            int[][] travelTime, int[] releaseDates, int[] dueDates,
            int[][] servicePairs, int[] serviceTimes,
            int[][] vehicleTravelTimes, int[][] inventories,
            int[] remainingServiceTimes) {

        final int n = travelTime.length;

        final ImmutableSet.Builder<Integer> routeSetBuilder = ImmutableSet
                .builder();
        int visitedLocations = 0;
        for (final SolutionObject sol : sols) {
            routeSetBuilder.addAll(toSet(sol.route));
            visitedLocations += sol.route.length - 2;
        }
        final Set<Integer> routeSet = routeSetBuilder.build();
        final Set<Integer> locationSet = Ranges
                .closedOpen(0, travelTime.length)
                .asSet(DiscreteDomains.integers());

        checkArgument(visitedLocations == n - 2, "The number of visits in routes should equal the number of locations.");

        // checks duplicates and missing locations
        checkArgument(routeSet.size() == n, "Every location should appear exactly once in one route. Missing location: %s.", Sets
                .difference(locationSet, routeSet));
        // checks for completeness of tour
        checkArgument(routeSet.equals(locationSet), "Not all locations are serviced, there is probably a non-existing location in the route. Set difference: %s.", Sets
                .difference(routeSet, locationSet));

        final ImmutableMultimap.Builder<Integer, Integer> inventoryBuilder = ImmutableMultimap
                .builder();
        for (int i = 0; i < inventories.length; i++) {
            inventoryBuilder.put(inventories[i][0], inventories[i][1]);
        }
        final Multimap<Integer, Integer> inventoryMap = inventoryBuilder
                .build();

        for (int v = 0; v < sols.length; v++) {
            final SolutionObject sol = sols[v];

            /*
             * CHECK SERVICE SEQUENCE
             */
            // checkArgument(sol.route.length == n,
            // "The route should always contain all locations.");
            checkArgument(sol.route[0] == 0, "The route should always start with the vehicle start location (0).");
            checkArgument(sol.route[sol.route.length - 1] == n - 1, "The route should always finish with the depot.");

            final Set<Integer> locs = ImmutableSet.copyOf(Ints
                    .asList(sol.route));
            final Collection<Integer> inventory = inventoryMap.get(v);
            for (final Integer i : inventory) {
                checkArgument(locs.contains(i), "Every location in the inventory of a vehicle should occur in its route, route for vehicle %s does not contain location %s.", v, i);
            }

            // check service pairs ordering, pickups should be visited before
            // their corresponding delivery location
            final Map<Integer, Integer> pairs = newHashMap();
            for (int i = 0; i < servicePairs.length; i++) {
                pairs.put(servicePairs[i][0], servicePairs[i][1]);
            }
            final Set<Integer> seen = newHashSet();
            for (int i = 1; i < n - 1; i++) {
                if (pairs.containsKey(sol.route[i])) {
                    checkArgument(!seen.contains(pairs.get(sol.route[i])), "Pickups should be visited before their corresponding deliveries. Location %s should be visited after location %s.", pairs
                            .get(sol.route[i]), sol.route[i]);
                }
                seen.add(sol.route[i]);
            }

            /*
             * CHECK ARRIVAL TIMES
             */
            checkArgument(sol.arrivalTimes.length == sol.route.length, "Number of arrival times should equal number of locations.");
            checkArgument(sol.arrivalTimes[0] == remainingServiceTimes[v], "The first arrival time should be the remaining service time for this vehicle, expected %s, was %s.", remainingServiceTimes[v], sol.arrivalTimes[0]);

            // check feasibility
            for (int i = 1; i < sol.route.length; i++) {
                final int prev = sol.route[i - 1];
                final int cur = sol.route[i];

                // we compute the travel time. If it is the first step in the
                // route, we use the time from vehicle location to the next
                // location in the route.
                final int tt = i == 1 ? vehicleTravelTimes[v][cur]
                        : travelTime[prev][cur];

                // we compute the first possible arrival time for the vehicle to
                // arrive at location i, given that it first visited location
                // i-1
                final int earliestArrivalTime = sol.arrivalTimes[i - 1]
                        + serviceTimes[prev] + tt;

                // we also have to take into account the time window
                final int minArrivalTime = Math
                        .max(earliestArrivalTime, releaseDates[cur]);

                checkArgument(sol.arrivalTimes[i] >= minArrivalTime, "Vehicle %s, route index %s, arrivalTime (%s) needs to be greater or equal to minArrivalTime (%s).", v, i, sol.arrivalTimes[i], minArrivalTime);
            }

            /*
             * CHECK OBJECTIVE VALUE
             */

            // sum travel time
            int totalTravelTime = 0;
            for (int i = 1; i < sol.route.length; i++) {
                if (i == 1) {
                    totalTravelTime += vehicleTravelTimes[v][sol.route[i]];
                } else {
                    totalTravelTime += travelTime[sol.route[i - 1]][sol.route[i]];
                }
            }

            // sum tardiness
            int tardiness = 0;
            for (int i = 0; i < sol.route.length; i++) {
                final int lateness = (sol.arrivalTimes[i] + serviceTimes[sol.route[i]])
                        - dueDates[sol.route[i]];
                if (lateness > 0) {
                    tardiness += lateness;
                }
            }
            checkArgument(sol.objectiveValue == totalTravelTime + tardiness, "Incorrect objective value (%s), it should be travel time + tardiness = %s + %s = %s.", sol.objectiveValue, totalTravelTime, tardiness, totalTravelTime
                    + tardiness);

        }

        return sols;
    }

    static Set<Integer> toSet(int[] arr) {
        final Set<Integer> set = newHashSet();
        for (int i = 0; i < arr.length; i++) {
            set.add(arr[i]);
        }
        return set;
    }

    private static class SingleValidator implements SingleVehicleArraysSolver {
        private final SingleVehicleArraysSolver delegateSolver;

        private SingleValidator(SingleVehicleArraysSolver delegate) {
            delegateSolver = delegate;
        }

        public SolutionObject solve(int[][] travelTime, int[] releaseDates,
                int[] dueDates, int[][] servicePairs, int[] serviceTimes) {
            // first check inputs
            validateInputs(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
            // execute solver
            final SolutionObject output = delegateSolver
                    .solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
            // check outputs
            return validateOutputs(output, travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
        }
    }

    private static class MultiValidator implements MultiVehicleArraysSolver {
        private final MultiVehicleArraysSolver delegateSolver;

        private MultiValidator(MultiVehicleArraysSolver delegate) {
            delegateSolver = delegate;
        }

        public SolutionObject[] solve(int[][] travelTime, int[] releaseDates,
                int[] dueDates, int[][] servicePairs, int[] serviceTimes,
                int[][] vehicleTravelTimes, int[][] inventories,
                int[] remainingServiceTimes) {
            validateInputs(travelTime, releaseDates, dueDates, servicePairs, serviceTimes, vehicleTravelTimes, inventories, remainingServiceTimes);
            final SolutionObject[] output = delegateSolver
                    .solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes, vehicleTravelTimes, inventories, remainingServiceTimes);
            return validateOutputs(output, travelTime, releaseDates, dueDates, servicePairs, serviceTimes, vehicleTravelTimes, inventories, remainingServiceTimes);
        }
    }
}
