/**
 * 
 */
package rinde.evo4mas.gendreau06;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.Vehicle;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.problem.common.DefaultParcel;
import rinde.sim.problem.common.ParcelDTO;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class GendreauContextBuilder {

	protected final RoadModel roadModel;
	protected final PDPModel pdpModel;
	protected final Truck truck;

	protected GendreauContext genericContext;

	// TODO add Communicator?
	public GendreauContextBuilder(RoadModel rm, PDPModel pm, Truck t) {
		roadModel = rm;
		pdpModel = pm;
		truck = t;
	}

	public void initRepeatedUsage(long time) {
		genericContext = createGenericContext(time);
	}

	public GendreauContext buildInRepetition(Parcel p, boolean isInCargo, boolean isAssignedToVehicle) {
		return createContext(genericContext, p, isInCargo, isAssignedToVehicle);
	}

	public GendreauContext buildFromScatch(long time, Parcel p, boolean isInCargo, boolean isAssignedToVehicle) {
		return createContext(createGenericContext(time), p, isInCargo, isAssignedToVehicle);
	}

	protected GendreauContext createContext(GendreauContext gc, Parcel p, boolean isInCargo, boolean isAssignedToVehicle) {

		final int numWaiters = 0;// isInCargo ?
									// coordinationModel.getNumWaitersFor(p) : 0
		return new GendreauContext(gc.vehicleDTO, gc.truckPosition, gc.truckContents, ((DefaultParcel) p).dto, gc.time,
				isInCargo, isAssignedToVehicle, numWaiters, gc.otherVehiclePositions, new HashSet<Parcel>());

	}

	protected GendreauContext createGenericContext(long time) {
		final Collection<Parcel> contents = pdpModel.getContents(truck);
		final List<Point> positions = newArrayList();
		final Set<Vehicle> vehicles = pdpModel.getVehicles();
		for (final Vehicle v : vehicles) {
			if (v != truck) {
				positions.add(roadModel.getPosition(v));
			}
		}
		return new GendreauContext(truck.getDTO(), roadModel.getPosition(truck), convert(contents), null, time, false,
				false, -1, positions, new HashSet<Parcel>());
	}

	protected static Set<ParcelDTO> convert(Collection<Parcel> parcels) {
		final Set<ParcelDTO> dtos = newLinkedHashSet();
		for (final Parcel p : parcels) {
			dtos.add(((DefaultParcel) p).dto);
		}
		return dtos;
	}

}