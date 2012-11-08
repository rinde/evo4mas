/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import java.util.List;

import rinde.evo4mas.evo.gp.GPComputationResult;
import rinde.evo4mas.evo.gp.GPStats;
import rinde.evo4mas.mas.common.ResultDTO;
import rinde.sim.problem.common.StatsTracker.StatisticsDTO;
import ec.EvolutionState;
import ec.Individual;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class EvoStatistics extends GPStats {

	private static final long serialVersionUID = -4756048854629216449L;

	@Override
	public void printMore(List<GPComputationResult> results) {
		// final List<FRResultDTO> results = ((GPFitness<FRResultDTO>)
		// best_i.fitness).getResults();
		// System.out.println(((FRSimulationDTO)
		// results.get(0).getComputationJob()).program.toString());

		final StatisticsDTO stats = ((ResultDTO) results.get(0)).stats;

		System.out.println(stats);
		System.out.println(results.get(0).getFitness());

		System.out.println("avg cost/delivery " + stats.totalDistance / stats.totalDeliveries);

	}

	@Override
	public void finalStatistics(final EvolutionState state, final int result) {
		Individual best = null; // quiets compiler complaints
		for (int y = 1; y < state.population.subpops[0].individuals.length; y++) {
			if (best == null || state.population.subpops[0].individuals[y].fitness.betterThan(best.fitness)) {
				best = state.population.subpops[0].individuals[y];
			}
		}
		// final List<GPComputationResult> list =
		// ((GPFitness<GPComputationResult>) best.fitness).getResults();

		// ((FREvaluator) state.evaluator)
		// .testOnTestSet( ((FRSimulationDTO)
		// list.get(0).getComputationJob()).truckHeuristic.clone());
	}
}
