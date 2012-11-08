/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import java.io.Serializable;

import rinde.evo4mas.evo.gp.GPComputationResult;
import rinde.evo4mas.evo.gp.GPProgram;
import rinde.evo4mas.evo.gp.GPProgramParser;
import rinde.sim.problem.common.StatsTracker.StatisticsDTO;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class FRResultDTO implements GPComputationResult, Serializable {

	private static final long serialVersionUID = 2053089876856764188L;
	protected final String scenarioKey;
	protected final GPProgram<FRContext> heuristic;
	protected final StatisticsDTO stats;
	protected final float fitness;

	public FRResultDTO(String scenario, GPProgram<FRContext> h, StatisticsDTO stat, float fit) {
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
