/**
 * 
 */
package rinde.evo4mas.gendreau06;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import org.jppf.task.storage.DataProvider;

import rinde.ecj.Heuristic;
import rinde.evo4mas.common.ExperimentUtil;
import rinde.evo4mas.common.ResultDTO;
import rinde.jppf.ComputationTask;
import rinde.sim.core.Simulator;
import rinde.sim.problem.common.AddVehicleEvent;
import rinde.sim.problem.common.DynamicPDPTWProblem;
import rinde.sim.problem.common.DynamicPDPTWProblem.Creator;
import rinde.sim.problem.common.ObjectiveFunction;
import rinde.sim.problem.common.StatsTracker.StatisticsDTO;
import rinde.sim.problem.gendreau06.Gendreau06ObjectiveFunction;
import rinde.sim.problem.gendreau06.Gendreau06Parser;
import rinde.sim.problem.gendreau06.Gendreau06Scenario;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class GSimulationTask extends ComputationTask<ResultDTO, Heuristic<GendreauContext>> {

	private static final long serialVersionUID = -4669749528059234353L;
	protected final String scenarioKey;
	protected final int numVehicles;

	public GSimulationTask(String scenario, Heuristic<GendreauContext> p, int vehicles) {
		super(p);
		scenarioKey = scenario;
		numVehicles = vehicles;
	}

	// extension hook
	protected void preSimulate(DynamicPDPTWProblem problem) {}

	public void run() {
		final ObjectiveFunction objFunc = new Gendreau06ObjectiveFunction();
		final DataProvider dataProvider = getDataProvider();
		String scenarioString;
		try {
			scenarioString = (String) dataProvider.getValue(scenarioKey);
			System.out.println(taskData.getId());
			final Gendreau06Scenario scenario = Gendreau06Parser.parse(new BufferedReader(new StringReader(
					scenarioString)), scenarioKey, numVehicles);
			final DynamicPDPTWProblem problem = new DynamicPDPTWProblem(scenario, 123, new CoordinationModel());
			problem.addCreator(AddVehicleEvent.class, new Creator<AddVehicleEvent>() {
				public boolean create(Simulator sim, AddVehicleEvent event) {
					return sim.register(new HeuristicTruck(event.vehicleDTO, taskData));
				}
			});
			preSimulate(problem);
			final StatisticsDTO stats = problem.simulate();
			final boolean isValid = objFunc.isValidResult(stats);

			final float fitness = isValid ? (float) objFunc.computeCost(stats) : Float.MAX_VALUE;
			setResult(new ResultDTO(scenarioKey, taskData.getId(), stats, fitness));
			System.out
					.println(fitness + " valid:" + isValid + " task done: " + objFunc.printHumanReadableFormat(stats));
		} catch (final Exception e) {
			throw new RuntimeException("Failed simulation task: " + taskData, e);
		}
	}

	@Override
	public ResultDTO getComputationResult() {
		return (ResultDTO) getResult();
	}

	public static GSimulationTask createTestableTask(final String fileName, Heuristic<GendreauContext> p, int vehicles,
			final boolean showGui) {
		try {
			final String scenarioString = ExperimentUtil.textFileToString(fileName);
			final GSimulationTask task = new GSimulationTask(new File(fileName).getName(), p, vehicles) {
				@Override
				protected void preSimulate(DynamicPDPTWProblem problem) {
					if (showGui) {
						problem.enableUI();
					}
				}
			};
			task.setDataProvider(new DataProvider() {
				public Object getValue(Object key) throws Exception {
					return scenarioString;
				}

				public void setValue(Object key, Object value) throws Exception {}
			});
			return task;
		} catch (final IOException e) {
			checkArgument(false, "something was wrong while reading " + fileName + " : " + e.getMessage());
		}
		return null;
	}
}
