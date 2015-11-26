package averroes.frameworks.soot;

import soot.Scene;
import soot.util.NumberedString;

public class Names {
	// Averroes-specific Classes
	public static final String RTA_CLASS = "averroes.Rta";
	public static final String XTA_CLASS = "averroes.Xta";

	// Fields
	public static final String SET_FIELD_NAME = "set";
	public static final String INT_FIELD_NAME = "integer";
	public static final String BOOLEAN_FIELD_NAME = "bool";
	public static final String RTA_SET_FIELD_SIGNATURE = "<" + RTA_CLASS + ": java.lang.Object " + SET_FIELD_NAME + ">";
	public static final String RTA_INT_FIELD_SIGNATURE = "<" + RTA_CLASS + ": I " + INT_FIELD_NAME + ">";
	public static final String RTA_BOOLEAN_FIELD_SIGNATURE = "<" + RTA_CLASS + ": Z " + BOOLEAN_FIELD_NAME + ">";

	// Soot-specific constants
	public static final NumberedString DEFAULT_CONSTRUCTOR_SIG = Scene.v().getSubSigNumberer()
			.findOrAdd("void <init>()");
}