/**
 * 
 */
package rinde.solver.pdptw;


/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public interface MultiVehicleSolver {

    /**
     * @param travelTime
     * @param releaseDates
     * @param dueDates
     * @param servicePairs
     * @param serviceTimes
     * @param vehicleTravelTimes contains travel times from each vehicle to
     *            every location
     * @param inventories n x 2 matrix of vehicle and location pairs.
     *            inventories[i][0] indicates the vehicle, inventories[i][1]
     *            indicates the location. Vehicles may occur more than once
     *            (i.e. they may have more than one location in their
     *            inventory). Only delivery locations can be in the inventory.
     * @param remainingServiceTimes contains the remaining service time for
     *            every vehicle. The length of this array equals the number of
     *            vehicles.
     * @return A solution object for every vehicle.
     */
    SolutionObject[] solve(int[][] travelTime, int[] releaseDates,
            int[] dueDates, int[][] servicePairs, int[] serviceTimes,
            int[][] vehicleTravelTimes, int[][] inventories,
            int[] remainingServiceTimes);

}
