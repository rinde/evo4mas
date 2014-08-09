/**
 * 
 */
package rinde.evo4mas.common;

import static java.util.Collections.unmodifiableCollection;

import java.util.Collection;

import com.github.rinde.rinsim.core.graph.Point;
import com.github.rinde.rinsim.core.pdptw.ParcelDTO;
import com.github.rinde.rinsim.core.pdptw.VehicleDTO;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class TruckContext {

	public final VehicleDTO vehicleDTO;
	public final Point truckPosition;
	public final Collection<ParcelDTO> truckContents;
	public final ParcelDTO parcel;
	public final boolean isInCargo;
	public final long time;

	public TruckContext(VehicleDTO v, Point tp, Collection<ParcelDTO> tc, ParcelDTO p, long tm, boolean c) {
		vehicleDTO = v;
		truckPosition = tp;
		truckContents = unmodifiableCollection(tc);
		parcel = p;
		time = tm;
		isInCargo = c;
	}

}
