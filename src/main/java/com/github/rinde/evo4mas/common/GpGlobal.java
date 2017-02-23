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
import static com.google.common.collect.Sets.newLinkedHashSet;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;

import com.github.rinde.logistics.pdptw.solver.CheapestInsertionHeuristic;
import com.github.rinde.rinsim.central.GlobalStateObject;
import com.github.rinde.rinsim.central.GlobalStateObject.VehicleStateObject;
import com.github.rinde.rinsim.central.Solvers;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.pdptw.common.StatisticsDTO;
import com.github.rinde.rinsim.scenario.gendreau06.Gendreau06ObjectiveFunction;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Doubles;

@AutoValue
public abstract class GpGlobal {
  private boolean isInsertComputed;
  private double insertCost;
  private double insertTravelTime;
  private double insertTardiness;
  private double insertOverTime;
  private double insertFlexibility;

  private boolean isAdoComputed;
  private double ado;
  private double mido;
  private double mado;

  GpGlobal() {}

  public abstract GlobalStateObject state();

  public abstract VehicleStateObject vehicle();

  public abstract Parcel parcel();

  public abstract Gendreau06ObjectiveFunction objFunc();

  public double insertionCost() {
    if (!isInsertComputed) {
      computeInsert();
    }
    return insertCost;
  }

  public double insertionTravelTime() {
    if (!isInsertComputed) {
      computeInsert();
    }
    return insertTravelTime;
  }

  public double insertionTardiness() {
    if (!isInsertComputed) {
      computeInsert();
    }
    return insertTardiness;
  }

  public double insertionOverTime() {
    if (!isInsertComputed) {
      computeInsert();
    }
    return insertOverTime;
  }

  public double insertionFlexibility() {
    if (!isInsertComputed) {
      computeInsert();
    }
    return insertFlexibility;
  }

  void computeInsert() {
    isInsertComputed = true;
    final StatisticsDTO baseline = Solvers.computeStats(
      state(),
      ImmutableList.of(vehicle().getRoute().get()));

    final long baselineFlexibility =
      GlobalStateObjectFunctions.computeFlexibility(state(),
        state().getVehicles().get(0).getRoute().get());

    try {
      final ImmutableList<ImmutableList<Parcel>> newRoute =
        CheapestInsertionHeuristic.solve(state(), objFunc());
      final StatisticsDTO insertionStats =
        Solvers.computeStats(state(), newRoute);

      insertCost =
        objFunc().computeCost(insertionStats) - objFunc().computeCost(baseline);

      insertTravelTime =
        objFunc().travelTime(insertionStats) - objFunc().travelTime(baseline);

      insertTardiness =
        objFunc().tardiness(insertionStats) - objFunc().tardiness(baseline);

      insertOverTime =
        objFunc().overTime(insertionStats) - objFunc().overTime(baseline);

      final long insertedFlex =
        GlobalStateObjectFunctions.computeFlexibility(state(), newRoute.get(0));
      insertFlexibility = insertedFlex - baselineFlexibility;

    } catch (final InterruptedException e) {
      // this should not be interrupted
      throw new IllegalStateException(e);
    }
  }

  public double ado() {
    if (!isAdoComputed) {
      computeAdoMidoMado();
    }
    return ado;
  }

  public double mido() {
    if (!isAdoComputed) {
      computeAdoMidoMado();
    }
    return mido;
  }

  public double mado() {
    if (!isAdoComputed) {
      computeAdoMidoMado();
    }
    return mado;
  }

  void computeAdoMidoMado() {
    isAdoComputed = true;
    final List<Parcel> route = vehicle().getRoute().get();
    if (route.isEmpty()) {
      ado = 0d;
      mido = 0d;
      mado = 0d;
    }
    final Set<Parcel> seen = new LinkedHashSet<>();
    final ListIterator<Parcel> it = route.listIterator(route.size());
    double totalDist = 0d;
    double minDist = Double.POSITIVE_INFINITY;
    double maxDist = Double.NEGATIVE_INFINITY;
    while (it.hasPrevious()) {
      final Parcel cur = it.previous();
      final Point curLoc;
      if (seen.contains(cur)) {
        // it is a pickup location
        curLoc = cur.getPickupLocation();
      } else {
        // it is a delivery location
        seen.add(cur);
        curLoc = cur.getDeliveryLocation();
      }
      final double dist1 =
        Point.distance(parcel().getDeliveryLocation(), curLoc);
      final double dist2 = Point.distance(parcel().getPickupLocation(), curLoc);
      totalDist += dist1 + dist2;
      minDist = Doubles.min(minDist, dist1, dist2);
      maxDist = Doubles.max(maxDist, dist1, dist2);
    }
    ado = distToTravelTimeInMin(totalDist / (route.size() * 2));
    mido = distToTravelTimeInMin(minDist);
    mado = distToTravelTimeInMin(maxDist);
  }

  double distToTravelTimeInMin(double dist) {
    final Measure<Double, Length> distance =
      Measure.valueOf(dist, state().getDistUnit());
    final Measure<Double, Velocity> velocity =
      Measure.valueOf(vehicle().getDto().getSpeed(), state().getSpeedUnit());
    return RoadModels.computeTravelTime(velocity, distance, NonSI.MINUTE);
  }

  public static GpGlobal create(GlobalStateObject state,
      Gendreau06ObjectiveFunction objFunc) {
    checkArgument(state.getVehicles().size() == 1,
      "Expected exactly 1 vehicle, found %s vehicles.",
      state.getVehicles().size());
    final VehicleStateObject vehicle = state.getVehicles().get(0);
    checkArgument(vehicle.getRoute().isPresent(),
      "The vehicle needs to have a route defined.");

    final Set<Parcel> unassigned =
      newLinkedHashSet(state.getAvailableParcels());
    unassigned.removeAll(vehicle.getRoute().get());
    checkArgument(unassigned.size() == 1,
      "Expected axactly 1 unassigned parcel, found %s unassigned parcels.",
      unassigned.size());

    return new AutoValue_GpGlobal(state, vehicle, unassigned.iterator().next(),
      objFunc);
  }
}
