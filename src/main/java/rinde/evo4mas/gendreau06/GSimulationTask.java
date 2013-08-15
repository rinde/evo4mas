/**
 * 
 */
package rinde.evo4mas.gendreau06;

import java.io.BufferedReader;
import java.io.StringReader;

import org.jppf.task.storage.DataProvider;

import rinde.ecj.Heuristic;
import rinde.evo4mas.common.ResultDTO;
import rinde.evo4mas.gendreau06.GSimulation.Configurator;
import rinde.jppf.ComputationTask;
import rinde.sim.pdptw.common.ObjectiveFunction;
import rinde.sim.pdptw.common.StatsTracker.StatisticsDTO;
import rinde.sim.pdptw.gendreau06.Gendreau06ObjectiveFunction;
import rinde.sim.pdptw.gendreau06.Gendreau06Parser;
import rinde.sim.pdptw.gendreau06.Gendreau06Scenario;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class GSimulationTask extends ComputationTask<ResultDTO, Heuristic<GendreauContext>> {
	private static final long serialVersionUID = 3936679021433997897L;
	protected final String scenarioKey;
	protected final int numVehicles;
	protected final Configurator configurator;

	public GSimulationTask(String scenario, Heuristic<GendreauContext> data, int vehicles, Configurator conf) {
		super(data);
		scenarioKey = scenario;
		numVehicles = vehicles;
		configurator = conf;
	}

	public void run() {
		final DataProvider dataProvider = getDataProvider();
		Gendreau06Scenario scenario;
		try {
			final String scenarioString = (String) dataProvider.getValue(scenarioKey);
			scenario = Gendreau06Parser
					.parse(new BufferedReader(new StringReader(scenarioString)), scenarioKey, numVehicles);
		} catch (final Exception e) {
			throw new RuntimeException("Failed loading scenario for task: " + taskData + " " + scenarioKey, e);
		}

		final StatisticsDTO stats = GSimulation.simulate(scenario, configurator, false);
		final ObjectiveFunction objFunc = new Gendreau06ObjectiveFunction();
		final boolean isValid = objFunc.isValidResult(stats);
		final float fitness = isValid ? (float) objFunc.computeCost(stats) : Float.MAX_VALUE;
		setResult(new ResultDTO(scenarioKey, taskData.getId(), stats, fitness));
	}
}
