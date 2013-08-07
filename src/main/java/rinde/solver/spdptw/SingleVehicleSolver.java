package rinde.solver.spdptw;

/**
 * Interface for solvers for the single vehicle pickup-and-delivery problem with
 * time windows (SPDPTW).
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public interface SingleVehicleSolver {

    /**
     * Gives a solution for the SPDPTW as specified by the parameters. The
     * returned solution does not necessary need to be optimal but it needs to
     * be feasible. The {@link SolverValidator} can check whether a
     * {@link SingleVehicleSolver} produces a valid solution and it can check whether the
     * parameters for the {@link SingleVehicleSolver} are valid.
     * <p>
     * All times are in seconds relative to the current time (0). All
     * constraints are soft, i.e. lateness at service locations and at depot are
     * allowed. The start location has index 0, the end location (depot) has
     * index n-1.
     * 
     * @param travelTime n x n distance matrix expressed in time:
     *            travelTime[i][j] specifies travelTime from location i to
     *            location j.
     * @param releaseDates specifies the left side of the time window for every
     *            location (hard constraint, earlier is not allowed).
     * @param dueDates specifies the right side of the time window for every
     *            location (soft constraint, lateness is allowed).
     * @param servicePairs n x 2 matrix of service location pairs,
     *            servicePairs[i][0] and servicePairs[i][1] specify the pickup
     *            and delivery location respectively. Each location may occur at
     *            maximum once in the matrix (either as an pickup or as a
     *            delivery).
     * @param vehicleLocation specifies the location index where the vehicle is
     *            currently. It is assumed that when a vehicle is at a customer
     *            location, all customer interactions are done.
     * @param serviceTimes specifies the service time for all locations (both
     *            pickups and deliveries).
     * @return The solution object which indicates a (usually the best found)
     *         solution for the SPDPTW.
     */
    SolutionObject solve(int[][] travelTime, int[] releaseDates,
            int[] dueDates, int[][] servicePairs, int[] serviceTimes);

}
