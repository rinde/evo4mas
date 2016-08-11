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
import static com.google.common.collect.Sets.newLinkedHashSet;

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
import com.github.rinde.rinsim.central.GlobalStateObject;
import com.github.rinde.rinsim.central.GlobalStateObject.VehicleStateObject;
import com.github.rinde.rinsim.central.GlobalStateObjects;
import com.github.rinde.rinsim.central.SimSolverBuilder;
import com.github.rinde.rinsim.central.Solver;
import com.github.rinde.rinsim.central.SolverUser;
import com.github.rinde.rinsim.central.Solvers;
import com.github.rinde.rinsim.central.Solvers.SolveArgs;
import com.github.rinde.rinsim.central.rt.RealtimeSolver;
import com.github.rinde.rinsim.central.rt.RtSimSolver;
import com.github.rinde.rinsim.central.rt.RtSimSolver.EventType;
import com.github.rinde.rinsim.central.rt.RtSimSolver.SolverEvent;
import com.github.rinde.rinsim.central.rt.RtSimSolverBuilder;
import com.github.rinde.rinsim.central.rt.RtSolverModel;
import com.github.rinde.rinsim.central.rt.RtSolverUser;
import com.github.rinde.rinsim.central.rt.RtStAdapters;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.event.Event;
import com.github.rinde.rinsim.event.EventAPI;
import com.github.rinde.rinsim.event.Listener;
import com.github.rinde.rinsim.pdptw.common.ObjectiveFunction;
import com.github.rinde.rinsim.pdptw.common.StatisticsDTO;
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

  final ObjectiveFunction objectiveFunction;
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

  private final long reauctionCooldownPeriod;

  EvoBidder(ObjectiveFunction objFunc,
      PriorityHeuristic<VehicleParcelContext> h, long cooldown) {
    super(SetFactories.synchronizedFactory(SetFactories.linkedHashSet()));
    objectiveFunction = objFunc;
    heuristic = new SolverAdapter(h);
    solver = RtStAdapters.toRealtime(heuristic);
    solverHandle = Optional.absent();
    cfbQueue = Queues.synchronizedQueue(new LinkedList<CallForBids>());
    parcelAuctioneers = new LinkedHashMap<>();
    reauctioning = new AtomicBoolean();
    computing = new AtomicBoolean();
    reauctionCooldownPeriod = cooldown;
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
            LOGGER.trace("{} cancel computation", decorator);

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
          // remove all call for bids which have already finished
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
    final Set<Parcel> parcels = newLinkedHashSet(assignedParcels);
    parcels.add(cfb.getParcel());
    final ImmutableList<Parcel> currentRoute =
      ImmutableList.copyOf(((Truck) vehicle.get()).getRoute());

    final GlobalStateObject state = solverHandle.get().getCurrentState(
      SolveArgs.create()
        .useCurrentRoutes(ImmutableList.of(currentRoute))
        .fixRoutes()
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
          LOGGER.trace("{} Computed new bid: {}", bidValue);

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
      SolveArgs.create().noCurrentRoutes().useParcels(assignedParcels));
    final StatisticsDTO stats =
      Solvers.computeStats(state, ImmutableList.of(currentRoute));

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
        if (!pdpModel.get().getParcelState(ap).isPickedUp()
          && !pdpModel.get().getParcelState(ap).isTransitionState()
          && !state.getVehicles().get(0).getDestination().asSet()
            .contains(ap)
          && !ap.equals(lastReceivedParcel)) {
          swappableParcels.add(ap);
        }
      }

      final double baseline = objectiveFunction.computeCost(stats);
      double lowestCost = baseline;
      @Nullable
      Parcel toSwap = null;

      LOGGER.trace("Compute cost of swapping");
      for (final Parcel sp : swappableParcels) {
        final List<Parcel> newRoute = new ArrayList<>();
        newRoute.addAll(currentRoute);
        newRoute.removeAll(Collections.singleton(sp));
        final double cost = objectiveFunction.computeCost(
          Solvers.computeStats(state,
            ImmutableList.of(ImmutableList.copyOf(newRoute))));
        if (cost < lowestCost) {
          lowestCost = cost;
          toSwap = sp;
        }
      }

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

        final VehicleParcelContext vpc =
          VehicleParcelContext.create(state.getTime(),
            state.getVehicles().get(0).getLocation(),
            state.getVehicles().get(0).getDto(), toSwap, true);

        final double bidValue = heuristic.heuristic.compute(vpc);

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
      PriorityHeuristic<VehicleParcelContext> heuristic,
      ObjectiveFunction objFunc) {
    return Builder.createRt(heuristic, objFunc);
  }

  public static Builder simulatedTimeBuilder(
      PriorityHeuristic<VehicleParcelContext> heuristic,
      ObjectiveFunction objFunc) {
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

  @AutoValue
  public abstract static class Builder
      implements StochasticSupplier<Bidder<DoubleBid>> {
    static final long DEFAULT_COOLDOWN_VALUE = -1L;

    Builder() {}

    abstract PriorityHeuristic<VehicleParcelContext> getPriorityHeuristic();

    abstract boolean isRealtime();

    abstract ObjectiveFunction getObjectiveFunction();

    // after an unsuccessful reauction, this period indicates the minimum amount
    // of time to wait before a new reauction may be started for the same parcel
    abstract long getReauctionCooldownPeriod();

    public Builder withReauctionCooldownPeriod(long periodMs) {
      return create(
        getPriorityHeuristic(),
        isRealtime(),
        getObjectiveFunction(),
        periodMs);
    }

    @Override
    public Bidder<DoubleBid> get(long seed) {
      if (isRealtime()) {
        return new EvoBidder(getObjectiveFunction(), getPriorityHeuristic(),
          getReauctionCooldownPeriod());
      } else {
        return new StEvoBidder(
          new EvoBidder(getObjectiveFunction(), getPriorityHeuristic(),
            getReauctionCooldownPeriod()));
      }
    }

    @Override
    public String toString() {
      return EvoBidder.class.getSimpleName()
        + (isRealtime() ? ".realtimeBuilder()" : ".simulatedTimeBuilder()");
    }

    static Builder createRt(PriorityHeuristic<VehicleParcelContext> heuristic,
        ObjectiveFunction objFunc) {
      return create(heuristic, true, objFunc, DEFAULT_COOLDOWN_VALUE);
    }

    static Builder createSt(PriorityHeuristic<VehicleParcelContext> heuristic,
        ObjectiveFunction objFunc) {
      return create(heuristic, false, objFunc, DEFAULT_COOLDOWN_VALUE);
    }

    static Builder create(PriorityHeuristic<VehicleParcelContext> heuristic,
        boolean realtime,
        ObjectiveFunction objectiveFunction,
        long reauctionCooldownPeriod) {
      return new AutoValue_EvoBidder_Builder(heuristic, realtime,
        objectiveFunction,
        reauctionCooldownPeriod);
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
    final PriorityHeuristic<VehicleParcelContext> heuristic;
    private final Map<GlobalStateObject, Double> results;

    SolverAdapter(PriorityHeuristic<VehicleParcelContext> h) {
      heuristic = h;
      results = Collections.synchronizedMap(
        new LinkedHashMap<GlobalStateObject, Double>());
    }

    double get(GlobalStateObject state) {
      return checkNotNull(results.remove(state));
    }

    @Override
    public ImmutableList<ImmutableList<Parcel>> solve(GlobalStateObject state)
        throws InterruptedException {
      checkArgument(state.getVehicles().size() == 1,
        "Expected exactly 1 vehicle, found %s vehicles.",
        state.getVehicles().size());
      final Set<Parcel> unassigned =
        GlobalStateObjects.unassignedParcels(state);
      checkState(unassigned.size() == 1);
      final VehicleStateObject vso = state.getVehicles().get(0);

      final VehicleParcelContext vpc = VehicleParcelContext.create(
        state.getTime(), vso.getLocation(), vso.getDto(),
        unassigned.iterator().next(), true);

      results.put(state, heuristic.compute(vpc));
      return ImmutableList.of();
    }
  }
}
