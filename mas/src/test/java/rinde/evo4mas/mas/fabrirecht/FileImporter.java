/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

import rinde.sim.problem.fabrirecht.FabriRechtParser;

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
				FabriRechtParser.toJson(FabriRechtParser.parse(p, csv.getAbsolutePath()), new FileWriter(d
						.getAbsolutePath() + "/" + name + ".scenario"));
			}

		}

	}
}
