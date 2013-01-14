/**
 * 
 */
package rinde.evo4mas.gendreau06;

import java.util.Collection;
import java.util.Set;

import rinde.ecj.Heuristic;
import rinde.sim.core.model.pdp.PDPModel.ParcelState;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.problem.common.VehicleDTO;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class MyopicTruck extends HeuristicTruck {

	/**
	 * @param pDto
	 * @param p
	 */
	public MyopicTruck(VehicleDTO pDto, Heuristic<GendreauContext> p) {
		super(pDto, p);
		// TODO Auto-generated constructor stub
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
				final GendreauContext gc = createContext(genericContext, p, false);
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

			final GendreauContext gc = createContext(genericContext, p, true);

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

}
