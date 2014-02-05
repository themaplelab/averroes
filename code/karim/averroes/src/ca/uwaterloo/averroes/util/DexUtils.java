package ca.uwaterloo.averroes.util;

import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.raw.MethodIdItem;
import org.jf.dexlib2.dexbacked.raw.ProtoIdItem;

import ca.uwaterloo.averroes.soot.Names;
import soot.ArrayType;
import soot.SootMethod;
import soot.Type;
import soot.dexpler.Util;

public class DexUtils {

	/**
	 * Get the SootMethod (from the Scene) corresponding to the given DEX method index. Note that all the items in the
	 * DEX global constant pool are in bytecode format, so we need to convert them to the probe strings
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

		return BytecodeUtils.asSootMethod(className, methodName, protoString);
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
}
