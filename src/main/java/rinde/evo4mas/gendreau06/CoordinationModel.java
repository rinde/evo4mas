/**
 * 
 */
package rinde.evo4mas.gendreau06;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.unmodifiableSet;

import java.util.HashSet;
import java.util.Set;

import rinde.sim.core.model.Model;
import rinde.sim.core.model.pdp.Parcel;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class CoordinationModel implements Model<HeuristicTruck> {

	protected final HashSet<Parcel> claims;

	public CoordinationModel() {
		claims = newHashSet();
	}

	public void claim(Parcel target) {
		checkArgument(!claims.contains(target), "A parcel can be claimed only once!");
		claims.add(target);
	}

	public Set<Parcel> getClaims() {
		return unmodifiableSet(claims);
	}

	public boolean register(HeuristicTruck element) {
		element.setCoordinationModel(this);
		return true;
	}

	public boolean unregister(HeuristicTruck element) {
		return false;
	}

	public Class<HeuristicTruck> getSupportedType() {
		return HeuristicTruck.class;
	}
}
