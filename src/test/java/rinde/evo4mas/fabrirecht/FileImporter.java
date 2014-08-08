/**
 * 
 */
package rinde.evo4mas.fabrirecht;

import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import rinde.sim.core.pdptw.AddParcelEvent;
import rinde.sim.pdptw.fabrirecht.FabriRechtParser;
import rinde.sim.pdptw.fabrirecht.FabriRechtScenario;
import rinde.sim.scenario.ScenarioBuilder;
import rinde.sim.scenario.TimedEvent;

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
				public boolean accept(File ddd, String name) {
					return name.endsWith("csv") && !name.contains("coord");
				}
			});

			for (final File csv : csvs) {
				final String name = csv.getName().substring(0, csv.getName().length() - 4);
				final String p = csv.getParent() + "/" + name + "_coord.csv";

				final FabriRechtScenario scen = FabriRechtParser.parse(p, csv.getAbsolutePath());
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
					System.out.println(name + " " + nop + " / " + scen.asList().size() * 2);
					faultyScenarios++;
				}

				if (apes == 0) {
					System.err.println(name + " is empty");
				} else {
					checkState(ScenarioBuilder.isTimeOrderingConsistent(scen), "bummer");
					FabriRechtParser.toJson(scen, new FileWriter(d.getAbsolutePath() + "/" + name + ".scenario"));

					final FabriRechtScenario scen2 = FabriRechtParser.fromJson(
					    Files.toString(new File(d.getAbsolutePath() + "/"
              + name + ".scenario"), Charsets.UTF_8));

					checkState(ScenarioBuilder.isTimeOrderingConsistent(scen2), "bummer2");
					checkState(scen.equals(scen2));
				}
			}
		}

		System.out.println(notPossible + " / " + totalTimeWindows + "     " + faultyScenarios + " / " + scenarios);

	}
}
