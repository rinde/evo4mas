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
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public abstract class GPFunc<C> extends GPNode {

	private static final long serialVersionUID = -861693143274097130L;
	private final int numChildren;
	private final String name;

	public GPFunc() {
		this(0);
	}

	public GPFunc(int children) {
		this(null, children);
	}

	public GPFunc(String name) {
		this(name, 0);
	}

	public GPFunc(String name, int children) {
		numChildren = children;
		if (name == null) {
			this.name = getClass().getSimpleName().toLowerCase();
		} else {
			this.name = name;
		}
	}

	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		// deliberatly not calling super

		// hack to add constraints (without needing the params file)

		constraints = GPNodeConstraints.constraintsFor("nc" + numChildren, state).constraintNumber;

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

	public int getNumChildren() {
		return numChildren;
	}

	@Override
	public String toString() {
		return name;
	}

	public GPFunc<C> create() {
		try {
			return this.getClass().getConstructor().newInstance();
		} catch (final Exception e) {
			throw new RuntimeException(
					"In order for this to work each GPFunc instance must have a publicly accessible zero-arg constructor. Typically the instances are inner public static classes.",
					e);
		}
	}

	@Override
	public void checkConstraints(final EvolutionState state, final int tree, final GPIndividual typicalIndividual,
			final Parameter individualBase) {
		super.checkConstraints(state, tree, typicalIndividual, individualBase);
		if (children.length != numChildren) {
			state.output.error("Incorrect number of children for node " + toStringForError() + " at " + individualBase);
		}
	}

	public abstract double execute(double[] input, C context);

	@Override
	public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual,
			Problem problem) {}
}
