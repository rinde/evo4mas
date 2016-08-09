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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;

import com.github.rinde.ecj.PriorityHeuristic;
import com.github.rinde.rinsim.central.GlobalStateObject;
import com.github.rinde.rinsim.central.GlobalStateObject.VehicleStateObject;
import com.github.rinde.rinsim.central.Solver;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.StochasticSupplier;
import com.github.rinde.rinsim.util.StochasticSuppliers.AbstractStochasticSupplier;
import com.google.common.collect.ImmutableList;

/**
 * Insertion heuristic {@link Solver} based on {@link PriorityHeuristic}. This
 * solver inserts all parcels in a route. Only works for a single vehicle, it
 * will throw an {@link IllegalArgumentException} when it encounters problems
 * that contain more than one vehicle.
 * @author Rinde van Lon
 */
public final class PriorityHeuristicSolver implements Solver {
  final PriorityHeuristic<VehicleParcelContext> heuristic;

  PriorityHeuristicSolver(PriorityHeuristic<VehicleParcelContext> prog) {
    heuristic = prog;
  }

  @Override
  public ImmutableList<ImmutableList<Parcel>> solve(GlobalStateObject state)
      throws InterruptedException {
    checkArgument(state.getVehicles().size() == 1,
      "Expected exactly 1 vehicle, found %s vehicles.",
      state.getVehicles().size());

    final VehicleStateObject vso = state.getVehicles().get(0);
    final Measure<Double, Velocity> speed = Measure
      .valueOf(vso.getDto().getSpeed(), state.getSpeedUnit());

    final Set<Parcel> assignablePickups = new LinkedHashSet<>(
      state.getAvailableParcels());
    final Set<Parcel> assignableDeliveries = new LinkedHashSet<>(
      vso.getContents());

    final List<Parcel> newRoute = new ArrayList<>();
    Point currentPosition = vso.getLocation();
    long currentTime = state.getTime();

    if (vso.getDestination().isPresent()) {
      final Parcel dest = vso.getDestination().get();
      newRoute.add(dest);

      final boolean isPickup = assignablePickups.contains(dest);
      final Point newPosition;
      if (isPickup) {
        newPosition = dest.getPickupLocation();
        assignablePickups.remove(dest);
        assignableDeliveries.add(dest);
      } else {
        newPosition = dest.getDeliveryLocation();
        assignableDeliveries.remove(dest);
      }

      final Measure<Double, Length> dist = Measure.valueOf(
        Point.distance(currentPosition, newPosition), state.getDistUnit());
      currentTime += Math.ceil(RoadModels.computeTravelTime(speed, dist,
        state.getTimeUnit()));

      if (isPickup) {
        currentTime = Math.max(currentTime, dest.getPickupTimeWindow().begin());
      } else {
        currentTime = Math.max(currentTime,
          dest.getDeliveryTimeWindow().begin());
      }

      currentTime += vso.getRemainingServiceTime();
      currentPosition = newPosition;
    }

    while (!assignablePickups.isEmpty() || !assignableDeliveries.isEmpty()) {
      double mx = Double.NEGATIVE_INFINITY;
      Parcel best = null;

      for (final Parcel p : assignablePickups) {
        final double priorityScore = heuristic.compute(
          VehicleParcelContext.create(currentTime, currentPosition,
            vso.getDto(), p, true));
        if (priorityScore > mx) {
          mx = priorityScore;
          best = p;
        }
      }
      for (final Parcel p : assignableDeliveries) {
        final double priorityScore = heuristic.compute(
          VehicleParcelContext.create(currentTime, currentPosition,
            vso.getDto(), p, false));
        if (priorityScore > mx) {
          mx = priorityScore;
          best = p;
        }
      }
      newRoute.add(best);

      final boolean isPickup = assignablePickups.contains(best);
      Point newPosition;
      if (isPickup) {
        assignablePickups.remove(best);
        assignableDeliveries.add(best);
        newPosition = best.getPickupLocation();
      } else {
        assignableDeliveries.remove(best);
        newPosition = best.getDeliveryLocation();
      }
      final Measure<Double, Length> dist = Measure.valueOf(
        Point.distance(currentPosition, newPosition), state.getDistUnit());
      currentTime += Math.ceil(RoadModels.computeTravelTime(speed, dist,
        state.getTimeUnit()));

      if (isPickup) {
        currentTime = Math.max(currentTime, best.getPickupTimeWindow().begin());
        currentTime += best.getPickupDuration();
      } else {
        currentTime = Math.max(currentTime,
          best.getDeliveryTimeWindow().begin());
        currentTime += best.getDeliveryDuration();
      }

      currentPosition = newPosition;
    }
    return ImmutableList.of(ImmutableList.copyOf(newRoute));
  }

  /**
   * Creates a new {@link PriorityHeuristicSolver} based on the specified
   * {@link PriorityHeuristic}.
   * @param heuristic The heuristic to use for determining the order of parcels
   *          in a route.
   * @return A new instance.
   */
  public static PriorityHeuristicSolver create(
      PriorityHeuristic<VehicleParcelContext> heuristic) {
    return new PriorityHeuristicSolver(heuristic);
  }

  /**
   * Creates a new {@link StochasticSupplier} for creating
   * {@link PriorityHeuristicSolver} instances.
   * @param heuristic The heuristic to use for determining the order of parcels
   *          in a route.
   * @return A new instance.
   */
  public static StochasticSupplier<PriorityHeuristicSolver> supplier(
      PriorityHeuristic<VehicleParcelContext> heuristic) {
    return new Sup(heuristic);
  }

  static class Sup extends AbstractStochasticSupplier<PriorityHeuristicSolver> {
    private static final long serialVersionUID = -8209571350704080148L;
    PriorityHeuristic<VehicleParcelContext> heuristic;

    Sup(PriorityHeuristic<VehicleParcelContext> h) {
      heuristic = h;
    }

    @Override
    public PriorityHeuristicSolver get(long seed) {
      return create(heuristic);
    }

    @Override
    public String toString() {
      return PriorityHeuristicSolver.class.getName() + ".supplier()";
    }
  }
}
