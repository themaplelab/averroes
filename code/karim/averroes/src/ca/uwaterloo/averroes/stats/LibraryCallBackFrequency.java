package ca.uwaterloo.averroes.stats;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;

public class LibraryCallBackFrequency {
	
	private Map<String, Map<String, String>> frequency; // maps method name to its frequency in all benchmarks
	
	public LibraryCallBackFrequency() {
		frequency = new HashMap<String, Map<String,String>>();
	}
	
	public void put(String name, String benchmark, String count) {
		if(frequency.containsKey(name)) {
			Map<String, String> freq = frequency.get(name);
			freq.put(benchmark, count);
		} else {
			Map<String, String> freq = defaultFrequencyMap();
			freq.put(benchmark, count);
			frequency.put(name, freq);
		}
	}
	
	public static Map<String, String> defaultFrequencyMap() {
		Map<String, String> result = new LinkedHashMap<String, String>();
		for(String benchmark : AverroesStatisticsGenerator.benchmarks) {
			String program = benchmark.substring(benchmark.indexOf('/') + 1);
			result.put(program, "");
		}
		return result;
	}

	public Map<String, Map<String, String>> frequency() {
		return frequency;
	}
	
	public String[] toArray(String name) {
		Map<String, String> freq = frequency.get(name);
		ArrayList<String> result = new ArrayList<String>();
		result.add(name);
		for(String benchmark : freq.keySet()) {
			result.add(freq.get(benchmark));
		}
		return result.toArray(new String[1]);
	}
}
