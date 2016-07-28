/**
 *
 */
package com.github.rinde.evo4mas.gendreau06;

import java.io.ByteArrayInputStream;

import org.jppf.task.storage.DataProvider;

import com.github.rinde.ecj.PriorityHeuristic;
import com.github.rinde.evo4mas.common.ResultDTO;
import com.github.rinde.evo4mas.gendreau06.route.EvoHeuristicRoutePlanner;
import com.github.rinde.jppf.ComputationTask;
import com.github.rinde.logistics.pdptw.mas.comm.BlackboardCommModel;
import com.github.rinde.logistics.pdptw.mas.comm.BlackboardUser;
import com.github.rinde.rinsim.experiment.Experiment;
import com.github.rinde.rinsim.experiment.MASConfiguration;
import com.github.rinde.rinsim.pdptw.common.ObjectiveFunction;
import com.github.rinde.rinsim.pdptw.common.StatisticsDTO;
import com.github.rinde.rinsim.scenario.gendreau06.Gendreau06ObjectiveFunction;
import com.github.rinde.rinsim.scenario.gendreau06.Gendreau06Parser;
import com.github.rinde.rinsim.scenario.gendreau06.Gendreau06Scenario;
import com.google.common.collect.ImmutableList;

/**
 * FIXME refactor
 * @author Rinde van Lon
 *
 */
public class GSimulationTask extends
    ComputationTask<ResultDTO, PriorityHeuristic<GendreauContext>> {
  private static final long serialVersionUID = 3936679021433997897L;
  protected final String scenarioKey;
  protected final int numVehicles;
  protected final MASConfiguration configuration;

  public GSimulationTask(String scenario, PriorityHeuristic<GendreauContext> data,
      int vehicles, MASConfiguration conf) {
    super(data);
    scenarioKey = scenario;
    numVehicles = vehicles;
    configuration = conf;
  }

  @Override
  public void run() {
    final DataProvider dataProvider = getDataProvider();
    Gendreau06Scenario scenario;

    try {
      final String scenarioString = (String) dataProvider.getValue(scenarioKey);
      scenario = Gendreau06Parser
          .parser()
          .addFile(new ByteArrayInputStream(scenarioString.getBytes()),
              scenarioKey)
          .setNumVehicles(numVehicles).parse().get(0);
    } catch (final Exception e) {
      throw new RuntimeException("Failed loading scenario for task: "
          + taskData + " " + scenarioKey, e);
    }

    final ObjectiveFunction objFunc = Gendreau06ObjectiveFunction.instance();
    final TruckConfiguration config = new TruckConfiguration(
        EvoHeuristicRoutePlanner.supplier(taskData), BlackboardUser.supplier(),
        ImmutableList.of(BlackboardCommModel.supplier()));

    final StatisticsDTO stats = Experiment.build(objFunc).addScenario(scenario)
        .addConfiguration(config).perform().results.asList().get(0).stats;

    final boolean isValid = objFunc.isValidResult(stats);
    final float fitness = isValid ? (float) objFunc.computeCost(stats)
        : Float.MAX_VALUE;
    setResult(new ResultDTO(scenarioKey, taskData.getId(), stats, fitness));
  }
}
