/**
 * 
 */
package rinde.evo4mas.gendreau06.deprecated;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newLinkedHashMap;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import rinde.evo4mas.gendreau06.deprecated.AuctionParcel.AuctionParcelEvent;
import rinde.sim.core.model.ModelProvider;
import rinde.sim.core.model.ModelReceiver;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.PDPModel.PDPModelEvent;
import rinde.sim.core.model.pdp.PDPModel.PDPModelEventType;
import rinde.sim.event.Event;
import rinde.sim.event.Listener;
import rinde.sim.ui.renderers.PanelRenderer;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class AuctionPanel implements PanelRenderer, ModelReceiver {

	protected Tree auctionTree;
	protected Map<AuctionTruck, TreeItem> truckMapping;
	protected Map<AuctionParcel, TreeItem> parcelMapping;

	protected PDPModel pdpModel;

	public AuctionPanel() {
		truckMapping = newLinkedHashMap();
		parcelMapping = newHashMap();
	}

	public void initializePanel(Composite parent) {
		final FillLayout layout = new FillLayout();
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		layout.type = SWT.VERTICAL;
		parent.setLayout(layout);

		auctionTree = new Tree(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
		// auctionTree.setHeaderVisible(true);
		// auctionTree.setLinesVisible(true);
	}

	public int preferredSize() {
		// TODO Auto-generated method stub
		return 200;
	}

	public int getPreferredPosition() {
		return SWT.RIGHT;
	}

	public String getName() {
		return "Auction info";
	}

	protected void handleNewParcel(final AuctionParcel ap) {
		auctionTree.getDisplay().asyncExec(new Runnable() {
			public void run() {
				final TreeItem truckItem = truckMapping.get(ap.getAssignedTruck());
				final TreeItem item = new TreeItem(truckItem, 0);
				item.setText(pdpModel.getParcelState(ap).toString());
				truckItem.setExpanded(true);
				parcelMapping.put(ap, item);
			}
		});
	}

	protected void handleParcelStatusUpdate(final AuctionParcel ap) {

		final TreeItem parcelItem = parcelMapping.get(ap);

		auctionTree.getDisplay().asyncExec(new Runnable() {
			public void run() {
				parcelItem.setText(pdpModel.getParcelState(ap).toString());
			}
		});

	}

	protected void handleNewVehicle(final AuctionTruck ap) {
		auctionTree.getDisplay().asyncExec(new Runnable() {
			public void run() {
				final TreeItem item = new TreeItem(auctionTree, 0);
				truckMapping.put(ap, item);
				item.setText("" + ap.hashCode());
			}
		});
	}

	public void registerModelProvider(ModelProvider mp) {
		pdpModel = mp.getModel(PDPModel.class);

		pdpModel.getEventAPI().addListener(new Listener() {
			public void handleEvent(final Event e) {
				if (e.getEventType() == PDPModelEventType.NEW_PARCEL) {
					((AuctionParcel) ((PDPModelEvent) e).parcel).getEventAPI().addListener(new Listener() {
						public void handleEvent(Event ev) {
							handleNewParcel(((AuctionParcel) ((PDPModelEvent) e).parcel));
						}
					}, AuctionParcelEvent.AUCTION_FINISHED);
				} else {
					handleNewVehicle((AuctionTruck) ((PDPModelEvent) e).vehicle);
				}
			}
		}, PDPModelEventType.NEW_PARCEL, PDPModelEventType.NEW_VEHICLE);

		pdpModel.getEventAPI()
				.addListener(new Listener() {
					public void handleEvent(Event ev) {
						handleParcelStatusUpdate((AuctionParcel) ((PDPModelEvent) ev).parcel);
					}
				}, PDPModelEventType.START_DELIVERY, PDPModelEventType.END_DELIVERY, PDPModelEventType.START_PICKUP, PDPModelEventType.END_PICKUP, PDPModelEventType.PARCEL_AVAILABLE);
	}
}
