/**
 * 
 */
package rinde.sim.pdptw.central;

import org.junit.Test;

import rinde.sim.core.graph.Point;
import rinde.sim.pdptw.central.GlobalStateObject.VehicleState;
import rinde.sim.problem.common.ParcelDTO;
import rinde.sim.problem.common.VehicleDTO;
import rinde.sim.util.TestUtil;
import rinde.sim.util.TimeWindow;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class SolverValidatorTest {

    protected static final ParcelDTO p1 = parcel();
    protected static final ParcelDTO p2 = parcel();
    protected static final ParcelDTO p3 = parcel();
    protected static final ParcelDTO p4 = parcel();
    protected static final ParcelDTO p5 = parcel();

    @Test(expected = IllegalArgumentException.class)
    public void validateNegativeTime() {
        @SuppressWarnings("null")
        final GlobalStateObject state =
                new GlobalStateObject(null, null, -1, null, null, null);
        SolverValidator.validateInputs(state);
    }

    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("null")
    public void validateNegativeRemainingTime() {
        final VehicleState vs1 = new VehicleState(vdto(), null, null, -1, null);
        final GlobalStateObject state =
                new GlobalStateObject(null, ImmutableList.of(vs1), 0, null,
                        null, null);
        SolverValidator.validateInputs(state);
    }

    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("null")
    public void validateZeroSpeed() {
        final VehicleDTO dto1 = new VehicleDTO(null, 0, 1, null);
        final VehicleState vs1 = new VehicleState(dto1, null, null, 0, null);
        final GlobalStateObject state =
                new GlobalStateObject(null, ImmutableList.of(vs1), 0, null,
                        null, null);
        SolverValidator.validateInputs(state);
    }

    @SuppressWarnings("null")
    @Test(expected = IllegalArgumentException.class)
    public void validateParcelAvailableAndInInventory() {
        final VehicleState vs1 =
                new VehicleState(vdto(), null, ImmutableSet.of(p1), 0, null);
        final GlobalStateObject state =
                new GlobalStateObject(ImmutableSet.of(p1),
                        ImmutableList.of(vs1), 0, null, null, null);
        SolverValidator.validateInputs(state);
    }

    @SuppressWarnings("null")
    @Test(expected = IllegalArgumentException.class)
    public void validateParcelInTwoInventories() {
        final VehicleState vs1 =
                new VehicleState(vdto(), null, ImmutableSet.of(p1), 0, null);
        final VehicleState vs2 =
                new VehicleState(vdto(), null, ImmutableSet.of(p1), 0, null);
        final ImmutableSet<ParcelDTO> empty = ImmutableSet.of();
        final GlobalStateObject state =
                new GlobalStateObject(empty, ImmutableList.of(vs1, vs2), 0,
                        null, null, null);
        SolverValidator.validateInputs(state);
    }

    @SuppressWarnings("null")
    @Test(expected = IllegalArgumentException.class)
    public void valiateInputsDestinationNotInContents() {
        final ImmutableSet<ParcelDTO> empty = ImmutableSet.of();
        final VehicleState vs1 = new VehicleState(vdto(), null, empty, 0, p1);
        final VehicleState vs2 =
                new VehicleState(vdto(), null, ImmutableSet.of(p2, p1), 0, null);

        final GlobalStateObject state =
                new GlobalStateObject(empty, ImmutableList.of(vs1, vs2), 0,
                        null, null, null);
        SolverValidator.validateInputs(state);
    }

    @SuppressWarnings("null")
    @Test
    public void validateCorrectInputs() {
        final ImmutableSet<ParcelDTO> empty = ImmutableSet.of();
        final VehicleState vs1 =
                new VehicleState(vdto(), null, ImmutableSet.of(p1), 0, p1);
        final VehicleState vs2 =
                new VehicleState(vdto(), null, ImmutableSet.of(p2), 0, null);
        final VehicleState vs3 = new VehicleState(vdto(), null, empty, 0, p3);
        final ImmutableSet<ParcelDTO> available = ImmutableSet.of(p3);
        final GlobalStateObject state =
                new GlobalStateObject(available,
                        ImmutableList.of(vs1, vs2, vs3), 0, null, null, null);
        SolverValidator.validateInputs(state);
    }

    @SuppressWarnings("null")
    @Test(expected = IllegalArgumentException.class)
    public void validateInvalidNumberOfRoutes() {
        final VehicleState vs1 =
                new VehicleState(vdto(), new Point(0, 0), null, 0, null);
        final ImmutableList<ImmutableList<ParcelDTO>> routes =
                ImmutableList.of();
        final GlobalStateObject state =
                new GlobalStateObject(null, ImmutableList.of(vs1), 0, null,
                        null, null);
        SolverValidator.validateOutputs(routes, state);
    }

    @SuppressWarnings("null")
    @Test(expected = IllegalArgumentException.class)
    public void validateParcelInTwoRoutes() {
        final ImmutableSet<ParcelDTO> empty = ImmutableSet.of();
        final VehicleState vs1 =
                new VehicleState(vdto(), new Point(0, 0), empty, 0, null);
        final VehicleState vs2 =
                new VehicleState(vdto(), new Point(0, 0), empty, 0, null);

        final ImmutableList<ImmutableList<ParcelDTO>> routes =
                ImmutableList.of(ImmutableList.of(p1, p1),
                                 ImmutableList.of(p1, p1));
        final ImmutableSet<ParcelDTO> availableParcels = ImmutableSet.of(p1);
        final ImmutableList<VehicleState> vehicles = ImmutableList.of(vs1, vs2);
        final GlobalStateObject state =
                new GlobalStateObject(availableParcels, vehicles, 0, null,
                        null, null);
        SolverValidator.validateOutputs(routes, state);
    }

    @SuppressWarnings("null")
    @Test(expected = IllegalArgumentException.class)
    public void validateParcelTooManyTimes1() {
        final ImmutableSet<ParcelDTO> empty = ImmutableSet.of();
        final VehicleState vs1 =
                new VehicleState(vdto(), new Point(0, 0), empty, 0, null);

        final ImmutableList<ImmutableList<ParcelDTO>> routes =
                ImmutableList.of(ImmutableList.of(p1, p1, p1));
        final ImmutableSet<ParcelDTO> availableParcels = ImmutableSet.of(p1);
        final ImmutableList<VehicleState> vehicles = ImmutableList.of(vs1);
        final GlobalStateObject state =
                new GlobalStateObject(availableParcels, vehicles, 0, null,
                        null, null);
        SolverValidator.validateOutputs(routes, state);
    }

    @SuppressWarnings("null")
    @Test(expected = IllegalArgumentException.class)
    public void validateParcelTooManyTimes2() {
        final VehicleState vs1 =
                new VehicleState(vdto(), new Point(0, 0), ImmutableSet.of(p1),
                        0, null);

        final ImmutableList<ImmutableList<ParcelDTO>> routes =
                ImmutableList.of(ImmutableList.of(p1, p1));
        final ImmutableSet<ParcelDTO> availableParcels = ImmutableSet.of();
        final ImmutableList<VehicleState> vehicles = ImmutableList.of(vs1);
        final GlobalStateObject state =
                new GlobalStateObject(availableParcels, vehicles, 0, null,
                        null, null);
        SolverValidator.validateOutputs(routes, state);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateParcelNotInCargo() {
        final ImmutableSet<ParcelDTO> empty = ImmutableSet.of();
        final VehicleState vs1 =
                new VehicleState(vdto(), new Point(0, 0), empty, 0, null);

        final ImmutableList<ImmutableList<ParcelDTO>> routes =
                ImmutableList.of(ImmutableList.of(p1));
        final ImmutableSet<ParcelDTO> availableParcels = ImmutableSet.of();
        final ImmutableList<VehicleState> vehicles = ImmutableList.of(vs1);
        @SuppressWarnings("null")
        final GlobalStateObject state =
                new GlobalStateObject(availableParcels, vehicles, 0, null,
                        null, null);
        SolverValidator.validateOutputs(routes, state);
    }

    @SuppressWarnings("null")
    @Test(expected = IllegalArgumentException.class)
    public void validateUnknownParcelInRoute() {
        final ImmutableSet<ParcelDTO> empty = ImmutableSet.of();
        final VehicleState vs1 =
                new VehicleState(vdto(), new Point(0, 0), empty, 0, null);
        final VehicleState vs2 =
                new VehicleState(vdto(), new Point(0, 0), empty, 0, null);

        final ImmutableList<ImmutableList<ParcelDTO>> routes =
                ImmutableList.of(ImmutableList.of(p1, p1),
                                 ImmutableList.of(p2, p3, p3, p2));
        final ImmutableSet<ParcelDTO> availableParcels =
                ImmutableSet.of(p1, p2);
        final ImmutableList<VehicleState> vehicles = ImmutableList.of(vs1, vs2);
        final GlobalStateObject state =
                new GlobalStateObject(availableParcels, vehicles, 0, null,
                        null, null);
        SolverValidator.validateOutputs(routes, state);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateIncompleteRoute1() {
        final ImmutableSet<ParcelDTO> empty = ImmutableSet.of();
        final VehicleState vs1 =
                new VehicleState(vdto(), new Point(0, 0), empty, 0, null);
        final VehicleState vs2 =
                new VehicleState(vdto(), new Point(0, 0), empty, 0, null);

        final ImmutableList<ImmutableList<ParcelDTO>> routes =
                ImmutableList.of(ImmutableList.of(p1, p1),
                                 ImmutableList.of(p2, p2));
        final ImmutableSet<ParcelDTO> availableParcels =
                ImmutableSet.of(p1, p2, p3);
        final ImmutableList<VehicleState> vehicles = ImmutableList.of(vs1, vs2);
        @SuppressWarnings("null")
        final GlobalStateObject state =
                new GlobalStateObject(availableParcels, vehicles, 0, null,
                        null, null);
        SolverValidator.validateOutputs(routes, state);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateIncompleteRouteForVehicle() {
        final VehicleState vs1 =
                new VehicleState(vdto(), new Point(0, 0), ImmutableSet.of(p1),
                        0, null);

        final ImmutableList<ParcelDTO> empty = ImmutableList.of();
        final ImmutableList<ImmutableList<ParcelDTO>> routes =
                ImmutableList.of(empty);
        final ImmutableSet<ParcelDTO> availableParcels = ImmutableSet.of();
        final ImmutableList<VehicleState> vehicles = ImmutableList.of(vs1);
        @SuppressWarnings("null")
        final GlobalStateObject state =
                new GlobalStateObject(availableParcels, vehicles, 0, null,
                        null, null);
        SolverValidator.validateOutputs(routes, state);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateOutputDestinationNotFirstInRoute() {
        final ImmutableSet<ParcelDTO> empty = ImmutableSet.of();
        final VehicleState vs1 =
                new VehicleState(vdto(), new Point(0, 0), empty, 0, p1);

        final ImmutableList<ImmutableList<ParcelDTO>> routes =
                ImmutableList.of(ImmutableList.of(p2, p1, p1, p2));

        final ImmutableSet<ParcelDTO> availableParcels =
                ImmutableSet.of(p1, p2);
        final ImmutableList<VehicleState> vehicles = ImmutableList.of(vs1);
        final GlobalStateObject state =
                new GlobalStateObject(availableParcels, vehicles, 0, null,
                        null, null);
        SolverValidator.validateOutputs(routes, state);
    }

    @SuppressWarnings("null")
    @Test
    public void validateCorrectOutput() {
        final ImmutableSet<ParcelDTO> empty = ImmutableSet.of();
        final VehicleState vs1 =
                new VehicleState(vdto(), new Point(0, 0), empty, 0, null);
        final VehicleState vs2 =
                new VehicleState(vdto(), new Point(0, 0), ImmutableSet.of(p3),
                        0, null);

        final ImmutableList<ImmutableList<ParcelDTO>> routes =
                ImmutableList.of(ImmutableList.of(p1, p1),
                                 ImmutableList.of(p2, p3, p2));
        final ImmutableSet<ParcelDTO> availableParcels =
                ImmutableSet.of(p1, p2);
        final ImmutableList<VehicleState> vehicles = ImmutableList.of(vs1, vs2);
        final GlobalStateObject state =
                new GlobalStateObject(availableParcels, vehicles, 0, null,
                        null, null);
        SolverValidator.validateOutputs(routes, state);
    }

    @Test
    public void testWrap() {
        TestUtil.testPrivateConstructor(SolverValidator.class);
        final ImmutableSet<ParcelDTO> empty = ImmutableSet.of();
        final VehicleState vs1 =
                new VehicleState(vdto(), new Point(0, 0), empty, 0, null);
        final VehicleState vs2 =
                new VehicleState(vdto(), new Point(0, 0), ImmutableSet.of(p3),
                        0, null);
        final VehicleState vs3 =
                new VehicleState(vdto(), new Point(0, 0), empty, 0, p4);

        final ImmutableList<ImmutableList<ParcelDTO>> routes =
                ImmutableList.of(ImmutableList.of(p1, p1),
                                 ImmutableList.of(p2, p3, p2),
                                 ImmutableList.of(p4, p5, p5, p4));
        final ImmutableSet<ParcelDTO> availableParcels =
                ImmutableSet.of(p1, p2, p4, p5);
        final ImmutableList<VehicleState> vehicles =
                ImmutableList.of(vs1, vs2, vs3);
        final GlobalStateObject state =
                new GlobalStateObject(availableParcels, vehicles, 0, null,
                        null, null);
        final Solver solver = SolverValidator.wrap(new FakeSolver(routes));
        solver.solve(state);
    }

    static ParcelDTO parcel() {
        return new ParcelDTO(new Point(0, 0), new Point(0, 0),
                TimeWindow.ALWAYS, TimeWindow.ALWAYS, 0, 0, 0, 0);
    }

    static VehicleDTO vdto() {
        return new VehicleDTO(new Point(0, 0), 1, 1, TimeWindow.ALWAYS);
    }

    class FakeSolver implements Solver {
        ImmutableList<ImmutableList<ParcelDTO>> answer;

        FakeSolver(ImmutableList<ImmutableList<ParcelDTO>> answer) {
            this.answer = answer;
        }

        public ImmutableList<ImmutableList<ParcelDTO>> solve(
                GlobalStateObject state) {
            return answer;
        }
    }

}
