package rinde.evo4mas.mas.fabrirecht;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Sets.newLinkedHashSet;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import rinde.evo4mas.evo.gp.GPProgram;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel.ParcelState;
import rinde.sim.core.model.pdp.PDPModel.VehicleParcelActionInfo;
import rinde.sim.core.model.pdp.PDPModel.VehicleState;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.problem.fabrirecht.FRParcel;
import rinde.sim.problem.fabrirecht.FRVehicle;
import rinde.sim.problem.fabrirecht.FabriRechtScenario;
import rinde.sim.problem.fabrirecht.ParcelDTO;
import rinde.sim.problem.fabrirecht.VehicleDTO;

class Truck extends FRVehicle implements CoordAgent {

	protected final Set<ParcelDTO> todoSet;
	protected final Map<ParcelDTO, Parcel> parcelMap;

	protected final GPProgram<FRContext> program;

	protected boolean hasChanged = true;
	protected Parcel currentTarget;

	protected CoordModel coordModel;

	protected final FabriRechtScenario scenario;

	/**
	 * @param pDto
	 */
	public Truck(VehicleDTO pDto, GPProgram<FRContext> p, FabriRechtScenario scen) {
		super(pDto);
		program = p;
		todoSet = newLinkedHashSet();
		parcelMap = newLinkedHashMap();
		scenario = scen;
	}

	// protected Target findTarget(long time) {
	// final Point truckPos = roadModel.getPosition(this);
	// final Collection<Parcel> currentContents = pdpModel.getContents(this);
	// final List<ParcelDTO> vehicleContents = newArrayList();
	// for (final Parcel p : currentContents) {
	// vehicleContents.add(((FRParcel) p).dto);
	// }
	// // final Collection<Parcel> parcels =
	// // pdpModel.getParcels(ParcelState.AVAILABLE, ParcelState.ANNOUNCED);
	// Parcel best = null;
	// double bestValue = Double.POSITIVE_INFINITY;
	// for (final ParcelDTO p : todoList) {
	// // TODO optimize: avoid creating all those objects in a loop
	// final double curr = program.execute(new FRContext(dto, truckPos,
	// vehicleContents, p, time, false));
	// if (curr < bestValue) {// && coordModel.canServe(p, curr)) {
	// bestValue = curr;
	// best = p;
	// }
	// }
	// for (final Parcel p : currentContents) {
	// final double curr = program.execute(new FRContext(dto, truckPos,
	// vehicleContents, ((FRParcel) p).dto, time,
	// true));
	// if (curr < bestValue) {
	// bestValue = curr;
	// best = p;
	// }
	// }
	// if (best == null) {
	// return null;
	// }
	// return new Target(best, bestValue);
	// }

	protected ParcelDTO next(long time) {
		return next(program, dto, todoSet, convert(pdpModel.getContents(this)), roadModel.getPosition(this), time);
	}

	protected static ParcelDTO next(GPProgram<FRContext> program, VehicleDTO dto, Collection<ParcelDTO> todo,
			Collection<ParcelDTO> contents, Point truckPos, long time) {
		ParcelDTO best = null;
		double bestValue = Double.POSITIVE_INFINITY;
		for (final ParcelDTO p : todo) {
			final double v = program.execute(new FRContext(dto, truckPos, contents, p, time, false));
			if (v < bestValue) {
				best = p;
				bestValue = v;
			}
		}
		for (final ParcelDTO p : contents) {
			final double v = program.execute(new FRContext(dto, truckPos, contents, p, time, true));
			if (v < bestValue) {
				best = p;
				bestValue = v;
			}
		}
		return best;
	}

	// public boolean acceptNewParcel(ParcelDTO p) {
	//
	// // calculate whether adding this parcel is possible without violating
	// // any constraints
	// final List<ParcelDTO> currentTodoList = newArrayList();
	// currentTodoList.add(p);
	// currentTodoList.addAll(todoList);
	//
	// return true;
	// // final double v = acceptanceFunction.execute(new FRContext(roadModel,
	// // pdpModel, this, p, p.orderArrivalTime,
	// // false));
	// // return v < 100;
	// }

	@Override
	protected void tickImpl(TimeLapse time) {
		try {

			if (currentTarget == null && time.hasTimeLeft()) {
				currentTarget = parcelMap.get(next(time.getTime()));
			}
			// TODO allow diversions?
			// if ((hasChanged && time.hasTimeLeft())
			// || (currentTarget == null ||
			// !roadModel.containsObject(currentTarget.target))) {
			// hasChanged = false;
			//
			// // if (currentTarget != null) {
			// // coordModel.doServe(currentTarget.target, this,
			// // currentTarget.heuristicValue);
			// // }
			// }

			if (currentTarget != null && time.hasTimeLeft()) {
				if (pdpModel.getParcelState(currentTarget) == ParcelState.IN_CARGO) {
					roadModel.moveTo(this, currentTarget.getDestination(), time);
					if (roadModel.getPosition(this).equals(currentTarget.getDestination())
							&& pdpModel
									.getTimeWindowPolicy()
									.canDeliver(currentTarget.getDeliveryTimeWindow(), time.getStartTime(), currentTarget
											.getDeliveryDuration())) {
						pdpModel.deliver(this, currentTarget, time);
						currentTarget = null;
					}

				} else {
					roadModel.moveTo(this, currentTarget, time);

					// System.out.println(time.getTime() + " move");
					if (roadModel.equalPosition(this, currentTarget)
							&& pdpModel
									.getTimeWindowPolicy()
									.canPickup(currentTarget.getPickupTimeWindow(), time.getStartTime(), currentTarget.getPickupDuration())
							&& getCapacity() >= pdpModel.getContentsSize(this) + currentTarget.getMagnitude()) {

						pdpModel.pickup(this, currentTarget, time);
						todoSet.remove(((FRParcel) currentTarget).dto);
						currentTarget = null;
					}
				}
			}
		} catch (final Exception e) {
			System.out.println(program.toString());
			throw new RuntimeException(e);
		}
	}

	// @Override
	// public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {
	// super.initRoadPDP(pRoadModel, pPdpModel);
	// pdpModel.getEventAPI().addListener(this, PDPModelEventType.NEW_PARCEL,
	// PDPModelEventType.START_PICKUP);
	// }
	//
	// public void handleEvent(Event e) {
	//
	// }

	class Target {
		public final ParcelDTO target;
		public final double heuristicValue;

		public Target(ParcelDTO t, double v) {
			target = t;
			heuristicValue = v;
		}
	}

	public void setCoordModel(CoordModel model) {
		coordModel = model;
	}

	public double getCost(ParcelDTO p) {
		return program.execute(new FRContext(dto, roadModel.getPosition(this), convert(pdpModel.getContents(this)), p,
				p.orderArrivalTime, false));
	}

	public GPProgram<FRContext> getProgram() {
		return program;
	}

	public boolean isFeasible(ParcelDTO newParcel) {
		try {
			// if( newParcel.)
			final Set<ParcelDTO> newTodoSet = newLinkedHashSet();
			newTodoSet.addAll(todoSet);
			newTodoSet.add(newParcel);

			int totalParcels = newTodoSet.size() + pdpModel.getContents(this).size();
			checkState(totalParcels > 0);

			final VehicleParcelActionInfo v = pdpModel.getVehicleState(this) == VehicleState.DELIVERING
					|| pdpModel.getVehicleState(this) == VehicleState.PICKING_UP ? pdpModel.getVehicleActionInfo(this)
					: null;

			final ParcelDTO process = v == null ? null : ((FRParcel) v.getParcel()).dto;

			final long timeleft = v == null ? 0 : v.timeNeeded();
			final boolean ispickup = pdpModel.getVehicleState(this) == VehicleState.PICKING_UP;

			if (process != null) {
				if (ispickup) {
					newTodoSet.add(process);
				} else {
					totalParcels += 1;
				}
			}

			final SubSimulation sim = new SubSimulation(scenario, newParcel.orderArrivalTime, this, process, timeleft,
					ispickup, roadModel.getPosition(this), newTodoSet, convert(pdpModel.getContents(this)));
			sim.start();

			// if (newParcel.pickupLocation.equals(new Point(22, 75))) {
			// System.out.println(newParcel.orderArrivalTime);

			if (!sim.isShutDownPrematurely() && sim.getStatistics().totalDeliveries != totalParcels
					&& sim.getSimulator().getCurrentTime() != scenario.timeWindow.end) {

				checkState(false);
			}

			// }

			//
			// throw new RuntimeException();

			final boolean verdict = !sim.isShutDownPrematurely() && sim.getStatistics().deliveryTardiness == 0
					&& sim.getStatistics().pickupTardiness == 0 && sim.getStatistics().totalDeliveries == totalParcels;
			// if (verdict) {
			//
			// System.out.println(sim.getStatistics());
			// System.out.println(totalParcels);
			// System.out.println("premature: " + sim.isShutDownPrematurely());
			// System.out.println(pdpModel.getVehicleState(this));
			//
			// }

			return verdict;

		} catch (final Exception e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		// FIXME add capacity constraints!!

		// TODO use sub-simulation??
		// final Set<ParcelDTO> tmpTodo = newLinkedHashSet();
		// tmpTodo.add(newParcel);
		// tmpTodo.addAll(todoSet);
		//
		// final Set<ParcelDTO> tmpContents = newLinkedHashSet();
		// tmpContents.addAll(convert(pdpModel.getContents(this)));
		//
		// Point tmpPos = roadModel.getPosition(this);
		// long tmpTime = newParcel.orderArrivalTime;
		// while (!(tmpTodo.isEmpty() && tmpContents.isEmpty())) {
		// final ParcelDTO next = next(program, dto, tmpTodo, tmpContents,
		// tmpPos, tmpTime);
		// if (next == null) {
		// // this means that the heuristic could not choose which order to
		// // serve even though there certainly are orders. This means it
		// // is a bad heuristic.
		//
		// return false;
		// }
		//
		// if (!tmpContents.contains(next)) {
		// // goto parcel pickup
		// final double dist = Point.distance(tmpPos, next.pickupLocation);
		// tmpPos = next.pickupLocation;
		// tmpTime += (long) dist;
		//
		// // we are too early -> wait
		// if (!next.pickupTimeWindow.isAfterStart(tmpTime)) {
		// // System.out.println("too early");
		// tmpTime = next.pickupTimeWindow.begin;
		// }
		// // check time window feasibility
		// if (!next.pickupTimeWindow.isIn(tmpTime) ||
		// !next.pickupTimeWindow.isIn(tmpTime + next.pickupDuration)) {
		// // System.out.println("time window not possible");
		// return false;
		// }
		// // 'pickup'
		// tmpTime += next.pickupDuration;
		// tmpTodo.remove(next);
		// tmpContents.add(next);
		// } else {
		// // goto parcel destination
		// final double dist = Point.distance(tmpPos, next.destinationLocation);
		// tmpPos = next.destinationLocation;
		// tmpTime += (long) dist;
		// // we are too early
		// if (!next.deliveryTimeWindow.isAfterStart(tmpTime)) {
		// tmpTime = next.deliveryTimeWindow.begin;
		// }
		// // check time window feasibility
		// if (!next.deliveryTimeWindow.isIn(tmpTime)
		// || !next.deliveryTimeWindow.isIn(tmpTime + next.deliveryDuration)) {
		// return false;
		// }
		// // 'deliver'
		// tmpTime += next.deliveryDuration;
		// tmpContents.remove(next);
		// }
		// }
		// return dto.availabilityTimeWindow.isIn(tmpTime);
	}

	public void receiveOrder(Parcel parcel) {
		// TODO Auto-generated method stub

		parcelMap.put(((FRParcel) parcel).dto, parcel);

		todoSet.add(((FRParcel) parcel).dto);

	}

	static Set<ParcelDTO> convert(Collection<Parcel> parcels) {
		final Set<ParcelDTO> dtos = newLinkedHashSet();
		for (final Parcel p : parcels) {
			dtos.add(((FRParcel) p).dto);
		}
		return dtos;
	}
}
