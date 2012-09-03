/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import static com.google.common.base.Preconditions.checkState;

import java.util.Set;

import rinde.sim.core.TickListener;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel.ParcelState;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.problem.fabrirecht.FRParcel;
import rinde.sim.problem.fabrirecht.FabriRechtScenario;
import rinde.sim.problem.fabrirecht.ParcelDTO;
import rinde.sim.scenario.ConfigurationException;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class SubSimulation extends Simulation {

	public SubSimulation(final FabriRechtScenario srcScenario, final long startTime, Truck t, ParcelDTO firstParcel,
			long processTimeLeft, boolean isPickup, Point truckPos, Set<ParcelDTO> todoSet, Set<ParcelDTO> contents)
			throws ConfigurationException {
		super(new FabriRechtScenario(srcScenario.min, srcScenario.max, srcScenario.timeWindow), t.getProgram());

		if (startTime > 0) {
			// fast forward time
			final TickListener fastForwardListener = new TickListener() {
				public void tick(TimeLapse timeLapse) {}

				public void afterTick(TimeLapse timeLapse) {
					if (timeLapse.getTime() == startTime) {
						stop();
					}
				}
			};
			getSimulator().addTickListener(fastForwardListener);
			getSimulator().start();
			getSimulator().removeTickListener(fastForwardListener);
			getSimulator().addTickListener(this);
		}
		checkState(getSimulator().getCurrentTime() == startTime);

		// move truck to current position
		final SubTruck newT = new SubTruck(t.getDTO(), t.getProgram(), srcScenario);
		getSimulator().register(newT);
		roadModel.removeObject(newT);
		roadModel.addObjectAt(newT, truckPos);

		if (firstParcel != null) {

			final long pickupDur = isPickup ? processTimeLeft : firstParcel.pickupDuration;
			final long deliverDur = isPickup ? firstParcel.deliveryDuration : processTimeLeft;

			final ParcelDTO dto = new ParcelDTO(firstParcel.pickupLocation, firstParcel.destinationLocation,
					firstParcel.pickupTimeWindow, firstParcel.deliveryTimeWindow, firstParcel.neededCapacity,
					firstParcel.orderArrivalTime, pickupDur, deliverDur);

			final Parcel p = new FRParcel(dto);
			getSimulator().register(p);
			if (!isPickup) {
				roadModel.removeObject(p);
				pdpModel.addParcelIn(newT, p);
			}

			newT.setFirstActionSubject(p);

		}

		// add cargo
		for (final ParcelDTO p : contents) {
			final Parcel newP = new FRParcel(p);
			getSimulator().register(newP);
			roadModel.removeObject(newP);
			pdpModel.addParcelIn(newT, newP);
		}

		// add todo list
		for (final ParcelDTO p : todoSet) {
			final Parcel newP = new FRParcel(p);
			getSimulator().register(newP);
			newT.receiveOrder(newP);
		}

		checkState(pdpModel.getParcels(ParcelState.values()).size() == contents.size() + todoSet.size()
				+ (firstParcel == null ? 0 : 1));

	}

	@Override
	public void stopCriterium(TimeLapse timeLapse) {
		if (timeLapse.getTime() >= fabriRechtScenario.timeWindow.end) {
			stop();
		}
	}

	// @Override
	// protected ParcelAssesor createParcelAssesor() {
	// return null;
	// }

}
