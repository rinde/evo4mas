/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.unmodifiableList;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rinde.cloud.javainterface.Computer;
import rinde.evo4mas.evo.gp.GPEvaluator;
import rinde.evo4mas.evo.gp.GPFunc;
import rinde.evo4mas.evo.gp.GPProgram;
import scala.actors.threadpool.Arrays;
import ec.EvolutionState;
import ec.gp.GPTree;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class FREvaluator extends GPEvaluator<FRSimulationDTO, FRResultDTO, FRContext> {

	private static final long serialVersionUID = 7755793133305470461L;
	static List<List<String>> folds = createFolds("files/scenarios/fabri-recht/pdp100_mitAnrufzeit/", 5);

	static List<List<String>> createFolds(String dir, int n) {
		final String[] scens = new File(dir).list(new FilenameFilter() {
			public boolean accept(File d, String name) {
				return name.endsWith(".scenario");
			}
		});
		// sort on file name such that produced folds are always deterministic
		// and do not depend on filesystem ordering.
		Arrays.sort(scens);
		final List<List<String>> fs = newArrayList();
		for (int i = 0; i < n; i++) {
			fs.add(new ArrayList<String>());
		}
		for (int i = 0; i < scens.length; i++) {
			fs.get(i % n).add(dir + scens[i]);
		}
		return fs;
	}

	static List<String> createTrainSet(List<List<String>> fds, int testFold) {
		final List<String> set = newArrayList();
		for (int i = 0; i < fds.size(); i++) {
			if (testFold != i) {
				set.addAll(fds.get(i));
			}
		}
		return set;
	}

	private final List<String> trainSet;
	private final List<String> testSet;
	private final int numScenariosPerGeneration;
	private final int numScenariosAtLastGeneration;

	public FREvaluator() {
		testSet = unmodifiableList(folds.get(0));
		trainSet = unmodifiableList(createTrainSet(folds, 0));
		numScenariosPerGeneration = 3;
		numScenariosAtLastGeneration = 10;
	}

	@Override
	public void evaluatePopulation(EvolutionState state) {
		System.out.println(getCurrentScenarios(state));
		super.evaluatePopulation(state);
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
	protected Collection<FRSimulationDTO> createComputationJobs(GPTree[] trees, EvolutionState state) {
		final GPProgram<FRContext> heuristic = new GPProgram<FRContext>((GPFunc<FRContext>) trees[0].child);
		final List<FRSimulationDTO> list = newArrayList();
		final List<String> scenarios = getCurrentScenarios(state);
		for (final String s : scenarios) {
			list.add(new FRSimulationDTO(heuristic.clone(), s));
		}
		return list;
		// final GPProgram<FRContext> acceptance = new
		// GPProgram<FRContext>((GPFunc<FRContext>) trees[1].child);
		// return asList(new FRSimulationDTO(heuristic,
		// "files/scenarios/fabri-recht/pdp100_mitAnrufzeit/lc107.scenario"));
	}

	@Override
	protected Computer<FRSimulationDTO, FRResultDTO> createComputer() {
		return new FRSimulationComputer();
	}

	@Override
	protected int expectedNumberOfResultsPerGPIndividual() {
		return numScenariosPerGeneration;
	}

	/**
	 * @param list
	 */
	public void testOnTestSet(GPProgram<FRContext> prog) {
		System.out.println("TEST RESULTS");
		final List<FRSimulationDTO> list = newArrayList();
		for (final String scen : testSet) {
			list.add(new FRSimulationDTO(prog.clone(), scen));
		}
		final Collection<FRResultDTO> results = compute(list);
		for (final FRResultDTO r : results) {
			System.out.println(r);
		}

	}

}
