/**
 * 
 */
package com.github.rinde.evo4mas.fabrirecht;

import java.util.List;

import com.github.rinde.evo4mas.common.ResultDTO;
import com.github.rinde.rinsim.pdptw.common.StatisticsDTO;

import rinde.ecj.GPStats;
import rinde.jppf.GPComputationResult;
import ec.EvolutionState;
import ec.Individual;

/**
 * @author Rinde van Lon 
 * 
 */
public class EvoStatistics extends GPStats {

  private static final long serialVersionUID = -4756048854629216449L;

  @Override
  public void printMore(EvolutionState state, Individual best,
      List<GPComputationResult> results) {
    // final List<FRResultDTO> results = ((GPFitness<FRResultDTO>)
    // best_i.fitness).getResults();
    // System.out.println(((FRSimulationDTO)
    // results.get(0).getComputationJob()).program.toString());

    final StatisticsDTO stats = ((ResultDTO) results.get(0)).stats;

    // System.out.println(stats);
    // System.out.println("Best of generation: " +
    // results.get(0).getTaskDataId());
    // System.out.println(results.get(0).getFitness());

    // System.out.println("avg cost/delivery " + stats.totalDistance /
    // stats.totalDeliveries);

  }

  @Override
  public void finalStatistics(final EvolutionState state, final int result) {
    Individual best = null; // quiets compiler complaints
    for (int y = 1; y < state.population.subpops[0].individuals.length; y++) {
      if (best == null
          || state.population.subpops[0].individuals[y].fitness
              .betterThan(best.fitness)) {
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
