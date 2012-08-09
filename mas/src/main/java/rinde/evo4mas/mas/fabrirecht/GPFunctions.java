/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;

import rinde.evo4mas.evo.gp.Constant;
import rinde.evo4mas.evo.gp.GPFunc;
import rinde.evo4mas.evo.gp.GPFuncSet;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class GPFunctions extends GPFuncSet<FRContext> {

	@Override
	public Collection<GPFunc<FRContext>> create() {
		return newArrayList(new If4(), new Add(), new Constant<FRContext>(1), new Constant<FRContext>(0));
	}

	class If4 extends GPFunc<FRContext> {

		public If4() {
			super(4);
		}

		@Override
		public double execute(double[] input, FRContext context) {
			return input[0] < input[1] ? input[2] : input[3];
		}

	}

	class Add extends GPFunc<FRContext> {
		public Add() {
			super(2);
		}

		@Override
		public double execute(double[] input, FRContext context) {
			return input[0] + input[1];
		}

	}

}
