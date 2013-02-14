/**
 * 
 */
package rinde.evo4mas.gendreau06;

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

	protected AuctionTruck assignedTruck;
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
		final Set<AuctionTruck> trucks = pRoadModel.getObjectsOfType(AuctionTruck.class);

		final Iterator<AuctionTruck> it = trucks.iterator();
		AuctionTruck bestTruck = it.next();
		double bestValue = bestTruck.getBidFor(this, dto.orderArrivalTime);

		while (it.hasNext()) {
			final AuctionTruck cur = it.next();
			final double curValue = cur.getBidFor(this, dto.orderArrivalTime);
			if (curValue < bestValue) {
				bestValue = curValue;
				bestTruck = cur;
			}
		}
		bestTruck.receiveParcel(this);
		assignedTruck = bestTruck;
		eventDispatcher.dispatchEvent(new Event(AuctionParcelEvent.AUCTION_FINISHED, this));
	}

	public EventAPI getEventAPI() {
		return eventDispatcher.getPublicEventAPI();
	}

	public AuctionTruck getAssignedTruck() {
		return assignedTruck;
	}
}
