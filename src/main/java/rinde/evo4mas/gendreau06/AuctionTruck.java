/**
 * 
 */
package rinde.evo4mas.gendreau06;

import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import rinde.ecj.Heuristic;
import rinde.sim.core.model.pdp.Parcel;
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
		super(pDto, p);

		todo = newHashSet();

	}

	public double getBidFor(AuctionParcel ap, long time) {
		return program.compute(createFullContext(time, ap, false));
	}

	public void receiveParcel(AuctionParcel ap) {
		todo.add(ap);
	}

	@Override
	protected Parcel next(long time) {
		final Collection<Parcel> contents = pdpModel.getContents(this);
		final GendreauContext genericContext = createGenericContext(time);

		final Iterator<Parcel> todoIterator = Collections2.filter(todo, new Predicate<Parcel>() {
			public boolean apply(Parcel input) {
				return tua.execute(null, createContext(genericContext, input, false)) < 1000;
			}
		}).iterator();
		final Iterator<Parcel> contentsIterator = contents.iterator();

		if (!todoIterator.hasNext() && !contentsIterator.hasNext()) {
			return null;
		}
		Parcel best = todoIterator.hasNext() ? todoIterator.next() : contentsIterator.next();
		double bestValue = program.compute(createContext(genericContext, best, !todoIterator.hasNext()));
		while (todoIterator.hasNext()) {
			final Parcel cur = todoIterator.next();
			final double curValue = program.compute(createContext(genericContext, cur, false));
			if (curValue < bestValue) {
				best = cur;
				bestValue = curValue;
			}
		}

		while (contentsIterator.hasNext()) {
			final Parcel cur = contentsIterator.next();
			final double curValue = program.compute(createContext(genericContext, cur, true));
			if (curValue < bestValue) {
				best = cur;
				bestValue = curValue;
			}
		}

		return best;
	}
}
