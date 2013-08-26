/**
 * 
 */
package rinde.evo4mas.fabrirecht;

import static com.google.common.base.Preconditions.checkState;
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

import org.jppf.client.JPPFClient;
import org.jppf.client.JPPFJob;
import org.jppf.task.storage.DataProvider;
import org.jppf.task.storage.MemoryMapDataProvider;

import rinde.ecj.GPBaseNode;
import rinde.ecj.GPEvaluator;
import rinde.ecj.GPProgram;
import rinde.ecj.GPProgramParser;
import rinde.ecj.Heuristic;
import rinde.evo4mas.common.ResultDTO;
import rinde.evo4mas.common.TruckContext;
import rinde.logistics.pdptw.mas.ExperimentUtil;
import ec.EvolutionState;
import ec.gp.GPTree;
import ec.util.Parameter;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class FREvaluator extends GPEvaluator<FRSimulationTask, ResultDTO, Heuristic<TruckContext>> {

	private static final long serialVersionUID = 7755793133305470461L;
	static List<List<String>> folds = ExperimentUtil
			.createFolds("files/scenarios/fabri-recht/pdp100_mitAnrufzeit/", 5, ".scenario");

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

	private final Map<String, String> scenarioCache;
	private final List<String> trainSet;
	private final List<String> testSet;
	private final int numScenariosPerGeneration;
	private final int numScenariosAtLastGeneration;

	public FREvaluator() {
		testSet = unmodifiableList(folds.get(0));
		trainSet = unmodifiableList(ExperimentUtil.createTrainSet(folds, 0));
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
	protected int expectedNumberOfResultsPerGPIndividual(EvolutionState state) {
		return numScenariosPerGeneration;
	}

	/**
	 * @param list
	 * @throws Exception
	 */
	public void testOnTestSet(GPProgram<TruckContext> heuristic) throws Exception {
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
		final Collection<ResultDTO> results = compute(job);
		for (final ResultDTO r : results) {
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
		final GPProgram<TruckContext> prog = GPProgramParser.parseProgramFunc(best, new FRFunctions().create());

		eval.testOnTestSet(prog);
	}
}
