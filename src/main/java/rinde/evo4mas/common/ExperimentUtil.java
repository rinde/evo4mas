/**
 * 
 */
package rinde.evo4mas.common;

import static com.google.common.collect.Lists.newArrayList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class ExperimentUtil {

	public static List<List<String>> createFolds(String dir, int n) {
		final String[] scens = new File(dir).list(new FilenameFilter() {
			public boolean accept(File d, String name) {
				return name.endsWith(".scenario");
			}
		});
		// sort on file name such that produced folds are always deterministic
		// and do not depend on filesystem ordering.
		Arrays.sort(scens);
		final List<List<String>> fs = newArrayList();
		for (int i = 0; i < n; i++) {
			fs.add(new ArrayList<String>());
		}
		for (int i = 0; i < scens.length; i++) {
			fs.get(i % n).add(dir + scens[i]);
		}
		return fs;
	}

	public static List<String> createTrainSet(List<List<String>> fds, int testFold) {
		final List<String> set = newArrayList();
		for (int i = 0; i < fds.size(); i++) {
			if (testFold != i) {
				set.addAll(fds.get(i));
			}
		}
		return set;
	}

	public static String textFileToString(String file) throws IOException {
		final StringBuilder sb = new StringBuilder();
		final BufferedReader bf = new BufferedReader(new FileReader(file));
		String line;
		while ((line = bf.readLine()) != null) {
			sb.append(line + "\n");
		}
		return sb.toString();

	}

}
