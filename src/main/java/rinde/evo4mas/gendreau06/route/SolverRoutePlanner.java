/**
 * 
 */
package rinde.evo4mas.gendreau06.route;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newLinkedList;

import java.util.Collection;
import java.util.Queue;

import javax.annotation.Nullable;

import rinde.sim.core.model.pdp.Parcel;
import rinde.solver.pdptw.SingleVehicleSolver;
import rinde.solver.pdptw.SolutionObject;

/**
 * A {@link RoutePlanner} implementation that uses a {@link SingleVehicleSolver}
 * that computes a complete route each time
 * {@link #update(Collection, Collection, long)} is called.
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public class SolverRoutePlanner extends AbstractRoutePlanner {

    protected final SingleVehicleSolver solver;
    protected Queue<Parcel> route;
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
    protected void doUpdate(Collection<Parcel> onMap, long time) {
        checkState(roadModel != null && pdpModel != null && vehicle != null);
        route = solver.solve(roadModel, pdpModel, vehicle, onMap, time);
    }

    public boolean hasNext() {
        return !route.isEmpty();
    }

    @Nullable
    public Parcel current() {
        return route.peek();
    }

    @Override
    protected void nextImpl(long time) {
        route.poll();
    }

}
