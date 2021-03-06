/**
 *
 */
package com.github.rinde.evo4mas.common;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.Collection;

import com.github.rinde.ecj.GPFunc;
import com.github.rinde.rinsim.core.model.pdp.ParcelDTO;
import com.github.rinde.rinsim.geom.Point;

/**
 * @author Rinde van Lon
 *
 */
public final class GPFunctions {

  private GPFunctions() {}

  /**
   * Average distance of current parcel pickup/delivery point to destinations of
   * all parcels the vehicle is carrying. In case the parcel is available this
   * function behaves as Ado: 'specifies the average distance of the delivery
   * point of a waiting order to the delivery points of all other orders that
   * are currently carried by the courier.' In case the parcel is in cargo the
   * function behaves as Add: 'specifies the average distance from the service
   * point to other service points which have yet to be visited, based on the
   * currently carrying orders.' Both Ado and Add are originally defined in the
   * paper by Beham et al.
   * @param <T> The type of context that is used.
   */
  public static class Ado<T extends TruckContext> extends GPFunc<T> {
    private static final long serialVersionUID = -4497905419697638750L;

    public Ado() {}

    @Override
    public double execute(double[] input, T context) {
      final Point poi = context.isInCargo ? context.parcel.getDeliveryLocation()
        : context.parcel.getPickupLocation();
      final Collection<ParcelDTO> contents = context.truckContents;
      if (contents.isEmpty()) {
        return 0d;
      }
      double distance = 0d;
      for (final ParcelDTO p : contents) {
        distance += Point.distance(poi, p.getDeliveryLocation());
      }
      return distance / contents.size();
    }
  }

  /**
   * Same as {@link Ado} but using the minimum distance.
   * @param <T> The type of context that is used.
   */
  public static class Mido<T extends TruckContext> extends GPFunc<T> {
    private static final long serialVersionUID = 2314969955830030083L;

    public Mido() {}

    @Override
    public double execute(double[] input, T context) {
      final Point poi = context.isInCargo ? context.parcel.getDeliveryLocation()
        : context.parcel.getPickupLocation();
      final Collection<ParcelDTO> contents = context.truckContents;
      if (contents.isEmpty()) {
        return 0d;
      }
      double minDistance = Double.POSITIVE_INFINITY;
      for (final ParcelDTO p : contents) {
        minDistance = min(minDistance,
          Point.distance(poi, p.getDeliveryLocation()));
      }
      return minDistance;
    }
  }

  /**
   * Same as {@link Ado} but using the maximum distance.
   * @param <T> The type of context that is used.
   */
  public static class Mado<T extends TruckContext> extends GPFunc<T> {
    private static final long serialVersionUID = -3969582933786406570L;

    public Mado() {}

    @Override
    public double execute(double[] input, T context) {
      final Point poi = context.isInCargo ? context.parcel.getDeliveryLocation()
        : context.parcel.getPickupLocation();
      final Collection<ParcelDTO> contents = context.truckContents;
      if (contents.isEmpty()) {
        return 0d;
      }
      double maxDistance = Double.NEGATIVE_INFINITY;
      for (final ParcelDTO p : contents) {
        maxDistance = max(maxDistance,
          Point.distance(poi, p.getDeliveryLocation()));
      }
      return maxDistance;
    }
  }

  /**
   * Returns the distance of the vehicle to the current parcel (either
   * destination or pickup location depending on whether it is in cargo or not).
   * @param <T> The type of context that is used.
   */
  public static class Dist<T extends TruckContext> extends GPFunc<T> {
    private static final long serialVersionUID = 2713253095353499761L;

    public Dist() {}

    @Override
    public double execute(double[] input, T context) {
      if (context.isInCargo) {
        return Point.distance(context.truckPosition,
          context.parcel.getDeliveryLocation());
      } else {
        return Point.distance(context.truckPosition,
          context.parcel.getPickupLocation());
      }
    }
  }

  /**
   * As defined by Beham et al. 'specifies the urgency of a waiting order, which
   * is defined as the order's due date minus the current date.' In case the
   * current parcel is in cargo we use the end time of the delivery instead of
   * the end time of the pickup.
   * @param <T> The type of context that is used.
   */
  public static class Urge<T extends TruckContext> extends GPFunc<T> {
    private static final long serialVersionUID = -1608855921866707712L;

    public Urge() {}

    @Override
    public double execute(double[] input, T context) {
      if (context.isInCargo) {
        return context.parcel.getDeliveryTimeWindow().end() - context.time;
      } else {
        return context.parcel.getPickupTimeWindow().end() - context.time;
      }
    }
  }

  // TODO too similar to timeUntilAvailable?
  /**
   * As defined by Beham et al. 'is the earliest start time in the form of the
   * difference to the current time that a new order is available at the service
   * point'.
   * @author Rinde van Lon
   *
   * @param <T> The type of context that is used.
   */
  public static class Est<T extends TruckContext> extends GPFunc<T> {
    private static final long serialVersionUID = -3811389876518540528L;

    public Est() {}

    @Override
    public double execute(double[] input, T context) {
      if (context.isInCargo) {
        return context.parcel.getDeliveryTimeWindow().begin() - context.time;
      } else {
        return context.parcel.getPickupTimeWindow().begin() - context.time;
      }
    }
  }

  /**
   * Time to live, indicates the time that is left until the end of the work
   * day.
   * @param <T> The type of context that is used.
   */
  public static class Ttl<T extends TruckContext> extends GPFunc<T> {
    private static final long serialVersionUID = -8186643123286080644L;

    public Ttl() {}

    @Override
    public double execute(double[] input, T context) {
      return context.vehicleDTO.getAvailabilityTimeWindow().end()
        - context.time;
    }
  }

}
