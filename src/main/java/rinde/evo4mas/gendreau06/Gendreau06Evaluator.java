/**
 * 
 */
package rinde.evo4mas.gendreau06;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.unmodifiableList;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jppf.client.JPPFJob;
import org.jppf.task.storage.DataProvider;
import org.jppf.task.storage.MemoryMapDataProvider;

import rinde.ecj.GPBaseNode;
import rinde.ecj.GPEvaluator;
import rinde.ecj.GPProgram;
import rinde.ecj.GPProgramParser;
import rinde.ecj.Heuristic;
import rinde.evo4mas.common.ExperimentUtil;
import rinde.evo4mas.common.ResultDTO;
import ec.EvolutionState;
import ec.gp.GPIndividual;
import ec.gp.GPTree;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class Gendreau06Evaluator extends GPEvaluator<GSimulationTask, ResultDTO, Heuristic<GendreauContext>> {

	static List<List<String>> folds = ExperimentUtil.createFolds("files/scenarios/gendreau06/", 5, "");

	private final List<String> trainSet;
	private final List<String> testSet;
	private final int numScenariosPerGeneration;
	private final int numScenariosAtLastGeneration;
	private final Map<String, String> scenarioCache;

	public Gendreau06Evaluator() {
		testSet = unmodifiableList(folds.get(0));
		trainSet = unmodifiableList(ExperimentUtil.createTrainSet(folds, 0));

		System.out.println(testSet + "\n" + trainSet);

		numScenariosAtLastGeneration = 5;
		numScenariosPerGeneration = 3;

		scenarioCache = newHashMap();
		try {
			for (final String s : testSet) {
				scenarioCache.put(s, ExperimentUtil.textFileToString(s));
			}
			for (final String s : trainSet) {
				scenarioCache.put(s, ExperimentUtil.textFileToString(s));
			}
		} catch (final FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
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

	void experimentOnTestSet(GPIndividual ind) {
		final GPProgram<GendreauContext> heuristic = GPProgramParser
				.convertToGPProgram((GPBaseNode<GendreauContext>) ind.trees[0].child);

		final DataProvider dataProvider = new MemoryMapDataProvider();
		final JPPFJob job = new JPPFJob(dataProvider);
		job.setBlocking(true);
		job.setName("Evaluation on test set");

		final List<GSimulationTask> list = newArrayList();
		final List<String> scenarios = testSet;
		for (final String s : scenarios) {
			try {
				dataProvider.setValue(s, scenarioCache.get(s));
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
			final int numVehicles = s.contains("_450_") ? 20 : 10;
			list.add(new GSimulationTask(s, heuristic.clone(), numVehicles, -1));
		}
		try {

			for (final GSimulationTask j : list) {
				if (compStrategy == ComputationStrategy.LOCAL) {
					j.setDataProvider(dataProvider);
				}
				job.addTask(j);
			}
			final Collection<ResultDTO> results = compute(job);

			for (final ResultDTO r : results) {
				System.out.println(r.scenarioKey + " " + r.fitness);
			}

		} catch (final Exception e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	protected Collection<GSimulationTask> createComputationJobs(DataProvider dataProvider, GPTree[] trees,
			EvolutionState state) {

		final GPProgram<GendreauContext> heuristic = GPProgramParser
				.convertToGPProgram((GPBaseNode<GendreauContext>) trees[0].child);

		final List<GSimulationTask> list = newArrayList();
		final List<String> scenarios = getCurrentScenarios(state);
		for (final String s : scenarios) {
			try {
				dataProvider.setValue(s, scenarioCache.get(s));
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
			final int numVehicles = s.contains("_450_") ? 20 : 10;
			list.add(new GSimulationTask(s, heuristic.clone(), numVehicles, 60000));
		}
		return list;
	}

	@Override
	protected int expectedNumberOfResultsPerGPIndividual() {
		return numScenariosPerGeneration;
	}

}
