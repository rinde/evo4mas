/**
 * 
 */
package rinde.evo4mas.gendreau06.route;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.model.pdp.Parcel;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class RandomPlanner extends AbstractRoutePlanner {

	protected List<Parcel> assignedParcels;
	protected Parcel current;

	protected final RandomGenerator rng;

	public RandomPlanner(long seed) {
		rng = new MersenneTwister(seed);
	}

	@Override
	public void update(Set<Parcel> parcels, long time) {
		// TODO also use parcels in cargo!
		if (parcels.isEmpty()) {
			assignedParcels = newArrayList();
			current = null;
		} else {
			assignedParcels = newArrayList(parcels);
			next();
		}
	}

	protected void next() {
		current = assignedParcels.remove(rng.nextInt(assignedParcels.size()));
	}

	@Override
	public Parcel peek() {
		return current;
	}

	@Override
	public void remove() {
		next();
	}

	@Override
	public boolean hasNext() {
		return !assignedParcels.isEmpty();
	}

}
