/**
 * 
 */
package com.github.rinde.evo4mas.fabrirecht;

import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.pdptw.ParcelDTO;

/**
 * 
 * 
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public interface CoordAgent {

	void setCoordModel(CoordModel model);

	// should return the cost of handling this parcel according to the agent.
	// the
	// returned value is interpreted as a bid for the parcel.
	double getCost(ParcelDTO p);

	// should return true if handling this parcel is possible within all
	// constraints defined by this parcel and all parcels already being served
	// by this agent.
	boolean isFeasible(ParcelDTO parcel);

	// is called when agent has won the auction
	void receiveOrder(Parcel parcel);

}
