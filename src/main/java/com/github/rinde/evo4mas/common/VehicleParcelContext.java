package com.github.rinde.evo4mas.common;

import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.geom.Point;
import com.google.auto.value.AutoValue;

/**
 * Context object that can be used to compute priority of a parcel based on a
 * vehicle and its current position.
 * @author Rinde van Lon
 */
@AutoValue
public abstract class VehicleParcelContext {

  /**
   * @return The time at which the priority for {@link #parcel()} should be
   *         computed.
   */
  public abstract long time();

  /**
   * @return The current position of the vehicle.
   */
  public abstract Point vehiclePosition();

  /**
   * @return Object containing general information of the vehicle.
   */
  public abstract VehicleDTO vehicle();

  /**
   * @return The parcel for which the priority should be computed.
   */
  public abstract Parcel parcel();

  /**
   * @return Boolean indicating whether the priority of a pickup or delivery
   *         operation for {@link #parcel()} needs to be computed.
   */
  public abstract boolean isPickup();

  static VehicleParcelContext create(long time, Point vehiclePosition,
      VehicleDTO vehicle, Parcel p, boolean pickup) {
    return new AutoValue_VehicleParcelContext(time, vehiclePosition, vehicle, p,
        pickup);
  }

}
