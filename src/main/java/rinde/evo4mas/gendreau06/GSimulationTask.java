/**
 * 
 */
package rinde.evo4mas.gendreau06;

import java.io.ByteArrayInputStream;

import org.jppf.task.storage.DataProvider;

import rinde.ecj.Heuristic;
import rinde.evo4mas.common.ResultDTO;
import rinde.evo4mas.gendreau06.route.EvoHeuristicRoutePlanner;
import rinde.jppf.ComputationTask;
import rinde.logistics.pdptw.mas.TruckConfiguration;
import rinde.logistics.pdptw.mas.comm.BlackboardCommModel;
import rinde.logistics.pdptw.mas.comm.BlackboardUser;
import rinde.sim.pdptw.common.ObjectiveFunction;
import rinde.sim.pdptw.common.StatisticsDTO;
import rinde.sim.pdptw.experiment.Experiment;
import rinde.sim.pdptw.experiment.MASConfiguration;
import rinde.sim.pdptw.gendreau06.Gendreau06ObjectiveFunction;
import rinde.sim.pdptw.gendreau06.Gendreau06Parser;
import rinde.sim.pdptw.gendreau06.Gendreau06Scenario;

import com.google.common.collect.ImmutableList;

/**
 * FIXME refactor
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class GSimulationTask extends
    ComputationTask<ResultDTO, Heuristic<GendreauContext>> {
  private static final long serialVersionUID = 3936679021433997897L;
  protected final String scenarioKey;
  protected final int numVehicles;
  protected final MASConfiguration configuration;

  public GSimulationTask(String scenario, Heuristic<GendreauContext> data,
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
              scenarioKey).setNumVehicles(numVehicles).parse().get(0);
    } catch (final Exception e) {
      throw new RuntimeException("Failed loading scenario for task: "
          + taskData + " " + scenarioKey, e);
    }

    final ObjectiveFunction objFunc = new Gendreau06ObjectiveFunction();
    final TruckConfiguration config = new TruckConfiguration(
        EvoHeuristicRoutePlanner.supplier(taskData), BlackboardUser.supplier(),
        ImmutableList.of(BlackboardCommModel.supplier()));

    final StatisticsDTO stats = Experiment.build(objFunc).addScenario(scenario)
        .addConfiguration(config).perform().results.get(0).stats;

    final boolean isValid = objFunc.isValidResult(stats);
    final float fitness = isValid ? (float) objFunc.computeCost(stats)
        : Float.MAX_VALUE;
    setResult(new ResultDTO(scenarioKey, taskData.getId(), stats, fitness));
  }
}
