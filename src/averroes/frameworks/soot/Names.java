package averroes.frameworks.soot;

import soot.Scene;
import soot.util.NumberedString;


public class Names {
	// Averroes-specific Classes
	public static final String RTA_CLASS = "averroes.Rta";
	public static final String XTA_CLASS = "averroes.Xta";

	// Fields
	public static final String RTA_SET_FIELD_NAME = "set";
	public static final String INT_FIELD_NAME = "integer";
	public static final String RTA_SET_FIELD_SIGNATURE = "<" + RTA_CLASS + ": java.lang.Object "
			+ RTA_SET_FIELD_NAME + ">";
	
	// Soot-specific constants
	public static final NumberedString DEFAULT_CONSTRUCTOR_SIG = Scene.v().getSubSigNumberer()
			.findOrAdd("void <init>()");
}