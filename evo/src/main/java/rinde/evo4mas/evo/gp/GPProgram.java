/**
 * 
 */
package rinde.evo4mas.evo.gp;

import java.io.Serializable;

import ec.gp.GPNode;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class GPProgram<C> implements Serializable {

	private static final long serialVersionUID = 2873071674972923971L;
	protected final GPFunc<C> root;

	public GPProgram(GPFunc<C> rootNode) {
		root = rootNode;
	}

	public double execute(C context) {
		return executeNode(root, context);
	}

	protected double executeNode(GPFunc<C> current, C context) {
		final GPNode[] children = current.children;
		final double[] vals = new double[children == null ? 0 : children.length];
		for (int i = 0; i < vals.length; i++) {
			vals[i] = executeNode((GPFunc<C>) children[i], context);
		}
		return current.execute(vals, context);
	}

	@Override
	public String toString() {
		return root.makeLispTree();
	}

	@Override
	public GPProgram<C> clone() {
		return new GPProgram<C>((GPFunc<C>) root.clone());
	}
}
