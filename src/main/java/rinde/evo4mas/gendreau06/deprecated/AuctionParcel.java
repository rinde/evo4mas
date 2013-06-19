/**
 * 
 */
package rinde.evo4mas.gendreau06.deprecated;

import static com.google.common.base.Preconditions.checkState;

import java.util.Iterator;
import java.util.Set;

import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.event.Event;
import rinde.sim.event.EventAPI;
import rinde.sim.event.EventDispatcher;
import rinde.sim.problem.common.DefaultParcel;
import rinde.sim.problem.common.ParcelDTO;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class AuctionParcel extends DefaultParcel {

	protected Bidder assignedTruck;
	protected EventDispatcher eventDispatcher;

	enum AuctionParcelEvent {
		AUCTION_FINISHED;
	}

	public AuctionParcel(ParcelDTO pDto) {
		super(pDto);
		eventDispatcher = new EventDispatcher(AuctionParcelEvent.AUCTION_FINISHED);
	}

	@Override
	public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {
		final Set<Bidder> trucks = pRoadModel.getObjectsOfType(Bidder.class);
		checkState(!trucks.isEmpty(), "there are no vehicles..");
		final Iterator<Bidder> it = trucks.iterator();
		Bidder bestTruck = it.next();
		// if there are no other trucks, there is no need to use the heuristic
		// at all (mainly used in test cases)
		if (it.hasNext()) {
			double bestValue = bestTruck.getBidFor(this, dto.orderArrivalTime);

			while (it.hasNext()) {
				final Bidder cur = it.next();
				final double curValue = cur.getBidFor(this, dto.orderArrivalTime);
				if (curValue < bestValue) {
					bestValue = curValue;
					bestTruck = cur;
				}
			}
		}
		bestTruck.receiveParcel(this);
		assignedTruck = bestTruck;
		eventDispatcher.dispatchEvent(new Event(AuctionParcelEvent.AUCTION_FINISHED, this));
	}

	public EventAPI getEventAPI() {
		return eventDispatcher.getPublicEventAPI();
	}

	public Bidder getAssignedTruck() {
		return assignedTruck;
	}
}
