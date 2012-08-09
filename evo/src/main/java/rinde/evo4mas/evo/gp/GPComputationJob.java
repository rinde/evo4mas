/**
 * 
 */
package rinde.evo4mas.evo.gp;

import rinde.cloud.javainterface.ComputationJob;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public interface GPComputationJob<C> extends ComputationJob {

	GPProgram<C> getProgram();

}
