package ca.uwaterloo.averroes.stats;

import java.util.ArrayList;

public class ReachableMethodsComparison {

	private ArrayList<Integer> dynCgc;
	private ArrayList<Integer> dyn_SparkAverroes;
	private ArrayList<Integer> dyn_DoopAverroes;
	private ArrayList<Integer> dynDoop;
	private ArrayList<Integer> dynSpark;
	private ArrayList<Integer> cgcDoop;
	private ArrayList<Integer> doopAverroes_Doop;
	private ArrayList<Integer> doop_DoopAverroes;
	private ArrayList<Integer> cgcSpark;
	private ArrayList<Integer> sparkAverroes_Spark;
	private ArrayList<Integer> spark_SparkAverroes;
	private ArrayList<Integer> doopAverroes_Cgc;
	private ArrayList<Integer> cgc_DoopAverroes;

	public ReachableMethodsComparison() {
		dynCgc = new ArrayList<Integer>();
		dyn_SparkAverroes = new ArrayList<Integer>();
		dyn_DoopAverroes = new ArrayList<Integer>();
		dynDoop = new ArrayList<Integer>();
		dynSpark = new ArrayList<Integer>();
		cgcDoop = new ArrayList<Integer>();
		doopAverroes_Doop = new ArrayList<Integer>();
		doop_DoopAverroes = new ArrayList<Integer>();
		cgcSpark = new ArrayList<Integer>();
		sparkAverroes_Spark = new ArrayList<Integer>();
		spark_SparkAverroes = new ArrayList<Integer>();
		doopAverroes_Cgc = new ArrayList<Integer>();
		cgc_DoopAverroes = new ArrayList<Integer>();
	}

	public ArrayList<Integer> dynCgc() {
		return dynCgc;
	}

	public ArrayList<Integer> dyn_SparkAverroes() {
		return dyn_SparkAverroes;
	}

	public ArrayList<Integer> dyn_DoopAverroes() {
		return dyn_DoopAverroes;
	}

	public ArrayList<Integer> dynDoop() {
		return dynDoop;
	}

	public ArrayList<Integer> dynSpark() {
		return dynSpark;
	}

	public ArrayList<Integer> cgcDoop() {
		return cgcDoop;
	}

	public ArrayList<Integer> doopAverroes_Doop() {
		return doopAverroes_Doop;
	}
	
	public ArrayList<Integer> doop_DoopAverroes() {
		return doop_DoopAverroes;
	}

	public ArrayList<Integer> cgcSpark() {
		return cgcSpark;
	}

	public ArrayList<Integer> sparkAverroes_Spark() {
		return sparkAverroes_Spark;
	}
	
	public ArrayList<Integer> spark_SparkAverroes() {
		return spark_SparkAverroes;
	}
	
	public ArrayList<Integer> doopAverroes_Cgc() {
		return doopAverroes_Cgc;
	}
	
	public ArrayList<Integer> cgc_DoopAverroes() {
		return cgc_DoopAverroes;
	}
}