/**
 * 
 */
package rinde.evo4mas.fabrirecht;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Sets.newLinkedHashSet;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rinde.sim.core.model.Model;
import rinde.sim.core.model.ModelProvider;
import rinde.sim.core.model.ModelReceiver;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.PDPModel.PDPModelEvent;
import rinde.sim.core.model.pdp.PDPModel.PDPModelEventType;
import rinde.sim.event.Event;
import rinde.sim.event.Listener;
import rinde.sim.problem.common.DefaultParcel;
import rinde.sim.problem.common.ParcelDTO;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class CoordModel implements Model<CoordAgent>, Listener, ModelReceiver {
    Set<CoordAgent> knownAgents;
    // protected final Map<Parcel, ServiceAssignment> parcelAssignments;
    // protected final Map<CoordAgent, Parcel> agentParcelAssignments;

    Map<ParcelDTO, CoordAgent> assignments;

    public CoordModel() {
        // parcelAssignments = newLinkedHashMap();
        // agentParcelAssignments = newLinkedHashMap();
        knownAgents = newLinkedHashSet();
        assignments = newLinkedHashMap();
    }

    public void registerModelProvider(ModelProvider mp) {
        // TODO Auto-generated method stub
        mp.getModel(PDPModel.class).getEventAPI()
                .addListener(this, PDPModelEventType.NEW_PARCEL);
    }

    // public boolean canServe(Parcel p, double val) {
    // if (parcelAssignments.containsKey(p)) {
    // return val < parcelAssignments.get(p).value;
    // }
    // return true;
    // }

    // public void doServe(Parcel p, CoordAgent a, double val) {
    // if (parcelAssignments.containsKey(p)) {
    // parcelAssignments.get(p).agent.notifyServiceChange();
    // }
    // parcelAssignments.put(p, new ServiceAssignment(p, a, val));
    // // if this agent has said to be serving some other parcel before, this
    // // assignment should be removed.
    // if (agentParcelAssignments.containsKey(a) &&
    // parcelAssignments.containsKey(agentParcelAssignments.get(a))
    // && parcelAssignments.get(agentParcelAssignments.get(a)).agent == a) {
    // parcelAssignments.remove(agentParcelAssignments.get(a));
    // }
    // agentParcelAssignments.put(a, p);
    // }

    public boolean register(CoordAgent element) {
        knownAgents.add(element);
        element.setCoordModel(this);
        return true;
    }

    public boolean unregister(CoordAgent element) {
        throw new UnsupportedOperationException();
    }

    public Class<CoordAgent> getSupportedType() {
        return CoordAgent.class;
    }

    // class ServiceAssignment {
    // public final Parcel parcel;
    // public final double value;
    // public final CoordAgent agent;
    //
    // public ServiceAssignment(Parcel p, CoordAgent a, double v) {
    // parcel = p;
    // value = v;
    // agent = a;
    // }
    // }

    public void handleEvent(Event e) {
        final PDPModelEvent pe = (PDPModelEvent) e;
        final DefaultParcel newParcel = (DefaultParcel) pe.parcel;
        if (assignments.containsKey(newParcel.dto)) {
            final CoordAgent ca = assignments.get(newParcel.dto);
            ca.receiveOrder(newParcel);
        }
    }

    public boolean acceptParcel(ParcelDTO dto) {
        if (dto.pickupTimeWindow.length() < dto.pickupDuration
                || dto.deliveryTimeWindow.length() < dto.deliveryDuration) {
            // System.out.println("kickout impossible parcel");
            return false;
        }

        // do auction for every newly arrived parcel
        final List<Bid> bids = newArrayList();
        for (final CoordAgent a : knownAgents) {
            bids.add(new Bid(a, a.getCost(dto)));
        }
        Collections.sort(bids);

        // System.out.println(bids);

        // System.out.println("Accept parcel? " + dto);
        for (final Bid b : bids) {
            if (b.bidder.isFeasible(dto)) {
                // System.out.println(" > ACCEPT");
                // the assignment is saved here. it will be added as soon as the
                // parcel is created on the map.
                assignments.put(dto, b.bidder);
                return true;
            }
            // System.out.println(" > not yet ..");
        }
        // System.out.println(" > NO");
        // System.out.println("the new parcel could not be assigned to anyone");
        return false;
    }

    class Bid implements Comparable<Bid> {
        public final CoordAgent bidder;
        public final double cost;

        public Bid(CoordAgent agent, double c) {
            bidder = agent;
            cost = c;
        }

        public int compareTo(Bid o) {
            return Double.compare(cost, o.cost);
        }

        @Override
        public String toString() {
            return new StringBuilder("[").append(bidder).append(",")
                    .append(cost).append("]").toString();
        }
    }

}
