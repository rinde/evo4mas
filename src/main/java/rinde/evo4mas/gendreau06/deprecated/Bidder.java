/**
 * 
 */
package rinde.evo4mas.gendreau06.deprecated;

import rinde.sim.core.model.road.RoadUser;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public interface Bidder extends RoadUser {

	double getBidFor(AuctionParcel ap, long time);

	void receiveParcel(AuctionParcel ap);

}
