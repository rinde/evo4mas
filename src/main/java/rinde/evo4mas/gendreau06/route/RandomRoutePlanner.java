/**
 * 
 */
package rinde.evo4mas.gendreau06.route;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Lists.newLinkedList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import javax.annotation.Nullable;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomAdaptor;

import rinde.sim.core.model.pdp.Parcel;

/**
 * A {@link RoutePlanner} implementation that creates random routes.
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public class RandomRoutePlanner extends AbstractRoutePlanner {

    protected Queue<Parcel> assignedParcels;
    protected final Random rng;

    /**
     * Creates a random route planner using the specified random seed.
     * @param seed The random seed.
     */
    public RandomRoutePlanner(long seed) {
        rng = new RandomAdaptor(new MersenneTwister(seed));
        assignedParcels = newLinkedList();
    }

    @Override
    protected void doUpdate(Collection<Parcel> onMap, long time) {
        checkState(pdpModel != null && vehicle != null);
        final Collection<Parcel> inCargo = pdpModel.getContents(vehicle);
        if (onMap.isEmpty() && inCargo.isEmpty()) {
            assignedParcels.clear();
        } else {
            final List<Parcel> ps = newArrayListWithCapacity((onMap.size() * 2)
                    + inCargo.size());
            // Parcels on map need to be visited twice, once for pickup, once
            // for delivery.
            ps.addAll(onMap);
            ps.addAll(onMap);
            ps.addAll(inCargo);
            Collections.shuffle(ps, rng);
            assignedParcels = newLinkedList(ps);
        }
    }

    @Override
    public void nextImpl(long time) {
        assignedParcels.poll();
    }

    public boolean hasNext() {
        return !assignedParcels.isEmpty();
    }

    @Nullable
    public Parcel current() {
        return assignedParcels.peek();
    }

}
