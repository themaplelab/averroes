package ca.uwaterloo.averroes.callgraph;

public enum CallGraphSource {
	DUMMY("dummy"),
	CGC("cgc"),
	DOOP("doop"),
	SPARK("spark"),
	WALA("wala"),
	DYNAMIC("dynamic"),
	AVERROES("averroes"),
	SPARK_AVERROES("sparkAverroes"),
	WALA_AVERROES("walaAverroes"),
	DOOP_AVERROES("doopAverroes");
	
	private String source;
	
	private CallGraphSource(String source) {
		this.source = source;
	}
	
	public String source() {
		return source;
	}

}
