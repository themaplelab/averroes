package ca.uwaterloo.averroes.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.basics.BasicFactory;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.simple.SimpleRelationFactory;
import org.deri.iris.terms.TermFactory;

/**
 * A utility class that imports results from Doop into IRIS format that can be read by our converters.
 * 
 * @author karim
 * 
 */
public class ResultImporter {

	/**
	 * Retrieve a file from Doop and convert it to an IRIS relation.
	 * 
	 * @param resultsFileName
	 * @return
	 * @throws IOException
	 */
	public static IRelation retrieve(String resultsFileName) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(resultsFileName));
		String predicate = "";
		String line;
		int size = 0;
		IRelation relation = new SimpleRelationFactory().createRelation();

		// First consume the first 10 lines, they're info about the predicate
		while ((line = in.readLine()) != null) {
			// Capture the size of the result set
			if (line.trim().startsWith("predicate:")) {
				predicate = line.substring(line.indexOf(" ") + 1, line.indexOf('('));
			} else if (line.trim().startsWith("size:")) {
				size = Integer.parseInt(line.trim().replace("size: ", ""));
			} else if (line.equals("/--- start of " + predicate + " facts ---\\")) {
				break;
			}
		}

		// Now loop for the size of the result set, gather the input
		for (int i = 0; i < size; i++) {
			line = in.readLine().trim();
			relation.add(lineToTuple(line));
		}

		in.close();
		return relation;
	}

	/**
	 * Get the insensitive call graph edges from Doop.
	 * 
	 * @param doopHome
	 * @return
	 * @throws IOException
	 */
	public static IRelation getDoopCallGraphEdges(String doopHome) throws IOException {
		return retrieve(doopHome.concat(File.separator).concat("InsensCallGraphEdge.results"));
	}

	/**
	 * Get the reflective call graph edges from Doop.
	 * 
	 * @param doopHome
	 * @return
	 * @throws IOException
	 */
	public static IRelation getReflectiveCallGraphEdges(String doopHome) throws IOException {
		return retrieve(doopHome.concat(File.separator).concat("ReflectiveCallGraphEdge.results"));
	}

	/**
	 * Get the entry points from Doop.
	 * 
	 * @param doopHome
	 * @return
	 * @throws IOException
	 */
	public static IRelation getDoopEntryPoints(String doopHome) throws IOException {
		return retrieve(doopHome.concat(File.separator).concat("MainMethodDeclaration.results"));
	}

	/**
	 * Get the LibraryPointsTo set from Doop.
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static IRelation getLibraryPointsTo(String fileName) throws IOException {
		return retrieve(fileName);
	}

	/**
	 * A helper function that converts a line from Doop to an IRIS tuple.
	 * 
	 * @param line
	 * @return
	 */
	public static ITuple lineToTuple(String line) {
		String regex = "((?!\\[(\\d+?)\\]).)+";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(line);
		List<ITerm> terms = new ArrayList<ITerm>();

		while (matcher.find()) {
			String s = matcher.group().trim();
			int start = s.indexOf(']') + 1;
			int end = s.length();

			if (s.endsWith(",")) {
				end--;
			}

			terms.add(TermFactory.getInstance().createString(s.substring(start, end)));
		}

		return BasicFactory.getInstance().createTuple(terms);
	}
}