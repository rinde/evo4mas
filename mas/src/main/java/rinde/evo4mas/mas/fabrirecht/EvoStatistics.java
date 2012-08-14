/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import java.util.List;

import rinde.evo4mas.evo.gp.GPFitness;
import ec.EvolutionState;
import ec.Individual;
import ec.Statistics;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class EvoStatistics extends Statistics {

	private static final long serialVersionUID = -4756048854629216449L;

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

		final List<FRResultDTO> results = ((GPFitness<FRResultDTO>) best_i.fitness).getResults();
		System.out.println(((FRSimulationDTO) results.get(0).compJob).program.toString());
		System.out.println(results.get(0).stats);
		System.out.println(results.get(0).getFitness());
		// bestOfGeneration.add(best_i);
	}
}
