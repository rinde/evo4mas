/**
 *
 */
package com.github.rinde.evo4mas.gendreau06.route;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.github.rinde.ecj.PriorityHeuristic;
import com.github.rinde.evo4mas.gendreau06.GendreauContext;
import com.github.rinde.evo4mas.gendreau06.GendreauContextBuilder;
import com.github.rinde.evo4mas.gendreau06.GendreauFunctions.TimeUntilAvailable;
import com.github.rinde.logistics.pdptw.mas.route.AbstractRoutePlanner;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.geom.Graphs.Heuristic;
import com.github.rinde.rinsim.util.StochasticSupplier;
import com.github.rinde.rinsim.util.StochasticSuppliers.AbstractStochasticSupplier;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

/**
 * A {@link com.github.rinde.logistics.pdptw.mas.route.RoutePlanner}
 * implementation that uses a (evolved) {@link Heuristic} for determining its
 * route. The route is build incrementally, one hop at a time.
 * @author Rinde van Lon
 */
public final class EvoHeuristicRoutePlanner extends AbstractRoutePlanner {

  private final PriorityHeuristic<GendreauContext> heuristic;
  private final TimeUntilAvailable<GendreauContext> tua;
  private Optional<Parcel> current;
  private Optional<GendreauContextBuilder> gendreauContextBuilder;

  private Set<Parcel> onMapSet;
  private Set<Parcel> inCargoSet;

  /**
   * Create a new route planner using the specified {@link Heuristic}.
   * @param h The heuristic to use for planning routes.
   */
  public EvoHeuristicRoutePlanner(PriorityHeuristic<GendreauContext> h) {
    heuristic = h;
    tua = new TimeUntilAvailable<GendreauContext>();
    onMapSet = newHashSet();
    inCargoSet = newHashSet();
    current = Optional.absent();
    gendreauContextBuilder = Optional.absent();
  }

  @Override
  protected void doUpdate(Set<Parcel> onMap, long time) {
    onMapSet = newHashSet(onMap);
    final Collection<Parcel> checked = Collections.checkedCollection(
        pdpModel.get().getContents(vehicle.get()), Parcel.class);
    inCargoSet = newHashSet(checked);
    computeCurrent(time);
  }

  protected void computeCurrent(long time) {
    final Set<Parcel> claimed = ImmutableSet.of();
    current = nextLoop(onMapSet, claimed, inCargoSet, time);
  }

  protected Optional<Parcel> nextLoop(Collection<Parcel> todo,
      Set<Parcel> alreadyClaimed, Collection<Parcel> contents,
      long time) {
    Optional<Parcel> best = Optional.absent();
    double bestValue = Double.POSITIVE_INFINITY;

    final GendreauContextBuilder gcb = gendreauContextBuilder.get();
    gcb.initRepeatedUsage(time);

    final StringBuilder sb = new StringBuilder();
    for (final Parcel p : todo) {
      // filter out the already claimed parcels
      if (!alreadyClaimed.contains(p)) {
        final GendreauContext gc = gcb.buildInRepetition(p, false, false);
        @SuppressWarnings("null")
        final double res = tua.execute(null, gc);

        // TODO this should be a differnt value? similar to isEarly
        if (res < 1000) {
          final double v = heuristic.compute(gc);

          sb.append(p).append(" ").append(v).append("\n");
          if (v < bestValue
              || ((Double.isInfinite(v) || Double.isNaN(v))
                  && bestValue == v)) {
            best = Optional.of(p);
            bestValue = v;
          }
        }
      }
    }
    for (final Parcel p : contents) {

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

  @Override
  public boolean hasNext() {
    return !isUpdated() ? false : !(onMapSet.isEmpty() && inCargoSet.isEmpty());
  }

  @Override
  public Optional<Parcel> current() {
    return current;
  }

  @Override
  protected void nextImpl(long time) {
    if (!current().isPresent()) {
      return;
    }
    final Parcel p = current.get();
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
  protected void afterInit() {
    gendreauContextBuilder = Optional.of(new GendreauContextBuilder(roadModel
        .get(), pdpModel.get(), vehicle.get()));
  }

  public static StochasticSupplier<EvoHeuristicRoutePlanner> supplier(
      final PriorityHeuristic<GendreauContext> h) {
    return new AbstractStochasticSupplier<EvoHeuristicRoutePlanner>() {
      @Override
      public EvoHeuristicRoutePlanner get(long seed) {
        return new EvoHeuristicRoutePlanner(h);
      }
    };

  }
}
