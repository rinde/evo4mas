/*
 * Copyright (C) 2013-2016 Rinde van Lon, iMinds-DistriNet, KU Leuven
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

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

import com.github.rinde.ecj.GPFunc;
import com.github.rinde.ecj.GPFuncSet;
import com.github.rinde.ecj.GPProgramParser;
import com.github.rinde.ecj.PriorityHeuristic;
import com.github.rinde.evo4mas.common.VehicleParcelContextFunctions.TravelTime;
import com.github.rinde.logistics.pdptw.mas.TruckFactory.DefaultTruckFactory;
import com.github.rinde.logistics.pdptw.mas.comm.AuctionCommModel;
import com.github.rinde.logistics.pdptw.mas.comm.AuctionPanel;
import com.github.rinde.logistics.pdptw.mas.comm.AuctionStopConditions;
import com.github.rinde.logistics.pdptw.mas.comm.Bidder;
import com.github.rinde.logistics.pdptw.mas.comm.DoubleBid;
import com.github.rinde.logistics.pdptw.mas.comm.RtSolverBidder;
import com.github.rinde.logistics.pdptw.mas.route.RoutePlanner;
import com.github.rinde.logistics.pdptw.mas.route.RtSolverRoutePlanner;
import com.github.rinde.rinsim.central.Solver;
import com.github.rinde.rinsim.central.SolverModel;
import com.github.rinde.rinsim.central.rt.RealtimeSolver;
import com.github.rinde.rinsim.central.rt.RtSolverModel;
import com.github.rinde.rinsim.central.rt.RtStAdapters;
import com.github.rinde.rinsim.core.model.ModelBuilder;
import com.github.rinde.rinsim.core.model.time.TimeModel;
import com.github.rinde.rinsim.experiment.Experiment;
import com.github.rinde.rinsim.experiment.ExperimentResults;
import com.github.rinde.rinsim.experiment.MASConfiguration;
import com.github.rinde.rinsim.experiment.PostProcessors;
import com.github.rinde.rinsim.io.FileProvider;
import com.github.rinde.rinsim.pdptw.common.AddParcelEvent;
import com.github.rinde.rinsim.pdptw.common.AddVehicleEvent;
import com.github.rinde.rinsim.pdptw.common.ObjectiveFunction;
import com.github.rinde.rinsim.pdptw.common.RouteFollowingVehicle;
import com.github.rinde.rinsim.pdptw.common.RoutePanel;
import com.github.rinde.rinsim.pdptw.common.StatisticsDTO;
import com.github.rinde.rinsim.pdptw.common.TimeLinePanel;
import com.github.rinde.rinsim.scenario.Scenario;
import com.github.rinde.rinsim.scenario.ScenarioConverters;
import com.github.rinde.rinsim.scenario.ScenarioIO;
import com.github.rinde.rinsim.scenario.TimedEvent;
import com.github.rinde.rinsim.scenario.gendreau06.Gendreau06ObjectiveFunction;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.PDPModelRenderer;
import com.github.rinde.rinsim.ui.renderers.PlaneRoadModelRenderer;
import com.github.rinde.rinsim.util.StochasticSupplier;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

/**
 *
 * @author Rinde van Lon
 */
public class StRtComparison {

  static final ObjectiveFunction OBJ_FUNC = Gendreau06ObjectiveFunction
    .instance();

  @Test
  public void test() {

    final PriorityHeuristic<VehicleParcelContext> heuristic = GPProgramParser
      .<VehicleParcelContext>parseProgramFunc("(traveltime)",
        new Funcs().create());

    final StochasticSupplier<? extends Solver> randomSolver =
      PriorityHeuristicSolver
        .supplier(heuristic);
    final StochasticSupplier<RealtimeSolver> rtRandomSolver = RtStAdapters
      .toRealtime(randomSolver);

    final StochasticSupplier<Bidder<DoubleBid>> stBidder = RtSolverBidder
      .simulatedTimeBuilder(OBJ_FUNC, randomSolver);
    final StochasticSupplier<Bidder<DoubleBid>> rtBidder = RtSolverBidder
      .realtimeBuilder(OBJ_FUNC, rtRandomSolver);

    final StochasticSupplier<RoutePlanner> rtRoutePlanner = RtSolverRoutePlanner
      .supplier(rtRandomSolver);
    final StochasticSupplier<RoutePlanner> stRoutePlanner = RtSolverRoutePlanner
      .simulatedTimeSupplier(randomSolver);

    final Scenario rtScenario = getScenario();
    final Scenario stScenario =
      ScenarioConverters.toSimulatedtime().apply(rtScenario);

    final FileProvider.Builder fileProviderBuilder = FileProvider.builder()
      .add(Paths.get("files/scenarios/vanLonHolvoet15"))
      .filter("glob:**.scen");

    final View.Builder gui = View.builder()
      .withAutoPlay()
      .with(PlaneRoadModelRenderer.builder())
      .with(PDPModelRenderer.builder())
      .with(AuctionPanel.builder())
      .with(RoutePanel.builder())
      .with(TimeLinePanel.builder());

    final ModelBuilder<?, ?> auctionModel = AuctionCommModel
      .builder(DoubleBid.class)
      .withStopCondition(
        AuctionStopConditions.and(
          AuctionStopConditions.<DoubleBid>atLeastNumBids(2),
          AuctionStopConditions.<DoubleBid>or(
            AuctionStopConditions.<DoubleBid>allBidders(),
            AuctionStopConditions
              .<DoubleBid>maxAuctionDuration(5000))))
      .withMaxAuctionDuration(30 * 60 * 1000L);

    final Experiment.Builder stExperiment = Experiment.builder()
      .addScenario(stScenario)
      .usePostProcessor(PostProcessors.statisticsPostProcessor(OBJ_FUNC))
      .showGui(gui)
      .showGui(false)
      .withThreads(1)
      .addConfiguration(
        MASConfiguration.pdptwBuilder()
          .addEventHandler(AddVehicleEvent.class,
            DefaultTruckFactory.builder()
              .setRoutePlanner(stRoutePlanner)
              .setCommunicator(stBidder)
              .setLazyComputation(false)
              .setRouteAdjuster(RouteFollowingVehicle.delayAdjuster())
              .build())
          .addModel(auctionModel)
          .addModel(SolverModel.builder())
          .build());

    final Experiment.Builder rtExperiment = Experiment.builder()
      .addScenario(rtScenario)
      .usePostProcessor(PostProcessors.statisticsPostProcessor(OBJ_FUNC))
      .showGui(gui)
      .showGui(false)
      .withThreads(1)
      .addConfiguration(
        MASConfiguration.pdptwBuilder()
          .addEventHandler(AddVehicleEvent.class,
            DefaultTruckFactory.builder()
              .setRoutePlanner(rtRoutePlanner)
              .setCommunicator(rtBidder)
              .setLazyComputation(false)
              .setRouteAdjuster(RouteFollowingVehicle.delayAdjuster())
              .build())
          .addModel(auctionModel)
          .addModel(RtSolverModel.builder())
          .build());

    final ExperimentResults rtResults = rtExperiment.perform();
    final ExperimentResults stResults = stExperiment.perform();

    assertThat(stResults.getResults()).hasSize(1);
    assertThat(rtResults.getResults()).hasSize(1);

    final StatisticsDTO stStats = (StatisticsDTO) stResults.getResults()
      .iterator().next()
      .getResultObject();
    final StatisticsDTO rtStats = (StatisticsDTO) rtResults.getResults()
      .iterator().next()
      .getResultObject();

    assertThat(stStats.pickupTardiness).isEqualTo(rtStats.pickupTardiness);
    assertThat(stStats.deliveryTardiness).isEqualTo(rtStats.deliveryTardiness);
    assertThat(stStats.totalDistance).isWithin(0.00001)
      .of(rtStats.totalDistance);
    assertThat(stStats.totalDeliveries).isEqualTo(rtStats.totalDeliveries);
    assertThat(stStats.simFinish).isEqualTo(rtStats.simFinish);
    assertThat(stStats.movedVehicles).isEqualTo(rtStats.movedVehicles);
    assertThat(stStats.overTime).isEqualTo(rtStats.overTime);
    assertThat(stStats.simulationTime - rtStats.simulationTime).isAtMost(1000L);
  }

  static Scenario getScenario() {
    Scenario scen;
    try {
      scen = ScenarioIO.read(
        Paths.get("files/scenarios/vanLonHolvoet15/0.50-20-1.00-0.scen"));
    } catch (final IOException e) {
      throw new IllegalStateException(e);
    }

    final List<AddParcelEvent> parcelSubset =
      FluentIterable.from(scen.getEvents())
        .filter(AddParcelEvent.class)
        .limit(10)
        .toList();

    final List<TimedEvent> list = FluentIterable.from(scen.getEvents())
      .filter(Predicates.not(Predicates.instanceOf(AddParcelEvent.class)))
      .append(parcelSubset).toList();

    return Scenario.builder(scen)
      .removeModelsOfType(TimeModel.AbstractBuilder.class)
      .addModel(TimeModel.builder().withTickLength(250).withRealTime())
      .clearEvents()
      .addEvents(list)
      .build();
  }

  static class Funcs extends GPFuncSet<VehicleParcelContext> {

    Funcs() {}

    @Override
    public List<GPFunc<VehicleParcelContext>> create() {
      return ImmutableList.<GPFunc<VehicleParcelContext>>of(new TravelTime());
    }
  }
}
