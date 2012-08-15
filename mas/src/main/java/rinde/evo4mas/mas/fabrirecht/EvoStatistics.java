/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import java.util.List;

import rinde.evo4mas.evo.gp.GPComputationResult;
import rinde.evo4mas.evo.gp.GPStats;

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
		System.out.println(((FRSimulationDTO) results.get(0).getComputationJob()).program.toString());
		System.out.println(((FRResultDTO) results.get(0)).stats);
		System.out.println(results.get(0).getFitness());
	}
}
