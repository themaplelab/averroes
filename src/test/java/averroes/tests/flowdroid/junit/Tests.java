package averroes.tests.flowdroid.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import averroes.frameworks.analysis.RtaJimpleBody;
import averroes.frameworks.analysis.XtaJimpleBody;
import averroes.tests.CommonOptions;
import soot.jimple.infoflow.IInfoflow;
import soot.jimple.infoflow.Infoflow;
import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.config.ConfigForTest;
import soot.jimple.infoflow.config.IInfoflowConfig;
import soot.jimple.infoflow.data.pathBuilders.IPathBuilderFactory;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.taintWrappers.EasyTaintWrapper;

public class Tests {

	private static final String sinkPrintlnString = "<java.io.PrintStream: void println(java.lang.String)>";
	private static final String sinkPrintlnObject = "<java.io.PrintStream: void println(java.lang.Object)>";
	private static final List<String> sinkPrintln = Arrays.asList(sinkPrintlnString, sinkPrintlnObject);

	private static final String sourceExample = "<example.Client: java.lang.String secret()>";

	/**
	 * Run FlowDroid using the RTA model.
	 * 
	 * @param testCase
	 * @param entryPoint
	 * @param resultCount
	 * @param selectHandWrittenModel
	 */
	public static void runFlowDroidRta(String testCase, String entryPoint, int resultCount, boolean selectHandWrittenModel) {
		System.out.println("Testing FlowDroid on " + testCase + " hand-written RTA...");
		initializeSoot();
		IInfoflow infoflow = initInfoflow();	
		infoflow.computeInfoflow(CommonOptions.getApplicationCode(testCase),
				CommonOptions.getFrameworkModel(testCase, RtaJimpleBody.name, selectHandWrittenModel), entryPoint,
				Arrays.asList(sourceExample), sinkPrintln);
		checkInfoflow(infoflow, resultCount);
	}
	
	/**
	 * Run FlowDroid using the XTA model.
	 * 
	 * @param testCase
	 * @param entryPoint
	 * @param resultCount
	 * @param selecHandWrittenModel
	 */
	public static void runFlowDroidXta(String testCase, String entryPoint, int resultCount, boolean selecHandWrittenModel) {
		System.out.println("Testing FlowDroid on " + testCase + " XTA...");
		initializeSoot();
		IInfoflow infoflow = initInfoflow();
		infoflow.computeInfoflow(CommonOptions.getApplicationCode(testCase),
				CommonOptions.getFrameworkModel(testCase, XtaJimpleBody.name, selecHandWrittenModel), entryPoint,
				Arrays.asList(sourceExample), sinkPrintln);
		checkInfoflow(infoflow, resultCount);
	}
	
	/**
	 * Run FlowDroid using the Averroes model.
	 * 
	 * @param testCase
	 * @param entryPoint
	 * @param resultCount
	 * @param selectHandWrittenModel
	 */
	public static void runFlowDroidAverroes(String testCase, String entryPoint, int resultCount, boolean selectHandWrittenModel) {
		System.out.println("Testing FlowDroid on " + testCase + " Averroes...");
		initializeSoot();
		IInfoflow infoflow = initInfoflow();
		infoflow.computeInfoflow(CommonOptions.getApplicationCode(testCase) + File.pathSeparator + CommonOptions.getAverroesLibraryClass(testCase, selectHandWrittenModel),
				CommonOptions.getAverroesPlaceholderLib(testCase, selectHandWrittenModel), Arrays.asList(entryPoint, "<averroes.Library: void <clinit>()>"),
				Arrays.asList(sourceExample), sinkPrintln);
		checkInfoflow(infoflow, resultCount);
	}

	/**
	 * Run FlowDroid using the original library code.
	 * 
	 * @param testCase
	 * @param entryPoint
	 * @param resultCount
	 */
	public static void runFlowDroid(String testCase, String entryPoint, int resultCount) {
		System.out.println("Testing FlowDroid on " + testCase + "...");
		initializeSoot();
		IInfoflow infoflow = initInfoflow();
		infoflow.computeInfoflow(CommonOptions.getApplicationCode(testCase), CommonOptions.getOriginalLibrary(testCase),
				entryPoint, Arrays.asList(sourceExample), sinkPrintln);
		checkInfoflow(infoflow, resultCount);
	}

	/**
	 * Check the correctness of the given infoflow, given the result count.
	 * 
	 * @param infoflow
	 * @param resultCount
	 */
	private static void checkInfoflow(IInfoflow infoflow, int resultCount) {
		if (infoflow.isResultAvailable()) {
			InfoflowResults map = infoflow.getResults();

			// Assert # of reported results
			assertEquals(resultCount, map.size());

			// Assert the availability of sinks
			List<String> sinks = sinkPrintln.stream().filter(s -> map.containsSinkMethod(s)).collect(Collectors.toList());
			assertTrue(!sinks.isEmpty());

			// Assert that certain paths exist
			sinks.forEach(s -> assertTrue(map.isPathBetweenMethods(s, sourceExample)));
		} else {
			fail("result is not available");
		}
	}

	/**
	 * Initialize Soot for analysis.
	 */
	private static void initializeSoot() {
		soot.G.reset();
		System.gc();
	}

	/**
	 * Initialize the information flow propagation.
	 * 
	 * @return
	 */
	private static IInfoflow initInfoflow() {
		return initInfoflow(false);
	}


	/**
	 * Initialize the information flow propagation.
	 * 
	 * @param useTaintWrapper
	 * @return
	 */
	private static IInfoflow initInfoflow(boolean useTaintWrapper) {
		Infoflow result = new Infoflow("", false, null);
		ConfigForTest testConfig = new ConfigForTest();
		result.setSootConfig(testConfig);
		// InfoflowConfiguration.setAccessPathLength(2);
		// result.getConfig().setEnableImplicitFlows(true);

		if (useTaintWrapper) {
			EasyTaintWrapper easyWrapper;
			try {
				easyWrapper = new EasyTaintWrapper(new File(CommonOptions.easyTaintWrappers));
				result.setTaintWrapper(easyWrapper);
			} catch (IOException e) {
				System.err.println("Could not initialized Taintwrapper:");
				e.printStackTrace();
			}
		}

		return result;
	}

}
