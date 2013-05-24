/**
 * 
 */
package rinde.evo4mas.gendreau06;

import java.util.Collection;

import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.problem.common.VehicleDTO;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class AuctionOptTruck extends AuctionTruck {

	/**
	 * @param pDto
	 * @param p
	 */
	public AuctionOptTruck(VehicleDTO pDto) {
		super(pDto, null);
	}

	@Override
	protected Parcel next(long time) {
		final Collection<Parcel> contents = pdpModel.getContents(this);
		final int numLocations = 1 + (todo.size() * 2) + contents.size();

		if (numLocations == 1) {
			// there are no orders
			return null;
		} else if (todo.size() + contents.size() == 1) {
			// if there is only one order, the solution is trivial
			if (!todo.isEmpty()) {
				return todo.iterator().next();
			} else {
				return contents.iterator().next();
			}
		}
		// else, we are going to look for the optimal solution

		final int[][] travelTime = new int[numLocations][numLocations];
		// gather all locations

		return null;

	}
}
