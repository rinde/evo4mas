/**
 * 
 */

/**
 * API for single pickup-and-delivery problem with time windows (SPDPTW).
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public interface SolverAPI {

	/**
	 * 
	 * All times are in seconds relative to the current time (0). All
	 * constraints are soft, i.e. lateness at service locations and at depot are
	 * allowed. The start location has index 0, the end location (depot) has
	 * index n-1.
	 * 
	 * The depot is a location with index 0.
	 * 
	 * @param dist n x n distance matrix expressed in time: travelTime[i][j]
	 *            specifies travelTime from location i to location j.
	 * @param releaseDates specifies the left side of the time window for every
	 *            location (hard constraint, earlier is not allowed).
	 * @param dueDates specifies the right side of the time window for every
	 *            location (soft constraint, lateness is allowed).
	 * @param servicePairs n x 2 matrix of service location pairs,
	 *            servicePairs[i][0] and servicePairs[i][1] specify the pickup
	 *            and delivery location respectively.
	 * @param vehicleLocation specifies the location index where the vehicle is
	 *            currently. It is assumed that when a vehicle is at a customer
	 *            location, all customer interactions are done.
	 * @param serviceTime specifies the service time for all locations (both
	 *            pickups and deliveries).
	 * @return The solution object which indicates the best found solution for
	 *         the SPDPTW.
	 */
	SolutionObject solve(int[][] travelTime, int[] releaseDates, int[] dueDates, int[][] servicePairs, int serviceTime);

}