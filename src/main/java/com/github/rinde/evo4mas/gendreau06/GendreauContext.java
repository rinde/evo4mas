/**
 * 
 */
package com.github.rinde.evo4mas.gendreau06;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.github.rinde.evo4mas.common.TruckContext;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.pdptw.ParcelDTO;
import com.github.rinde.rinsim.core.pdptw.VehicleDTO;
import com.github.rinde.rinsim.geom.Point;

/**
 * Context object used in GP.
 * 
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class GendreauContext extends TruckContext {

	/**
	 * Indicates whether the current parcel is already assigned to this vehicle.
	 * If <code>true</code>, it means that the parcel is either in the todo list
	 * or in the cargo.
	 */
	public final boolean isAssignedToVehicle;
	public final int numWaiters;
	public final List<Point> otherVehiclePositions;
	public final Set<Parcel> todoList;

	/**
	 * @param v Vehicle info
	 * @param tp Current position of vehicle
	 * @param tc Contents of vehicle
	 * @param p Info of current parcel
	 * @param tm Current time
	 * @param c Is in cargo
	 * @param a {@link #isAssignedToVehicle}
	 * @param w Number of waiters
	 * @param ovp Other vehicle positions
	 */
	public GendreauContext(VehicleDTO v, Point tp, Collection<ParcelDTO> tc, ParcelDTO p, long tm, boolean c,
			boolean a, int w, List<Point> ovp, Set<Parcel> tl) {
		super(v, tp, tc, p, tm, c);
		isAssignedToVehicle = a;
		numWaiters = w;
		otherVehiclePositions = unmodifiableList(ovp);
		todoList = unmodifiableSet(tl);
	}

}
