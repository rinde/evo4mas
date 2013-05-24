package rinde.solver.spdptw;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Set;

import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.Ranges;

/**
 * 
 */

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class SolverValidator {

	public static void validateInputs(int[][] travelTime, int[] releaseDates, int[] dueDates, int[][] servicePairs,
			int serviceTime) {

		final int n = travelTime.length;
		checkArgument(n > 0);
		// check that matrix is n x n
		for (int i = 0; i < n; i++) {
			checkArgument(travelTime[i].length == n, "row %s has invalid length %s", i, travelTime[i].length);
		}
		checkArgument(releaseDates.length == n);
		checkArgument(dueDates.length == n);
		checkArgument(serviceTime >= 0, "serviceTime should be a positive number.");

		// check time windows validity
		for (int i = 0; i < n; i++) {
			checkArgument(releaseDates[i] <= dueDates[i], "index %s, release date (%s) should always be before the due date (%s)", i, releaseDates[i], dueDates[i]);
		}

		checkArgument(releaseDates[0] == 0 && dueDates[0] == 0, "start location should have release date and due date 0");
		checkArgument(releaseDates[n - 1] == 0, "depot should have release date 0");

		// check that every pair consists of valid ids and that a location is in
		// only one pair
		final Set<Integer> set = newHashSet();
		for (int i = 0; i < servicePairs.length; i++) {
			checkArgument(servicePairs[i].length == 2);
			for (int j = 0; j < 2; j++) {
				checkArgument(servicePairs[i][j] > 0 && servicePairs[i][j] < n - 1, "pair consists of an invalid location (start location and depot are not allowed), location is %s", servicePairs[i][j]);
				checkArgument(!set.contains(servicePairs[i][j]), "location can be part of only one pair, duplicate location: "
						+ servicePairs[i][j]);
				set.add(servicePairs[i][j]);
			}
		}

	}

	public static void validate(SolutionObject obj, int[][] travelTime, int[] releaseDates, int[] dueDates,
			int[][] servicePairs, int serviceTime) {
		validateInputs(travelTime, releaseDates, dueDates, servicePairs, serviceTime);
		final int n = travelTime.length;
		/*
		 * CHECK SERVICE SEQUENCE
		 */
		checkArgument(obj.serviceSequence.length == n);
		checkArgument(obj.serviceSequence[0] == 0, "the serviceSequence should always start with the vehicleLocation");
		checkArgument(obj.serviceSequence[n - 1] == n - 1, "the serviceSequence should always finish with the depot");

		final Set<Integer> sequenceSet = toSet(obj.serviceSequence);
		final Set<Integer> locationSet = Ranges.closedOpen(0, travelTime.length).asSet(DiscreteDomains.integers());

		// checks duplicates
		checkArgument(sequenceSet.size() == n, "every location in serviceSequence should appear exactly once");
		// checks for completeness of tour
		checkArgument(sequenceSet.equals(locationSet), "not all locations are serviced, there is probably a non-existing location in the tour");

		/*
		 * CHECK ARRIVAL TIMES
		 */
		checkArgument(obj.arrivalTimes.length == n, "number of arrival times should equal number of locations");
		checkArgument(obj.arrivalTimes[0] == 0, "the first arrival time should always be 0");

		// check feasibility
		for (int i = 1; i < n; i++) {
			final int minArrivalTime = obj.arrivalTimes[obj.serviceSequence[i - 1]]
					+ travelTime[obj.serviceSequence[i - 1]][obj.serviceSequence[i]] + (i > 1 ? serviceTime : 0);
			checkArgument(obj.arrivalTimes[obj.serviceSequence[i]] >= minArrivalTime, "index %s arrivalTime %s minArrivalTime %s", i, obj.arrivalTimes[i], minArrivalTime);
		}

		/*
		 * CHECK OBJECTIVE VALUE
		 */

		// sum travel time
		int totalTravelTime = 0;
		for (int i = 1; i < n; i++) {
			totalTravelTime += travelTime[obj.serviceSequence[i - 1]][obj.serviceSequence[i]];
		}

		// sum tardiness
		int tardiness = 0;
		for (int i = 0; i < n; i++) {
			// leaving at first point and arriving at depot costs no service
			// time
			final int st = i == 0 || i == n - 1 ? 0 : serviceTime;
			final int lateness = (obj.arrivalTimes[i] + st) - dueDates[i];
			if (lateness > 0) {
				tardiness += lateness;
			}
		}
		checkArgument(obj.objectiveValue == totalTravelTime + tardiness, "travel time %s, tardiness %s", totalTravelTime, tardiness);
	}

	static Set<Integer> toSet(int[] arr) {
		final Set<Integer> set = newHashSet();
		for (int i = 0; i < arr.length; i++) {
			set.add(arr[i]);
		}
		return set;
	}

}
