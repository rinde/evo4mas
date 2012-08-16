/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import static com.google.common.collect.Maps.newLinkedHashMap;

import java.util.Map;

import rinde.sim.core.model.Model;
import rinde.sim.core.model.pdp.Parcel;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class CoordModel implements Model<CoordAgent> {
	protected final Map<Parcel, ServiceAssignment> parcelAssignments;
	protected final Map<CoordAgent, Parcel> agentParcelAssignments;

	public CoordModel() {
		parcelAssignments = newLinkedHashMap();
		agentParcelAssignments = newLinkedHashMap();
	}

	public boolean canServe(Parcel p, double val) {
		if (parcelAssignments.containsKey(p)) {
			return val < parcelAssignments.get(p).value;
		}
		return true;
	}

	public void doServe(Parcel p, CoordAgent a, double val) {
		if (parcelAssignments.containsKey(p)) {
			parcelAssignments.get(p).agent.notifyServiceChange();
		}
		parcelAssignments.put(p, new ServiceAssignment(p, a, val));
		// if this agent has said to be serving some other parcel before, this
		// assignment should be removed.
		if (agentParcelAssignments.containsKey(a) && parcelAssignments.containsKey(agentParcelAssignments.get(a))
				&& parcelAssignments.get(agentParcelAssignments.get(a)).agent == a) {
			parcelAssignments.remove(agentParcelAssignments.get(a));
		}
		agentParcelAssignments.put(a, p);
	}

	public boolean register(CoordAgent element) {
		element.setCoordModel(this);
		return true;
	}

	public boolean unregister(CoordAgent element) {
		throw new UnsupportedOperationException();
	}

	public Class<CoordAgent> getSupportedType() {
		return CoordAgent.class;
	}

	class ServiceAssignment {
		public final Parcel parcel;
		public final double value;
		public final CoordAgent agent;

		public ServiceAssignment(Parcel p, CoordAgent a, double v) {
			parcel = p;
			value = v;
			agent = a;
		}
	}

}
