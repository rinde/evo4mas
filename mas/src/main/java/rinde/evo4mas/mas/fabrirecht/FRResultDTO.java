/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import rinde.cloud.javainterface.ComputationJob;
import rinde.evo4mas.evo.gp.GPComputationResult;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class FRResultDTO implements GPComputationResult {

	protected final ComputationJob compJob;
	protected final float fitness;

	public FRResultDTO(ComputationJob cj, float fit) {
		compJob = cj;
		fitness = fit;
	}

	public ComputationJob getComputationJob() {
		return compJob;
	}

	public float getFitness() {
		return fitness;
	}
}
