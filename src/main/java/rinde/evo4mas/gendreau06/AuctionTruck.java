/**
 * 
 */
package rinde.evo4mas.gendreau06;

import static com.google.common.collect.Sets.newLinkedHashSet;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import rinde.ecj.Heuristic;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.problem.common.DefaultParcel;
import rinde.sim.problem.common.VehicleDTO;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class AuctionTruck extends HeuristicTruck {

	protected final Set<Parcel> todo;

	/**
	 * @param pDto
	 * @param p
	 */
	public AuctionTruck(VehicleDTO pDto, Heuristic<GendreauContext> p) {
		super(pDto, p, createStateMachine(new EarlyTarget(), new GotoPickup(), new AuctionPickup()));
		todo = newLinkedHashSet();
	}

	public double getBidFor(AuctionParcel ap, long time) {
		return program.compute(createFullContext(time, ap, false, false));
	}

	public void receiveParcel(AuctionParcel ap) {
		todo.add(ap);
	}

	@Override
	protected Parcel next(long time) {
		final Collection<Parcel> contents = pdpModel.getContents(this);
		final GendreauContext genericContext = createGenericContext(time);

		// filter out all parcels which are not soon available
		final Iterator<Parcel> todoIterator = Collections2.filter(todo, new Predicate<Parcel>() {
			public boolean apply(Parcel input) {
				return tua.execute(null, createContext(genericContext, input, false, true)) < 1000;
			}
		}).iterator();
		final Iterator<Parcel> contentsIterator = contents.iterator();

		if (!todoIterator.hasNext() && !contentsIterator.hasNext()) {
			return null;
		}
		Parcel best = todoIterator.hasNext() ? todoIterator.next() : contentsIterator.next();
		double bestValue = program.compute(createContext(genericContext, best, !todoIterator.hasNext(), true));
		while (todoIterator.hasNext()) {
			final Parcel cur = todoIterator.next();
			final double curValue = program.compute(createContext(genericContext, cur, false, true));
			if (curValue < bestValue) {
				best = cur;
				bestValue = curValue;
			}
		}

		while (contentsIterator.hasNext()) {
			final Parcel cur = contentsIterator.next();
			final double curValue = program.compute(createContext(genericContext, cur, true, true));
			if (curValue < bestValue) {
				best = cur;
				bestValue = curValue;
			}
		}

		return best;
	}

	public static class AuctionPickup extends Pickup {
		@Override
		public void onEntry(TruckEvent event, HeuristicTruck context) {
			super.onEntry(event, context);
			((AuctionTruck) context).todo.remove(context.currentTarget);
		}

	}

	@Override
	protected GendreauContext createContext(GendreauContext gc, Parcel p, boolean isInCargo, boolean isAssignedToVehicle) {
		return new GendreauContext(gc.vehicleDTO, gc.truckPosition, gc.truckContents, ((DefaultParcel) p).dto, gc.time,
				isInCargo, isAssignedToVehicle, 0, gc.otherVehiclePositions, todo);
	}
}
