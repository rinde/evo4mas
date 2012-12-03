package rinde.evo4mas.fabrirecht;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Sets.newLinkedHashSet;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import rinde.ecj.GPProgram;
import rinde.evo4mas.common.TruckContext;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel.ParcelState;
import rinde.sim.core.model.pdp.PDPModel.VehicleParcelActionInfo;
import rinde.sim.core.model.pdp.PDPModel.VehicleState;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.problem.common.DefaultParcel;
import rinde.sim.problem.common.DefaultVehicle;
import rinde.sim.problem.common.ParcelDTO;
import rinde.sim.problem.common.StatsTracker.StatisticsDTO;
import rinde.sim.problem.common.VehicleDTO;
import rinde.sim.problem.fabrirecht.FabriRechtScenario;

class Truck extends DefaultVehicle implements CoordAgent {

	protected final Set<ParcelDTO> todoSet;
	protected final Map<ParcelDTO, Parcel> parcelMap;

	protected final GPProgram<TruckContext> program;

	protected boolean hasChanged = true;
	protected Parcel currentTarget;

	protected CoordModel coordModel;

	protected final FabriRechtScenario scenario;

	/**
	 * @param pDto
	 */
	public Truck(VehicleDTO pDto, GPProgram<TruckContext> p, FabriRechtScenario scen) {
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

	protected static ParcelDTO next(GPProgram<TruckContext> program, VehicleDTO dto, Collection<ParcelDTO> todo,
			Collection<ParcelDTO> contents, Point truckPos, long time) {
		ParcelDTO best = null;
		// int contentsSize = 0;
		// for( ParcelDTO p : contents ){
		// contentsSize+=p.neededCapacity;
		// }
		// dto.capacity

		double bestValue = Double.POSITIVE_INFINITY;
		for (final ParcelDTO p : todo) {
			final double v = program.execute(new TruckContext(dto, truckPos, contents, p, time, false));
			if (v < bestValue) {
				best = p;
				bestValue = v;
			}
		}
		for (final ParcelDTO p : contents) {
			final double v = program.execute(new TruckContext(dto, truckPos, contents, p, time, true));
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

			// check if we have enough time to get back, if needed we turn
			// around
			final double d = Point.distance(getDTO().startPosition, roadModel.getPosition(this));
			if (getDTO().availabilityTimeWindow.end - time.getEndTime() < d) {
				roadModel.moveTo(this, getDTO().startPosition, time);
			} else {

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
										.canPickup(currentTarget.getPickupTimeWindow(), time.getStartTime(), currentTarget
												.getPickupDuration())
								&& getCapacity() >= pdpModel.getContentsSize(this) + currentTarget.getMagnitude()) {

							if (!(this instanceof SubTruck)
									&& time.getTime() > currentTarget.getPickupTimeWindow().end
											- currentTarget.getPickupDuration()) {
								System.err.println("pickup tardiness " + currentTarget);
							}
							pdpModel.pickup(this, currentTarget, time);
							todoSet.remove(((DefaultParcel) currentTarget).dto);
							currentTarget = null;
						}
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
		return program.execute(new TruckContext(dto, roadModel.getPosition(this), convert(pdpModel.getContents(this)), p,
				p.orderArrivalTime, false));
	}

	public GPProgram<TruckContext> getProgram() {
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

			// in case we are currently busy delivering / picking up we need to
			// make sure to finish that action first
			final VehicleParcelActionInfo v = pdpModel.getVehicleState(this) == VehicleState.DELIVERING
					|| pdpModel.getVehicleState(this) == VehicleState.PICKING_UP ? pdpModel.getVehicleActionInfo(this)
					: null;
			// get the parcel which we are currently performing either
			// delivery/pickup on
			final ParcelDTO process = v == null ? null : ((DefaultParcel) v.getParcel()).dto;
			// how much time is needed for completion of this action
			final long timeleft = v == null ? 0 : v.timeNeeded();
			final boolean ispickup = pdpModel.getVehicleState(this) == VehicleState.PICKING_UP;

			if (process != null) {
				if (ispickup) {
					newTodoSet.add(process);
					checkState(pdpModel.getContentsSize(this) + process.neededCapacity <= pdpModel
							.getContainerCapacity(this));
				} else {
					totalParcels += 1;
				}
			}

			final Collection<Parcel> contents = pdpModel.getContents(this);
			final Set<ParcelDTO> dtos = newLinkedHashSet();
			for (final Parcel p : contents) {
				if (v == null || p != v.getParcel()) {
					dtos.add(((DefaultParcel) p).dto);
				}
			}

			final SubSimulation sim = new SubSimulation(scenario, newParcel.orderArrivalTime, this, process, timeleft,
					ispickup, roadModel.getPosition(this), newTodoSet, dtos);
			final StatisticsDTO stats = sim.start();

			// if (newParcel.pickupLocation.equals(new Point(22, 75))) {
			// System.out.println(newParcel.orderArrivalTime);

			if (stats.simFinish && stats.totalDeliveries != totalParcels
					&& sim.problemInstance.getStatistics().simulationTime != scenario.timeWindow.end) {

				System.out.println(stats);
				checkState(false);
			}

			final boolean verdict = stats.deliveryTardiness == 0 && stats.pickupTardiness == 0
					&& stats.totalDeliveries == totalParcels && stats.vehiclesAtDepot == 1;

			if (verdict && newParcel.pickupLocation.equals(new Point(28, 52))) {
				System.out.println("is feasible stats: " + stats);
			}

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

		parcelMap.put(((DefaultParcel) parcel).dto, parcel);

		todoSet.add(((DefaultParcel) parcel).dto);

	}

	static Set<ParcelDTO> convert(Collection<Parcel> parcels) {
		final Set<ParcelDTO> dtos = newLinkedHashSet();
		for (final Parcel p : parcels) {
			dtos.add(((DefaultParcel) p).dto);
		}
		return dtos;
	}
}