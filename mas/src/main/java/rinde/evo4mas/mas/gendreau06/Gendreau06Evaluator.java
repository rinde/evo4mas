/**
 * 
 */
package rinde.evo4mas.mas.gendreau06;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.unmodifiableList;

import java.util.Collection;
import java.util.List;

import org.jppf.task.storage.DataProvider;

import rinde.evo4mas.evo.gp.GPBaseNode;
import rinde.evo4mas.evo.gp.GPEvaluator;
import rinde.evo4mas.evo.gp.GPProgram;
import rinde.evo4mas.evo.gp.GPProgramParser;
import rinde.evo4mas.mas.common.ExperimentUtil;
import rinde.evo4mas.mas.common.ResultDTO;
import rinde.evo4mas.mas.common.TruckContext;
import rinde.evo4mas.mas.fabrirecht.FRSimulationTask;
import ec.EvolutionState;
import ec.gp.GPTree;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class Gendreau06Evaluator extends GPEvaluator<GSimulationTask, ResultDTO, TruckContext> {

	static List<List<String>> folds = ExperimentUtil.createFolds("files/scenarios/gendreau06/", 5);

	private final List<String> trainSet;
	private final List<String> testSet;
	private final int numScenariosPerGeneration;
	private final int numScenariosAtLastGeneration;

	public Gendreau06Evaluator() {
		testSet = unmodifiableList(folds.get(0));
		trainSet = unmodifiableList(ExperimentUtil.createTrainSet(folds, 0));
		numScenariosAtLastGeneration = 5;
		numScenariosPerGeneration = 3;

	}

	List<String> getCurrentScenarios(EvolutionState state) {
		final List<String> list = newArrayList();
		final int numScens = state.generation == state.numGenerations - 1 ? numScenariosAtLastGeneration
				: numScenariosPerGeneration;
		for (int i = 0; i < numScenariosPerGeneration; i++) {
			list.add(trainSet.get((state.generation * numScens + i) % trainSet.size()));
		}
		return list;
	}

	@Override
	protected Collection<GSimulationTask> createComputationJobs(DataProvider dataProvider, GPTree[] trees,
			EvolutionState state) {
		final GPProgram<TruckContext> heuristic = GPProgramParser
				.convertToGPProgram((GPBaseNode<TruckContext>) trees[0].child);

		final List<FRSimulationTask> list = newArrayList();
		final List<String> scenarios = getCurrentScenarios(state);
		for (final String s : scenarios) {
			try {
				dataProvider.setValue(s, scenarioCache.get(s));
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
			list.add(new FRSimulationTask(s, heuristic.clone(), numVehicles, vehicleCapacity));
		}
		return list;
		// final GPProgram<FRContext> acceptance = new
		// GPProgram<FRContext>((GPFunc<FRContext>) trees[1].child);
		// return asList(new FRSimulationDTO(heuristic,
		// "files/scenarios/fabri-recht/pdp100_mitAnrufzeit/lc107.scenario"));
	}

	@Override
	protected int expectedNumberOfResultsPerGPIndividual() {
		// TODO Auto-generated method stub
		return 0;
	}

}
