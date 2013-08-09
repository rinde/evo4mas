/**
 * 
 */
package rinde.solver.pdptw;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.junit.Test;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class ArraysSolversTest {

    @Test
    public void test() {

        final Measure<Double, Velocity> speed = Measure
                .valueOf(40d, NonSI.KILOMETERS_PER_HOUR);

        final Measure<Double, Length> distance = Measure
                .valueOf(100d, SI.KILOMETER);

        final Measure<Double, Length> meters = distance.to(SI.METER);

        System.out.println(speed.getUnit().getStandardUnit());

        // m/s
        final double ms = speed.doubleValue(SI.METERS_PER_SECOND);
        final double m = distance.doubleValue(SI.METER);

        final Measure<Double, Duration> duration = Measure
                .valueOf(m / ms, SI.SECOND);

        System.out.println(duration.doubleValue(NonSI.HOUR));

        final Unit<?> conv = SI.KILOMETER.divide(NonSI.KILOMETERS_PER_HOUR)
                .times(100d);

        System.out.println(conv.getConverterTo(NonSI.HOUR).convert(40d));

        System.out.println(NonSI.KILOMETERS_PER_HOUR.divide(distance
                .doubleValue(SI.KILOMETER)));
        // final Unit<?> conv =
        // SI.KILOMETER.divide(NonSI.KILOMETERS_PER_HOUR).times(distance);
        // speed.to(conv)
    }
}
