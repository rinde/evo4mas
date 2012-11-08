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
import rinde.evo4mas.mas.common.TruckContext;
import rinde.sim.core.graph.Point;
import rinde.sim.problem.common.ParcelDTO;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class GPFunctions extends GPFuncSet<TruckContext> {

	private static final long serialVersionUID = -1347739703291676886L;

	@SuppressWarnings("unchecked")
	@Override
	public Collection<GPFunc<TruckContext>> create() {
		return newArrayList(
		/* GENERIC FUNCTIONS */
		new If4<TruckContext>(), /* */
				new Add<TruckContext>(), /* */
				new Sub<TruckContext>(), /* */
				new Div<TruckContext>(), /* */
				new Mul<TruckContext>(), /* */
				new Pow<TruckContext>(),
				/* CONSTANTS */
				new Constant<TruckContext>(1), /* */
				new Constant<TruckContext>(0), /* */
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

	public static class Ado extends GPFunc<TruckContext> {
		private static final long serialVersionUID = -4497905419697638750L;

		@Override
		public double execute(double[] input, TruckContext context) {
			final Collection<ParcelDTO> contents = context.truckContents;
			if (contents.isEmpty()) {
				return 0d;
			}
			double distance = 0d;
			for (final ParcelDTO p : contents) {
				distance += Point.distance(context.parcel.destinationLocation, p.destinationLocation);
			}
			return distance / contents.size();
		}
	}

	public static class Mido extends GPFunc<TruckContext> {
		private static final long serialVersionUID = 2314969955830030083L;

		@Override
		public double execute(double[] input, TruckContext context) {
			final Collection<ParcelDTO> contents = context.truckContents;
			if (contents.isEmpty()) {
				return 0d;
			}
			double minDistance = Double.POSITIVE_INFINITY;
			for (final ParcelDTO p : contents) {
				minDistance = min(minDistance, Point.distance(context.parcel.destinationLocation, p.destinationLocation));
			}
			return minDistance;
		}
	}

	public static class Mado extends GPFunc<TruckContext> {
		private static final long serialVersionUID = -3969582933786406570L;

		@Override
		public double execute(double[] input, TruckContext context) {
			final Collection<ParcelDTO> contents = context.truckContents;
			if (contents.isEmpty()) {
				return 0d;
			}
			double maxDistance = Double.NEGATIVE_INFINITY;
			for (final ParcelDTO p : contents) {
				maxDistance = max(maxDistance, Point.distance(context.parcel.destinationLocation, p.destinationLocation));
			}
			return maxDistance;
		}
	}

	public static class Dist extends GPFunc<TruckContext> {
		private static final long serialVersionUID = 2713253095353499761L;

		@Override
		public double execute(double[] input, TruckContext context) {
			if (context.isInCargo) {
				return Point.distance(context.truckPosition, context.parcel.destinationLocation);
			} else {
				return Point.distance(context.truckPosition, context.parcel.pickupLocation);
			}
		}
	}

	public static class Urge extends GPFunc<TruckContext> {
		private static final long serialVersionUID = -1608855921866707712L;

		@Override
		public double execute(double[] input, TruckContext context) {
			if (context.isInCargo) {
				return context.parcel.deliveryTimeWindow.end - context.time;
			} else {
				return context.parcel.pickupTimeWindow.end - context.time;
			}
		}
	}

	public static class Est extends GPFunc<TruckContext> {
		private static final long serialVersionUID = -3811389876518540528L;

		@Override
		public double execute(double[] input, TruckContext context) {
			if (context.isInCargo) {
				return context.parcel.deliveryTimeWindow.begin - context.time;
			} else {
				return context.parcel.pickupTimeWindow.begin - context.time;
			}
		}
	}

	// time left until truck is stopped
	public static class Ttl extends GPFunc<TruckContext> {
		private static final long serialVersionUID = -8186643123286080644L;

		@Override
		public double execute(double[] input, TruckContext context) {
			return context.vehicleDTO.availabilityTimeWindow.end - context.time;
		}
	}

}
