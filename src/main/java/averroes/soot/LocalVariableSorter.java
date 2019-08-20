package averroes.soot;

import java.util.List;
import java.util.stream.Collectors;
import soot.Body;
import soot.Local;
import soot.jimple.internal.JimpleLocal;

public class LocalVariableSorter {

  /**
   * Sort the locals according the first use/def (i.e., appearance in a Jimple statement).
   *
   * @param body
   */
  public static void transform(Body body) {
    List<Local> orderedLocals =
        body.getUnits().stream()
            .map(u -> u.getUseAndDefBoxes())
            .flatMap(l -> l.stream())
            .map(vb -> vb.getValue())
            .filter(v -> v instanceof JimpleLocal)
            .distinct()
            .map(v -> (JimpleLocal) v)
            .collect(Collectors.toList());
    body.getLocals().clear();
    body.getLocals().addAll(orderedLocals);
  }
}
