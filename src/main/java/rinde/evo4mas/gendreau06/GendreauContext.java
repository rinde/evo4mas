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
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class GendreauContext extends TruckContext {

	public final int numWaiters;
	public List<Point> otherVehiclePositions;

	public GendreauContext(VehicleDTO v, Point tp, Collection<ParcelDTO> tc, ParcelDTO p, long tm, boolean c, int w,
			List<Point> ovp) {
		super(v, tp, tc, p, tm, c);
		numWaiters = w;
		otherVehiclePositions = unmodifiableList(ovp);
	}

}