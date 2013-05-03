package ca.uwaterloo.averroes.stats;

import java.util.ArrayList;

public class ExecutionTime {
	
	public ArrayList<String> cgc;
	public ArrayList<String> doop;
	public ArrayList<String> spark;
	public ArrayList<String> doopTotal;
	public ArrayList<String> sparkTotal;
	
	public ArrayList<String> sparkAverroes;
	public ArrayList<String> doopAverroes;
	public ArrayList<String> sparkAverroesTotal;
	public ArrayList<String> doopAverroesTotal;
	
	public ArrayList<String> sootLoading;
	public ArrayList<String> jarFactory;
	public ArrayList<String> jarVerification;
	public ArrayList<String> jarFactoryTotal;
	
	public ArrayList<String> organizer;
	
	public ExecutionTime() {
		cgc = new ArrayList<String>();
		doop = new ArrayList<String>();
		spark = new ArrayList<String>();
		doopTotal = new ArrayList<String>();
		sparkTotal = new ArrayList<String>();
		
		sparkAverroes = new ArrayList<String>();
		doopAverroes = new ArrayList<String>();
		sparkAverroesTotal = new ArrayList<String>();
		doopAverroesTotal = new ArrayList<String>();
		
		organizer = new ArrayList<String>();
		
		sootLoading = new ArrayList<String>();
		jarFactory = new ArrayList<String>();
		jarVerification = new ArrayList<String>();
		jarFactoryTotal = new ArrayList<String>();
	}
}