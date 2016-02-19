package averroes.frameworks;

import java.util.List;

import soot.G;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
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

			// Set some soot parameters
			Options.v().set_process_dir(FrameworksOptions.getInputs());
			Options.v()
					.set_soot_classpath(FrameworksOptions.getSootClassPath());
//			Options.v().setPhaseOption("jb.dae", "enabled:false");
//			Options.v().set_verbose(true);

			// Load the necessary classes
			Scene.v().loadNecessaryClasses();

			// Print out files
			Scene.v().getApplicationClasses().stream().map(c -> c.getMethods())
					.flatMap(List::stream).filter(SootMethod::isConcrete)
					.forEach(m -> {
						Printers.print(PrinterType.EXPECTED, m);
					});

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}