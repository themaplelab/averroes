package ca.uwaterloo.averroes.stats;

import java.util.ArrayList;

public class CallEdgeComparison {

	private ArrayList<String> dynCgc;
	private ArrayList<String> dyn_SparkAverroes;
	private ArrayList<String> dyn_DoopAverroes;
	private ArrayList<String> dynDoop;
	private ArrayList<String> dynSpark;
	private ArrayList<String> cgcDoop;
	private ArrayList<String> doopAverroes_Doop;
	private ArrayList<String> doop_DoopAverroes;
	private ArrayList<String> cgcSpark;
	private ArrayList<String> sparkAverroes_Spark;
	private ArrayList<String> spark_SparkAverroes;
	private ArrayList<String> doopAverroes_Cgc;
	private ArrayList<String> cgc_DoopAverroes;

	public CallEdgeComparison() {
		dynCgc = new ArrayList<String>();
		dyn_SparkAverroes = new ArrayList<String>();
		dyn_DoopAverroes = new ArrayList<String>();
		dynDoop = new ArrayList<String>();
		dynSpark = new ArrayList<String>();
		cgcDoop = new ArrayList<String>();
		doopAverroes_Doop = new ArrayList<String>();
		doop_DoopAverroes = new ArrayList<String>();
		cgcSpark = new ArrayList<String>();
		sparkAverroes_Spark = new ArrayList<String>();
		spark_SparkAverroes = new ArrayList<String>();
		doopAverroes_Cgc = new ArrayList<String>();
		cgc_DoopAverroes = new ArrayList<String>();
	}

	public ArrayList<String> dynCgc() {
		return dynCgc;
	}

	public ArrayList<String> dyn_SparkAverroes() {
		return dyn_SparkAverroes;
	}

	public ArrayList<String> dyn_DoopAverroes() {
		return dyn_DoopAverroes;
	}

	public ArrayList<String> dynDoop() {
		return dynDoop;
	}

	public ArrayList<String> dynSpark() {
		return dynSpark;
	}

	public ArrayList<String> cgcDoop() {
		return cgcDoop;
	}

	public ArrayList<String> doopAverroes_Doop() {
		return doopAverroes_Doop;
	}
	
	public ArrayList<String> doop_DoopAverroes() {
		return doop_DoopAverroes;
	}

	public ArrayList<String> cgcSpark() {
		return cgcSpark;
	}

	public ArrayList<String> sparkAverroes_Spark() {
		return sparkAverroes_Spark;
	}
	
	public ArrayList<String> spark_SparkAverroes() {
		return spark_SparkAverroes;
	}
	
	public ArrayList<String> doopAverroes_Cgc() {
		return doopAverroes_Cgc;
	}
	
	public ArrayList<String> cgc_DoopAverroes() {
		return cgc_DoopAverroes;
	}
}