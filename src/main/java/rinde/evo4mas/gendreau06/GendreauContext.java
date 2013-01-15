/**
 * 
 */
package rinde.evo4mas.gendreau06;

import static java.util.Collections.unmodifiableList;

import java.util.Collection;
import java.util.List;

import rinde.evo4mas.common.TruckContext;
import rinde.sim.core.graph.Point;
import rinde.sim.problem.common.ParcelDTO;
import rinde.sim.problem.common.VehicleDTO;

/**
 * Context object used in GP.
 * 
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class GendreauContext extends TruckContext {

	public List<Point> otherVehiclePositions;
	public final int numWaiters;

	/**
	 * @param v Vehicle info
	 * @param tp Current position of vehicle
	 * @param tc Contents of vehicle
	 * @param p Info of current parcel
	 * @param tm Current time
	 * @param c Is in cargo
	 * @param w Number of waiters
	 * @param ovp Other vehicle positions
	 */
	public GendreauContext(VehicleDTO v, Point tp, Collection<ParcelDTO> tc, ParcelDTO p, long tm, boolean c, int w,
			List<Point> ovp) {
		super(v, tp, tc, p, tm, c);
		numWaiters = w;
		otherVehiclePositions = unmodifiableList(ovp);
	}

}
