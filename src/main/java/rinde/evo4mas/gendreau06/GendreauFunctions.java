/**
 * 
 */
package rinde.evo4mas.gendreau06;

import rinde.ecj.GPFunc;
import rinde.sim.core.graph.Point;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class GendreauFunctions {

    /**
     * Returns number of parcels in cargo of vehicle.
     * @param <C> The type of context which this {@link GPFunc} expects.
     */
    public static class CargoSize<C extends GendreauContext> extends GPFunc<C> {
        private static final long serialVersionUID = -3041300164485908524L;

        @Override
        public double execute(double[] input, GendreauContext context) {
            return context.truckContents.size();
        }
    }

    /**
     * Returns <code>1</code> if current parcel is in cargo <code>0</code>
     * otherwise.
     * @param <C> The type of context which this {@link GPFunc} expects.
     */
    public static class IsInCargo<C extends GendreauContext> extends GPFunc<C> {
        private static final long serialVersionUID = -3041300164485908524L;

        @Override
        public double execute(double[] input, C context) {
            return context.isInCargo ? 1d : 0d;
        }
    }

    /**
     * This function returns the time that is left until the current parcel is
     * available or <code>0</code> if it is available. In case the parcel has to
     * be picked up availability is indicated by its pickup time window, if it
     * is already in cargo availability is indicated by its delivery time
     * window. The time is corrected for the travel time of the vehicle to the
     * parcel.
     * @param <C> The type of context which this {@link GPFunc} expects.
     */
    public static class TimeUntilAvailable<C extends GendreauContext> extends
            GPFunc<C> {
        private static final long serialVersionUID = -3527221929651639824L;

        @Override
        public double execute(double[] input, C context) {
            final boolean isPickup = !context.isInCargo;

            final Point loc =
                    isPickup ? context.parcel.pickupLocation
                            : context.parcel.destinationLocation;
            final long travelTime =
                    (long) ((Point.distance(loc, context.truckPosition) / 30d) * 3600000d);
            final long timeToBegin =
                    (isPickup ? context.parcel.pickupTimeWindow.begin
                            : context.parcel.deliveryTimeWindow.begin)
                            - context.time - travelTime;

            return Math.min(0, timeToBegin);
        }
    }

    /**
     * Average distance of current parcel to all vehicles.
     * 
     * From Beham: 'specifies the average distance of the service point to the
     * other couriers'.
     * @param <C> The type of context which this {@link GPFunc} expects.
     */
    public static class Adc<C extends GendreauContext> extends GPFunc<C> {
        private static final long serialVersionUID = -3351761832322115361L;

        @Override
        public double execute(double[] input, C context) {
            final Point poi =
                    context.isInCargo ? context.parcel.destinationLocation
                            : context.parcel.pickupLocation;
            double dist = 0;
            for (final Point p : context.otherVehiclePositions) {
                dist += Point.distance(poi, p);
            }
            dist /= context.otherVehiclePositions.size();
            return dist;
        }
    }

    /**
     * Distance of current parcel to closest vehicle.
     * @param <C> The type of context which this {@link GPFunc} expects.
     */
    public static class Midc<C extends GendreauContext> extends GPFunc<C> {
        private static final long serialVersionUID = 4350517514894413992L;

        @Override
        public double execute(double[] input, C context) {
            final Point poi =
                    context.isInCargo ? context.parcel.destinationLocation
                            : context.parcel.pickupLocation;
            double min = Double.POSITIVE_INFINITY;
            for (final Point p : context.otherVehiclePositions) {
                final double dist = Point.distance(poi, p);
                if (dist < min) {
                    min = dist;
                }
            }
            return min;
        }
    }

    /**
     * Distance of current parcel to vehicle farthest away.
     * @param <C> The type of context which this {@link GPFunc} expects.
     */
    public static class Madc<C extends GendreauContext> extends GPFunc<C> {
        private static final long serialVersionUID = -7052728913139911309L;

        @Override
        public double execute(double[] input, C context) {
            final Point poi =
                    context.isInCargo ? context.parcel.destinationLocation
                            : context.parcel.pickupLocation;
            double max = Double.NEGATIVE_INFINITY;
            for (final Point p : context.otherVehiclePositions) {
                final double dist = Point.distance(poi, p);
                if (dist > max) {
                    max = dist;
                }
            }
            return max;
        }
    }

}
