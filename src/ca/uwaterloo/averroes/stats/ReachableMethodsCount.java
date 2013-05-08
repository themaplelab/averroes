package ca.uwaterloo.averroes.stats;

import java.util.ArrayList;

public class ReachableMethodsCount {
	
	private ArrayList<Integer> cgc;
	private ArrayList<Integer> sparkAverroes;
	private ArrayList<Integer> doopAverroes;
	private ArrayList<Integer> doop;
	private ArrayList<Integer> spark;
	private ArrayList<Integer> dynamic;
	
	public ReachableMethodsCount() {
		cgc = new ArrayList<Integer>();
		sparkAverroes = new ArrayList<Integer>();
		doopAverroes = new ArrayList<Integer>();
		doop = new ArrayList<Integer>();
		spark = new ArrayList<Integer>();
		dynamic = new ArrayList<Integer>();
	}
	
	public ArrayList<Integer> cgc() {
		return cgc;
	}
	
	public ArrayList<Integer> sparkAverroes() {
		return sparkAverroes;
	}
	
	public ArrayList<Integer> doopAverroes() {
		return doopAverroes;
	}

	public ArrayList<Integer> doop() {
		return doop;
	}

	public ArrayList<Integer> spark() {
		return spark;
	}

	public ArrayList<Integer> dynamic() {
		return dynamic;
	}
}