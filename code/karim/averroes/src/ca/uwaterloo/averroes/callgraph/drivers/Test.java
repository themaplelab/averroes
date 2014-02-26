package ca.uwaterloo.averroes.callgraph.drivers;

import java.io.IOException;

import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.raw.FieldIdItem;
import org.jf.dexlib2.dexbacked.raw.HeaderItem;

import soot.dexpler.Util;
import ca.uwaterloo.averroes.properties.AverroesProperties;

public class Test {

	public static void main(String[] args) {
		try {
			DexBackedDexFile dexFile = DexFileFactory.loadDexFile(AverroesProperties.getApkLocation(), 17);

			int fieldCount = dexFile.readSmallUint(HeaderItem.FIELD_COUNT_OFFSET);
			System.out.println(fieldCount);

			for (int i = 0; i < fieldCount; i++) {
				int fieldOffset = dexFile.getFieldIdItemOffset(i);

				int nameIndex = dexFile.readSmallUint(fieldOffset + FieldIdItem.NAME_OFFSET);
				String fieldName = dexFile.getString(nameIndex);

				int classIndex = dexFile.readUshort(fieldOffset + FieldIdItem.CLASS_OFFSET);
				String className = Util.dottedClassName(dexFile.getType(classIndex));

				int typeIndex = dexFile.readUshort(fieldOffset + FieldIdItem.TYPE_OFFSET);
				String fieldDescriptor = Util.dottedClassName(dexFile.getType(typeIndex));

				if (className.equals(fieldDescriptor)) {
					System.out.println("<" + className + ": " + fieldDescriptor + " " + fieldName + ">");
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}