package soot.asm;


import averroes.soot.Hierarchy;
import soot.SootClass;
import soot.SootMethod;
import soot.coffi.AverroesApplicationConstantPool;
import soot.coffi.ClassFile;

public class AverroesApplicationConstantPoolASM extends AverroesApplicationConstantPool {

    public AverroesApplicationConstantPoolASM(Hierarchy hierarchy) {
        super(hierarchy);
    }

    private static AsmClassSource getAsmClassSource(SootClass cls) {
        return null;
    }
}
