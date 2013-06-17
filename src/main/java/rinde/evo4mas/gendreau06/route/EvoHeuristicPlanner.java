/**
 * 
 */
package rinde.evo4mas.gendreau06.route;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rinde.ecj.Heuristic;
import rinde.evo4mas.gendreau06.GendreauContext;
import rinde.evo4mas.gendreau06.GendreauFunctions.TimeUntilAvailable;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.Vehicle;
import rinde.sim.problem.common.DefaultParcel;
import rinde.sim.problem.common.ParcelDTO;

import com.google.common.collect.ImmutableSet;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class EvoHeuristicPlanner extends AbstractRoutePlanner {

	protected final Heuristic<GendreauContext> heuristic;
	protected final TimeUntilAvailable<GendreauContext> tua;
	protected Parcel current;

	protected Set<Parcel> onMapSet;
	protected Set<Parcel> inCargoSet;

	public EvoHeuristicPlanner(Heuristic<GendreauContext> h) {
		heuristic = h;
		tua = new TimeUntilAvailable<GendreauContext>();
	}

	@Override
	protected void doUpdate(Collection<Parcel> onMap, Collection<Parcel> inCargo, long time) {
		onMapSet = newHashSet(onMap);
		inCargoSet = newHashSet(inCargo);
		computeCurrent(time);
	}

	protected void computeCurrent(long time) {
		final Set<Parcel> claimed = ImmutableSet.of();
		current = nextLoop(onMapSet, claimed, inCargoSet, createGenericContext(time));
	}

	protected Parcel nextLoop(Collection<Parcel> todo, Set<Parcel> alreadyClaimed, Collection<Parcel> contents,
			GendreauContext genericContext) {
		Parcel best = null;
		double bestValue = Double.POSITIVE_INFINITY;

		final StringBuilder sb = new StringBuilder();
		for (final Parcel p : todo) {
			// filter out the already claimed parcels
			if (!alreadyClaimed.contains(p)) {
				final GendreauContext gc = createContext(genericContext, p, false, false);
				final double res = tua.execute(null, gc);

				// TODO this should be a differnt value? similar to isEarly
				if (res < 1000) {
					final double v = heuristic.compute(gc);

					sb.append(p).append(" ").append(v).append("\n");
					if (v < bestValue || ((Double.isInfinite(v) || Double.isNaN(v)) && bestValue == v)) {
						best = p;
						bestValue = v;
					}
				}
			}
		}
		// if (best == null) {
		// System.err.println(sb.toString());
		// System.err.println(bestValue);
		// }
		for (final Parcel p : contents) {

			final GendreauContext gc = createContext(genericContext, p, true, false);

			final double v = heuristic.compute(gc);
			if (v < bestValue || ((Double.isInfinite(v) || Double.isNaN(v)) && bestValue == v)) {
				best = p;
				bestValue = v;
			}
		}
		if (best == null) {
			// System.out.println("todo: " + todo + "\ncontents: " + contents +
			// "\nclaimed: " + alreadyClaimed);
		}

		return best;
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

	public boolean hasNext() {
		return !isUpdated() ? false : !(onMapSet.isEmpty() && inCargoSet.isEmpty());
	}

	public Parcel current() {
		return current;
	}

	@Override
	protected void nextImpl(long time) {
		if (current() == null) {
			return;
		}
		// current should exist in exactly one of the sets
		checkArgument(onMapSet.contains(current) ^ inCargoSet.contains(current), "current: %s should exist in one of the sets", current);
		if (onMapSet.contains(current)) {
			inCargoSet.add(current);
			onMapSet.remove(current);
		} else {
			inCargoSet.remove(current);
		}
		computeCurrent(time);
	}
}
