/**
 * 
 */
package rinde.evo4mas.evo.gp;


/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class GenericFunctions {

	public static class If4<T> extends GPFunc<T> {
		public If4() {
			super(4);
		}

		@Override
		public double execute(double[] input, T context) {
			return input[0] < input[1] ? input[2] : input[3];
		}

	}

	public static class Add<T> extends GPFunc<T> {
		public Add() {
			super(2);
		}

		@Override
		public double execute(double[] input, T context) {
			return input[0] + input[1];
		}
	}

	public static class Sub<T> extends GPFunc<T> {
		public Sub() {
			super(2);
		}

		@Override
		public double execute(double[] input, T context) {
			return input[0] - input[1];
		}
	}

	public static class Mul<T> extends GPFunc<T> {
		public Mul() {
			super(2);
		}

		@Override
		public double execute(double[] input, T context) {
			// return input[0] * input[1];
			return 0d;
		}
	}

	public static class Div<T> extends GPFunc<T> {
		public Div() {
			super(2);
		}

		// protected division
		@Override
		public double execute(double[] input, T context) {
			if (input[1] == 0d) {
				return 1d;
			}
			return input[0] / input[1];
		}
	}

}
