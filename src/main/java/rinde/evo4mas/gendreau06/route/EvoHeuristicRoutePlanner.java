/**
 * 
 */
package rinde.evo4mas.gendreau06.route;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.annotation.Nullable;

import rinde.ecj.Heuristic;
import rinde.evo4mas.gendreau06.GCBuilderReceiver;
import rinde.evo4mas.gendreau06.GendreauContext;
import rinde.evo4mas.gendreau06.GendreauContextBuilder;
import rinde.evo4mas.gendreau06.GendreauFunctions.TimeUntilAvailable;
import rinde.sim.pdptw.common.DefaultParcel;

import com.google.common.collect.ImmutableSet;

/**
 * A {@link RoutePlanner} implementation that uses a (evolved) {@link Heuristic}
 * for determining its route. The route is build incrementally, one hop at a
 * time.
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public class EvoHeuristicRoutePlanner extends AbstractRoutePlanner implements
        GCBuilderReceiver {

    protected final Heuristic<GendreauContext> heuristic;
    protected final TimeUntilAvailable<GendreauContext> tua;
    @Nullable
    protected DefaultParcel current;
    @Nullable
    protected GendreauContextBuilder gendreauContextBuilder;

    protected Set<DefaultParcel> onMapSet;
    protected Set<DefaultParcel> inCargoSet;

    /**
     * Create a new route planner using the specified {@link Heuristic}.
     * @param h The heuristic to use for planning routes.
     */
    public EvoHeuristicRoutePlanner(Heuristic<GendreauContext> h) {
        heuristic = h;
        tua = new TimeUntilAvailable<GendreauContext>();
        onMapSet = newHashSet();
        inCargoSet = newHashSet();
    }

    @Override
    protected void doUpdate(Collection<DefaultParcel> onMap, long time) {
        onMapSet = newHashSet(onMap);
        checkState(pdpModel != null && vehicle != null);
        // this is safe because the code actually checks the type
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final Collection<DefaultParcel> checked = Collections
                .checkedCollection((Collection) pdpModel.getContents(vehicle), DefaultParcel.class);
        inCargoSet = newHashSet(checked);
        computeCurrent(time);
    }

    protected void computeCurrent(long time) {
        final Set<DefaultParcel> claimed = ImmutableSet.of();
        current = nextLoop(onMapSet, claimed, inCargoSet, time);
    }

    @Nullable
    protected DefaultParcel nextLoop(Collection<DefaultParcel> todo,
            Set<DefaultParcel> alreadyClaimed,
            Collection<DefaultParcel> contents, long time) {
        DefaultParcel best = null;
        double bestValue = Double.POSITIVE_INFINITY;

        final GendreauContextBuilder gcb = gendreauContextBuilder;
        if (gcb == null) {
            throw new RuntimeException(
                    "GendreauContextBuilder has not been set via #receive(GendreauContextBuilder)");
        }
        gcb.initRepeatedUsage(time);

        final StringBuilder sb = new StringBuilder();
        for (final DefaultParcel p : todo) {
            // filter out the already claimed parcels
            if (!alreadyClaimed.contains(p)) {
                final GendreauContext gc = gcb
                        .buildInRepetition(p, false, false);
                @SuppressWarnings("null")
                final double res = tua.execute(null, gc);

                // TODO this should be a differnt value? similar to isEarly
                if (res < 1000) {
                    final double v = heuristic.compute(gc);

                    sb.append(p).append(" ").append(v).append("\n");
                    if (v < bestValue
                            || ((Double.isInfinite(v) || Double.isNaN(v)) && bestValue == v)) {
                        best = p;
                        bestValue = v;
                    }
                }
            }
        }
        // if (best == null) {
        // System.err.println(sb.toString());
        // System.err.println(bestValue);
        // }
        for (final DefaultParcel p : contents) {

            final GendreauContext gc = gcb.buildInRepetition(p, true, false);

            final double v = heuristic.compute(gc);
            if (v < bestValue
                    || ((Double.isInfinite(v) || Double.isNaN(v)) && bestValue == v)) {
                best = p;
                bestValue = v;
            }
        }
        if (best == null) {
            // System.out.println("todo: " + todo + "\ncontents: " + contents +
            // "\nclaimed: " + alreadyClaimed);
        }

        return best;
    }

    /*
     * protected GendreauContext createContext(GendreauContext gc, Parcel p,
     * boolean isInCargo, boolean isAssignedToVehicle) { final int numWaiters =
     * 0;// isInCargo ? // coordinationModel.getNumWaitersFor(p) : 0 return new
     * GendreauContext(gc.vehicleDTO, gc.truckPosition, gc.truckContents,
     * ((DefaultParcel) p).dto, gc.time, isInCargo, isAssignedToVehicle,
     * numWaiters, gc.otherVehiclePositions, new HashSet<Parcel>()); } protected
     * GendreauContext createGenericContext(long time) { final
     * Collection<Parcel> contents = pdpModel.getContents(truck); final
     * List<Point> positions = newArrayList(); final Set<Vehicle> vehicles =
     * pdpModel.getVehicles(); for (final Vehicle v : vehicles) { if (v !=
     * truck) { positions.add(roadModel.getPosition(v)); } } return new
     * GendreauContext(truck.getDTO(), roadModel.getPosition(truck),
     * convert(contents), null, time, false, false, -1, positions, new
     * HashSet<Parcel>()); } protected static Set<ParcelDTO>
     * convert(Collection<Parcel> parcels) { final Set<ParcelDTO> dtos =
     * newLinkedHashSet(); for (final Parcel p : parcels) {
     * dtos.add(((DefaultParcel) p).dto); } return dtos; }
     */

    public boolean hasNext() {
        return !isUpdated() ? false : !(onMapSet.isEmpty() && inCargoSet
                .isEmpty());
    }

    @Nullable
    public DefaultParcel current() {
        return current;
    }

    @Override
    protected void nextImpl(long time) {
        if (current() == null) {
            return;
        }
        // current should exist in exactly one of the sets
        checkArgument(onMapSet.contains(current) ^ inCargoSet.contains(current), "current: %s should exist in one of the sets", current);
        if (onMapSet.contains(current)) {
            inCargoSet.add(current);
            onMapSet.remove(current);
        } else {
            inCargoSet.remove(current);
        }
        computeCurrent(time);
    }

    public void receive(GendreauContextBuilder gcb) {
        gendreauContextBuilder = gcb;

    }
}
