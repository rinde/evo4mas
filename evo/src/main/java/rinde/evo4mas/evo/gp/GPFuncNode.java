/**
 * 
 */
package rinde.evo4mas.evo.gp;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Serializable;

/**
 * Immutable function node, is used during execution.
 * 
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public class GPFuncNode<C> implements Serializable {
	private static final long serialVersionUID = 4880258638384679392L;
	private final GPFunc<C> func;
	private final GPFuncNode<C>[] children;

	public GPFuncNode(GPFunc<C> function) {
		this(function, null);
	}

	public GPFuncNode(GPFunc<C> function, GPFuncNode<C>[] c) {
		checkArgument((function.getNumChildren() == 0 && c == null) || function.getNumChildren() == c.length, "GPFuncNode must always be initialized with the number of children that it requires.");
		func = function;
		children = c;
	}

	public int getNumChildren() {
		return children == null ? 0 : children.length;
	}

	public GPFuncNode<C> getChild(int index) {
		return children[index];
	}

	public GPFunc<C> getFunction() {
		return func;
	}

	@Override
	public String toString() {
		return func.name();
	}
}
