/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.unmodifiableList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jppf.client.JPPFClient;
import org.jppf.client.JPPFJob;
import org.jppf.task.storage.DataProvider;
import org.jppf.task.storage.MemoryMapDataProvider;

import rinde.evo4mas.evo.gp.GPBaseNode;
import rinde.evo4mas.evo.gp.GPEvaluator;
import rinde.evo4mas.evo.gp.GPProgram;
import rinde.evo4mas.evo.gp.GPProgramParser;
import scala.actors.threadpool.Arrays;
import ec.EvolutionState;
import ec.gp.GPTree;
import ec.util.Parameter;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class FREvaluator extends GPEvaluator<FRSimulationTask, FRResultDTO, FRContext> {

	private static final long serialVersionUID = 7755793133305470461L;
	static List<List<String>> folds = createFolds("files/scenarios/fabri-recht/pdp100_mitAnrufzeit/", 5);

	public static final String P_NUM_VEHICLES = "num_vehicles";
	public static final String P_VEHICLE_CAPACITY = "vehicle_capacity";

	protected int numVehicles;
	protected int vehicleCapacity;

	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		numVehicles = state.parameters.getInt(base.push(P_NUM_VEHICLES), null);
		checkState(numVehicles > 0, "eval.num_vehicles must be defined as a positive integer");
		vehicleCapacity = state.parameters.getInt(base.push(P_VEHICLE_CAPACITY), null);
		checkState(vehicleCapacity > 0, "eval.vehicle_capacity must be defined as a positive integer");
	}

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

	private final Map<String, String> scenarioCache;
	private final List<String> trainSet;
	private final List<String> testSet;
	private final int numScenariosPerGeneration;
	private final int numScenariosAtLastGeneration;

	public FREvaluator() {
		testSet = unmodifiableList(folds.get(0));
		trainSet = unmodifiableList(createTrainSet(folds, 0));
		numScenariosPerGeneration = 3;
		numScenariosAtLastGeneration = 10;
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
	protected Collection<FRSimulationTask> createComputationJobs(DataProvider dataProvider, GPTree[] trees,
			EvolutionState state) {
		final GPProgram<FRContext> heuristic = GPProgramParser
				.convertToGPProgram((GPBaseNode<FRContext>) trees[0].child);

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
		return numScenariosPerGeneration;
	}

	/**
	 * @param list
	 * @throws Exception
	 */
	public void testOnTestSet(GPProgram<FRContext> heuristic) throws Exception {
		System.out.println("TEST RESULTS");
		final DataProvider dataProvider = new MemoryMapDataProvider();
		final JPPFJob job = new JPPFJob(dataProvider);
		job.setBlocking(true);
		job.setName("Test best result on test set");

		for (final String s : testSet) {
			System.out.println(s);// + " " + scenarioCache.get(s));
			dataProvider.setValue(s, scenarioCache.get(s));
			job.addTask(new FRSimulationTask(s, heuristic.clone(), numVehicles, vehicleCapacity));
		}
		System.out.println("Executing " + testSet.size() + " jobs.");
		final Collection<FRResultDTO> results = compute(job);
		for (final FRResultDTO r : results) {
			System.out.println(r);
		}
	}

	public static void main(String[] args) throws Exception {
		final FREvaluator eval = new FREvaluator();
		eval.numVehicles = 5;
		eval.vehicleCapacity = 4;
		eval.compStrategy = ComputationStrategy.DISTRIBUTED;
		eval.jppfClient = new JPPFClient();

		final String best = "(if4 (div (mul (sub dist mido) mado) (sub 1.0 0.0)) (add mado (div (add (div (div mido 1.0) mido) (mul (if4 mido urge 1.0 ado) (mul mado ttl))) (if4 mado mido (mul (mul (if4 (sub 1.0 dist) 0.0 urge ado) (mul (sub (pow ttl 0.0) (pow (mul (sub dist mido) (add mado (div 0.0 ttl))) (if4 (if4 urge dist mado est) (if4 0.0 urge urge mado) (mul mado ttl) (div 0.0 ado)))) (add (if4 0.0 dist dist (if4 (div (sub urge mido) (sub est ttl)) (pow mido est) (div (add ado urge) mido) (mul (add dist dist) (div mido 1.0)))) (sub (add 1.0 urge) (pow (mul (if4 1.0 ado ado urge) (add mado (div 0.0 ttl))) (if4 (pow (add mado dist) 0.0) (if4 0.0 urge urge mado) (mul mado ttl) (div 0.0 ado))))))) (sub (add 1.0 urge) (pow (if4 0.0 dist dist mido) (if4 (add mido 0.0) (if4 0.0 urge urge mado) (pow ttl 1.0) (div 0.0 ado))))) (sub urge mido)))) (if4 ado urge 1.0 urge) (if4 (add mido 0.0) (sub ado mido) (add ado urge) (pow ttl 1.0)))";
		final GPProgram<FRContext> prog = GPProgramParser.parseProgramFunc(best, new GPFunctions().create());

		eval.testOnTestSet(prog);
	}
}
