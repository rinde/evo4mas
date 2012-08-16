/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import java.util.Map.Entry;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;

import rinde.evo4mas.mas.fabrirecht.CoordModel.ServiceAssignment;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.RoadUser;
import rinde.sim.ui.renderers.ModelProvider;
import rinde.sim.ui.renderers.ModelRenderer;
import rinde.sim.ui.renderers.ViewPort;
import rinde.sim.ui.renderers.ViewRect;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class CoordModelRenderer implements ModelRenderer {

	protected CoordModel cm;
	protected RoadModel rm;

	public CoordModelRenderer() {}

	public void renderStatic(GC gc, ViewPort vp) {}

	public void renderDynamic(GC gc, ViewPort vp, long time) {

		for (final Entry<Parcel, ServiceAssignment> entry : cm.parcelAssignments.entrySet()) {
			if (rm.containsObject(entry.getKey())) {
				final Point p = rm.getPosition(entry.getKey());
				final int x = vp.toCoordX(p.x);
				final int y = vp.toCoordY(p.y);
				// gc.fillRectangle(x - 2, y - 2, 20, 20);

				final Point p2 = rm.getPosition((RoadUser) entry.getValue().agent);
				final int x2 = vp.toCoordX(p2.x);
				final int y2 = vp.toCoordY(p2.y);

				gc.setForeground(new Color(gc.getDevice(), new RGB(200, 100, 0)));
				gc.setLineWidth(2);
				gc.drawLine(x, y, x2, y2);
			}
		}

	}

	public ViewRect getViewRect() {
		return null;
	}

	public void register(CoordModel m) {
		cm = m;
	}

	public Class<CoordModel> getSupportedModelType() {
		return CoordModel.class;
	}

	public void registerModelProvider(ModelProvider mp) {
		cm = mp.getModel(CoordModel.class);
		rm = mp.getModel(RoadModel.class);
	}

}
