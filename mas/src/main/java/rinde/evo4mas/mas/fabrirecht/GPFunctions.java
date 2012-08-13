/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;

import rinde.evo4mas.evo.gp.Constant;
import rinde.evo4mas.evo.gp.GPFunc;
import rinde.evo4mas.evo.gp.GPFuncSet;
import rinde.evo4mas.evo.gp.GenericFunctions.Add;
import rinde.evo4mas.evo.gp.GenericFunctions.Div;
import rinde.evo4mas.evo.gp.GenericFunctions.If4;
import rinde.evo4mas.evo.gp.GenericFunctions.Mul;
import rinde.evo4mas.evo.gp.GenericFunctions.Sub;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.Parcel;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class GPFunctions extends GPFuncSet<FRContext> {

	@SuppressWarnings("unchecked")
	@Override
	public Collection<GPFunc<FRContext>> create() {
		return newArrayList(
		/* GENERIC FUNCTIONS */
		new If4<FRContext>(), /* */
				new Add<FRContext>(), /* */
				new Sub<FRContext>(), /* */
				new Div<FRContext>(), /* */
				new Mul<FRContext>(), /* */
				/* CONSTANTS */
				new Constant<FRContext>(1), /* */
				new Constant<FRContext>(0), /* */
				/* DOMAIN SPECIFIC FUNCTIONS */
				new Ado(), /* */
				new Dist());
	}

	public static class Ado extends GPFunc<FRContext> {
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

	public static class Dist extends GPFunc<FRContext> {
		@Override
		public double execute(double[] input, FRContext context) {
			if (context.isInCargo) {
				return Point.distance(context.roadModel.getPosition(context.truck), context.parcel.getDestination());
			} else {
				return Point.distance(context.roadModel.getPosition(context.truck), context.roadModel
						.getPosition(context.parcel));
			}
		}
	}

}
