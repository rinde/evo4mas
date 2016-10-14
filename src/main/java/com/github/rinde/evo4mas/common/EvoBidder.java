/*
 * Copyright (C) 2011-2016 Rinde van Lon, iMinds-DistriNet, KU Leuven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.rinde.evo4mas.common;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;

import com.github.rinde.ecj.PriorityHeuristic;
import com.github.rinde.logistics.pdptw.mas.Truck;
import com.github.rinde.logistics.pdptw.mas.comm.AbstractBidder;
import com.github.rinde.logistics.pdptw.mas.comm.Auctioneer;
import com.github.rinde.logistics.pdptw.mas.comm.Bidder;
import com.github.rinde.logistics.pdptw.mas.comm.DoubleBid;
import com.github.rinde.logistics.pdptw.mas.comm.ForwardingBidder;
import com.github.rinde.logistics.pdptw.mas.comm.SetFactories;
import com.github.rinde.logistics.pdptw.solver.CheapestInsertionHeuristic;
import com.github.rinde.rinsim.central.GlobalStateObject;
import com.github.rinde.rinsim.central.SimSolverBuilder;
import com.github.rinde.rinsim.central.Solver;
import com.github.rinde.rinsim.central.SolverUser;
import com.github.rinde.rinsim.central.Solvers;
import com.github.rinde.rinsim.central.Solvers.MeasureableSolver;
import com.github.rinde.rinsim.central.Solvers.SolveArgs;
import com.github.rinde.rinsim.central.Solvers.SolverTimeMeasurement;
import com.github.rinde.rinsim.central.rt.RealtimeSolver;
import com.github.rinde.rinsim.central.rt.RtSimSolver;
import com.github.rinde.rinsim.central.rt.RtSimSolver.EventType;
import com.github.rinde.rinsim.central.rt.RtSimSolver.SolverEvent;
import com.github.rinde.rinsim.central.rt.RtSimSolverBuilder;
import com.github.rinde.rinsim.central.rt.RtSolverModel;
import com.github.rinde.rinsim.central.rt.RtSolverUser;
import com.github.rinde.rinsim.central.rt.RtStAdapters;
import com.github.rinde.rinsim.central.rt.SleepySolver;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.event.Event;
import com.github.rinde.rinsim.event.EventAPI;
import com.github.rinde.rinsim.event.Listener;
import com.github.rinde.rinsim.pdptw.common.ObjectiveFunction;
import com.github.rinde.rinsim.pdptw.common.StatisticsDTO;
import com.github.rinde.rinsim.scenario.gendreau06.Gendreau06ObjectiveFunction;
import com.github.rinde.rinsim.util.StochasticSupplier;
import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Queues;

/**
 *
 * @author Rinde van Lon
 */
public class EvoBidder
    extends AbstractBidder<DoubleBid>
    implements RtSolverUser, TickListener {

  // 5 minutes
  private static final long MAX_LOSING_TIME = 5 * 60 * 1000;

  final Gendreau06ObjectiveFunction objectiveFunction;
  Optional<RtSimSolver> solverHandle;
  final Queue<CallForBids> cfbQueue;
  Listener currentListener;
  Map<Parcel, Auctioneer<DoubleBid>> parcelAuctioneers;

  AtomicBoolean reauctioning;
  AtomicBoolean computing;

  long lastAuctionWinTime;

  // this field will either be set to the decorator reference (if the bidder is
  // decorated) or it will not be set, in that case it will refer to 'this'.
  // This field prevents the decorated bidder from leaking from its decorator.
  @Nullable
  Bidder<DoubleBid> decorator;
  final SolverAdapter heuristic;
  private final RealtimeSolver solver;
  private final ParcelSwapSelector parcelSwapSelector;
  private final long reauctionCooldownPeriod;
  private final Optional<MeasureableSolver> measureDecorator;
  private final long computationDelay;

  EvoBidder(Gendreau06ObjectiveFunction objFunc,
      PriorityHeuristic<GpGlobal> h,
      long cooldown,
      ParcelSwapSelectorType selector,
      boolean isTimeMeasurementEnabled,
      long compDelay) {
    super(SetFactories.synchronizedFactory(SetFactories.linkedHashSet()));
    objectiveFunction = objFunc;
    heuristic = new SolverAdapter(h, objectiveFunction);

    // decorator galore! ;-)
    Solver deco = heuristic;
    if (compDelay > 0L) {
      deco = SleepySolver.create(compDelay, heuristic);
    }

    if (isTimeMeasurementEnabled) {
      measureDecorator =
        Optional.of(Solvers.timeMeasurementDecorator(deco));
      solver = RtStAdapters.toRealtime(measureDecorator.get());
    } else {
      measureDecorator = Optional.absent();
      solver = RtStAdapters.toRealtime(deco);
    }
    computationDelay = compDelay;

    solverHandle = Optional.absent();
    cfbQueue = Queues.synchronizedQueue(new LinkedList<CallForBids>());
    parcelAuctioneers = new LinkedHashMap<>();
    reauctioning = new AtomicBoolean();
    computing = new AtomicBoolean();
    reauctionCooldownPeriod = cooldown;
    if (selector == ParcelSwapSelectorType.OBJ_FUNC) {
      parcelSwapSelector = new ObjFuncSelector(objFunc);
    } else {
      parcelSwapSelector =
        new PriorityHeuristicSelector(objectiveFunction, heuristic.heuristic);
    }
  }

  // throws IllegalStateException if the option is not enabled
  public List<SolverTimeMeasurement> getTimeMeasurements() {
    checkState(measureDecorator.isPresent());
    return measureDecorator.get().getTimeMeasurements();
  }

  @Override
  public void callForBids(final Auctioneer<DoubleBid> auctioneer,
      final Parcel parcel, final long time) {
    LOGGER.trace("{} receive callForBids {} {} {}", decorator, auctioneer,
      parcel, time);
    cfbQueue.add(CallForBids.create(auctioneer, parcel, time));
    parcelAuctioneers.put(parcel, auctioneer);

    // avoid multiple bids at the same time
    checkState(solverHandle.isPresent(),
      "A %s could not be obtained, probably missing a %s.",
      RtSimSolver.class.getSimpleName(),
      RtSolverModel.class.getSimpleName());
    next();
  }

  @Override
  public void afterInit() {
    super.afterInit();
    if (decorator == null) {
      decorator = this;
    }
    ((Truck) vehicle.get()).getEventAPI().addListener(new Listener() {
      @Override
      public void handleEvent(Event e) {
        LOGGER.trace("{} Route change -> reauction", vehicle.get());
        reauction();
      }
    }, Truck.TruckEvent.ROUTE_CHANGE);
  }

  @Override
  public void endOfAuction(Auctioneer<DoubleBid> auctioneer, Parcel parcel,
      long time) {
    final CallForBids endedAuction =
      CallForBids.create(auctioneer, parcel, time);

    // we have won
    if (equals(auctioneer.getWinner())) {
      lastAuctionWinTime = time;
    }

    synchronized (solverHandle.get().getLock()) {
      synchronized (computing) {
        if (computing.get()) {
          // if current computation is about this auction -> cancel it
          if (endedAuction.equals(cfbQueue.peek())) {
            LOGGER.info("{} cancel computation", decorator);

            computing.set(false);
            final EventAPI ev = solverHandle.get().getEventAPI();

            // in some cases the listener is already removed because it was
            // called before it could be removed, we can safely ignore this
            if (ev.containsListener(currentListener, EventType.DONE)) {
              ev.removeListener(currentListener, EventType.DONE);
            }
            solverHandle.get().cancel();
          }
          cfbQueue.remove(endedAuction);
          next();
        }
      }
    }

    if (!equals(auctioneer.getWinner())
      && time - lastAuctionWinTime > MAX_LOSING_TIME
      && !assignedParcels.isEmpty()) {
      LOGGER.trace("{} We haven't won an auction for a while -> reauction",
        decorator);
      // we haven't won an auction for a while
      reauction();
    }
  }

  void next() {
    synchronized (computing) {
      if (!cfbQueue.isEmpty() && !computing.get()) {
        while (!cfbQueue.isEmpty()
          && cfbQueue.peek().getAuctioneer().hasWinner()) {
          // remove all calls for bids which have already finished
          cfbQueue.remove();
        }
        if (!cfbQueue.isEmpty()) {
          computeBid(cfbQueue.peek());
        }
      }
    }
  }

  void computeBid(final CallForBids cfb) {
    checkState(!cfb.getAuctioneer().hasWinner());
    checkState(!computing.getAndSet(true));
    LOGGER.trace("{} Start computing bid {}", decorator, cfb);

    final ImmutableList<Parcel> currentRoute =
      ImmutableList.copyOf(((Truck) vehicle.get()).getRoute());

    final Set<Parcel> parcels = new LinkedHashSet<>(currentRoute);
    parcels.add(cfb.getParcel());

    final GlobalStateObject state = solverHandle.get().getCurrentState(
      SolveArgs.create()
        .useCurrentRoutes(ImmutableList.of(currentRoute))
        // .fixRoutes()
        .useParcels(parcels));

    final EventAPI ev = solverHandle.get().getEventAPI();
    final Bidder<DoubleBid> bidder = decorator;
    currentListener = new Listener() {
      boolean exec;

      // this is called to notify us of the newly computed schedule
      @Override
      public void handleEvent(Event e) {
        synchronized (computing) {
          final SolverEvent event = (SolverEvent) e;
          checkState(cfbQueue.peek().equals(cfb));
          checkArgument(event.hasScheduleAndState(),
            "Solver was terminated before it found a solution.");

          checkState(!exec, "%s handleEvent was already called.", bidder);
          checkState(ev.containsListener(this, EventType.DONE));
          ev.removeListener(this, EventType.DONE);
          exec = true;

          // check if we receive the callback of the expected computation, (with
          // the correct state). If this is not the case, the callback was
          // probably already done before the listener could be removed. This
          // can be safely ignored.
          if (!event.getState().equals(state)) {
            return;
          }

          final double bidValue = heuristic.get(state);
          LOGGER.trace("{} Computed new bid: {}", decorator, bidValue);

          cfb.getAuctioneer().submit(DoubleBid.create(
            cfb.getTime(), bidder, cfb.getParcel(), bidValue));

          cfbQueue.poll();
          checkState(computing.getAndSet(false));
        }
      }

      @Override
      public String toString() {
        return cfb.getParcel() + "-auction-listener-" + bidder;
      }
    };
    // add callback to solver, such that we get the newly computed schedule as
    // soon as it is done computing (note, we are NOT interested in intermediary
    // schedules since we can only propose one bid, therefore we wait for the
    // best schedule).
    solverHandle.get().getEventAPI()
      .addListener(currentListener, EventType.DONE);

    LOGGER.trace("{} Compute new bid, currentRoute {}, parcels {}.", decorator,
      currentRoute, parcels);
    solverHandle.get().solve(state);
  }

  @Override
  public void receiveParcel(Auctioneer<DoubleBid> auctioneer, Parcel p,
      long auctionStartTime) {
    LOGGER.trace("{} RECEIVE PARCEL {} {} {}", decorator, auctioneer, p,
      auctionStartTime);

    super.receiveParcel(auctioneer, p, auctionStartTime);
    checkArgument(auctioneer.getWinner().equals(decorator));
  }

  @SuppressWarnings({"null", "unused"})
  void reauction() {
    if (assignedParcels.isEmpty()) {
      return;
    }
    LOGGER.trace("{} Considering a reauction, assignedParcels: {}.", decorator,
      assignedParcels.size());
    final ImmutableList<Parcel> currentRoute =
      ImmutableList.copyOf(((Truck) vehicle.get()).getRoute());
    final GlobalStateObject state = solverHandle.get().getCurrentState(
      SolveArgs.create()
        .noCurrentRoutes()
        .useParcels(currentRoute));

    final Parcel lastReceivedParcel = Iterables.getLast(assignedParcels);

    if (!reauctioning.get()) {
      // find all swappable parcels, a parcel can be swapped if it is not yet in
      // cargo (it must occur twice in route for that)
      // TODO filter out parcels that will be visited within several seconds
      // (length of auction)
      final Multiset<Parcel> routeMultiset =
        LinkedHashMultiset.create(currentRoute);
      final Set<Parcel> swappableParcels = new LinkedHashSet<>();
      for (final Parcel ap : assignedParcels) {
        final Auctioneer<DoubleBid> auct = parcelAuctioneers.get(ap);
        if (!pdpModel.get().getParcelState(ap).isPickedUp()
          && !pdpModel.get().getParcelState(ap).isTransitionState()
          && !state.getVehicles().get(0).getDestination().asSet()
            .contains(ap)
          && !ap.equals(lastReceivedParcel)
          && state.getTime()
            - auct.getLastAttemptTime() <= reauctionCooldownPeriod) {
          swappableParcels.add(ap);
        }
      }

      @Nullable
      final Parcel toSwap =
        parcelSwapSelector.select(state, currentRoute, swappableParcels);

      // we have found the most expensive parcel in the route, that is, removing
      // this parcel from the route will yield the greatest cost reduction.
      if (toSwap != null
        && !reauctioning.get()
        && !toSwap.equals(lastReceivedParcel)) {

        final Auctioneer<DoubleBid> auct = parcelAuctioneers.get(toSwap);
        if (auct.getLastUnsuccessTime() > 0
          && state.getTime()
            - auct.getLastUnsuccessTime() <= reauctionCooldownPeriod) {
          LOGGER.trace("Not reauctioning, was unsuccessful too recently");
          return;
        }

        // try to reauction
        reauctioning.set(true);
        LOGGER.trace("Found most expensive parcel for reauction: {}.", toSwap);

        final List<Parcel> routeWithout = new ArrayList<>(currentRoute);
        routeWithout.removeAll(Collections.singleton(toSwap));

        final GlobalStateObject state2 = solverHandle.get().getCurrentState(
          SolveArgs.create()
            .useCurrentRoutes(
              ImmutableList.of(ImmutableList.copyOf(routeWithout)))
            .useParcels(currentRoute));

        final double bidValue =
          heuristic.heuristic
            .compute(GpGlobal.create(state2, objectiveFunction));
        final DoubleBid initialBid =
          DoubleBid.create(state.getTime(), decorator, toSwap, bidValue);

        auct.auctionParcel(decorator, state.getTime(), initialBid,
          new Listener() {
            @Override
            public void handleEvent(Event e) {
              reauctioning.set(false);
            }
          });
      }
    }
  }

  @Override
  public boolean releaseParcel(Parcel p) {
    LOGGER.trace("{} RELEASE PARCEL {}", decorator, p);
    // remove the parcel from the route immediately to avoid going there
    final List<Parcel> currentRoute =
      new ArrayList<>(((Truck) vehicle.get()).getRoute());
    if (currentRoute.contains(p)) {
      final List<Parcel> original = new ArrayList<>(currentRoute);
      LOGGER.trace(" > remove parcel from route: {}", currentRoute);
      currentRoute.removeAll(Collections.singleton(p));

      final Truck truck = (Truck) vehicle.get();
      truck.setRoute(currentRoute);
      if (truck.getRoute().contains(p)) {
        LOGGER.warn("Could not release parcel, cancelling auction.");
        // set back original route
        truck.setRoute(original);
        return false;
      }
      LOGGER.trace(" > new route: {}", truck.getRoute());
    }
    return super.releaseParcel(p);
  }

  @Override
  public void tick(TimeLapse timeLapse) {
    next();
  }

  @Override
  public void afterTick(TimeLapse timeLapse) {
    next();
  }

  @Override
  public void setSolverProvider(RtSimSolverBuilder builder) {
    solverHandle = Optional.of(builder.setVehicles(vehicle.asSet())
      .build(solver));
  }

  public static Builder realtimeBuilder(
      PriorityHeuristic<GpGlobal> heuristic,
      Gendreau06ObjectiveFunction objFunc) {
    return Builder.createRt(heuristic, objFunc);
  }

  public static Builder simulatedTimeBuilder(
      PriorityHeuristic<GpGlobal> heuristic,
      Gendreau06ObjectiveFunction objFunc) {
    return Builder.createSt(heuristic, objFunc);
  }

  @AutoValue
  abstract static class CallForBids {

    CallForBids() {}

    abstract Auctioneer<DoubleBid> getAuctioneer();

    abstract Parcel getParcel();

    abstract long getTime();

    static CallForBids create(Auctioneer<DoubleBid> auctioneer, Parcel parcel,
        long time) {
      return new AutoValue_EvoBidder_CallForBids(auctioneer, parcel, time);
    }
  }

  /**
   *
   * @author Rinde van Lon
   */
  @AutoValue
  public abstract static class Builder
      implements StochasticSupplier<Bidder<DoubleBid>>, Serializable {
    static final long DEFAULT_COOLDOWN_VALUE = -1L;
    static final long DEFAULT_COMP_DELAY = 0L;
    static final ParcelSwapSelectorType DEFAULT_PARCEL_SWAP_SELECTOR_TYPE =
      ParcelSwapSelectorType.OBJ_FUNC;
    private static final long serialVersionUID = 117918742255072246L;

    Builder() {}

    abstract PriorityHeuristic<GpGlobal> getPriorityHeuristic();

    abstract boolean isRealtime();

    abstract Gendreau06ObjectiveFunction getObjectiveFunction();

    // after an unsuccessful reauction, this period indicates the minimum amount
    // of time to wait before a new reauction may be started for the same parcel
    abstract long getReauctionCooldownPeriod();

    /**
     * Indicates whether the {@link #getPriorityHeuristic()} or
     * {@link CheapestInsertionHeuristic} should be used for choosing the parcel
     * for a reauction.
     * @return <code>true</code> indicates {@link CheapestInsertionHeuristic},
     *         <code>false</code> indicates {@link #getPriorityHeuristic()}.
     *         Default value: <code>false</code>.
     */
    abstract ParcelSwapSelectorType getParcelSwapSelectorType();

    abstract boolean isTimeMeasurementEnabled();

    // a delay > 0 means that Thread.sleep will be called before invocation of
    // the heuristic.
    abstract long getComputationDelay();

    public Builder withReauctionCooldownPeriod(long periodMs) {
      return create(
        getPriorityHeuristic(),
        isRealtime(),
        getObjectiveFunction(),
        periodMs,
        getParcelSwapSelectorType(),
        isTimeMeasurementEnabled(),
        getComputationDelay());
    }

    // TODO validate options by testing output with PrioHeur: "(insertioncost)"

    public Builder withCheapestInsertionHeuristicForReauction() {
      return create(
        getPriorityHeuristic(),
        isRealtime(),
        getObjectiveFunction(),
        getReauctionCooldownPeriod(),
        ParcelSwapSelectorType.OBJ_FUNC,
        isTimeMeasurementEnabled(),
        getComputationDelay());

    }

    public Builder withPriorityHeuristicForReauction() {
      return create(
        getPriorityHeuristic(),
        isRealtime(),
        getObjectiveFunction(),
        getReauctionCooldownPeriod(),
        ParcelSwapSelectorType.PRIO_HEUR,
        isTimeMeasurementEnabled(),
        getComputationDelay());
    }

    public Builder withTimeMeasurement(boolean enable) {
      return create(
        getPriorityHeuristic(),
        isRealtime(),
        getObjectiveFunction(),
        getReauctionCooldownPeriod(),
        getParcelSwapSelectorType(),
        enable,
        getComputationDelay());
    }

    public Builder withComputationDelay(long ms) {
      return create(
        getPriorityHeuristic(),
        isRealtime(),
        getObjectiveFunction(),
        getReauctionCooldownPeriod(),
        getParcelSwapSelectorType(),
        isTimeMeasurementEnabled(),
        ms);
    }

    @Override
    public Bidder<DoubleBid> get(long seed) {
      if (isRealtime()) {
        return new EvoBidder(getObjectiveFunction(),
          getPriorityHeuristic(),
          getReauctionCooldownPeriod(),
          getParcelSwapSelectorType(),
          isTimeMeasurementEnabled(),
          getComputationDelay());
      } else {
        return new StEvoBidder(
          new EvoBidder(getObjectiveFunction(),
            getPriorityHeuristic(),
            getReauctionCooldownPeriod(),
            getParcelSwapSelectorType(),
            isTimeMeasurementEnabled(),
            getComputationDelay()));
      }
    }

    @Override
    public String toString() {
      return EvoBidder.class.getSimpleName()
        + (isRealtime() ? ".realtimeBuilder()" : ".simulatedTimeBuilder()");
    }

    static Builder createRt(PriorityHeuristic<GpGlobal> heuristic,
        Gendreau06ObjectiveFunction objFunc) {
      return create(heuristic, true, objFunc, DEFAULT_COOLDOWN_VALUE,
        DEFAULT_PARCEL_SWAP_SELECTOR_TYPE, false, DEFAULT_COMP_DELAY);
    }

    static Builder createSt(PriorityHeuristic<GpGlobal> heuristic,
        Gendreau06ObjectiveFunction objFunc) {
      return create(heuristic, false, objFunc, DEFAULT_COOLDOWN_VALUE,
        DEFAULT_PARCEL_SWAP_SELECTOR_TYPE, false, DEFAULT_COMP_DELAY);
    }

    static Builder create(PriorityHeuristic<GpGlobal> heuristic,
        boolean realtime,
        Gendreau06ObjectiveFunction objectiveFunction,
        long reauctionCooldownPeriod,
        ParcelSwapSelectorType type,
        boolean enableTimeMeasurement,
        long computationDelay) {
      return new AutoValue_EvoBidder_Builder(
        heuristic,
        realtime,
        objectiveFunction,
        reauctionCooldownPeriod,
        type,
        enableTimeMeasurement,
        computationDelay);
    }
  }

  static final class StEvoBidder extends ForwardingBidder<DoubleBid>
      implements SolverUser, TickListener {

    final EvoBidder delegate;
    final SolverUser stAdapter;

    StEvoBidder(EvoBidder deleg) {
      deleg.decorator = this;
      delegate = deleg;
      stAdapter = RtStAdapters.toSimTime(deleg);
    }

    @Override
    protected EvoBidder delegate() {
      return delegate;
    }

    @Override
    public void setSolverProvider(SimSolverBuilder builder) {
      stAdapter.setSolverProvider(builder);
    }

    @Override
    public void tick(TimeLapse timeLapse) {
      delegate().tick(timeLapse);
    }

    @Override
    public void afterTick(TimeLapse timeLapse) {
      delegate().afterTick(timeLapse);
    }

    @Override
    public String toString() {
      return StEvoBidder.class.getSimpleName() + "{" + delegate.toString()
        + "}";
    }
  }

  static class SolverAdapter implements Solver {
    final PriorityHeuristic<GpGlobal> heuristic;
    private final Map<GlobalStateObject, Double> results;
    private final Gendreau06ObjectiveFunction objectiveFunction;

    SolverAdapter(PriorityHeuristic<GpGlobal> h,
        Gendreau06ObjectiveFunction objFunc) {
      heuristic = h;
      objectiveFunction = objFunc;
      results = Collections.synchronizedMap(
        new LinkedHashMap<GlobalStateObject, Double>());
    }

    double get(GlobalStateObject state) {
      return checkNotNull(results.remove(state));
    }

    @Override
    public ImmutableList<ImmutableList<Parcel>> solve(GlobalStateObject state)
        throws InterruptedException {
      final GpGlobal gpg = GpGlobal.create(state, objectiveFunction);

      results.put(state, heuristic.compute(gpg));
      return ImmutableList.of();
    }
  }

  enum ParcelSwapSelectorType {
    OBJ_FUNC, PRIO_HEUR;
  }

  interface ParcelSwapSelector {
    @Nullable
    Parcel select(GlobalStateObject state,
        ImmutableList<Parcel> currentRoute, Iterable<Parcel> swappableParcels);
  }

  static class PriorityHeuristicSelector implements ParcelSwapSelector {
    final PriorityHeuristic<GpGlobal> priorityHeuristic;
    final Gendreau06ObjectiveFunction objectiveFunction;

    PriorityHeuristicSelector(Gendreau06ObjectiveFunction objFunc,
        PriorityHeuristic<GpGlobal> heuristic) {
      priorityHeuristic = heuristic;
      objectiveFunction = objFunc;
    }

    @Nullable
    @Override
    public Parcel select(GlobalStateObject state,
        ImmutableList<Parcel> currentRoute, Iterable<Parcel> swappableParcels) {
      @Nullable
      Parcel toSwap = null;
      double highestCost = Double.MIN_VALUE;
      for (final Parcel sp : swappableParcels) {
        final List<Parcel> newRoute = new ArrayList<>();
        newRoute.addAll(currentRoute);
        newRoute.removeAll(Collections.singleton(sp));

        // compute the cost of adding parcel 'sp' to the current route
        final double cost = priorityHeuristic
          .compute(GpGlobal.create(state
            .withRoutes(ImmutableList.of(ImmutableList.copyOf(newRoute))),
            objectiveFunction));

        // the parcel with the highest cost will be selected to see if we can
        // reauction it
        if (cost > highestCost) {
          highestCost = cost;
          toSwap = sp;
        }
      }
      return toSwap;
    }
  }

  static class ObjFuncSelector implements ParcelSwapSelector {
    final ObjectiveFunction objectiveFunction;

    ObjFuncSelector(ObjectiveFunction objFunc) {
      objectiveFunction = objFunc;
    }

    @Nullable
    @Override
    public Parcel select(GlobalStateObject state,
        ImmutableList<Parcel> currentRoute, Iterable<Parcel> swappableParcels) {
      final StatisticsDTO stats =
        Solvers.computeStats(state, ImmutableList.of(currentRoute));

      final double baseline = objectiveFunction.computeCost(stats);
      double lowestCost = baseline;
      @Nullable
      Parcel toSwap = null;

      LOGGER.trace("Compute cost of swapping");
      for (final Parcel sp : swappableParcels) {
        final List<Parcel> newRoute = new ArrayList<>();
        newRoute.addAll(currentRoute);
        newRoute.removeAll(Collections.singleton(sp));

        // costComputer.compute(state, sp)

        final double cost = objectiveFunction.computeCost(
          Solvers.computeStats(state,
            ImmutableList.of(ImmutableList.copyOf(newRoute))));
        // we select the lowest cost here because that means the biggest cost
        // reduction
        if (cost < lowestCost) {
          lowestCost = cost;
          toSwap = sp;
        }
      }
      return toSwap;
    }
  }

}
