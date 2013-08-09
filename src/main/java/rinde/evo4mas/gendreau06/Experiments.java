/**
 * 
 */
package rinde.evo4mas.gendreau06;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.measure.unit.SI;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.evo4mas.common.ExperimentUtil;
import rinde.evo4mas.gendreau06.GSimulation.Configurator;
import rinde.evo4mas.gendreau06.comm.AuctionCommModel;
import rinde.evo4mas.gendreau06.comm.BlackboardCommModel;
import rinde.evo4mas.gendreau06.comm.BlackboardUser;
import rinde.evo4mas.gendreau06.comm.Communicator;
import rinde.evo4mas.gendreau06.comm.RandomBidder;
import rinde.evo4mas.gendreau06.route.RandomRoutePlanner;
import rinde.evo4mas.gendreau06.route.SolverRoutePlanner;
import rinde.sim.core.Simulator;
import rinde.sim.core.model.Model;
import rinde.sim.problem.common.AddVehicleEvent;
import rinde.sim.problem.common.StatsTracker.StatisticsDTO;
import rinde.sim.problem.gendreau06.Gendreau06ObjectiveFunction;
import rinde.solver.pdptw.SingleVehicleSolverAdapter;
import rinde.solver.pdptw.SolverDebugger;
import rinde.solver.pdptw.SolverValidator;
import rinde.solver.pdptw.single.MipSolver;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class Experiments {

    enum ExperimentClass {
        /**
         * _240_24
         */
        SHORT_LOW_FREQ(240, 24, 10),

        /**
         * _240_33
         */
        SHORT_HIGH_FREQ(240, 33, 10),

        /**
         * _450_24
         */
        LONG_LOW_FREQ(450, 24, 20);

        public final String fileId;
        public final int duration;
        public final int frequency;
        public final int vehicles;

        private ExperimentClass(int d, int f, int v) {
            duration = d;
            frequency = f;
            vehicles = v;
            fileId = "_" + duration + "_" + frequency;
        }
    }

    public static void main(String[] args) {

        experiments(123, 1, new RandomSolver());
    }

    static void experiments(long masterSeed, int repetitions,
            Configurator... configurators) {
        for (final Configurator c : configurators) {
            fullExperiment(c, masterSeed, repetitions, ExperimentClass.values());
        }
    }

    static void fullExperiment(Configurator c, long masterSeed,
            int repetitions, ExperimentClass... claz) {
        for (final ExperimentClass ec : claz) {
            fullExperiment(c, masterSeed, repetitions, ec);
        }
    }

    static void fullExperiment(Configurator c, long masterSeed,
            int repetitions, ExperimentClass claz) {
        final List<String> files = ExperimentUtil
                .getFilesFromDir("files/scenarios/gendreau06/", claz.fileId);
        final RandomGenerator rng = new MersenneTwister(masterSeed);
        final long[] seeds = new long[repetitions];
        for (int i = 0; i < repetitions; i++) {
            seeds[i] = rng.nextLong();
        }
        final StringBuilder sb = new StringBuilder();
        sb.append("seed,instance,duration,frequency,cost,tardiness,travelTime,overTime\n");
        for (final String file : files) {
            for (int i = 0; i < repetitions; i++) {
                if (c instanceof RandomSeed) {
                    ((RandomSeed) c).setSeed(seeds[i]);
                }
                final StatisticsDTO stats = GSimulation
                        .simulate(file, claz.vehicles, c, false);
                final Gendreau06ObjectiveFunction obj = new Gendreau06ObjectiveFunction();
                checkState(obj.isValidResult(stats));

                // example: req_rapide_1_240_24
                final String[] name = new File(file).getName().split("_");

                final int instanceNumber = Integer.parseInt(name[2]);
                final int duration = Integer.parseInt(name[3]);
                final int frequency = Integer.parseInt(name[4]);
                checkArgument(duration == claz.duration);
                checkArgument(frequency == claz.frequency);

                sb.append(seeds[i]).append(",")/* seed */
                .append(instanceNumber).append(",")/* instance */
                .append(duration).append(",") /* duration */
                .append(frequency).append(",")/* frequency */
                .append(obj.computeCost(stats)).append(',')/* cost */
                .append(obj.tardiness(stats)).append(',')/* tardiness */
                .append(obj.travelTime(stats)).append(',')/* travelTime */
                .append(obj.overTime(stats))/* overTime */
                .append("\n");
            }
        }
        final File dir = new File("files/results/gendreau" + claz.fileId);
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdir();
        }
        final File file = new File(dir.getPath() + "/"
                + c.getClass().getSimpleName() + "_" + masterSeed + claz.fileId
                + ".txt");
        if (file.exists()) {
            file.delete();
        }

        try {
            Files.write(sb.toString(), file, Charsets.UTF_8);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    interface RandomSeed {
        void setSeed(long seed);
    }

    // random auctioneer with solver route planner
    public static class RandomSolver implements Configurator, RandomSeed {

        protected final RandomGenerator rng;

        public RandomSolver() {
            rng = new MersenneTwister(0L);
        }

        public boolean create(Simulator sim, AddVehicleEvent event) {
            final Communicator c = new RandomBidder(rng.nextLong());
            sim.register(c);
            return sim.register(new Truck(event.vehicleDTO,
                    new SolverRoutePlanner(new SingleVehicleSolverAdapter(
                            SolverDebugger.wrap(SolverValidator
                                    .wrap(new MipSolver())), SI.SECOND)), c));
        }

        public void setSeed(long seed) {
            rng.setSeed(seed);
        }

        public Model<?>[] createModels() {
            return new Model<?>[] { new AuctionCommModel() };
        }

    }

    /**
     * <ul>
     * <li>RandomRoutePlanner</li>
     * <li>AuctionCommModel</li>
     * <li>RandomBidder</li>
     * </ul>
     * 
     * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
     */
    public static class RandomRandom implements Configurator, RandomSeed {
        protected final RandomGenerator rng;

        public RandomRandom() {
            rng = new MersenneTwister(0L);
        }

        public boolean create(Simulator sim, AddVehicleEvent event) {
            final Communicator c = new RandomBidder(rng.nextLong());
            sim.register(c);
            return sim.register(new Truck(event.vehicleDTO,
                    new RandomRoutePlanner(rng.nextLong()), c));
        }

        public Model<?>[] createModels() {
            return new Model<?>[] { new AuctionCommModel() };
        }

        public void setSeed(long seed) {
            rng.setSeed(seed);
        }

    }

    public static class RandomBB implements Configurator, RandomSeed {
        protected final RandomGenerator rng;

        public RandomBB() {
            rng = new MersenneTwister(0L);
        }

        public boolean create(Simulator sim, AddVehicleEvent event) {
            final Communicator c = new BlackboardUser();
            sim.register(c);
            return sim.register(new Truck(event.vehicleDTO,
                    new RandomRoutePlanner(rng.nextLong()), c));
        }

        public Model<?>[] createModels() {
            return new Model<?>[] { new BlackboardCommModel() };
        }

        public void setSeed(long seed) {
            rng.setSeed(seed);
        }
    }

}
