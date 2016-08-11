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
import com.github.rinde.logistics.pdptw.solver.CheapestInsertionHeuristic;
import com.github.rinde.rinsim.central.GlobalStateObject;
import com.github.rinde.rinsim.central.Solvers;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.pdptw.common.StatisticsDTO;
import com.github.rinde.rinsim.scenario.gendreau06.Gendreau06ObjectiveFunction;
import com.google.common.collect.ImmutableList;

/**
 * These functions consider {@link VehicleParcelContext} as a pair of pickup and
 * delivery operations. Instead of a single pickup or delivery operation, hence
 * {@link VehicleParcelContext#isPickup()} is ignored.
 * @author Rinde van Lon
 */
public final class VehicleParcelContextPairFunctions {

  private VehicleParcelContextPairFunctions() {}

  public static class InsertionCost extends GPFunc<GlobalStateObject> {

    public InsertionCost() {}

    @Override
    public double execute(double[] input, GlobalStateObject context) {

      final StatisticsDTO baseline = Solvers.computeStats(
        context,
        ImmutableList.of(context.getVehicles().get(0).getRoute().get()));

      ImmutableList<ImmutableList<Parcel>> newRoute;
      try {
        newRoute = CheapestInsertionHeuristic.supplier(
          Gendreau06ObjectiveFunction.instance()).get(0).solve(context);
        final StatisticsDTO insertionStats =
          Solvers.computeStats(context, newRoute);
      } catch (final InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      // TODO Auto-generated method stub
      return 0;
    }

  }

}
