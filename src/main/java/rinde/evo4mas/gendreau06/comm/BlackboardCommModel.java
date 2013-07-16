/**
 * 
 */
package rinde.evo4mas.gendreau06.comm;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Collections.unmodifiableSet;

import java.util.Set;

import rinde.sim.core.model.pdp.Parcel;

/**
 * This is an implementation of a blackboard communication model. It allows
 * {@link BlackboardUser}s to claim {@link Parcel}s. Via the blackboard, all
 * other {@link BlackboardUser}s are notified of claims. With this communication
 * strategy race conditions between {@link BlackboardUser}s can be prevented.
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public class BlackboardCommModel extends AbstractCommModel<BlackboardUser> {

	protected final Set<Parcel> unclaimedParcels;

	/**
	 * New empty blackboard comm model.
	 */
	public BlackboardCommModel() {
		unclaimedParcels = newLinkedHashSet();
	}

	/**
	 * Lays a claim on the specified {@link Parcel}. This means that this parcel
	 * is no longer available to other {@link BlackboardUser}s.
	 * @param claimer The user that claims the parcel.
	 * @param p The parcel that is claimed.
	 */
	public void claim(BlackboardUser claimer, Parcel p) {
		checkArgument(unclaimedParcels.contains(p));
		unclaimedParcels.remove(p);
		for (final BlackboardUser bu : communicators) {
			if (bu != claimer) {
				bu.update();
			}
		}
	}

	@Override
	protected void receiveParcel(Parcel p, long time) {
		unclaimedParcels.add(p);
		// notify all users of the new parcel
		for (final BlackboardUser bu : communicators) {
			bu.update();
		}
	}

	/**
	 * @return All unclaimed parcels.
	 */
	public Set<Parcel> getUnclaimedParcels() {
		return unmodifiableSet(unclaimedParcels);
	}

	@Override
	public boolean register(BlackboardUser element) {
		super.register(element);
		element.init(this);
		return true;
	}

	public Class<BlackboardUser> getSupportedType() {
		return BlackboardUser.class;
	}
}
