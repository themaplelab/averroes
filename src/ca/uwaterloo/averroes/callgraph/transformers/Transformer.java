package ca.uwaterloo.averroes.callgraph.transformers;

import java.util.HashMap;
import java.util.Map;

import soot.PhaseOptions;

public enum Transformer {
	CHA("class hierarchy analysis", chaOptions()), SPARK("spark", sparkOptions());

	private String transformer;
	private Map<String, String> options;

	private Transformer(String format, Map<String, String> options) {
		this.transformer = format;
		this.options = options;
	}

	public String format() {
		return transformer;
	}

	public Map<String, String> options() {
		return options;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, String> chaOptions() {
		Map<String, String> opts = new HashMap<String, String>(PhaseOptions.v().getPhaseOptions("cg.cha"));
		opts.put("enabled", "true");
		opts.put("verbose", "true");
		return opts;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, String> sparkOptions() {
		Map<String, String> opts = new HashMap<String, String>(PhaseOptions.v().getPhaseOptions("cg.spark"));
		opts.put("enabled", "true");
		opts.put("verbose", "true");
		opts.put("simulate-natives", "false"); // TODO
		opts.put("force-gc", "true");
		// opts.put("ignore-types", "true");
		return opts;
	}
}
