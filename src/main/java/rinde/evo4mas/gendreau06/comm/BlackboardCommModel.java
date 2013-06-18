/**
 * 
 */
package rinde.evo4mas.gendreau06.comm;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Collections.unmodifiableSet;

import java.util.List;
import java.util.Set;

import rinde.sim.core.model.Model;
import rinde.sim.core.model.pdp.Parcel;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class BlackboardCommModel extends AbstractCommModel implements Model<BlackboardUser> {

	protected final List<BlackboardUser> users;
	protected final Set<Parcel> unclaimedParcels;

	public BlackboardCommModel() {
		users = newArrayList();
		unclaimedParcels = newLinkedHashSet();
	}

	public void claim(BlackboardUser claimer, Parcel p) {
		checkArgument(unclaimedParcels.contains(p));
		unclaimedParcels.remove(p);
		for (final BlackboardUser bu : users) {
			if (bu != claimer) {
				bu.update();
			}
		}
	}

	@Override
	protected void receiveParcel(Parcel p, long time) {
		unclaimedParcels.add(p);
		// notify all users of the new parcel
		for (final BlackboardUser bu : users) {
			bu.update();
		}
	}

	public Set<Parcel> getUnclaimedParcels() {
		return unmodifiableSet(unclaimedParcels);
	}

	public boolean register(BlackboardUser element) {
		users.add(element);
		element.init(this);
		return true;
	}

	public boolean unregister(BlackboardUser element) {
		throw new UnsupportedOperationException();
	}

	public Class<BlackboardUser> getSupportedType() {
		return BlackboardUser.class;
	}
}
