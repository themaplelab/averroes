package averroes.frameworks;

import java.util.List;

import soot.G;
import soot.Scene;
import soot.SootMethod;
import soot.options.Options;
import averroes.frameworks.options.FrameworksOptions;
import averroes.util.io.Printers;
import averroes.util.io.Printers.PrinterType;

/**
 * A simple Jimple printer.
 * 
 * @author Karim Ali
 * 
 */
public class JimplePrinter {

	/**
	 * The main Averroes method.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			// Process the arguments
			FrameworksOptions.processArguments(args);

			// Reset Soot
			G.reset();

			// Prepare the soot classpath
			// FrameworksClassProvider provider = new FrameworksClassProvider();
			// provider.prepareClasspath();

			// Set some soot parameters
			// SourceLocator.v().setClassProviders(
			// Collections.singletonList((ClassProvider) provider));
			// Options.v().classes().addAll(provider.getClassNames());
			Options.v().set_process_dir(FrameworksOptions.getInputs());
			Options.v().set_soot_classpath(FrameworksOptions.getSootClassPath());

			// Load the necessary classes
			System.out.println(Scene.v().getSootClassPath());
			Scene.v().loadNecessaryClasses();

			// Print out files
			Scene.v()
					.getApplicationClasses()
					.stream()
					.map(c -> c.getMethods())
					.flatMap(List::stream)
					.filter(SootMethod::isConcrete)
					.forEach(
							m -> {
								Printers.getPrintStream(PrinterType.EXPECTED).println("==========================");
								Printers.getPrintStream(PrinterType.EXPECTED).println("EXPECTED output");
								Printers.getPrintStream(PrinterType.EXPECTED).println("==========================");
								Printers.getPrintStream(PrinterType.EXPECTED).println(m.retrieveActiveBody());
							});

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}