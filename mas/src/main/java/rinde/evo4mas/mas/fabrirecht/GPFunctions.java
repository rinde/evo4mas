/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;

import rinde.evo4mas.evo.gp.Constant;
import rinde.evo4mas.evo.gp.GPFunc;
import rinde.evo4mas.evo.gp.GPFuncSet;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.Parcel;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class GPFunctions extends GPFuncSet<FRContext> {

	@Override
	public Collection<GPFunc<FRContext>> create() {
		return newArrayList(new If4(), new Add(), new Constant<FRContext>(1), new Constant<FRContext>(0), new Ado());
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

	class Ado extends GPFunc<FRContext> {
		public Ado() {
			super(0);
		}

		@Override
		public double execute(double[] input, FRContext context) {

			final Collection<Parcel> contents = context.pdpModel.getContents(context.truck);
			double distance = 0d;
			for (final Parcel p : contents) {
				distance += Point.distance(context.parcel.getDestination(), p.getDestination());
			}
			return distance / contents.size();
		}

	}

}
