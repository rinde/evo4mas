/**
 * 
 */
package rinde.evo4mas.fabrirecht;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import rinde.sim.pdptw.fabrirecht.FabriRechtParser;
import rinde.sim.pdptw.fabrirecht.FabriRechtScenario;
import rinde.sim.scenario.AddParcelEvent;
import rinde.sim.scenario.Scenario;
import rinde.sim.scenario.TimedEvent;
import rinde.sim.scenario.TimedEvent.TimeComparator;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class FileImporter {

  /**
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    final String sourceDir = "/Users/rindevanlon/Downloads/fabri_recht/Instanzen/";
    final String targetDir = "files/scenarios/fabri-recht/";

    final File f = new File(sourceDir);
    final File[] dirs = f.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.isDirectory();
      }
    });

    int scenarios = 0;
    int faultyScenarios = 0;
    int notPossible = 0;
    int totalTimeWindows = 0;

    for (final File dir : dirs) {
      final File d = new File(targetDir + "/" + dir.getName());
      d.mkdir();

      final File[] csvs = dir.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File ddd, String name) {
          return name.endsWith("csv") && !name.contains("coord");
        }
      });

      for (final File csv : csvs) {
        final String name = csv.getName().substring(0,
            csv.getName().length() - 4);
        final String p = csv.getParent() + "/" + name + "_coord.csv";

        final FabriRechtScenario scen = FabriRechtParser.parse(p,
            csv.getAbsolutePath());
        scenarios++;
        boolean faultyScenario = false;
        int nop = 0;
        int apes = 0;
        for (final TimedEvent te : scen.asList()) {

          if (te instanceof AddParcelEvent) {
            apes++;
            final AddParcelEvent ape = (AddParcelEvent) te;
            totalTimeWindows += 2;

            if (ape.parcelDTO.deliveryTimeWindow.length() < ape.parcelDTO.deliveryDuration) {
              nop++;
              notPossible++;
              faultyScenario = true;
            }
            if (ape.parcelDTO.pickupTimeWindow.length() < ape.parcelDTO.pickupDuration) {
              nop++;
              notPossible++;
              faultyScenario = true;
            }
          }
        }
        if (faultyScenario) {
          System.out.println(name + " " + nop + " / " + scen.asList().size()
              * 2);
          faultyScenarios++;
        }

        if (apes == 0) {
          System.err.println(name + " is empty");
        } else {
          checkState(isTimeOrderingConsistent(scen), "bummer");
          FabriRechtParser.toJson(scen, new FileWriter(d.getAbsolutePath()
              + "/" + name + ".scenario"));

          final FabriRechtScenario scen2 = FabriRechtParser.fromJson(
              Files.toString(new File(d.getAbsolutePath() + "/"
                  + name + ".scenario"), Charsets.UTF_8));

          checkState(isTimeOrderingConsistent(scen2), "bummer2");
          checkState(scen.equals(scen2));
        }
      }
    }

    System.out.println(notPossible + " / " + totalTimeWindows + "     "
        + faultyScenarios + " / " + scenarios);

  }

  /**
   * Checks whether the specified scenario is time consistent, i.e. all events
   * should be sorted by time.
   * @param scen The scenario to check.
   * @return <code>true</code> if it is consistent, <code>false</code>
   *         otherwise.
   */
  public static boolean isTimeOrderingConsistent(Scenario scen) {
    final List<TimedEvent> es = newArrayList(scen.asList());
    Collections.sort(es, TimeComparator.INSTANCE);
    return scen.asList().equals(es);
  }
}
