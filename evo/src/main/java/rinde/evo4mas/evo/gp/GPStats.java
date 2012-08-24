/**
 * 
 */
package rinde.evo4mas.evo.gp;

import java.util.List;

import ec.EvolutionState;
import ec.Individual;
import ec.Statistics;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class GPStats extends Statistics {

	@Override
	public void postEvaluationStatistics(final EvolutionState state) {
		super.postEvaluationStatistics(state);

		if (state.population.subpops.length > 1) {
			throw new IllegalStateException("More than one subpop is not supported.");
		}

		Individual best_i = null; // quiets compiler complaints
		for (int y = 1; y < state.population.subpops[0].individuals.length; y++) {
			if (best_i == null || state.population.subpops[0].individuals[y].fitness.betterThan(best_i.fitness)) {
				best_i = state.population.subpops[0].individuals[y];
			}
		}

		final List<GPComputationResult> results = ((GPFitness<GPComputationResult>) best_i.fitness).getResults();
		System.out.println(results);
		System.out.println(((GPComputationJob) results.get(0).getComputationJob()).getPrograms());
		System.out.println(results.get(0).getFitness());
		printMore(results);
	}

	public void printMore(List<GPComputationResult> results) {}

}
