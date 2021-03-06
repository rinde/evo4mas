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
import static com.google.common.collect.Sets.newHashSet;

import java.math.RoundingMode;
import java.util.LinkedHashSet;
import java.util.ListIterator;
import java.util.Set;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;

import com.github.rinde.ecj.GPFunc;
import com.github.rinde.rinsim.central.GlobalStateObject;
import com.github.rinde.rinsim.central.GlobalStateObject.VehicleStateObject;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.collect.ImmutableList;
import com.google.common.math.DoubleMath;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

/**
 * These functions consider {@link VehicleParcelContext} as a pair of pickup and
 * delivery operations. Instead of a single pickup or delivery operation, hence
 * {@link VehicleParcelContext#isPickup()} is ignored.
 * @author Rinde van Lon
 */
public final class GlobalStateObjectFunctions {
  static final double MS_IN_M = 60000;

  private GlobalStateObjectFunctions() {}

  static long computeFlexibility(GlobalStateObject gso,
      ImmutableList<Parcel> route) {
    final LongList earliest = computeEarliestArrivalTimes(gso, route);
    final LongList latest = computeLatestArrivalTimes(gso, route, earliest);

    long sum = 0;
    for (int i = 0; i < earliest.size(); i++) {
      sum += latest.getLong(i) - earliest.getLong(i);
    }
    return sum;
  }

  static LongList computeLatestArrivalTimes(GlobalStateObject gso,
      ImmutableList<Parcel> route, LongList earliestArrivalTimes) {
    final ListIterator<Parcel> iterator = route.listIterator(route.size());
    final Set<Parcel> seenSet = new LinkedHashSet<>();
    final VehicleStateObject vehicle = gso.getVehicles().get(0);
    final Measure<Double, Velocity> speed = Measure.valueOf(
      vehicle.getDto().getSpeed(), gso.getSpeedUnit());

    final LongList arrivalTimes = new LongArrayList(route.size() + 1);

    Point curLoc = vehicle.getDto().getStartPosition();
    long curTWend = vehicle.getDto().getAvailabilityTimeWindow().end();

    arrivalTimes.add(0,
      Math.max(curTWend, earliestArrivalTimes.getLong(route.size())));

    while (iterator.hasPrevious()) {
      final int index = iterator.previousIndex();
      final Parcel prev = iterator.previous();

      Point prevLoc;
      final long prevTWend;
      long prevDur;
      if (seenSet.contains(prev)) {
        prevLoc = prev.getPickupLocation();
        prevTWend = prev.getPickupTimeWindow().end();
        prevDur = prev.getPickupDuration();
      } else {
        prevLoc = prev.getDeliveryLocation();
        prevTWend = prev.getDeliveryTimeWindow().end();
        prevDur = prev.getDeliveryDuration();
      }
      seenSet.add(prev);

      final Measure<Double, Length> distance = Measure.valueOf(
        Point.distance(prevLoc, curLoc), gso.getDistUnit());
      final long tt = DoubleMath.roundToLong(
        RoadModels.computeTravelTime(speed, distance, gso.getTimeUnit()),
        RoundingMode.CEILING);

      final long latestTimeToLeave =
        Math.min(curTWend, arrivalTimes.getLong(0) - tt - prevDur);

      arrivalTimes.add(0,
        Math.max(latestTimeToLeave, earliestArrivalTimes.getLong(index)));

      curLoc = prevLoc;
      curTWend = prevTWend;
    }
    return arrivalTimes;
  }

  static LongList computeEarliestArrivalTimes(GlobalStateObject gso,
      ImmutableList<Parcel> route) {
    final long startTime = gso.getTime();
    final Set<Parcel> parcels = newHashSet();
    final VehicleStateObject vso = gso.getVehicles().get(0);
    final LongList arrivalTimesList = new LongArrayList();
    parcels.addAll(route);

    long time = startTime;
    Point vehicleLocation = vso.getLocation();
    final Measure<Double, Velocity> speed = Measure.valueOf(
      vso.getDto().getSpeed(), gso.getSpeedUnit());
    final Set<Parcel> seen = newHashSet();
    for (int j = 0; j < route.size(); j++) {
      final Parcel cur = route.get(j);
      final boolean inCargo = vso.getContents().contains(cur)
        || seen.contains(cur);
      seen.add(cur);
      if (vso.getDestination().isPresent() && j == 0) {
        checkArgument(
          vso.getDestination().asSet().contains(cur),
          "If a vehicle has a destination, the first position in the route "
            + "must equal this. Expected %s, is %s.",
          vso.getDestination().get(), cur);
      }

      boolean firstAndServicing = false;
      if (j == 0 && vso.getRemainingServiceTime() > 0) {
        // we are already at the service location
        firstAndServicing = true;
        arrivalTimesList.add(time);
        time += vso.getRemainingServiceTime();
      } else {
        // vehicle is not there yet, go there first, then service
        final Point nextLoc = inCargo ? cur.getDeliveryLocation()
          : cur.getPickupLocation();
        final Measure<Double, Length> distance = Measure.valueOf(
          Point.distance(vehicleLocation, nextLoc), gso.getDistUnit());
        vehicleLocation = nextLoc;
        final long tt = DoubleMath.roundToLong(
          RoadModels.computeTravelTime(speed, distance, gso.getTimeUnit()),
          RoundingMode.CEILING);
        time += tt;
      }
      if (inCargo) {
        // check if we are early
        if (cur.getDeliveryTimeWindow().isBeforeStart(time)) {
          time = cur.getDeliveryTimeWindow().begin();
        }

        if (!firstAndServicing) {
          arrivalTimesList.add(time);
          time += cur.getDeliveryDuration();
        }
      } else {
        // check if we are early
        if (cur.getPickupTimeWindow().isBeforeStart(time)) {
          time = cur.getPickupTimeWindow().begin();
        }
        if (!firstAndServicing) {
          arrivalTimesList.add(time);
          time += cur.getPickupDuration();
        }
      }
    }

    // go to depot
    final Measure<Double, Length> distance = Measure.valueOf(
      Point.distance(vehicleLocation, vso.getDto().getStartPosition()),
      gso.getDistUnit());
    final long tt = DoubleMath.roundToLong(
      RoadModels.computeTravelTime(speed, distance, gso.getTimeUnit()),
      RoundingMode.CEILING);
    time += tt;

    arrivalTimesList.add(time);
    return arrivalTimesList;
  }

  public static class InsertionFlexibility extends GPFunc<GpGlobal> {
    public InsertionFlexibility() {}

    @Override
    public double execute(double[] input, GpGlobal context) {
      return context.insertionFlexibility();
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
  public static class Slack extends GPFunc<GpGlobal> {
    public Slack() {}

    @Override
    public double execute(double[] input, GpGlobal context) {
      final GlobalStateObject state = context.state();
      final long currentTime = state.getTime();

      final VehicleStateObject vso = state.getVehicles().get(0);

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

      // slack/idle time
      final double slack = vso.getDto().getAvailabilityTimeWindow().end()
        - currentTime - utilization;

      return slack / MS_IN_M;
    }
  }

  /**
   * Average distance of unassigned parcel pickup AND delivery point to all
   * locations in the vehicle's current route. This is an adaptation of Ado by
   * Beham et al.
   *
   * @author Rinde van Lon
   */
  public static class Ado extends GPFunc<GpGlobal> {
    private static final long serialVersionUID = 2722583906726554580L;

    public Ado() {}

    @Override
    public double execute(double[] input, GpGlobal context) {
      return context.ado();
    }
  }

  public static class Mido extends GPFunc<GpGlobal> {

    public Mido() {}

    @Override
    public double execute(double[] input, GpGlobal context) {
      return context.mido();
    }
  }

  public static class Mado extends GPFunc<GpGlobal> {
    public Mado() {}

    @Override
    public double execute(double[] input, GpGlobal context) {
      return context.mado();
    }
  }

  public static class RouteLength extends GPFunc<GpGlobal> {
    public RouteLength() {}

    @Override
    public double execute(double[] input, GpGlobal context) {
      return context.vehicle().getRoute().get().size();
    }
  }

  public static class PickupUrgency extends GPFunc<GpGlobal> {
    public PickupUrgency() {}

    @Override
    public double execute(double[] input, GpGlobal context) {
      return context.parcel().getPickupTimeWindow().end()
        - context.state().getTime();
    }
  }

  public static class DeliveryUrgency extends GPFunc<GpGlobal> {
    public DeliveryUrgency() {}

    @Override
    public double execute(double[] input, GpGlobal context) {
      return context.parcel().getDeliveryTimeWindow().end()
        - context.state().getTime();
    }
  }

}
