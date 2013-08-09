/**
 * 
 */
package rinde.solver.pdptw;

import java.util.Queue;

import rinde.sim.central.GlobalStateObject;
import rinde.sim.problem.common.DefaultParcel;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public interface Solver {

    Queue<DefaultParcel> solve(GlobalStateObject state);

}
