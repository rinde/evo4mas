/**
 * 
 */
package rinde.evo4mas.evo.gp;

import rinde.cloud.javainterface.ComputationJob;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class GPComputationResultImpl // implements GPComputationResult {
{
	private static final long serialVersionUID = 2596795183812240883L;
	protected final ComputationJob compJob;
	protected final float fitness;

	public GPComputationResultImpl(ComputationJob job, float f) {
		compJob = job;
		fitness = f;
	}

	public ComputationJob getComputationJob() {
		return compJob;
	}

	public float getFitness() {
		return fitness;
	}

}
