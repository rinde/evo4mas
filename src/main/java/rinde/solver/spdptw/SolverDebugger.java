/**
 * 
 */
package rinde.solver.spdptw;

import java.util.Arrays;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class SolverDebugger implements Solver {

	private final Solver solver;

	private SolverDebugger(Solver solver) {
		this.solver = solver;
	}

	public SolutionObject solve(int[][] travelTime, int[] releaseDates, int[] dueDates, int[][] servicePairs,
			int serviceTime) {

		System.out.println("int[][] travelTime = " + fix(Arrays.deepToString(travelTime)));
		System.out.println("int[] releaseDates = " + fix(Arrays.toString(releaseDates)));
		System.out.println("int[] dueDates = " + fix(Arrays.toString(dueDates)));
		System.out.println("int[][] servicePairs = " + fix(Arrays.deepToString(servicePairs)));
		System.out.println("int serviceTime = " + serviceTime + ";");

		final long start = System.currentTimeMillis();
		final SolutionObject sol = solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTime);
		System.out.println(System.currentTimeMillis() - start + "ms");

		System.out.println("route: " + Arrays.toString(sol.route));
		System.out.println("arrivalTimes: " + Arrays.toString(sol.arrivalTimes));
		System.out.println("objectiveValue: " + sol.objectiveValue);

		int totalTravelTime = 0;
		for (int i = 1; i < travelTime.length; i++) {
			totalTravelTime += travelTime[sol.route[i - 1]][sol.route[i]];
		}
		System.out.println("travel time :  " + totalTravelTime);

		// code for debugging arrival times
		// for (int i = 1; i < travelTime.length; i++) {
		// System.out.println(sol.route[i - 1] + " -> " + sol.route[i] + " = " +
		// sol.arrivalTimes[sol.route[i - 1]]
		// + " + " + travelTime[sol.route[i - 1]][sol.route[i]] + " + " + (i > 1
		// ? serviceTime : 0) + " = "
		// + " (" + sol.arrivalTimes[sol.route[i]] + ")");
		// }

		return sol;
	}

	public static Solver wrap(Solver s) {
		return new SolverDebugger(s);
	}

	static String fix(String s) {
		return s.replace('[', '{').replace(']', '}') + ";";
	}

}
