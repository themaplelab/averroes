package ca.uwaterloo.averroes.util;

import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.raw.MethodIdItem;
import org.jf.dexlib2.dexbacked.raw.ProtoIdItem;

import soot.SootMethod;
import soot.coffi.Util;

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
		String className = Util.v().jimpleTypeOfFieldDescriptor(dexFile.getType(classIndex)).toString();

		int protoIndex = dexFile.readUshort(methodOffset + MethodIdItem.PROTO_OFFSET);
		String protoString = ProtoIdItem.asString(dexFile, protoIndex);

		int nameIndex = dexFile.readSmallUint(methodOffset + MethodIdItem.NAME_OFFSET);
		String methodName = dexFile.getString(nameIndex);

		return BytecodeUtils.asSootMethod(className, methodName, protoString);
	}
}
