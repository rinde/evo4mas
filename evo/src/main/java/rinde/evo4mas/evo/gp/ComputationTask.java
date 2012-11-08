/**
 * 
 */
package rinde.evo4mas.evo.gp;

import org.jppf.server.protocol.JPPFTask;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public abstract class ComputationTask<T> extends JPPFTask {

	public abstract T getComputationResult();

	public abstract String getGPId();

}
