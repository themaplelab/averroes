/*******************************************************************************
 * Copyright (c) 2015 Karim Ali and Ondřej Lhoták.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Karim Ali - initial API and implementation and/or initial documentation
 *******************************************************************************/
package averroes.soot;

import soot.SootMethod;

public class Names {
	// Averroes-specific Classes
	public static final String AVERROES_LIBRARY_CLASS = "averroes.Library";
	public static final String AVERROES_LIBRARY_CLASS_BC_SIG = "averroes/Library";
	public static final String AVERROES_ABSTRACT_LIBRARY_CLASS = "averroes.AbstractLibrary";
	public static final String RTA_CLASS = "rta.RTA";
	public static final String XTA_CLASS = "xta.XTA";

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
	public static final String INSTANCE_FIELD_SIGNATURE = "<" + AVERROES_ABSTRACT_LIBRARY_CLASS + ": "
			+ AVERROES_ABSTRACT_LIBRARY_CLASS + " " + INSTANCE + ">";
	
	public static final String SET_FIELD_PREFIX = "set_f_";
	public static final String SET_METHOD_PREFIX = "set_m_";
	public static final String SET_FIELD_NAME = "set";
	public static final String INT_FIELD_NAME = "integer";
	public static final String BOOLEAN_FIELD_NAME = "bool";
	public static final String GUARD_FIELD_NAME = "guard";
	public static final String RTA_SET_FIELD_SIGNATURE = "<" + RTA_CLASS + ": java.lang.Object " + SET_FIELD_NAME + ">";
	public static final String RTA_INT_FIELD_SIGNATURE = "<" + RTA_CLASS + ": int " + INT_FIELD_NAME + ">";
	public static final String RTA_BOOLEAN_FIELD_SIGNATURE = "<" + RTA_CLASS + ": boolean " + BOOLEAN_FIELD_NAME + ">";
	public static final String RTA_GUARD_FIELD_SIGNATURE = "<" + RTA_CLASS + ": boolean " + GUARD_FIELD_NAME + ">";
	public static final String XTA_GUARD_FIELD_SIGNATURE = "<" + XTA_CLASS + ": boolean " + GUARD_FIELD_NAME + ">";

	// Methods
	public static final String AVERROES_DO_IT_ALL_METHOD_NAME = "doItAll";
	public static final String MAIN_METHOD = "main";
	public static final String AVERROES_ABSTRACT_DO_IT_ALL_METHOD_SIGNATURE = "<" + AVERROES_ABSTRACT_LIBRARY_CLASS
			+ ": void " + AVERROES_DO_IT_ALL_METHOD_NAME + "()>";
	public static final String AVERROES_LIBRARY_DO_IT_ALL_METHOD_SIGNATURE = "<" + AVERROES_LIBRARY_CLASS + ": void "
			+ AVERROES_DO_IT_ALL_METHOD_NAME + "()>";
	public static final String AVERROES_LIBRARY_CLINIT_METHOD_SIGNATURE = "<" + AVERROES_LIBRARY_CLASS + ": void "
			+ SootMethod.staticInitializerName + "()>";

	// Reflection methods
	public static final String FOR_NAME_SIG = "<java.lang.Class: java.lang.Class forName(java.lang.String)>";
	public static final String NEW_INSTANCE_SIG = "<java.lang.Class: java.lang.Object newInstance()>";

	// Finalize
	public static final String FINALIZE_SIG = "<java.lang.Object: void finalize()>";

	// Soot-specific constants
	public static final String DEFAULT_CONSTRUCTOR_SUBSIG = "void <init>()";

	// Other
	public static final String BLOB = "blob";
}