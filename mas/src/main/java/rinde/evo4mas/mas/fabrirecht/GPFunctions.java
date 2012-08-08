/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import rinde.evo4mas.evo.gp.GPFunc;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class GPFunctions {

	class If4 extends GPFunc<FRContext> {

		public If4() {
			super(4);
		}

		public double execute(double[] input, FRContext context) {
			return input[0] < input[1] ? input[2] : input[3];
		}

	}

}
