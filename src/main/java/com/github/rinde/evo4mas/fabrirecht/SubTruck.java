/**
 * 
 */
package com.github.rinde.evo4mas.fabrirecht;

import com.github.rinde.evo4mas.common.TruckContext;
import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.PDPModel.ParcelState;
import com.github.rinde.rinsim.core.pdptw.VehicleDTO;
import com.github.rinde.rinsim.pdptw.fabrirecht.FabriRechtScenario;

import rinde.ecj.Heuristic;

/**
 * @author Rinde van Lon 
 * 
 */
public class SubTruck extends Truck {

    Parcel firstSubject;

    /**
     * @param pDto
     * @param p
     * @param scen
     */
    public SubTruck(VehicleDTO pDto, Heuristic<TruckContext> p,
            FabriRechtScenario scen) {
        super(pDto, p, scen);
    }

    public void setFirstActionSubject(Parcel p) {
        firstSubject = p;
    }

    @Override
    protected void tickImpl(TimeLapse time) {
        final PDPModel pm = pdpModel.get();
        if (firstSubject != null) {
            if (pm.getParcelState(firstSubject) == ParcelState.IN_CARGO) {
                pm.deliver(this, firstSubject, time);
            } else {
                pm.pickup(this, firstSubject, time);
            }
            firstSubject = null;
        }
        if (time.hasTimeLeft()) {
            super.tickImpl(time);
        }
    }

}
