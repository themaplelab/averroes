package ca.uwaterloo.averroes.util;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.raw.FieldIdItem;
import org.jf.dexlib2.dexbacked.raw.MethodIdItem;
import org.jf.dexlib2.dexbacked.raw.ProtoIdItem;

import soot.ArrayType;
import soot.DexClassProvider;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.dexpler.Util;
import ca.uwaterloo.averroes.properties.AverroesProperties;
import ca.uwaterloo.averroes.soot.Names;

public class DexUtils {

	/**
	 * Get the SootMethod (from the Scene) corresponding to the given DEX method index. Note that all the items in the
	 * DEX global constant pool are in bytecode format, so we need to convert them to the Soot strings.
	 * 
	 * @param dexFile
	 * @param methodIndex
	 * @return
	 */
	public static SootMethod asSootMethod(DexBackedDexFile dexFile, int methodIndex) {
		int methodOffset = dexFile.getMethodIdItemOffset(methodIndex);

		int classIndex = dexFile.readUshort(methodOffset + MethodIdItem.CLASS_OFFSET);
		String className = Util.dottedClassName(dexFile.getType(classIndex));

		int protoIndex = dexFile.readUshort(methodOffset + MethodIdItem.PROTO_OFFSET);
		String protoString = ProtoIdItem.asString(dexFile, protoIndex);

		int nameIndex = dexFile.readSmallUint(methodOffset + MethodIdItem.NAME_OFFSET);
		String methodName = dexFile.getString(nameIndex);

		return BytecodeUtils.makeSootMethod(className, methodName, protoString);
	}

	/**
	 * Get the SootField (from the Scene) corresponding ot the given DEX field index. Note that all the items in the DEX
	 * global constant pool are in bytecode format, so we need to convert them to the Soot strings.
	 * 
	 * @param dexFile
	 * @param fieldIndex
	 * @return
	 */
	public static SootField asSootField(DexBackedDexFile dexFile, int fieldIndex) {
		int fieldOffset = dexFile.getFieldIdItemOffset(fieldIndex);

		int classIndex = dexFile.readUshort(fieldOffset + FieldIdItem.CLASS_OFFSET);
		String className = Util.dottedClassName(dexFile.getType(classIndex));

		int typeIndex = dexFile.readUshort(fieldOffset + FieldIdItem.TYPE_OFFSET);
		String fieldDescriptor = dexFile.getType(typeIndex);

		int nameIndex = dexFile.readSmallUint(fieldOffset + FieldIdItem.NAME_OFFSET);
		String fieldName = dexFile.getString(nameIndex);

		return BytecodeUtils.makeSootField(className, fieldName, fieldDescriptor);
	}

	/**
	 * Check if a dex method is the Array.clone() method. We need to ignore processing such methods because Soot won't
	 * find the class long[], for example.
	 * 
	 * @param dexFile
	 * @param methodIndex
	 * @return
	 */
	public static boolean isArrayClone(DexBackedDexFile dexFile, int methodIndex) {
		int methodOffset = dexFile.getMethodIdItemOffset(methodIndex);

		int classIndex = dexFile.readUshort(methodOffset + MethodIdItem.CLASS_OFFSET);
		Type type = soot.coffi.Util.v().jimpleTypeOfFieldDescriptor(dexFile.getType(classIndex));

		int nameIndex = dexFile.readSmallUint(methodOffset + MethodIdItem.NAME_OFFSET);
		String methodName = dexFile.getString(nameIndex);

		return type instanceof ArrayType && methodName.equalsIgnoreCase(Names.CLONE);
	}

	/**
	 * Get applications classes from the classes.dex of a given android apk. That's according to application_includes in
	 * the averroes.properties file.
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static Set<String> applicationClassesOfDex(String path) throws IOException {
		Set<String> result = new HashSet<String>();
		Set<String> allClasses = DexClassProvider.classesOfDex(new File(path));

		for (String className : allClasses) {
			if (AverroesProperties.isApplicationClass(className)) {
				result.add(className);
			}
		}

		return result;
	}
}
