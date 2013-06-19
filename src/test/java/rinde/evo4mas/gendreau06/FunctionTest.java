/**
 * 
 */
package rinde.evo4mas.gendreau06;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import rinde.ecj.GPFunc;
import rinde.ecj.GPProgramParser;
import rinde.evo4mas.common.GPFunctions.Dist;
import rinde.evo4mas.common.GPFunctions.Urge;
import rinde.evo4mas.common.TruckContext;
import rinde.evo4mas.gendreau06.deprecated.CoordinationModel;
import rinde.evo4mas.gendreau06.deprecated.MyopicTruck;
import rinde.sim.core.Simulator;
import rinde.sim.core.TickListener;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.TimeLapseFactory;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.problem.common.AddParcelEvent;
import rinde.sim.problem.common.AddVehicleEvent;
import rinde.sim.problem.common.DefaultParcel;
import rinde.sim.problem.common.DynamicPDPTWProblem;
import rinde.sim.problem.common.DynamicPDPTWProblem.Creator;
import rinde.sim.problem.common.ParcelDTO;
import rinde.sim.problem.common.VehicleDTO;
import rinde.sim.problem.gendreau06.Gendreau06Scenario;
import rinde.sim.problem.gendreau06.GendreauTestUtil;
import rinde.sim.scenario.TimedEvent;
import rinde.sim.util.TimeWindow;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class FunctionTest {

	private static final long SERVICE_TIME = 5 * 60 * 1000;

	private static final double EPSILON = 0.0000001;

	TestTruck tt;

	@Test
	public void testDist() throws IOException {

		final ParcelDTO dto1 = new ParcelDTO(new Point(2, 2.5), new Point(1, 2.5), new TimeWindow(0, 10000),
				new TimeWindow(10000, 20000), 0, 0, SERVICE_TIME, SERVICE_TIME);

		final ParcelDTO dto2 = new ParcelDTO(new Point(2, 2.5), new Point(1, 2.5), new TimeWindow(10000, 20000),
				new TimeWindow(10000, 20000), 0, 0, SERVICE_TIME, SERVICE_TIME);

		final TestInstance ti = new TestInstance(dto1, dto2);
		final Map<ParcelDTO, Parcel> parcels = ti.getParcelMap();

		assertEquals(0d, ti.getTestTruck().testFunc(new Dist<TruckContext>(), 0, parcels.get(dto1), false), EPSILON);
		assertEquals(1d, ti.getTestTruck().testFunc(new Dist<TruckContext>(), 0, parcels.get(dto1), true), EPSILON);

		assertEquals(10000d, ti.getTestTruck().testFunc(new Urge<TruckContext>(), 0, parcels.get(dto1), false), EPSILON);
		assertEquals(20000d, ti.getTestTruck().testFunc(new Urge<TruckContext>(), 0, parcels.get(dto2), false), EPSILON);

		assertFalse(ti.getTestTruck().testIsTooEarly(parcels.get(dto1), 0));
		assertTrue(ti.getTestTruck().testIsTooEarly(parcels.get(dto2), 0));

	}

	static List<TimedEvent> convert(ParcelDTO... dtos) {
		final List<TimedEvent> events = newArrayList();
		for (final ParcelDTO dto : dtos) {
			events.add(new AddParcelEvent(dto));
		}
		return events;
	}

	class TestInstance {
		final DynamicPDPTWProblem problem;
		TestTruck tt;

		public TestInstance(ParcelDTO... dtos) {
			this(convert(dtos));
		}

		public TestInstance(List<TimedEvent> events) {
			final Gendreau06Scenario gs = GendreauTestUtil.create(events);
			problem = new DynamicPDPTWProblem(gs, 123, new CoordinationModel());
			problem.addCreator(AddVehicleEvent.class, new Creator<AddVehicleEvent>() {
				public boolean create(Simulator sim, AddVehicleEvent event) {
					tt = new TestTruck(event.vehicleDTO);
					return sim.register(tt);
				}
			});
			problem.getSimulator().addTickListener(new TickListener() {

				public void tick(TimeLapse timeLapse) {
					problem.getSimulator().stop();
				}

				public void afterTick(TimeLapse timeLapse) {}
			});
			problem.simulate();
		}

		public Set<Parcel> getParcels() {
			return problem.getSimulator().getModelProvider().getModel(RoadModel.class).getObjectsOfType(Parcel.class);
		}

		public Map<ParcelDTO, Parcel> getParcelMap() {
			final Set<Parcel> parcels = getParcels();
			final Map<ParcelDTO, Parcel> map = newLinkedHashMap();
			for (final Parcel p : parcels) {
				map.put(((DefaultParcel) p).dto, p);
			}
			return map;
		}

		public TestTruck getTestTruck() {
			return tt;
		}

	}

	class TestTruck extends MyopicTruck {

		// used to pass test variables
		Parcel parcel;
		boolean isInCargo;
		double value;
		GPFunc<TruckContext> testFunc;

		public TestTruck(VehicleDTO pDto) {
			// the supplied program is ignored
			super(pDto, GPProgramParser.parseProgramFunc("(urge)", new MyopicFunctions().create()));
		}

		@Override
		protected void tickImpl(TimeLapse time) {
			if (parcel == null) {
				return;
			}
			next(time.getTime());
		}

		@Override
		protected Parcel nextLoop(Collection<Parcel> todo, Set<Parcel> alreadyClaimed, Collection<Parcel> contents,
				GendreauContext genericContext) {
			final GendreauContext gc = createContext(genericContext, parcel, isInCargo, false);
			value = testFunc.execute(null, gc);
			return null;
		}

		public double testFunc(GPFunc<TruckContext> func, long time, Parcel p, Boolean cargo) {
			parcel = p;
			testFunc = func;
			isInCargo = cargo;
			tickImpl(TimeLapseFactory.create(time, time + 1000));
			parcel = null;
			return value;
		}

		public boolean testIsTooEarly(Parcel p, long time) {
			return super.isTooEarly(p, getPosition(), TimeLapseFactory.create(time, time + 1000));
		}

		public Point getPosition() {
			return roadModel.getPosition(this);
		}
	}

}
