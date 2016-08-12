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

import java.util.LinkedHashSet;
import java.util.Set;

import com.github.rinde.ecj.GPFunc;
import com.github.rinde.logistics.pdptw.solver.CheapestInsertionHeuristic;
import com.github.rinde.rinsim.central.GlobalStateObject;
import com.github.rinde.rinsim.central.GlobalStateObject.VehicleStateObject;
import com.github.rinde.rinsim.central.GlobalStateObjects;
import com.github.rinde.rinsim.central.Solvers;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.pdptw.common.StatisticsDTO;
import com.github.rinde.rinsim.scenario.gendreau06.Gendreau06ObjectiveFunction;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

/**
 * These functions consider {@link VehicleParcelContext} as a pair of pickup and
 * delivery operations. Instead of a single pickup or delivery operation, hence
 * {@link VehicleParcelContext#isPickup()} is ignored.
 * @author Rinde van Lon
 */
public final class GlobalStateObjectFunctions {
  static final double MS_IN_M = 60000;
  static final Gendreau06ObjectiveFunction OBJ_FUNC =
    Gendreau06ObjectiveFunction.instance(50d);

  private GlobalStateObjectFunctions() {}

  @AutoValue
  public abstract static class GpGlobal {

    GpGlobal() {}

    public abstract GlobalStateObject state();

    public abstract Parcel unassignedParcel();

    public abstract double insertionCost();

    public abstract double insertionTravelTime();

    public abstract double insertionTardiness();

    public abstract double insertionOverTime();

    public static GpGlobal create(GlobalStateObject state) {
      System.out.println(state);
      checkArgument(state.getVehicles().size() == 1,
        "Expected exactly 1 vehicle, found %s vehicles.",
        state.getVehicles().size());
      final Set<Parcel> ps = GlobalStateObjects.unassignedParcels(state);
      checkArgument(ps.size() == 1,
        "Expected axactly 1 unassigned parcel, found %s unassigned parcels.",
        ps.size());

      final StatisticsDTO baseline = Solvers.computeStats(
        state,
        ImmutableList.of(state.getVehicles().get(0).getRoute().get()));

      final ImmutableList<ImmutableList<Parcel>> newRoute;
      try {
        newRoute = CheapestInsertionHeuristic.solve(state, OBJ_FUNC);
        final StatisticsDTO insertionStats =
          Solvers.computeStats(state, newRoute);

        final double insertionCost =
          OBJ_FUNC.computeCost(insertionStats) - OBJ_FUNC.computeCost(baseline);

        final double insertionTravelTime =
          OBJ_FUNC.travelTime(insertionStats) - OBJ_FUNC.travelTime(baseline);

        final double insertionTardiness =
          OBJ_FUNC.tardiness(insertionStats) - OBJ_FUNC.tardiness(baseline);

        final double insertionOverTime =
          OBJ_FUNC.overTime(insertionStats) - OBJ_FUNC.overTime(baseline);

        return new AutoValue_GlobalStateObjectFunctions_GpGlobal(state,
          ps.iterator().next(), insertionCost, insertionTravelTime,
          insertionTardiness, insertionOverTime);

      } catch (final InterruptedException e) {
        throw new IllegalStateException();
      }
    }

  }

  public static class InsertionCost extends GPFunc<GpGlobal> {
    public InsertionCost() {}

    @Override
    public double execute(double[] input, GpGlobal context) {
      return context.insertionCost();
    }
  }

  public static class InsertionTravelTime extends GPFunc<GpGlobal> {
    public InsertionTravelTime() {}

    @Override
    public double execute(double[] input, GpGlobal context) {
      return context.insertionTravelTime();
    }
  }

  public static class InsertionTardiness extends GPFunc<GpGlobal> {
    public InsertionTardiness() {}

    @Override
    public double execute(double[] input, GpGlobal context) {
      return context.insertionTardiness();
    }
  }

  public static class InsertionOverTime extends GPFunc<GpGlobal> {
    public InsertionOverTime() {}

    @Override
    public double execute(double[] input, GpGlobal context) {
      return context.insertionOverTime();
    }
  }

  // the time in minutes
  public static class Time extends GPFunc<GpGlobal> {
    public Time() {}

    @Override
    public double execute(double[] input, GpGlobal context) {
      return context.state().getTime() / MS_IN_M;
    }
  }

  // the time left
  public static class TimeLeft extends GPFunc<GpGlobal> {
    public TimeLeft() {}

    @Override
    public double execute(double[] input, GpGlobal context) {
      return (context.state().getVehicles().get(0).getDto()
        .getAvailabilityTimeWindow().end() - context.state().getTime())
        / MS_IN_M;
    }
  }

  // returns the utilization of the vehicle in minutes (i.e. the accumulative
  // time it will take the vehicle to complete it's current route)
  public static class Utilization extends GPFunc<GpGlobal> {
    public Utilization() {}

    @Override
    public double execute(double[] input, GpGlobal context) {
      final VehicleStateObject vso = context.state().getVehicles().get(0);
      final Point currentLocation = vso.getLocation();
      final Set<Parcel> inCargo = new LinkedHashSet<>(vso.getContents());

      double utilization = vso.getRemainingServiceTime();
      for (final Parcel p : vso.getRoute().get()) {
        Point loc;
        if (inCargo.contains(p)) {
          loc = p.getDeliveryLocation();
          utilization += p.getDeliveryDuration();
        } else {
          loc = p.getPickupLocation();
          utilization += p.getPickupDuration();
          inCargo.add(p);
        }
        utilization += VehicleParcelContextFunctions.computeTravelTime(
          vso.getDto(), currentLocation, loc);
      }
      return utilization / MS_IN_M;
    }
  }

}
