/**
 * 
 */
package rinde.evo4mas.evo.gp;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class Constant<C> extends GPFunc<C> {

	private final double value;

	public Constant(double val) {
		super(0);
		value = val;
	}

	@Override
	public double execute(double[] input, C context) {
		return value;
	}

	@Override
	public GPFunc<C> create() {
		return new Constant<C>(value);
	}

}
