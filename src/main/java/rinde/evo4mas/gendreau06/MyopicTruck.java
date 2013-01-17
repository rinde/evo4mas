/**
 * 
 */
package rinde.evo4mas.gendreau06;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import rinde.ecj.Heuristic;
import rinde.sim.core.model.pdp.PDPModel.ParcelState;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.problem.common.DefaultParcel;
import rinde.sim.problem.common.VehicleDTO;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class MyopicTruck extends HeuristicTruck {

	protected CoordinationModel coordinationModel;

	public MyopicTruck(VehicleDTO pDto, Heuristic<GendreauContext> p) {
		super(pDto, p, createStateMachine(new MyopicEarlyTarget(), new MyopicGotoPickup(), new Pickup()));
	}

	@Override
	protected Parcel next(long time) {
		final Collection<Parcel> todo = pdpModel.getParcels(ParcelState.ANNOUNCED, ParcelState.AVAILABLE);
		final Set<Parcel> alreadyClaimed = coordinationModel.getClaims();
		final Collection<Parcel> contents = pdpModel.getContents(this);
		return nextLoop(todo, alreadyClaimed, contents, createGenericContext(time));
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
					final double v = program.compute(gc);

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

			final double v = program.compute(gc);
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

	public void setCoordinationModel(CoordinationModel cm) {
		coordinationModel = cm;
	}

	public static class MyopicEarlyTarget extends EarlyTarget {
		@Override
		public void onEntry(TruckEvent event, HeuristicTruck context) {
			((MyopicTruck) context).coordinationModel.waitFor(((MyopicTruck) context), context.currentTarget);
		}

		@Override
		public void onExit(TruckEvent event, HeuristicTruck context) {
			((MyopicTruck) context).coordinationModel.unwaitFor(((MyopicTruck) context), context.currentTarget);
		}
	}

	public static class MyopicGotoPickup extends GotoPickup {
		@Override
		public void onEntry(TruckEvent event, HeuristicTruck context) {
			((MyopicTruck) context).coordinationModel.claim(context.currentTarget);
		}
	}

	@Override
	protected GendreauContext createContext(GendreauContext gc, Parcel p, boolean isInCargo, boolean isAssignedToVehicle) {
		return new GendreauContext(gc.vehicleDTO, gc.truckPosition, gc.truckContents, ((DefaultParcel) p).dto, gc.time,
				isInCargo, isAssignedToVehicle, isInCargo ? coordinationModel.getNumWaitersFor(p) : 0,
				gc.otherVehiclePositions, new HashSet<Parcel>());

	}
}
