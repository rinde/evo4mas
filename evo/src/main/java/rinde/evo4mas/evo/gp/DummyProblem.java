package rinde.evo4mas.evo.gp;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPProblem;

public class DummyProblem extends GPProblem {
	private static final long serialVersionUID = -7808255017262044584L;

	public void evaluate(EvolutionState state, Individual ind, int subpopulation, int threadnum) {
		throw new IllegalStateException();
	}
}