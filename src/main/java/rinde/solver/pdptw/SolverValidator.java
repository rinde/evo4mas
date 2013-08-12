package rinde.solver.pdptw;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ranges;

/**
 * Provides methods for validating input to {@link SingleVehicleArraysSolver}s
 * and for validating output from {@link SingleVehicleArraysSolver}s. Also
 * provides a {@link #wrap(SingleVehicleArraysSolver)} method which wraps any
 * solver such that both inputs and outputs are validated every time
 * {@link SingleVehicleArraysSolver#solve(int[][], int[], int[], int[][], int[])}
 * is called.
 * 
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public final class SolverValidator {

    private SolverValidator() {}

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
            return validate(output, travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
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
            return validate(output, travelTime, releaseDates, dueDates, servicePairs, serviceTimes, vehicleTravelTimes, inventories, remainingServiceTimes);
        }
    }

    /**
     * Wraps the original {@link SingleVehicleArraysSolver} such that both the
     * inputs to the solver and the outputs from the solver are validated. When
     * an invalid input or output is detected a {@link IllegalArgumentException
     * is thrown}.
     * @param delegate The {@link SingleVehicleArraysSolver} that will be used
     *            for the actual solving.
     * @return The wrapped solver.
     */
    public static SingleVehicleArraysSolver wrap(
            SingleVehicleArraysSolver delegate) {
        return new SingleValidator(delegate);
    }

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
    public static SolutionObject validate(SolutionObject sol,
            int[][] travelTime, int[] releaseDates, int[] dueDates,
            int[][] servicePairs, int[] serviceTimes) {
        final int n = travelTime.length;
        /*
         * CHECK SERVICE SEQUENCE
         */
        checkArgument(sol.route.length == n, "The route should always contain all locations.");
        checkArgument(sol.route[0] == 0, "The route should always start with the vehicle start location (0).");
        checkArgument(sol.route[n - 1] == n - 1, "The route should always finish with the depot.");

        final Set<Integer> routeSet = toSet(sol.route);
        final Set<Integer> locationSet = Ranges
                .closedOpen(0, travelTime.length)
                .asSet(DiscreteDomains.integers());

        // checks duplicates
        checkArgument(routeSet.size() == n, "Every location in route should appear exactly once.");
        // checks for completeness of tour
        checkArgument(routeSet.equals(locationSet), "Not all locations are serviced, there is probably a non-existing location in the route.");

        // check service pairs ordering, pickups shoud be visited before their
        // corresponding delivery location
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
        checkArgument(sol.arrivalTimes.length == n, "Number of arrival times should equal number of locations.");
        checkArgument(sol.arrivalTimes[0] == 0, "The first arrival time should always be 0.");

        // check feasibility
        for (int i = 1; i < n; i++) {
            final int prev = sol.route[i - 1];
            final int cur = sol.route[i];

            // we compute the first possible arrival time for the vehicle to
            // arrive at location i, given that it first visited location i-1
            final int earliestArrivalTime = sol.arrivalTimes[prev]
                    + serviceTimes[prev] + travelTime[prev][cur];

            // we also have to take into account the time window
            final int minArrivalTime = Math
                    .max(earliestArrivalTime, releaseDates[cur]);

            checkArgument(sol.arrivalTimes[cur] >= minArrivalTime, "Route index %s, arrivalTime (%s) needs to be greater or equal to minArrivalTime (%s).", i, sol.arrivalTimes[sol.route[i]], minArrivalTime);
        }

        /*
         * CHECK OBJECTIVE VALUE
         */

        // sum travel time
        int totalTravelTime = 0;
        for (int i = 1; i < n; i++) {
            totalTravelTime += travelTime[sol.route[i - 1]][sol.route[i]];
        }

        // sum tardiness
        int tardiness = 0;
        for (int i = 0; i < n; i++) {
            final int lateness = (sol.arrivalTimes[i] + serviceTimes[i])
                    - dueDates[i];
            if (lateness > 0) {
                tardiness += lateness;
            }
        }
        checkArgument(sol.objectiveValue == totalTravelTime + tardiness, "Incorrect objective value (%s), it should be travel time + tardiness = %s + %s = %s.", sol.objectiveValue, totalTravelTime, tardiness, totalTravelTime
                + tardiness);
        return sol;
    }

    public static SolutionObject[] validate(SolutionObject[] sols,
            int[][] travelTime, int[] releaseDates, int[] dueDates,
            int[][] servicePairs, int[] serviceTimes,
            int[][] vehicleTravelTimes, int[][] inventories,
            int[] remainingServiceTimes) {

        return sols;
    }

    static Set<Integer> toSet(int[] arr) {
        final Set<Integer> set = newHashSet();
        for (int i = 0; i < arr.length; i++) {
            set.add(arr[i]);
        }
        return set;
    }

}
