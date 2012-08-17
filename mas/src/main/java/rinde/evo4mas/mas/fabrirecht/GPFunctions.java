/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.Collection;

import rinde.evo4mas.evo.gp.GPFunc;
import rinde.evo4mas.evo.gp.GPFuncSet;
import rinde.evo4mas.evo.gp.GenericFunctions.Add;
import rinde.evo4mas.evo.gp.GenericFunctions.Constant;
import rinde.evo4mas.evo.gp.GenericFunctions.Div;
import rinde.evo4mas.evo.gp.GenericFunctions.If4;
import rinde.evo4mas.evo.gp.GenericFunctions.Mul;
import rinde.evo4mas.evo.gp.GenericFunctions.Pow;
import rinde.evo4mas.evo.gp.GenericFunctions.Sub;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.Parcel;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class GPFunctions extends GPFuncSet<FRContext> {

	private static final long serialVersionUID = -1347739703291676886L;

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
				new Pow<FRContext>(),
				/* CONSTANTS */
				new Constant<FRContext>(1), /* */
				new Constant<FRContext>(0), /* */
				/* DOMAIN SPECIFIC FUNCTIONS */
				new Ado(), /* */
				new Mido(), /* */
				new Mado(), /* */
				new Dist(), /* */
				new Urge(), /* */
				new Est(), /* */
				new Ttl() /* */

		);
	}

	public static class Ado extends GPFunc<FRContext> {
		private static final long serialVersionUID = -4497905419697638750L;

		@Override
		public double execute(double[] input, FRContext context) {
			final Collection<Parcel> contents = context.pdpModel.getContents(context.truck);
			if (contents.isEmpty()) {
				return 0d;
			}
			double distance = 0d;
			for (final Parcel p : contents) {
				distance += Point.distance(context.parcel.destinationLocation, p.getDestination());
			}
			return distance / contents.size();
		}
	}

	public static class Mido extends GPFunc<FRContext> {
		private static final long serialVersionUID = 2314969955830030083L;

		@Override
		public double execute(double[] input, FRContext context) {
			final Collection<Parcel> contents = context.pdpModel.getContents(context.truck);
			if (contents.isEmpty()) {
				return 0d;
			}
			double minDistance = Double.POSITIVE_INFINITY;
			for (final Parcel p : contents) {
				minDistance = min(minDistance, Point.distance(context.parcel.destinationLocation, p.getDestination()));
			}
			return minDistance;
		}
	}

	public static class Mado extends GPFunc<FRContext> {
		private static final long serialVersionUID = -3969582933786406570L;

		@Override
		public double execute(double[] input, FRContext context) {
			final Collection<Parcel> contents = context.pdpModel.getContents(context.truck);
			if (contents.isEmpty()) {
				return 0d;
			}
			double maxDistance = Double.NEGATIVE_INFINITY;
			for (final Parcel p : contents) {
				maxDistance = max(maxDistance, Point.distance(context.parcel.destinationLocation, p.getDestination()));
			}
			return maxDistance;
		}
	}

	public static class Dist extends GPFunc<FRContext> {
		private static final long serialVersionUID = 2713253095353499761L;

		@Override
		public double execute(double[] input, FRContext context) {
			if (context.isInCargo) {
				return Point.distance(context.roadModel.getPosition(context.truck), context.parcel.destinationLocation);
			} else {
				return Point.distance(context.roadModel.getPosition(context.truck), context.parcel.pickupLocation);
			}
		}
	}

	public static class Urge extends GPFunc<FRContext> {
		private static final long serialVersionUID = -1608855921866707712L;

		@Override
		public double execute(double[] input, FRContext context) {
			if (context.isInCargo) {
				return context.parcel.deliveryTimeWindow.end - context.time;
			} else {
				return context.parcel.pickupTimeWindow.end - context.time;
			}
		}
	}

	public static class Est extends GPFunc<FRContext> {
		private static final long serialVersionUID = -3811389876518540528L;

		@Override
		public double execute(double[] input, FRContext context) {
			if (context.isInCargo) {
				return context.parcel.deliveryTimeWindow.begin - context.time;
			} else {
				return context.parcel.pickupTimeWindow.begin - context.time;
			}
		}
	}

	// time left until truck is stopped
	public static class Ttl extends GPFunc<FRContext> {
		private static final long serialVersionUID = -8186643123286080644L;

		@Override
		public double execute(double[] input, FRContext context) {
			return context.truck.getDTO().availabilityTimeWindow.end - context.time;
		}
	}

}
