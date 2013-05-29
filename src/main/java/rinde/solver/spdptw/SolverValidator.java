package rinde.solver.spdptw;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Set;

import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.Ranges;

/**
 * Provides methods for validating input to {@link Solver}s and for validating
 * output from {@link Solver}s. Also provides a {@link #wrap(Solver)} method
 * which wraps any solver such that both inputs and outputs are validated every
 * time {@link Solver#solve(int[][], int[], int[], int[][], int)} is called.
 * 
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public final class SolverValidator implements Solver {

	private final Solver delegateSolver;

	private SolverValidator(Solver delegate) {
		delegateSolver = delegate;
	}

	public SolutionObject solve(int[][] travelTime, int[] releaseDates, int[] dueDates, int[][] servicePairs,
			int serviceTime) {
		// first check inputs
		validateInputs(travelTime, releaseDates, dueDates, servicePairs, serviceTime);
		// execute solver
		final SolutionObject output = delegateSolver
				.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTime);
		// check outputs
		return validate(output, travelTime, releaseDates, dueDates, servicePairs, serviceTime);
	}

	/**
	 * Wraps the original {@link Solver} such that both the inputs to the solver
	 * and the outputs from the solver are validated. When an invalid input or
	 * output is detected a {@link IllegalArgumentException is thrown}.
	 * @param delegate The {@link Solver} that will be used for the actual
	 *            solving.
	 */
	public static Solver wrap(Solver delegate) {
		return new SolverValidator(delegate);
	}

	/**
	 * Validates the inputs for the {@link Solver}. This method checks all
	 * properties as defined in
	 * {@link Solver#solve(int[][], int[], int[], int[][], int)}. If the inputs
	 * do are not correct an {@link IllegalArgumentException} is thrown.
	 * @param travelTime Parameter as specified by
	 *            {@link Solver#solve(int[][], int[], int[], int[][], int)}.
	 * @param releaseDates Parameter as specified by
	 *            {@link Solver#solve(int[][], int[], int[], int[][], int)}.
	 * @param dueDates Parameter as specified by
	 *            {@link Solver#solve(int[][], int[], int[], int[][], int)}.
	 * @param servicePairs Parameter as specified by
	 *            {@link Solver#solve(int[][], int[], int[], int[][], int)}.
	 * @param serviceTime Parameter as specified by
	 *            {@link Solver#solve(int[][], int[], int[], int[][], int)}.
	 */
	public static void validateInputs(int[][] travelTime, int[] releaseDates, int[] dueDates, int[][] servicePairs,
			int serviceTime) {

		final int n = travelTime.length;
		checkArgument(n > 0, "Travel time matrix cannot be empty");
		// check that matrix is n x n
		for (int i = 0; i < n; i++) {
			checkArgument(travelTime[i].length == n, "row %s has invalid length %s", i, travelTime[i].length);
		}
		checkArgument(releaseDates.length == n, "ReleaseDates array has incorrect length (%s) should be %s", releaseDates.length, n);
		checkArgument(dueDates.length == n, "dueDates array has incorrect length (%s) should be %s", dueDates.length, n);
		checkArgument(serviceTime >= 0, "serviceTime should be a positive number.");

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
				checkArgument(servicePairs[i][j] > 0 && servicePairs[i][j] < n - 1, "Pair consists of an invalid location (start location and depot are not allowed), index is %s, location is %s", i, servicePairs[i][j]);
				checkArgument(!set.contains(servicePairs[i][j]), "Location can be part of only one pair, duplicate location: %s (index %s,%s)", servicePairs[i][j], i, j);
				set.add(servicePairs[i][j]);
			}
		}

	}

	/**
	 * Validates the {@link SolutionObject} that is produced by a {@link Solver}
	 * . If the {@link SolutionObject} is infeasible, an
	 * {@link IllegalArgumentException} is thrown.
	 * @param sol The {@link SolutionObject} that is validated.
	 * @param travelTime Parameter as specified by
	 *            {@link Solver#solve(int[][], int[], int[], int[][], int)}.
	 * @param releaseDates Parameter as specified by
	 *            {@link Solver#solve(int[][], int[], int[], int[][], int)}.
	 * @param dueDates Parameter as specified by
	 *            {@link Solver#solve(int[][], int[], int[], int[][], int)}.
	 * @param servicePairs Parameter as specified by
	 *            {@link Solver#solve(int[][], int[], int[], int[][], int)}.
	 * @param serviceTime Parameter as specified by
	 *            {@link Solver#solve(int[][], int[], int[], int[][], int)}.
	 * @return The solution as is supplied, used for method chaining.
	 */
	public static SolutionObject validate(SolutionObject sol, int[][] travelTime, int[] releaseDates, int[] dueDates,
			int[][] servicePairs, int serviceTime) {
		final int n = travelTime.length;
		/*
		 * CHECK SERVICE SEQUENCE
		 */
		checkArgument(sol.route.length == n, "The route should always contain all locations.");
		checkArgument(sol.route[0] == 0, "The route should always start with the vehicle start location (0).");
		checkArgument(sol.route[n - 1] == n - 1, "The route should always finish with the depot.");

		final Set<Integer> routeSet = toSet(sol.route);
		final Set<Integer> locationSet = Ranges.closedOpen(0, travelTime.length).asSet(DiscreteDomains.integers());

		// checks duplicates
		checkArgument(routeSet.size() == n, "Every location in route should appear exactly once.");
		// checks for completeness of tour
		checkArgument(routeSet.equals(locationSet), "Not all locations are serviced, there is probably a non-existing location in the route.");

		/*
		 * CHECK ARRIVAL TIMES
		 */
		checkArgument(sol.arrivalTimes.length == n, "Number of arrival times should equal number of locations.");
		checkArgument(sol.arrivalTimes[0] == 0, "The first arrival time should always be 0.");

		// check feasibility
		for (int i = 1; i < n; i++) {
			final int minArrivalTime = sol.arrivalTimes[sol.route[i - 1]] + travelTime[sol.route[i - 1]][sol.route[i]]
					+ (i > 1 ? serviceTime : 0);
			checkArgument(sol.arrivalTimes[sol.route[i]] >= minArrivalTime, "Index %s, arrivalTime (%s) needs to be greater or equal to minArrivalTime (%s).", i, sol.arrivalTimes[sol.route[i]], minArrivalTime);
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
			// leaving at first point and arriving at depot costs no service
			// time
			final int st = i == 0 || i == n - 1 ? 0 : serviceTime;
			final int lateness = (sol.arrivalTimes[i] + st) - dueDates[i];
			if (lateness > 0) {
				tardiness += lateness;
			}
		}
		checkArgument(sol.objectiveValue == totalTravelTime + tardiness, "Incorrect objective value (%s), it should be travel time + tardiness = %s + %s = %s.", sol.objectiveValue, totalTravelTime, tardiness, totalTravelTime
				+ tardiness);
		return sol;
	}

	static Set<Integer> toSet(int[] arr) {
		final Set<Integer> set = newHashSet();
		for (int i = 0; i < arr.length; i++) {
			set.add(arr[i]);
		}
		return set;
	}

}
