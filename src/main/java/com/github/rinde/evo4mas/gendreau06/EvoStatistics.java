/**
 * 
 */
package com.github.rinde.evo4mas.gendreau06;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.github.rinde.evo4mas.common.ResultDTO;

import rinde.ecj.GPStats;
import rinde.jppf.GPComputationResult;
import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.util.Parameter;

/**
 * @author Rinde van Lon 
 * 
 */
public class EvoStatistics extends GPStats {

	private static final long serialVersionUID = -4756048854629216449L;

	String path;

	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);

		final String dirName = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
		path = "runs/" + dirName;
		checkState(new File(path).mkdir(), "dir %s could not be created", path);
		createSymbolicLink(path, "runs/latest");
	}

	public static void createSymbolicLink(String target, String link) {
		final String[] cmd = { "ln", "-sFh", new File(target).getAbsolutePath(), new File(link).getAbsolutePath() };
		int exitCode;
		try {
			exitCode = Runtime.getRuntime().exec(cmd).waitFor();
			checkState(exitCode == 0, "ln signaled an error with exit code %s", exitCode);
		} catch (final Exception e) {
			throw new RuntimeException("Symbolic link creation failed.", e);
		}

	}

	@Override
	public void printMore(EvolutionState state, Individual best, List<GPComputationResult> results) {

		// TODO also write fitness values of current best individual on train
		// data?

		// do experiment on test set with best individual
		final List<ResultDTO> testResults = newArrayList(((Gendreau06Evaluator) state.evaluator)
				.experimentOnTestSet((GPIndividual) best));

		Collections.sort(testResults, new Comparator<ResultDTO>() {
			public int compare(ResultDTO o1, ResultDTO o2) {
				return o1.taskDataId.compareTo(o2.taskDataId);
			}
		});

		// write results to some file
		final StringBuilder sb = new StringBuilder();
		sb.append(state.generation);

		float sum = 0f;
		for (final ResultDTO dto : testResults) {
			sum += dto.fitness;
		}
		sb.append(" ").append(sum / testResults.size());
		for (final ResultDTO dto : testResults) {
			sb.append(" ").append(dto.fitness);
		}

		try {
			final PrintWriter out = new PrintWriter(
					new BufferedWriter(new FileWriter(path + "/fitness-test.log", true)));
			out.println(sb.toString());
			out.close();
		} catch (final IOException e) {
			throw new RuntimeException("Something went wrong when writing results.");
		}

	}

	@Override
	public void finalStatistics(final EvolutionState state, final int result) {

		Individual best = null;
		for (int y = 1; y < state.population.subpops[0].individuals.length; y++) {
			if (best == null || state.population.subpops[0].individuals[y].fitness.betterThan(best.fitness)) {
				best = state.population.subpops[0].individuals[y];
			}
		}

		final Collection<ResultDTO> results = ((Gendreau06Evaluator) state.evaluator)
				.experimentOnTestSet((GPIndividual) best);

		for (final ResultDTO r : results) {
			System.out.println(r.scenarioKey + " " + r.fitness);
		}

		// final List<GPComputationResult> list =
		// ((GPFitness<GPComputationResult>) best.fitness).getResults();

		// ((FREvaluator) state.evaluator)
		// .testOnTestSet( ((FRSimulationDTO)
		// list.get(0).getComputationJob()).truckHeuristic.clone());
	}
}
