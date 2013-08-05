/**
 * 
 */
package rinde.dynscale;

import static com.google.common.collect.Sets.newHashSet;

import java.util.List;

import rinde.sim.problem.common.AddParcelEvent;
import rinde.sim.scenario.Scenario;
import rinde.sim.scenario.TimedEvent;

import com.google.common.collect.ImmutableList;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public final class Metrics {

    private Metrics() {}

    // should return the load at every time instance of a scenario
    public static ImmutableList<Double> measureLoad(Scenario s) {
        final ImmutableList.Builder<Double> builder = ImmutableList.builder();

        return builder.build();
    }

    public static List<LoadPart> measureLoad(AddParcelEvent event) {
        // pickup lower bound,
        final long pickupLb = event.parcelDTO.pickupTimeWindow.begin;
        // pickup upper bound
        final long pickupUb = event.parcelDTO.pickupTimeWindow.end
                + event.parcelDTO.pickupDuration;
        final double pickupLoad = 1 / (pickupUb - pickupLb);

        // delivery lower bound
        final long deliveryLb = event.parcelDTO.deliveryTimeWindow.begin;
        // delivery upper bound
        final long deliveryUb = event.parcelDTO.deliveryTimeWindow.end
                + event.parcelDTO.deliveryDuration;
        final double deliveryLoad = 1 / (deliveryUb - deliveryLb);

        final long travelLb = pickupLb + event.parcelDTO.pickupDuration;
        final long travelUb = event.parcelDTO.deliveryTimeWindow.end;
        final double travelLoad = 1 / (travelUb - travelLb);

        // TODO return complete load?
        return null;

    }

    // to use for parts of the timeline to avoid excessively long list with
    // mostly 0s.
    class LoadPart {
        long startTime;
        List<Double> load;
    }

    // TODO
    public static void computeStress() {}

    public static double measureDynamism(Scenario s) {
        final List<TimedEvent> list = s.asList();
        final ImmutableList.Builder<Long> times = ImmutableList.builder();
        for (final TimedEvent te : list) {
            if (te instanceof AddParcelEvent) {
                times.add(te.time);
            }
        }
        return measureDynamism(times.build());
    }

    public static double measureDynamism(List<Long> arrivalTimes) {
        // announcements are distinct arrival times
        final int announcements = newHashSet(arrivalTimes).size();
        final int orders = arrivalTimes.size();
        return announcements / (double) orders;
    }

    public static long travelTime(Point p1, Point p2, double speed) {
        return DoubleMath
                .roundToLong(Point.distance(p1, p2) / speed, RoundingMode.CEILING);
    }

}
