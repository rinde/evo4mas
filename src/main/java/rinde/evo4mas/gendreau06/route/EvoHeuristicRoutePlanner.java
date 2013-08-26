/**
 * 
 */
package rinde.evo4mas.gendreau06.route;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import rinde.ecj.Heuristic;
import rinde.evo4mas.gendreau06.GendreauContext;
import rinde.evo4mas.gendreau06.GendreauContextBuilder;
import rinde.evo4mas.gendreau06.GendreauFunctions.TimeUntilAvailable;
import rinde.logistics.pdptw.mas.route.AbstractRoutePlanner;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.pdptw.common.DefaultParcel;
import rinde.sim.pdptw.common.DefaultVehicle;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

/**
 * A {@link rinde.logistics.pdptw.mas.route.RoutePlanner} implementation that
 * uses a (evolved) {@link Heuristic} for determining its route. The route is
 * build incrementally, one hop at a time.
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public class EvoHeuristicRoutePlanner extends AbstractRoutePlanner {

    protected final Heuristic<GendreauContext> heuristic;
    protected final TimeUntilAvailable<GendreauContext> tua;
    protected Optional<DefaultParcel> current;
    protected Optional<GendreauContextBuilder> gendreauContextBuilder;

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
        current = Optional.absent();
        gendreauContextBuilder = Optional.absent();
    }

    @Override
    protected void doUpdate(Collection<DefaultParcel> onMap, long time) {
        onMapSet = newHashSet(onMap);
        // this is safe because the code actually checks the type
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final Collection<DefaultParcel> checked =
                Collections.checkedCollection((Collection) pdpModel.get()
                        .getContents(vehicle.get()), DefaultParcel.class);
        inCargoSet = newHashSet(checked);
        computeCurrent(time);
    }

    protected void computeCurrent(long time) {
        final Set<DefaultParcel> claimed = ImmutableSet.of();
        current = nextLoop(onMapSet, claimed, inCargoSet, time);
    }

    protected Optional<DefaultParcel> nextLoop(Collection<DefaultParcel> todo,
            Set<DefaultParcel> alreadyClaimed,
            Collection<DefaultParcel> contents, long time) {
        Optional<DefaultParcel> best = Optional.absent();
        double bestValue = Double.POSITIVE_INFINITY;

        final GendreauContextBuilder gcb = gendreauContextBuilder.get();
        gcb.initRepeatedUsage(time);

        final StringBuilder sb = new StringBuilder();
        for (final DefaultParcel p : todo) {
            // filter out the already claimed parcels
            if (!alreadyClaimed.contains(p)) {
                final GendreauContext gc =
                        gcb.buildInRepetition(p, false, false);
                @SuppressWarnings("null")
                final double res = tua.execute(null, gc);

                // TODO this should be a differnt value? similar to isEarly
                if (res < 1000) {
                    final double v = heuristic.compute(gc);

                    sb.append(p).append(" ").append(v).append("\n");
                    if (v < bestValue
                            || ((Double.isInfinite(v) || Double.isNaN(v)) && bestValue == v)) {
                        best = Optional.of(p);
                        bestValue = v;
                    }
                }
            }
        }
        for (final DefaultParcel p : contents) {

            final GendreauContext gc = gcb.buildInRepetition(p, true, false);

            final double v = heuristic.compute(gc);
            if (v < bestValue
                    || ((Double.isInfinite(v) || Double.isNaN(v)) && bestValue == v)) {
                best = Optional.of(p);
                bestValue = v;
            }
        }
        return best;
    }

    public boolean hasNext() {
        return !isUpdated() ? false : !(onMapSet.isEmpty() && inCargoSet
                .isEmpty());
    }

    public Optional<DefaultParcel> current() {
        return current;
    }

    @Override
    protected void nextImpl(long time) {
        if (!current().isPresent()) {
            return;
        }
        final DefaultParcel p = current.get();
        // current should exist in exactly one of the sets
        checkArgument(onMapSet.contains(p) ^ inCargoSet.contains(p),
            "current: %s should exist in one of the sets", p);
        if (onMapSet.contains(p)) {
            inCargoSet.add(p);
            onMapSet.remove(p);
        } else {
            inCargoSet.remove(p);
        }
        computeCurrent(time);
    }

    @Override
    public void init(RoadModel rm, PDPModel pm, DefaultVehicle dv) {
        super.init(rm, pm, dv);
        gendreauContextBuilder =
                Optional.of(new GendreauContextBuilder(rm, pm, dv));
    }
}
