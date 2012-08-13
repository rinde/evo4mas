package rinde.evo4mas.evo.gp;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Maps.newHashMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.HashMultimap;

import ec.gp.GPNode;

/**
 * @author Rinde van Lon (rinde.vanlon@cs.kuleuven.be)
 * 
 */
public class GPProgramParser {

	public static void parse() {
		// (foreach (nearbypacks) (addtoplan (foreachpackref)))

		final String input = "(foreach (nearbypacks) (addtoplan (foreachpackref)))";

		// Pattern pat = Pattern.compile("\\(([a-z-\\(\\)\\s]*)\\)$");
		final Pattern pat = Pattern.compile("\\(([a-z-]*)");

		// input.

		// System.out.println(input.matches("\\(([a-z-\\(\\)\\s]*)\\)$"));

		final String newInput = input;
		// while (true) {
		final Matcher m = pat.matcher(newInput);
		if (m.matches()) {

			for (int i = 1; i <= m.groupCount(); i++) {
				System.out.println(m.group(i));
			}
		}

		System.out.println(input);
		final Node n = new Node("super");
		System.out.println(parseProgram(input, 0, n));
		System.out.println(n.children.get(0));
		// }
	}

	public static String fixBraces(String program) {

		final StringBuilder sb = new StringBuilder();

		boolean addedBrace = false;
		for (int i = 0; i < program.length(); i++) {
			final char cur = program.charAt(i);
			final boolean prevIsLetterOrBrace = i > 0
					&& (program.charAt(i - 1) == '(' || isAllowableFuncChar(program.charAt(i - 1)));
			final boolean isLetter = isAllowableFuncChar(cur);

			if (isLetter && !prevIsLetterOrBrace) {
				sb.append("(");
				addedBrace = true;
			} else if (!isLetter && addedBrace) {
				addedBrace = false;
				sb.append(")");
			}
			sb.append(cur);
		}
		return sb.toString();
	}

	static boolean isAllowableFuncChar(char ch) {
		return Character.isLetter(ch) || Character.isDigit(ch) || ch == '.';
	}

	public static <T> GPProgram<T> parseProgram(String program, Collection<GPFunc<T>> functions) {
		final Map<String, GPFunc<T>> funcMap = newHashMap();
		for (final GPFunc<T> func : functions) {
			funcMap.put(func.name(), func);
		}

		final Node n = new Node("super");
		parseProgram(fixBraces(program), 0, n);
		return new GPProgram<T>(convert(n.children.get(0), funcMap));
	}

	// public static GPNode copy(GPNode node) {
	// final GPNode copy = new SuperNode(node.name(), node.children.length);
	// copy.children = new GPNode[node.children.length];
	// for (int i = 0; i < node.children.length; i++) {
	// copy.children[i] = copy(node.children[i]);
	// }
	// return copy;
	// }

	private static <T> GPFunc<T> convert(Node n, Map<String, GPFunc<T>> funcMap) {
		// final GPNode gpnode = new GPFunc(n.name, n.children.size());
		final GPFunc<T> func = funcMap.get(n.name).create();
		func.children = new GPNode[n.children.size()];
		checkState(n.children.size() == func.getNumChildren(), "the supplied program is invalid, the number of children does not match the expected number of children");
		for (int i = 0; i < n.children.size(); i++) {
			func.children[i] = convert(n.children.get(i), funcMap);
		}
		return func;
	}

	static class Node {

		String name;
		List<Node> children;

		public Node(String name) {
			this.name = name;
			children = new ArrayList<Node>();
		}

		void addChild(Node n) {
			children.add(n);
		}

		@Override
		public String toString() {
			if (children.isEmpty()) {
				return "(" + name + ")";
			}
			return "(" + name + " " + children.toString().replace("[", "").replace("]", "").replace(",", "").trim()
					+ ")";
		}
	}

	private static int parseProgram(String string, int index, Node n) {
		boolean funcStart = false;
		final StringBuilder funcName = new StringBuilder();
		Node current = null;
		for (int i = index; i < string.length(); i++) {
			if (string.charAt(i) == '(' && !funcStart) {
				funcStart = true;
			} else if (funcStart) {
				if (current == null && (string.charAt(i) == '(' || string.charAt(i) == ')' || string.charAt(i) == ' ')) {
					current = new Node(funcName.toString());
					n.addChild(current);
				}

				if (string.charAt(i) == '(') {
					// new function, will be argument of parent
					i = parseProgram(string, i, current);
				} else if (string.charAt(i) == ')') {
					// end of function definition
					return i;
				} else {
					funcName.append(string.charAt(i));
				}
			}

		}
		return -1;
	}

	// public static void main(String[] args) throws IOException {
	// // parseGrammar("files/grammars/pdp2.grammar");
	//
	// // final String res =
	// //
	// fixBraces("(multiply (divide four two) (min (min (multiply (if3 four distance (max (multiply ten (multiply ten one)) (invert (invert four)))) (min (invert (multiply (min (max timeleft (invert distance)) (multiply (invert distance) (if3 four distance one))) (plus one (multiply (invert distance) (multiply (max (multiply four (max (multiply ten distance) (multiply (divide (divide one timeleft) two) four))) timeleft) one))))) (multiply two (if3 timeleft (invert distance) one)))) (divide (invert (multiply four (multiply ten one))) (multiply (max (multiply ten (multiply ten one)) (multiply (if3 timeleft ten one) four)) (multiply (if3 four distance (max ten four)) (if3 four nearbypackages ten))))) (divide (multiply (if3 timeleft ten (invert distance)) four) (divide (invert (multiply ten four)) (multiply (max (multiply (multiply ten (max (max (multiply ten (multiply ten one)) (divide one timeleft)) (divide (divide (divide (multiply ten distance) (max (multiply ten distance) zero)) (multiply ten four)) (multiply (divide four two) four)))) (if3 distance four (multiply (max one (multiply timeleft (divide four two))) (multiply (min (multiply (multiply ten distance) (if3 (invert (invert four)) nearbypackages ten)) distance) (if3 four nearbypackages ten))))) (multiply timeleft timeleft)) (multiply (min nearbypackages distance) (if3 four nearbypackages ten)))))))");
	//
	// final BufferedReader gp101 = new BufferedReader(new FileReader(
	// "/Users/rindevanlon/Desktop/graph/gp101-2012-03-06-13-57-11/best-at-generation-100.txt"));
	//
	// final GPNode program101 = parseProgram(gp101.readLine());
	// gp101.close();
	//
	// final BufferedReader gp51 = new BufferedReader(new FileReader(
	// "/Users/rindevanlon/Desktop/graph/gp51-2012-03-05-22-07-21/best-at-generation-050.txt"));
	//
	// final GPNode program51 = parseProgram(gp51.readLine());
	// gp51.close();
	//
	// System.out.println("gp51: " + countNodes(program51));
	// System.out.println("gp101: " + countNodes(program101));
	//
	// // final BufferedWriter w = new BufferedWriter(new FileWriter(
	// //
	// "/Users/rindevanlon/Desktop/graph/gp101-2012-03-06-13-57-11/best-at-generation-100.dot"));
	// // final String tree = program.makeGraphvizTree();
	// //
	// // w.write(tree);
	// // w.close();
	//
	// // System.out.println(tree);
	//
	// }

	static int countNodes(GPNode root) {
		int num = root.children.length;
		for (int i = 0; i < root.children.length; i++) {
			num += countNodes(root.children[i]);
		}
		return num;
	}

	// public static GPNode generateProgram(HashMultimap<String, List<String>>
	// grammar, RandomGenerator rnd) {
	// // for (Entry<String, List<String>> entry : grammar.entries()) {
	// // System.out.println(entry);
	// // }
	// final List<List<String>> options = new
	// ArrayList<List<String>>(grammar.get("<start>"));
	// return generateProgram(grammar, rnd,
	// options.get(rnd.nextInt(options.size())));
	// }

	// private static GPNode generateProgram(HashMultimap<String, List<String>>
	// grammar, RandomGenerator rnd,
	// List<String> current) {
	// // System.out.println(current);
	// GPNode root = null;
	// final List<GPNode> children = new ArrayList<GPNode>();
	// for (int i = 0; i < current.size(); i++) {
	// final String s = current.get(i);
	// // check if it is a function
	// if (i == 0 && !s.contains("<")) {
	// root = new SuperNode(s, current.size() - 1);
	// } else if (s.contains("<")) {
	// final List<List<String>> options = new
	// ArrayList<List<String>>(grammar.get(s));
	// children.add(generateProgram(grammar, rnd,
	// options.get(rnd.nextInt(options.size()))));
	// } else {
	// children.add(new SuperNode(s));
	// }
	// }
	//
	// if (root == null) {
	// assert children.size() == 1;
	// return children.get(0);
	// }
	// assert children.size() == current.size() - 1 : children;
	// root.children = children.toArray(new SuperNode[children.size()]);
	// return root;
	// }

	public static HashMultimap<String, List<String>> parseGrammar(String grammarFile) {
		try {
			final BufferedReader reader = new BufferedReader(new FileReader(grammarFile));

			final HashMultimap<String, List<String>> grammar = HashMultimap.create();

			final String node = "\\s*<([a-z-]+)>\\s*";
			// String pipe = "\\|";
			final String func = "\\(\\w+(?:" + node + ")*\\)";

			// Pattern pattern = Pattern.compile(node + "::=(?:(?:" + node +
			// ")*|\\|)+");
			final Pattern nodePattern = Pattern.compile(node);
			// Pattern funcPattern = Pattern.compile(func);
			final Pattern combPattern = Pattern.compile("(?:" + node + "|" + func + ")");

			String line;
			int lineNr = 0;
			final List<String> functions = new ArrayList<String>();
			while ((line = reader.readLine()) != null) {
				lineNr++;
				line = line.replace(" ", "");
				if (line.startsWith("#")) {
					continue;
				}
				final Scanner s = new Scanner(line);
				s.useDelimiter("::=");

				if (!s.hasNext(nodePattern)) {
					throw new IllegalArgumentException("Not a valid construct at line: " + lineNr);
				}
				final String key = s.next(nodePattern);
				final Scanner sub = new Scanner(s.next());
				sub.useDelimiter("\\|");
				while (sub.hasNext()) {
					final String cur = sub.next();
					final Scanner rewrite = new Scanner(cur);
					final List<String> list = new ArrayList<String>();
					while (true) {
						final String found = rewrite.findInLine(combPattern);
						if (found == null) {
							break;
						} else if (found.matches(func)) {
							final String function = found.replace("(", "").replace(")", "");
							final Scanner funcScan = new Scanner(function);

							final String funcName = funcScan.findInLine("\\w*");
							if (funcName == null) {
								throw new IllegalArgumentException("A function must have a name. In: " + cur);
							}
							list.add(funcName);
							functions.add(function);
							while (true) {
								final String param = funcScan.findInLine(nodePattern);
								if (param != null) {
									list.add(param);
								} else {
									break;
								}
							}
						} else {
							list.add(found);
						}
					}
					grammar.put(key, list);
					// if () {
					// System.out.println(sub.next(nodePattern));
					// } else {
					// System.err.println(sub.next());
					// }

				}
			}
			return grammar;
		} catch (final Throwable t) {
			throw new RuntimeException(t);
		}

		// System.out.println("GRAMMAR");
		// for (Entry<String, List<String>> entry : grammar.entries()) {
		// System.out.println(entry);
		// }
		// System.out.println("FUNCTIONS:");
		// for (String s : functions) {
		// System.out.println(s);
		// }
		// System.out.println("--------");
		// for (Entry<String, List<String>> entry : grammar.entries()) {
		// if (entry.getValue().contains("<pack-expr>")) {
		// System.out.println(entry.getValue().toString().replace(",",
		// "").replace("[", "(").replace("]", ")"));
		// }
		// }

		/*
		 * String line; while ((line = reader.readLine()) != null) { line =
		 * line.replace(" ", ""); System.out.println(line); String[] parts =
		 * line.split("::="); if (!nodePattern.matcher(parts[0]).matches()) {
		 * throw new IllegalArgumentException("Invalid node: " + parts[0]); }
		 * String key = parts[0]; if (parts[1].contains("|")) { String[]
		 * subnodes = parts[1].split("\\|"); for (String subnode : subnodes) {
		 * System.out.println(subnode); if
		 * (!combPattern.matcher(subnode).matches()) { throw new
		 * IllegalArgumentException("Invalid subnode: " + subnode); } Matcher m
		 * = combPattern.matcher(subnode); Scanner s; while (m.find()) { for
		 * (int i = 1; i <= m.groupCount(); i++) { System.out.println(i + " " +
		 * m.group(i)); } } // } else { // System.err.println("no match"); // }
		 * } } else { } }
		 */
	}
}
