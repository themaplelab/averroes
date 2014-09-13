package ca.uwaterloo.averroes.callgraph.transformers;

import java.util.HashMap;
import java.util.Map;

import soot.PhaseOptions;


public class Transformer {
	
	public static Map<String, String> chaOptions() {
		Map<String, String> opts = new HashMap<String, String>(PhaseOptions.v().getPhaseOptions("cg.cha"));
		opts.put("enabled", "true");
		opts.put("verbose", "true");
		return opts;
	}

	public static Map<String, String> sparkOptions(boolean isAve) {
		Map<String, String> opts = new HashMap<String, String>(PhaseOptions.v().getPhaseOptions("cg.spark"));
		opts.put("enabled", "true");
		opts.put("verbose", "true");
		if(isAve) opts.put("simulate-natives", "false"); // this should only be false for SparkAve
		opts.put("force-gc", "true");
		// opts.put("ignore-types", "true");
		return opts;
	}
}
