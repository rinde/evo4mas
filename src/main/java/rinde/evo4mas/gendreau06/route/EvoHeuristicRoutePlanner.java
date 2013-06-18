/**
 * 
 */
package rinde.evo4mas.gendreau06.route;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;
import java.util.Set;

import rinde.ecj.Heuristic;
import rinde.evo4mas.gendreau06.GCBuilderReceiver;
import rinde.evo4mas.gendreau06.GendreauContext;
import rinde.evo4mas.gendreau06.GendreauContextBuilder;
import rinde.evo4mas.gendreau06.GendreauFunctions.TimeUntilAvailable;
import rinde.sim.core.model.pdp.Parcel;

import com.google.common.collect.ImmutableSet;

/**
 * A {@link RoutePlanner} implementation that uses a (evolved) {@link Heuristic}
 * for determining its route. The route is build incrementally, one hop at a
 * time.
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public class EvoHeuristicRoutePlanner extends AbstractRoutePlanner implements GCBuilderReceiver {

	protected final Heuristic<GendreauContext> heuristic;
	protected final TimeUntilAvailable<GendreauContext> tua;
	protected Parcel current;
	protected GendreauContextBuilder gendreauContextBuilder;

	protected Set<Parcel> onMapSet;
	protected Set<Parcel> inCargoSet;

	public EvoHeuristicRoutePlanner(Heuristic<GendreauContext> h) {
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
		current = nextLoop(onMapSet, claimed, inCargoSet, time);
	}

	protected Parcel nextLoop(Collection<Parcel> todo, Set<Parcel> alreadyClaimed, Collection<Parcel> contents,
			long time) {
		Parcel best = null;
		double bestValue = Double.POSITIVE_INFINITY;

		gendreauContextBuilder.initRepeatedUsage(time);

		final StringBuilder sb = new StringBuilder();
		for (final Parcel p : todo) {
			// filter out the already claimed parcels
			if (!alreadyClaimed.contains(p)) {
				final GendreauContext gc = gendreauContextBuilder.buildInRepetition(p, false, false);
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

			final GendreauContext gc = gendreauContextBuilder.buildInRepetition(p, true, false);

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

	/*
	 * protected GendreauContext createContext(GendreauContext gc, Parcel p,
	 * boolean isInCargo, boolean isAssignedToVehicle) { final int numWaiters =
	 * 0;// isInCargo ? // coordinationModel.getNumWaitersFor(p) : 0 return new
	 * GendreauContext(gc.vehicleDTO, gc.truckPosition, gc.truckContents,
	 * ((DefaultParcel) p).dto, gc.time, isInCargo, isAssignedToVehicle,
	 * numWaiters, gc.otherVehiclePositions, new HashSet<Parcel>()); } protected
	 * GendreauContext createGenericContext(long time) { final
	 * Collection<Parcel> contents = pdpModel.getContents(truck); final
	 * List<Point> positions = newArrayList(); final Set<Vehicle> vehicles =
	 * pdpModel.getVehicles(); for (final Vehicle v : vehicles) { if (v !=
	 * truck) { positions.add(roadModel.getPosition(v)); } } return new
	 * GendreauContext(truck.getDTO(), roadModel.getPosition(truck),
	 * convert(contents), null, time, false, false, -1, positions, new
	 * HashSet<Parcel>()); } protected static Set<ParcelDTO>
	 * convert(Collection<Parcel> parcels) { final Set<ParcelDTO> dtos =
	 * newLinkedHashSet(); for (final Parcel p : parcels) {
	 * dtos.add(((DefaultParcel) p).dto); } return dtos; }
	 */

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

	public void receive(GendreauContextBuilder gcb) {
		gendreauContextBuilder = gcb;

	}
}
