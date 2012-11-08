/**
 * 
 */
package rinde.evo4mas.evo.gp;

import java.io.Serializable;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public abstract class GPFunc<C> implements Serializable {
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

	public int getNumChildren() {
		return numChildren;
	}

	@Override
	public String toString() {
		return name;
	}

	public String name() {
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

	public abstract double execute(double[] input, C context);
}
