/**
 * 
 */
package rinde.evo4mas.gendreau06;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.unmodifiableList;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jppf.task.storage.DataProvider;

import rinde.ecj.GPBaseNode;
import rinde.ecj.GPEvaluator;
import rinde.ecj.GPProgram;
import rinde.ecj.GPProgramParser;
import rinde.evo4mas.common.ExperimentUtil;
import rinde.evo4mas.common.ResultDTO;
import rinde.evo4mas.common.TruckContext;
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
	private final Map<String, String> scenarioCache;
	private final int numVehicles;

	public Gendreau06Evaluator() {
		testSet = unmodifiableList(folds.get(0));
		trainSet = unmodifiableList(ExperimentUtil.createTrainSet(folds, 0));
		numScenariosAtLastGeneration = 5;
		numScenariosPerGeneration = 3;

		// TODO set proper nr of vehicles
		numVehicles = 5;
		scenarioCache = newHashMap();
		try {
			for (final String s : testSet) {
				scenarioCache.put(s, readScenarioFile(s));
			}
			for (final String s : trainSet) {
				scenarioCache.put(s, readScenarioFile(s));
			}
		} catch (final FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String readScenarioFile(String file) throws IOException {
		final StringBuilder sb = new StringBuilder();
		final BufferedReader bf = new BufferedReader(new FileReader(file));
		String line;
		while ((line = bf.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();

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

		final List<GSimulationTask> list = newArrayList();
		final List<String> scenarios = getCurrentScenarios(state);
		for (final String s : scenarios) {
			try {
				dataProvider.setValue(s, scenarioCache.get(s));
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
			list.add(new GSimulationTask(s, heuristic.clone(), numVehicles));
		}
		return list;
	}

	@Override
	protected int expectedNumberOfResultsPerGPIndividual() {
		// TODO Auto-generated method stub
		return 0;
	}

}
