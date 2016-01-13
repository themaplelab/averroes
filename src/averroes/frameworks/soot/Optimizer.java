package averroes.frameworks.soot;

import soot.PackManager;
import soot.Transform;

public class Optimizer {
    public void optimize() {
        for(int i = 0; i < 5; i++) {
            new CHABuilder().run();
            System.out.println("pass "+i);
            ((Transform) PackManager.v().getPhase("wjtp.si")).apply();
        }
    }
}
