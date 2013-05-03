package ca.uwaterloo.averroes.stats;

import java.util.ArrayList;

public class CallEdgeCount {
	
	private ArrayList<String> cgc;
	private ArrayList<String> sparkAverroes;
	private ArrayList<String> doopAverroes;
	private ArrayList<String> doop;
	private ArrayList<String> spark;
	private ArrayList<String> dynamic;
	
	public CallEdgeCount() {
		cgc = new ArrayList<String>();
		sparkAverroes = new ArrayList<String>();
		doopAverroes = new ArrayList<String>();
		doop = new ArrayList<String>();
		spark = new ArrayList<String>();
		dynamic = new ArrayList<String>();
	}
	
	public ArrayList<String> cgc() {
		return cgc;
	}
	
	public ArrayList<String> sparkAverroes() {
		return sparkAverroes;
	}
	
	public ArrayList<String> doopAverroes() {
		return doopAverroes;
	}

	public ArrayList<String> doop() {
		return doop;
	}

	public ArrayList<String> spark() {
		return spark;
	}

	public ArrayList<String> dynamic() {
		return dynamic;
	}
}