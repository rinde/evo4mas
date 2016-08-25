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

import static com.google.common.truth.Truth.assertThat;
import static java.util.Arrays.asList;

import java.util.List;

import org.junit.Test;

import com.github.rinde.ecj.GPProgram;
import com.github.rinde.ecj.GPProgramParser;
import com.github.rinde.evo4mas.common.GlobalStateObjectFunctions.GpGlobal;
import com.github.rinde.evo4mas.common.GlobalStateObjectFunctions.InsertionCost;
import com.github.rinde.logistics.pdptw.mas.TruckFactory.DefaultTruckFactory;
import com.github.rinde.logistics.pdptw.mas.comm.AuctionCommModel;
import com.github.rinde.logistics.pdptw.mas.comm.AuctionPanel;
import com.github.rinde.logistics.pdptw.mas.comm.AuctionStopConditions;
import com.github.rinde.logistics.pdptw.mas.comm.Bidder;
import com.github.rinde.logistics.pdptw.mas.comm.DoubleBid;
import com.github.rinde.logistics.pdptw.mas.route.RoutePlanner;
import com.github.rinde.logistics.pdptw.mas.route.RtSolverRoutePlanner;
import com.github.rinde.logistics.pdptw.solver.CheapestInsertionHeuristic;
import com.github.rinde.rinsim.central.SolverModel;
import com.github.rinde.rinsim.core.model.ModelBuilder;
import com.github.rinde.rinsim.experiment.Experiment;
import com.github.rinde.rinsim.experiment.Experiment.SimulationResult;
import com.github.rinde.rinsim.experiment.ExperimentResults;
import com.github.rinde.rinsim.experiment.MASConfiguration;
import com.github.rinde.rinsim.experiment.PostProcessors;
import com.github.rinde.rinsim.pdptw.common.AddParcelEvent;
import com.github.rinde.rinsim.pdptw.common.AddVehicleEvent;
import com.github.rinde.rinsim.pdptw.common.ObjectiveFunction;
import com.github.rinde.rinsim.pdptw.common.RouteFollowingVehicle;
import com.github.rinde.rinsim.pdptw.common.RoutePanel;
import com.github.rinde.rinsim.pdptw.common.TimeLinePanel;
import com.github.rinde.rinsim.scenario.Scenario;
import com.github.rinde.rinsim.scenario.ScenarioConverters;
import com.github.rinde.rinsim.scenario.gendreau06.Gendreau06ObjectiveFunction;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.PDPModelRenderer;
import com.github.rinde.rinsim.ui.renderers.PlaneRoadModelRenderer;
import com.github.rinde.rinsim.util.StochasticSupplier;

/**
 *
 * @author Rinde van Lon
 */
public class EvoBidderTest {
  static final ObjectiveFunction OBJ_FUNC =
    Gendreau06ObjectiveFunction.instance(50d);

  // checks that obj_func insertion cost gives same result as prio heuristic
  // based insertion cost
  @Test
  public void test() {
    final GPProgram<GpGlobal> program =
      GPProgramParser.parseProgramFunc("(insertioncost)",
        asList(new InsertionCost()));

    final StochasticSupplier<Bidder<DoubleBid>> defaultEvoBidder =
      EvoBidder.simulatedTimeBuilder(program, OBJ_FUNC)
        .withReauctionCooldownPeriod(60000L);

    final StochasticSupplier<Bidder<DoubleBid>> prioHeurEvoBidder =
      EvoBidder.simulatedTimeBuilder(program, OBJ_FUNC)
        .withReauctionCooldownPeriod(60000L)
        .withPriorityHeuristicForReauction();

    final StochasticSupplier<RoutePlanner> stRoutePlanner = RtSolverRoutePlanner
      .simulatedTimeSupplier(CheapestInsertionHeuristic.supplier(OBJ_FUNC));

    final Scenario rtScenario = StRtComparison.getScenario();
    final Scenario stScenario =
      ScenarioConverters.toSimulatedtime().apply(rtScenario);

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
              .setCommunicator(defaultEvoBidder)
              .setLazyComputation(false)
              .setRouteAdjuster(RouteFollowingVehicle.delayAdjuster())
              .build())
          .addModel(auctionModel)
          .addModel(SolverModel.builder())
          .build())

      .addConfiguration(
        MASConfiguration.pdptwBuilder()
          .addEventHandler(AddVehicleEvent.class,
            DefaultTruckFactory.builder()
              .setRoutePlanner(stRoutePlanner)
              .setCommunicator(prioHeurEvoBidder)
              .setLazyComputation(false)
              .setRouteAdjuster(RouteFollowingVehicle.delayAdjuster())
              .build())
          .addEventHandler(AddParcelEvent.class, AddParcelEvent.namedHandler())
          .addModel(auctionModel)
          .addModel(SolverModel.builder())
          .build());

    final ExperimentResults results = stExperiment.perform();

    assertThat(results.getResults()).hasSize(2);
    final List<SimulationResult> res = results.getResults().asList();
    assertThat(res.get(0).getResultObject())
      .isEqualTo(res.get(1).getResultObject());

  }
}
