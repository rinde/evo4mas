/**
 * 
 */
package rinde.evo4mas.gendreau06.route;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newLinkedList;

import java.util.Collection;
import java.util.Queue;

import javax.annotation.Nullable;

import rinde.sim.central.Converter;
import rinde.sim.problem.common.DefaultParcel;
import rinde.solver.pdptw.SingleVehicleSolver;
import rinde.solver.pdptw.SolutionObject;

/**
 * A {@link RoutePlanner} implementation that uses a {@link SingleVehicleSolver}
 * that computes a complete route each time {@link #update(Collection, long)} is
 * called.
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public class SolverRoutePlanner extends AbstractRoutePlanner {

    protected final SingleVehicleSolver solver;
    protected Queue<DefaultParcel> route;
    @Nullable
    protected SolutionObject solutionObject;

    /**
     * Create a route planner that uses the specified
     * {@link SingleVehicleSolver} to compute the best route.
     * @param s {@link SingleVehicleSolver} used for route planning.
     */
    public SolverRoutePlanner(SingleVehicleSolver s) {
        solver = s;
        route = newLinkedList();
    }

    @Override
    protected void doUpdate(Collection<DefaultParcel> onMap, long time) {
        checkState(roadModel != null && pdpModel != null && vehicle != null);
        route = solver.solve(Converter
                .convert(roadModel, pdpModel, vehicle, onMap, time));
    }

    public boolean hasNext() {
        return !route.isEmpty();
    }

    @Nullable
    public DefaultParcel current() {
        return route.peek();
    }

    @Override
    protected void nextImpl(long time) {
        route.poll();
    }

}
