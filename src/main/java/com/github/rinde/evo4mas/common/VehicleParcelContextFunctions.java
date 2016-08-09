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

import com.github.rinde.ecj.GPFunc;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.geom.Point;

/**
 *
 * @author Rinde van Lon
 */
public final class VehicleParcelContextFunctions {

  private static final double SEC_IN_M = 60d;

  private VehicleParcelContextFunctions() {}

  static double computeTravelTime(VehicleDTO v, Point p1, Point p2) {
    return (Point.distance(p1, p2) / v.getSpeed()) / SEC_IN_M;
  }

  public static class Urgency extends GPFunc<VehicleParcelContext> {

    public Urgency() {}

    @Override
    public double execute(double[] input, VehicleParcelContext context) {
      if (context.isPickup()) {
        return context.parcel().getPickupTimeWindow().end() - context.time();
      }
      return context.parcel().getDeliveryTimeWindow().end() - context.time();
    }
  }

  public static class TravelTime extends GPFunc<VehicleParcelContext> {

    public TravelTime() {}

    @Override
    public double execute(double[] input, VehicleParcelContext context) {
      final Point parcelLoc = context.isPickup()
        ? context.parcel().getPickupLocation()
        : context.parcel().getDeliveryLocation();
      return computeTravelTime(context.vehicle(), context.vehiclePosition(),
        parcelLoc);
    }
  }
}
