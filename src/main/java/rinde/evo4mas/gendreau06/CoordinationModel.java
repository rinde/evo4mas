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

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class CoordinationModel implements Model<MyopicTruck> {
	// Blackboard communication model

	protected final HashSet<Parcel> claims;
	protected final Multimap<Parcel, MyopicTruck> waiting;

	public CoordinationModel() {
		claims = newHashSet();
		waiting = LinkedHashMultimap.create();
	}

	public void claim(Parcel target) {
		checkArgument(!claims.contains(target), "A parcel can be claimed only once!");
		claims.add(target);
	}

	public void unclaim(Parcel target) {
		checkArgument(claims.contains(target));
		claims.remove(target);
	}

	public void waitFor(MyopicTruck t, Parcel p) {
		checkArgument(!waiting.containsEntry(p, t));
		waiting.put(p, t);
	}

	public void unwaitFor(MyopicTruck t, Parcel p) {
		checkArgument(waiting.containsEntry(p, t));
		waiting.remove(p, t);
	}

	public int getNumWaitersFor(Parcel p) {
		return waiting.get(p).size();
	}

	public Set<Parcel> getClaims() {
		return unmodifiableSet(claims);
	}

	public boolean register(MyopicTruck element) {
		element.setCoordinationModel(this);
		return true;
	}

	public boolean unregister(MyopicTruck element) {
		return false;
	}

	public Class<MyopicTruck> getSupportedType() {
		return MyopicTruck.class;
	}
}
