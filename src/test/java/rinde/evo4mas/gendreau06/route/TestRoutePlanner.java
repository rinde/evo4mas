/**
 * 
 */
package rinde.evo4mas.gendreau06.route;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newLinkedList;

import java.util.Collection;
import java.util.Queue;

import rinde.sim.core.model.pdp.Parcel;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class TestRoutePlanner extends AbstractRoutePlanner {

    protected final Queue<Parcel> route;

    public TestRoutePlanner() {
        route = newLinkedList();
    }

    public Parcel current() {
        return route.peek();
    }

    public boolean hasNext() {
        return !route.isEmpty();
    }

    @Override
    protected void doUpdate(Collection<Parcel> onMap, long time) {
        checkState(pdpModel != null && vehicle != null);
        final Collection<Parcel> inCargo = pdpModel.getContents(vehicle);
        route.clear();
        route.addAll(onMap);
        route.addAll(inCargo);
        route.addAll(onMap);
    }

    @Override
    protected void nextImpl(long time) {
        route.poll();
    }

}
