package ca.uwaterloo.averroes.callgraph.drivers;

import java.io.IOException;

import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.raw.HeaderItem;
import org.jf.dexlib2.dexbacked.raw.MethodIdItem;

import soot.dexpler.Util;
import ca.uwaterloo.averroes.properties.AverroesProperties;

public class Test {

	public static void main(String[] args) {
		try {
			DexBackedDexFile dex = DexFileFactory.loadDexFile(AverroesProperties.getApkLocation(), 17);
			int methodCount = dex.readSmallUint(HeaderItem.METHOD_COUNT_OFFSET);
			System.out.println(methodCount);
			for (int i = 0; i < methodCount; i++) {
				int methodOffset = dex.getMethodIdItemOffset(i);
				int typeIndex = dex.readUshort(methodOffset + MethodIdItem.CLASS_OFFSET);
				String className = Util.dottedClassName(dex.getType(typeIndex));
				System.out.println(className);
			}

			// for (ClassDef c : dex.getClasses()) {
			// String name = Util.dottedClassName(c.getType());
			// System.out.println(name);
			// }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}