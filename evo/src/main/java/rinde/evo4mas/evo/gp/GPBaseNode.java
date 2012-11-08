/**
 * 
 */
package rinde.evo4mas.evo.gp;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeConstraints;
import ec.util.Parameter;

/**
 * This class is used during evolution.
 * 
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class GPBaseNode<C> extends GPNode {

	private final GPFunc<C> func;

	public GPBaseNode(GPFunc<C> function) {
		func = function;
	}

	public GPFunc<C> getFunc() {
		return func;
	}

	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		// deliberatly not calling super

		// hack to add constraints (without needing the params file)

		constraints = GPNodeConstraints.constraintsFor("nc" + func.getNumChildren(), state).constraintNumber;

		// The number of children is determined by the constraints. Though
		// for some special versions of GPNode, we may have to enforce certain
		// rules, checked in children versions of setup(...)

		final GPNodeConstraints constraintsObj = constraints(((GPInitializer) state.initializer));
		final int len = constraintsObj.childtypes.length;
		if (len == 0) {
			children = constraintsObj.zeroChildren;
		} else {
			children = new GPNode[len];
		}
	}

	@Override
	public void checkConstraints(final EvolutionState state, final int tree, final GPIndividual typicalIndividual,
			final Parameter individualBase) {
		super.checkConstraints(state, tree, typicalIndividual, individualBase);
		if (children.length != func.getNumChildren()) {
			state.output.error("Incorrect number of children for node " + toStringForError() + " at " + individualBase);
		}
	}

	@Override
	public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual,
			Problem problem) {}

	@Override
	public String toString() {
		return func.name();
	}

	public GPBaseNode<C> create() {
		return new GPBaseNode<C>(func);
	}

	public int getNumChildren() {
		return children.length;
	}

}
