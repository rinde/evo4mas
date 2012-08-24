/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import static java.util.Collections.unmodifiableCollection;

import java.util.Collection;

import rinde.sim.core.graph.Point;
import rinde.sim.problem.fabrirecht.ParcelDTO;
import rinde.sim.problem.fabrirecht.VehicleDTO;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class FRContext {

	public final VehicleDTO vehicleDTO;
	public final Point truckPosition;
	public final Collection<ParcelDTO> truckContents;
	public final ParcelDTO parcel;
	public final boolean isInCargo;
	public final long time;

	public FRContext(VehicleDTO v, Point tp, Collection<ParcelDTO> tc, ParcelDTO p, long tm, boolean c) {
		vehicleDTO = v;
		truckPosition = tp;
		truckContents = unmodifiableCollection(tc);
		parcel = p;
		time = tm;
		isInCargo = c;
	}

}
