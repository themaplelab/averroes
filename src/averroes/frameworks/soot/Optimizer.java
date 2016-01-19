package averroes.frameworks.soot;

import soot.PackManager;
import soot.SootMethod;
import soot.Transform;

public class Optimizer {
    public void optimize() {
        for(int i = 0; i < 5; i++) {
            new CHABuilder().run();
            ((Transform) PackManager.v().getPhase("wjtp.si")).apply();
        }

    }

    /** Return true if it is possible for the application to override this library method. */
    public static boolean isOverridable(SootMethod method) {
        if(method.isFinal()) return false;
        if(method.isPrivate()) return false;

        if(method.isPublic()) return true;
        if(method.isProtected()) return true;

        // TODO: Check whether method is overridden (in library) by any other overridable method.
        // If yes, then return true because if the app overrides the overriding method, then
        // it also overrides this one.

        return false;
    }

}
