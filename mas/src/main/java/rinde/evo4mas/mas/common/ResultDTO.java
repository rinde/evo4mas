/**
 * 
 */
package rinde.evo4mas.mas.common;

import java.io.Serializable;

import rinde.evo4mas.evo.gp.GPComputationResult;
import rinde.evo4mas.evo.gp.GPProgram;
import rinde.evo4mas.evo.gp.GPProgramParser;
import rinde.sim.problem.common.StatsTracker.StatisticsDTO;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class ResultDTO implements GPComputationResult, Serializable {

	private static final long serialVersionUID = 2053089876856764188L;
	public final String scenarioKey;
	public final GPProgram<TruckContext> heuristic;
	public final StatisticsDTO stats;
	public final float fitness;

	public ResultDTO(String scenario, GPProgram<TruckContext> h, StatisticsDTO stat, float fit) {
		scenarioKey = scenario;
		heuristic = h;
		stats = stat;
		fitness = fit;
	}

	public float getFitness() {
		return fitness;
	}

	public String getGPId() {
		return GPProgramParser.toLisp(heuristic);
	}

	@Override
	public String toString() {
		return new StringBuilder(scenarioKey.toString()).append("\n").append(heuristic.toString()).append("\n")
				.append(stats.toString()).toString();
	}
}
