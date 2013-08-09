/**
 * 
 */
package rinde.evo4mas.gendreau06.comm;

import java.util.Collection;

import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.event.Event;
import rinde.sim.event.EventDispatcher;
import rinde.sim.event.Listener;
import rinde.sim.problem.common.DefaultParcel;

/**
 * This {@link Communicator} implementation allows communication via a
 * blackboard system. It requires the {@link BlackboardCommModel}.
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public class BlackboardUser implements Communicator {

    protected BlackboardCommModel bcModel;
    protected final EventDispatcher eventDispatcher;

    /**
     * Constructor.
     */
    public BlackboardUser() {
        eventDispatcher = new EventDispatcher(CommunicatorEventType.values());
    }

    /**
     * @param model Injects the {@link BlackboardCommModel}.
     */
    public void init(BlackboardCommModel model) {
        bcModel = model;
    }

    public void waitFor(DefaultParcel p) {}

    /**
     * Lay a claim on the specified {@link Parcel}.
     * @param p The parcel to claim.
     */
    public void claim(DefaultParcel p) {
        // forward call to model
        bcModel.claim(this, p);
    }

    /**
     * Notifies this blackboard user of a change in the environment.
     */
    public void update() {
        eventDispatcher.dispatchEvent(new Event(CommunicatorEventType.CHANGE,
                this));
    }

    public void addUpdateListener(Listener l) {
        eventDispatcher.addListener(l, CommunicatorEventType.CHANGE);
    }

    public Collection<DefaultParcel> getParcels() {
        return bcModel.getUnclaimedParcels();
    }

}
