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
import java.util.List;

import org.junit.Test;

import com.github.rinde.ecj.PriorityHeuristic;
import com.github.rinde.rinsim.central.Central;
import com.github.rinde.rinsim.central.SolverValidator;
import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.ParcelDTO;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.RoadModelBuilders;
import com.github.rinde.rinsim.experiment.Experiment;
import com.github.rinde.rinsim.experiment.ExperimentResults;
import com.github.rinde.rinsim.experiment.PostProcessors;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.pdptw.common.AddDepotEvent;
import com.github.rinde.rinsim.pdptw.common.AddParcelEvent;
import com.github.rinde.rinsim.pdptw.common.AddVehicleEvent;
import com.github.rinde.rinsim.pdptw.common.PDPRoadModel;
import com.github.rinde.rinsim.pdptw.common.StatisticsDTO;
import com.github.rinde.rinsim.scenario.Scenario;
import com.github.rinde.rinsim.scenario.TimeOutEvent;
import com.github.rinde.rinsim.scenario.gendreau06.Gendreau06ObjectiveFunction;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.PlaneRoadModelRenderer;
import com.github.rinde.rinsim.ui.renderers.RoadUserRenderer;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

/**
 *
 * @author Rinde van Lon
 */
public class PriorityHeuristicSolverTest {

  ParcelDTO A = Parcel.builder(new Point(0, 0), new Point(2, 2))
      .toString("A")
      .buildDTO();
  ParcelDTO B = Parcel.builder(new Point(0, 0), new Point(2, 2))
      .toString("B")
      .buildDTO();

  @Test
  public void test() throws InterruptedException {
    final ListPriorityHeuristic lph = new ListPriorityHeuristic(A, B);

    final ExperimentResults res = Experiment.builder()
        .addScenario(Scenario.builder()
            .addEvent(AddDepotEvent.create(-1, new Point(0, 0)))
            .addEvent(AddVehicleEvent.create(-1, VehicleDTO.builder().build()))
            .addEvent(AddParcelEvent.create(A))
            .addEvent(TimeOutEvent.create(60 * 60 * 1000L))
            .addModel(PDPRoadModel.builder(RoadModelBuilders.plane()))
            .addModel(DefaultPDPModel.builder())
            .build())
        .addConfiguration(Central.solverConfiguration(
            SolverValidator.wrap(PriorityHeuristicSolver.supplier(lph))))
        .withThreads(1)
        .usePostProcessor(PostProcessors
            .statisticsPostProcessor(Gendreau06ObjectiveFunction.instance()))
        .showGui(View.builder()
            .with(PlaneRoadModelRenderer.builder())
            .with(RoadUserRenderer.builder())
            .withAutoPlay())
        .showGui(false)

        .perform();

    System.out.println(((StatisticsDTO) res.getResults().iterator().next()
        .getResultObject()).simulationTime);

    // final Solver s = PriorityHeuristicSolver.create(lph);
    //
    // final List<Parcel> route =
    // s.solve(GlobalStateObjectBuilder.globalBuilder()
    // .addAvailableParcels(A, B)
    // .addVehicle(GlobalStateObjectBuilder.vehicleBuilder()
    // .build())
    // .build())
    // .get(0);

    // assertThat(route).containsAllIn(asList(A, A, B, B)).inOrder();

    // TODO compare with simulation
    // how? we need intermediary steps from sim
    // possibly from within vehicle?

    System.out.println(Joiner.on("\n").join(lph.contexts));
  }

  static class ListPriorityHeuristic
      implements PriorityHeuristic<VehicleParcelContext> {

    final List<ParcelDTO> list;
    List<VehicleParcelContext> contexts;

    public ListPriorityHeuristic(ParcelDTO... ps) {
      list = ImmutableList.copyOf(ps);
      contexts = new ArrayList<>();
    }

    @Override
    public double compute(VehicleParcelContext input) {
      contexts.add(input);
      final int index = list.indexOf(input.parcel().getDto());
      checkArgument(index >= 0);
      return list.size() - index;
    }

    @Override
    public String getId() {
      return "ListPriorityHeuristic";
    }
  }

}
