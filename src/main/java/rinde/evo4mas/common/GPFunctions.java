/**
 * 
 */
package rinde.evo4mas.common;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.Collection;

import rinde.ecj.GPFunc;
import rinde.sim.core.graph.Point;
import rinde.sim.problem.common.ParcelDTO;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class GPFunctions {

	public static class Ado<T extends TruckContext> extends GPFunc<T> {
		private static final long serialVersionUID = -4497905419697638750L;

		@Override
		public double execute(double[] input, T context) {
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

	public static class Mido<T extends TruckContext> extends GPFunc<T> {
		private static final long serialVersionUID = 2314969955830030083L;

		@Override
		public double execute(double[] input, T context) {
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

	public static class Mado<T extends TruckContext> extends GPFunc<T> {
		private static final long serialVersionUID = -3969582933786406570L;

		@Override
		public double execute(double[] input, T context) {
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

	public static class Dist<T extends TruckContext> extends GPFunc<T> {
		private static final long serialVersionUID = 2713253095353499761L;

		@Override
		public double execute(double[] input, T context) {
			if (context.isInCargo) {
				return Point.distance(context.truckPosition, context.parcel.destinationLocation);
			} else {
				return Point.distance(context.truckPosition, context.parcel.pickupLocation);
			}
		}
	}

	public static class Urge<T extends TruckContext> extends GPFunc<T> {
		private static final long serialVersionUID = -1608855921866707712L;

		@Override
		public double execute(double[] input, T context) {
			if (context.isInCargo) {
				return context.parcel.deliveryTimeWindow.end - context.time;
			} else {
				return context.parcel.pickupTimeWindow.end - context.time;
			}
		}
	}

	public static class Est<T extends TruckContext> extends GPFunc<T> {
		private static final long serialVersionUID = -3811389876518540528L;

		@Override
		public double execute(double[] input, T context) {
			if (context.isInCargo) {
				return context.parcel.deliveryTimeWindow.begin - context.time;
			} else {
				return context.parcel.pickupTimeWindow.begin - context.time;
			}
		}
	}

	// time left until truck is stopped
	public static class Ttl<T extends TruckContext> extends GPFunc<T> {
		private static final long serialVersionUID = -8186643123286080644L;

		@Override
		public double execute(double[] input, T context) {
			return context.vehicleDTO.availabilityTimeWindow.end - context.time;
		}
	}

}
