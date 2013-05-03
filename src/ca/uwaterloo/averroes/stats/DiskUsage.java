package ca.uwaterloo.averroes.stats;

import java.util.ArrayList;

import ca.uwaterloo.averroes.util.MathUtils;

public class DiskUsage {

	public ArrayList<String> cgc;
	public ArrayList<String> doop;
	public ArrayList<String> spark;

	public ArrayList<String> doopAverroes;
	public ArrayList<String> sparkAverroes;

	public ArrayList<String> inputApplicationClasses;
	public ArrayList<String> inputApplicationClassesSize;

	public ArrayList<String> originalLibraryClasses;
	public ArrayList<String> inputLibraryClasses;
	public ArrayList<String> inputLibraryClassesSize;

	public ArrayList<String> finalLibraryClassesSize;

	public ArrayList<String> inputLibraryMethods;
	public ArrayList<String> inputLibraryFields;

	public ArrayList<String> referencedLibraryMethods;
	public ArrayList<String> referencedLibraryFields;

	public ArrayList<String> removedLibraryMethods;
	public ArrayList<String> removedLibraryFields;
	
	public ArrayList<String> libraryMethods;
	public ArrayList<String> libraryFields;
	
	public ArrayList<String> generatedLibraryClasses;
	public ArrayList<String> generatedLibraryMethods;

	public DiskUsage() {
		cgc = new ArrayList<String>();
		doop = new ArrayList<String>();
		spark = new ArrayList<String>();

		doopAverroes = new ArrayList<String>();
		sparkAverroes = new ArrayList<String>();

		inputApplicationClasses = new ArrayList<String>();
		inputApplicationClassesSize = new ArrayList<String>();

		originalLibraryClasses = new ArrayList<String>();
		inputLibraryClasses = new ArrayList<String>();
		inputLibraryClassesSize = new ArrayList<String>();

		finalLibraryClassesSize = new ArrayList<String>();

		inputLibraryMethods = new ArrayList<String>();
		inputLibraryFields = new ArrayList<String>();

		referencedLibraryMethods = new ArrayList<String>();
		referencedLibraryFields = new ArrayList<String>();

		removedLibraryMethods = new ArrayList<String>();
		removedLibraryFields = new ArrayList<String>();
		
		libraryMethods = new ArrayList<String>();
		libraryFields = new ArrayList<String>();
		
		generatedLibraryClasses = new ArrayList<String>();
		generatedLibraryMethods = new ArrayList<String>();
	}

	// Size is in B
	public static String sizeInMB(String size) {
		double result = MathUtils.round(Double.parseDouble(size) / 1024 / 1024, 2);
		return String.valueOf(result);
	}

	// Size is in KB
	public static String sizeInMB(double size) {
		double result = MathUtils.round(size / 1024, 2);
		return String.valueOf(result);
	}
}