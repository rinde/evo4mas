/**
 * 
 */
package rinde.evo4mas.gendreau06.route;

import static com.google.common.collect.Lists.newLinkedList;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomAdaptor;

import rinde.sim.core.model.pdp.Parcel;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class RandomPlanner extends AbstractRoutePlanner {

	protected Queue<Parcel> assignedParcels;
	protected final Random rng;

	public RandomPlanner(long seed) {
		rng = new RandomAdaptor(new MersenneTwister(seed));
		assignedParcels = newLinkedList();
	}

	@Override
	protected void doUpdate(Collection<Parcel> onMap, Collection<Parcel> inCargo, long time) {
		if (onMap.isEmpty() && inCargo.isEmpty()) {
			assignedParcels = newLinkedList();
		} else {
			final LinkedList<Parcel> ps = newLinkedList(onMap);
			ps.addAll(onMap);
			ps.addAll(inCargo);
			Collections.shuffle(ps, rng);
			assignedParcels = ps;
		}
	}

	@Override
	public void nextImpl(long time) {
		assignedParcels.poll();
	}

	public boolean hasNext() {
		return !assignedParcels.isEmpty();
	}

	public Parcel current() {
		return assignedParcels.peek();
	}

}
