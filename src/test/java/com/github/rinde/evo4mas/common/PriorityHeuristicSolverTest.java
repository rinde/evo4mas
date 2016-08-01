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
import static com.google.common.truth.Truth.assertThat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.github.rinde.ecj.PriorityHeuristic;
import com.github.rinde.rinsim.central.Central;
import com.github.rinde.rinsim.central.SolverValidator;
import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.DependencyProvider;
import com.github.rinde.rinsim.core.model.ModelBuilder.AbstractModelBuilder;
import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.PDPModel.PDPModelEventType;
import com.github.rinde.rinsim.core.model.pdp.PDPModelEvent;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.ParcelDTO;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.RoadModelBuilders;
import com.github.rinde.rinsim.event.Event;
import com.github.rinde.rinsim.event.Listener;
import com.github.rinde.rinsim.experiment.Experiment;
import com.github.rinde.rinsim.experiment.Experiment.SimArgs;
import com.github.rinde.rinsim.experiment.ExperimentResults;
import com.github.rinde.rinsim.experiment.MASConfiguration;
import com.github.rinde.rinsim.experiment.PostProcessor;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.pdptw.common.AddDepotEvent;
import com.github.rinde.rinsim.pdptw.common.AddParcelEvent;
import com.github.rinde.rinsim.pdptw.common.AddVehicleEvent;
import com.github.rinde.rinsim.pdptw.common.PDPRoadModel;
import com.github.rinde.rinsim.pdptw.common.StatisticsDTO;
import com.github.rinde.rinsim.pdptw.common.StatsStopConditions;
import com.github.rinde.rinsim.pdptw.common.StatsTracker;
import com.github.rinde.rinsim.scenario.Scenario;
import com.github.rinde.rinsim.scenario.TimeOutEvent;
import com.github.rinde.rinsim.scenario.gendreau06.Gendreau06ObjectiveFunction;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.PlaneRoadModelRenderer;
import com.github.rinde.rinsim.ui.renderers.RoadUserRenderer;
import com.github.rinde.rinsim.util.TimeWindow;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

/**
 *
 * @author Rinde van Lon
 */
public class PriorityHeuristicSolverTest {

  ParcelDTO A = Parcel.builder(new Point(0, 0), new Point(2, 2))
      .serviceDuration(6 * 1000)
      .pickupTimeWindow(TimeWindow.create(2000, 8000))
      .deliveryTimeWindow(TimeWindow.create(10000, 90000))
      .toString("A")
      .buildDTO();
  ParcelDTO B = Parcel.builder(new Point(3, 0), new Point(2, 3))
      .toString("B")
      .buildDTO();
  ParcelDTO C = Parcel.builder(new Point(4, 0), new Point(6, 3))
      .toString("C")
      .pickupTimeWindow(TimeWindow.create(30 * 60 * 1000, 35 * 60 * 1000))
      .deliveryTimeWindow(TimeWindow.create(60 * 60 * 1000, 65 * 60 * 1000))
      .serviceDuration(5)
      .buildDTO();
  ParcelDTO D = Parcel.builder(new Point(5, 3), new Point(1, 2))
      .toString("D")
      .orderAnnounceTime(3000)
      .pickupTimeWindow(TimeWindow.create(3000, 12000))
      .pickupDuration(7000)
      .deliveryDuration(9000)
      .buildDTO();
  ParcelDTO E = Parcel.builder(new Point(3, 3), new Point(1, 0))
      .pickupTimeWindow(TimeWindow.create(863928, 863929))
      .toString("E")
      .buildDTO();

  @Test
  public void test() throws InterruptedException {
    final ListPriorityHeuristic lph = new ListPriorityHeuristic(A, B, C, E, D);

    final ExperimentResults res = Experiment.builder()
        .addScenario(Scenario.builder()
            .addEvent(AddDepotEvent.create(-1, new Point(0, 0)))
            .addEvent(AddVehicleEvent.create(-1, VehicleDTO.builder()
                .availabilityTimeWindow(TimeWindow.create(0, 60 * 60 * 1000L))
                .build()))
            .addEvent(AddParcelEvent.create(A))
            .addEvent(AddParcelEvent.create(B))
            .addEvent(AddParcelEvent.create(C))
            .addEvent(AddParcelEvent.create(D))
            .addEvent(AddParcelEvent.create(E))
            .addEvent(TimeOutEvent.create(30 * 60 * 1000L))
            .addModel(PDPRoadModel.builder(RoadModelBuilders.plane()))
            .addModel(DefaultPDPModel.builder())
            .addModel(DebugModel.builder())
            .setStopCondition(StatsStopConditions.vehiclesDoneAndBackAtDepot())
            .build())
        .addConfiguration(
            MASConfiguration.builder(
                Central.solverConfiguration(
                    SolverValidator
                        .wrap(PriorityHeuristicSolver.supplier(lph))))
                .addEventHandler(AddParcelEvent.class,
                    AddParcelEvent.namedHandler())
                .build())
        .withThreads(1)
        .usePostProcessor(new DebugPostProcessor())
        .showGui(View.builder()
            .with(PlaneRoadModelRenderer.builder())
            .with(RoadUserRenderer.builder())
            .withAutoPlay())
        .showGui(false)

        .perform();

    final DebugResultObject result = ((DebugResultObject) res.getResults()
        .iterator().next().getResultObject());

    final List<PosTime> resultPosList = new ArrayList<>(result.posTimeList());
    resultPosList.remove(resultPosList.size() - 1);
    resultPosList.add(0, PosTime.create(new Point(0, 0), 0));

    final Set<PosTime> heuristicPosTimes = new LinkedHashSet<>();
    for (final VehicleParcelContext context : lph.contexts) {
      heuristicPosTimes
          .add(PosTime.create(context.vehiclePosition(), context.time()));
    }

    final Iterator<PosTime> hpt = heuristicPosTimes.iterator();
    final Iterator<PosTime> rpt = resultPosList.iterator();

    while (hpt.hasNext() && rpt.hasNext()) {
      System.out.println(hpt.next() + " \t\t" + rpt.next());
    }

    assertThat(heuristicPosTimes).containsExactlyElementsIn(resultPosList)
        .inOrder();

  }

  @AutoValue
  abstract static class DebugResultObject {
    abstract ImmutableList<PosTime> posTimeList();

    abstract StatisticsDTO stats();

    static DebugResultObject create(List<PosTime> list, StatisticsDTO stats) {
      return new AutoValue_PriorityHeuristicSolverTest_DebugResultObject(
          ImmutableList.copyOf(list), stats);
    }
  }

  static class DebugPostProcessor implements PostProcessor<DebugResultObject> {

    @Override
    public DebugResultObject collectResults(Simulator sim, SimArgs args) {
      final StatisticsDTO stats = sim.getModelProvider()
          .getModel(StatsTracker.class).getStatistics();
      Gendreau06ObjectiveFunction.instance().isValidResult(stats);
      final List<PosTime> list = sim.getModelProvider()
          .getModel(DebugModel.class).posTimeList;
      return DebugResultObject.create(list, stats);
    }

    @Override
    public FailureStrategy handleFailure(Exception e, Simulator sim,
        SimArgs args) {
      return FailureStrategy.ABORT_EXPERIMENT_RUN;
    }
  }

  @AutoValue
  abstract static class PosTime {
    abstract Point pos();

    abstract long time();

    static PosTime create(Point p, long t) {
      return new AutoValue_PriorityHeuristicSolverTest_PosTime(p, t);
    }
  }

  static class DebugModel
      extends com.github.rinde.rinsim.core.model.Model.AbstractModelVoid {

    List<PosTime> posTimeList;

    DebugModel(PDPModel pdpModel) {
      posTimeList = new ArrayList<>();
      pdpModel.getEventAPI().addListener(new Listener() {

        @Override
        public void handleEvent(Event e) {
          final PDPModelEvent ev = (PDPModelEvent) e;

          Point p;
          if (e.getEventType() == PDPModelEventType.END_PICKUP) {
            p = ev.parcel.getPickupLocation();
          } else {
            p = ev.parcel.getDeliveryLocation();
          }
          posTimeList.add(PosTime.create(p, ev.time));
        }
      }, PDPModelEventType.END_PICKUP, PDPModelEventType.END_DELIVERY);
    }

    static Builder builder() {
      return new AutoValue_PriorityHeuristicSolverTest_DebugModel_Builder();
    }

    @AutoValue
    abstract static class Builder
        extends AbstractModelBuilder<DebugModel, Void> {

      Builder() {
        setDependencies(PDPModel.class);
      }

      @Override
      public DebugModel build(DependencyProvider dependencyProvider) {
        return new DebugModel(dependencyProvider.get(PDPModel.class));
      }

    }
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
