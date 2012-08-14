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
		private static final long serialVersionUID = -8010536154981009677L;

		public If4() {
			super(4);
		}

		@Override
		public double execute(double[] input, T context) {
			return input[0] < input[1] ? input[2] : input[3];
		}

	}

	public static class Add<T> extends GPFunc<T> {
		private static final long serialVersionUID = 2200299240321191164L;

		public Add() {
			super(2);
		}

		@Override
		public double execute(double[] input, T context) {
			return input[0] + input[1];
		}
	}

	public static class Sub<T> extends GPFunc<T> {
		private static final long serialVersionUID = -1363621468791103104L;

		public Sub() {
			super(2);
		}

		@Override
		public double execute(double[] input, T context) {
			return input[0] - input[1];
		}
	}

	public static class Mul<T> extends GPFunc<T> {
		private static final long serialVersionUID = 537369514239069421L;

		public Mul() {
			super(2);
		}

		@Override
		public double execute(double[] input, T context) {
			return input[0] * input[1];
		}
	}

	public static class Div<T> extends GPFunc<T> {
		private static final long serialVersionUID = 6727402143693804260L;

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
