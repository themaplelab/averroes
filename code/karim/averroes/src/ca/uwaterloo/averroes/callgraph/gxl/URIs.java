package ca.uwaterloo.averroes.callgraph.gxl;

import java.net.URI;
import java.net.URISyntaxException;

public class URIs {
	final private String scheme = "http";
	final private String host = "plg.uwaterloo.ca";
	private String path = "/~karim/projects/cgc/schemas/callgraph.gxl";

	// Graphs
	private URI uCallGraph;

	// Nodes
	private URI uMethod;
	private URI uClass;
	private URI uRoot;
	private URI uLibrary;

	// Edges
	private URI declaredIn;
	private URI calls;
	private URI callsLibrary;
	private URI libraryCallsBack;
	private URI entryPoint;
	
	// Singleton
	private static URIs uris = null;

	private URIs() {
		try {
			uCallGraph = new URI(scheme, host, path, "CallGraph");

			uMethod = new URI(scheme, host, path, "Method");
			uClass = new URI(scheme, host, path, "Class");
			uRoot = new URI(scheme, host, path, "Root");
			uLibrary = new URI(scheme, host, path, "Library");

			declaredIn = new URI(scheme, host, path, "declaredIn");
			calls = new URI(scheme, host, path, "calls");
			callsLibrary = new URI(scheme, host, path, "callsLibrary");
			libraryCallsBack = new URI(scheme, host, path, "libraryCallsBack");
			entryPoint = new URI(scheme, host, path, "entryPoint");
		} catch (URISyntaxException e) {
			throw new RuntimeException("Caught URISyntaxException: " + e);
		}
	}
	
	// Singleton interface method
	public static URIs v() {
		if(uris == null) {
			uris = new URIs();
		}
		return uris;
	}

	public URI uCallGraph() {
		return uCallGraph;
	}

	public URI uMethod() {
		return uMethod;
	}

	public URI uClass() {
		return uClass;
	}

	public URI uRoot() {
		return uRoot;
	}

	public URI uLibrary() {
		return uLibrary;
	}

	public URI declaredIn() {
		return declaredIn;
	}

	public URI calls() {
		return calls;
	}

	public URI callsLibrary() {
		return callsLibrary;
	}

	public URI libraryCallsBack() {
		return libraryCallsBack;
	}

	public URI entryPoint() {
		return entryPoint;
	}
}