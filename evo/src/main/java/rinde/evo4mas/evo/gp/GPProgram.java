/**
 * 
 */
package rinde.evo4mas.evo.gp;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class GPProgram<C> {

	protected final GPFunc<C> root;

	public GPProgram(GPFunc<C> rootNode) {
		root = rootNode;
	}

	public double execute(C context) {
		return executeNode(root, context);
	}

	protected double executeNode(GPFunc<C> current, C context) {
		final GPFunc<C>[] children = (GPFunc<C>[]) current.children;
		final double[] vals = new double[children.length];
		for (int i = 0; i < children.length; i++) {
			vals[i] = executeNode(children[i], context);
		}
		return current.execute(vals, context);
	}
}
