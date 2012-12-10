/**
 * 
 */
package rinde.evo4mas.gendreau06;

import java.util.List;

import rinde.ecj.GPStats;
import rinde.jppf.GPComputationResult;
import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class EvoStatistics extends GPStats {

	private static final long serialVersionUID = -4756048854629216449L;

	@Override
	public void printMore(List<GPComputationResult> results) {

	}

	@Override
	public void finalStatistics(final EvolutionState state, final int result) {

		Individual best = null;
		for (int y = 1; y < state.population.subpops[0].individuals.length; y++) {
			if (best == null || state.population.subpops[0].individuals[y].fitness.betterThan(best.fitness)) {
				best = state.population.subpops[0].individuals[y];
			}
		}

		((Gendreau06Evaluator) state.evaluator).experimentOnTestSet((GPIndividual) best);

		// final List<GPComputationResult> list =
		// ((GPFitness<GPComputationResult>) best.fitness).getResults();

		// ((FREvaluator) state.evaluator)
		// .testOnTestSet( ((FRSimulationDTO)
		// list.get(0).getComputationJob()).truckHeuristic.clone());
	}
}
