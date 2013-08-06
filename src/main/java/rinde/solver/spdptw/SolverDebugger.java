/**
 * 
 */
package rinde.solver.spdptw;

import static java.lang.System.out;

import java.util.Arrays;

/**
 * A {@link Solver} wrapper that adds debugging.
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public class SolverDebugger implements Solver {

    private final Solver solver;

    private SolverDebugger(Solver solver) {
        this.solver = solver;
    }

    public SolutionObject solve(int[][] travelTime, int[] releaseDates,
            int[] dueDates, int[][] servicePairs, int[] serviceTimes) {

        out.println("int[][] travelTime = "
                + fix(Arrays.deepToString(travelTime)));
        out.println("int[] releaseDates = "
                + fix(Arrays.toString(releaseDates)));
        out.println("int[] dueDates = " + fix(Arrays.toString(dueDates)));
        out.println("int[][] servicePairs = "
                + fix(Arrays.deepToString(servicePairs)));
        out.println("int[] serviceTime = " + fix(Arrays.toString(serviceTimes)));

        final long start = System.currentTimeMillis();
        final SolutionObject sol = solver
                .solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
        out.println(System.currentTimeMillis() - start + "ms");

        out.println("route: " + Arrays.toString(sol.route));
        out.println("arrivalTimes: " + Arrays.toString(sol.arrivalTimes));
        out.println("objectiveValue: " + sol.objectiveValue);

        int totalTravelTime = 0;
        for (int i = 1; i < travelTime.length; i++) {
            totalTravelTime += travelTime[sol.route[i - 1]][sol.route[i]];
        }
        out.println("travel time :  " + totalTravelTime);

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

    /**
     * Wraps the specified {@link Solver} to allow easy debugging. Every
     * invocation of {@link Solver#solve(int[][], int[], int[], int[][], int[])}
     * all inputs and outputs are printed to sys.out.
     * @param s The {@link Solver} to wrap.
     * @return The wrapped solver.
     */
    public static Solver wrap(Solver s) {
        return new SolverDebugger(s);
    }

    static String fix(String s) {
        return s.replace('[', '{').replace(']', '}') + ";";
    }

}
