package ca.uwaterloo.averroes.soot;

import soot.Scene;
import soot.util.NumberedString;

public class Names {
	// Averroes-specific Classes
	public static final String AVERROES_LIBRARY_CLASS = "ca.uwaterloo.averroes.Library";
	public static final String AVERROES_LIBRARY_CLASS_BC_SIG = "ca/uwaterloo/averroes/Library";
	public static final String AVERROES_ABSTRACT_LIBRARY_CLASS = "ca.uwaterloo.averroes.AbstractLibrary";
	public static final String ANDROID_DUMMY_MAIN_CLASS = "ca.uwaterloo.averroes.AndroidDummyMainClass";

	// Classes
	public static final String JAVA_LANG_OBJECT = "java.lang.Object";
	public static final String JAVA_LANG_CLASS = "java.lang.Class";
	public static final String JAVA_LANG_THROWABLE = "java.lang.Throwable";
	public static final String JAVA_LANG_REF_FINALIZER = "java.lang.ref.Finalizer";
	public static final String JAVA_LANG_STRING = "java.lang.String";

	// Fields
	public static final String LIBRARY_POINTS_TO = "libraryPointsTo";
	public static final String LIBRARY_POINTS_TO_FIELD_SIGNATURE = "<" + AVERROES_ABSTRACT_LIBRARY_CLASS
			+ ": java.lang.Object " + LIBRARY_POINTS_TO + ">";
	public static final String FINALIZE_POINTS_TO = "finalizePointsTo";
	public static final String FINALIZE_POINTS_TO_FIELD_SIGNATURE = "<" + AVERROES_ABSTRACT_LIBRARY_CLASS
			+ ": java.lang.Object " + FINALIZE_POINTS_TO + ">";
	public static final String INSTANCE = "instance";
	public static final String INSTANCE_FIELD_SIGNATURE = "<" + AVERROES_ABSTRACT_LIBRARY_CLASS
			+ ": " + AVERROES_ABSTRACT_LIBRARY_CLASS + " " + INSTANCE + ">";

	// Methods
	public static final String AVERROES_DO_IT_ALL_METHOD_NAME = "doItAll";
	public static final String MAIN_METHOD = "main";
	public static final String AVERROES_ABSTRACT_DO_IT_ALL_METHOD_SIGNATURE = "<" + AVERROES_ABSTRACT_LIBRARY_CLASS + ": void "
			+ AVERROES_DO_IT_ALL_METHOD_NAME + "()>";

	// Reflection methods
	public static final String FOR_NAME_SIG = "<java.lang.Class: java.lang.Class forName(java.lang.String)>";
	public static final String NEW_INSTANCE_SIG = "<java.lang.Class: java.lang.Object newInstance()>";

	// Finalize
	public static final String FINALIZE_SIG = "<java.lang.Object: void finalize()>";

	// Android-specific
	public static final String ANDROID_VIEW = "android.view.View";
	public static final String ANDROID_R = "R";
	public static final String CLONE = "clone";

	public static String getOnClickSubSig(String methodName) {
		return "void " + methodName + "(" + ANDROID_VIEW + ")";
	}

	// Soot-specific constants
	public static final NumberedString DEFAULT_CONSTRUCTOR_SIG = Scene.v().getSubSigNumberer()
			.findOrAdd("void <init>()");

	// Other
	public static final String BLOB = "blob";
}