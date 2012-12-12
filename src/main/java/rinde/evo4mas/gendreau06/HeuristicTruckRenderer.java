/**
 * 
 */
package rinde.evo4mas.gendreau06;

import org.eclipse.swt.graphics.GC;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.Vehicle;
import rinde.sim.ui.renderers.PDPModelRenderer;
import rinde.sim.ui.renderers.ViewPort;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class HeuristicTruckRenderer extends PDPModelRenderer {

	@Override
	protected void drawMore(GC gc, ViewPort vp, long time, Vehicle v, Point p) {

		if (v instanceof HeuristicTruck) {

			final int x = vp.toCoordX(p.x);
			final int y = vp.toCoordY(p.y);

			final HeuristicTruck ht = (HeuristicTruck) v;

			gc.drawText(ht.stateMachine.getCurrentState().toString(), x, y + 20);

		}

	}

}
