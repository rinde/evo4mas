/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import rinde.cloud.javainterface.ComputationJob;
import rinde.evo4mas.evo.gp.GPComputationResult;
import rinde.sim.problem.fabrirecht.FabriRechtProblem.StatisticsDTO;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class FRResultDTO implements GPComputationResult {

	private static final long serialVersionUID = 2053089876856764188L;
	protected final ComputationJob compJob;
	protected final StatisticsDTO stats;
	protected final float fitness;

	public FRResultDTO(ComputationJob cj, StatisticsDTO stat, float fit) {
		compJob = cj;
		stats = stat;
		fitness = fit;
	}

	public ComputationJob getComputationJob() {
		return compJob;
	}

	public float getFitness() {
		return fitness;
	}
}
