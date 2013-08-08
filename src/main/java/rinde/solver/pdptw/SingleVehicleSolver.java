/**
 * 
 */
package rinde.solver.pdptw;

import java.util.Collection;
import java.util.Queue;

import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.problem.common.DefaultVehicle;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public interface SingleVehicleSolver {

    Queue<Parcel> solve(RoadModel rm, PDPModel pm, DefaultVehicle vehicle,
            Collection<Parcel> assignedParcels, long time);

}
