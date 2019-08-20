package averroes.soot;

import java.util.Iterator;
import soot.Body;
import soot.Local;

public class LocalVariableRenamer {

  /**
   * Apply various heuristics to rename local variables.
   *
   * @param body
   */
  public static void transform(Body body) {
    removeDollarSigns(body);
  }

  /**
   * Remove the ugly dollar signs in the names of local variables.
   *
   * @param body
   */
  private static void removeDollarSigns(Body body) {
    Iterator<Local> it = body.getLocals().iterator();
    while (it.hasNext()) {
      Local local = it.next();
      String currentName = local.getName();
      int dollarIndex = currentName.indexOf('$');

      if (dollarIndex == 0) {
        // meaning there is a $ sign in the first location
        String newName = currentName.substring(1);

        if (isUniqueName(newName, body)) {
          local.setName(newName);
        }
      }
    }
  }

  /**
   * Checks if the given local variable name is unique wihtin the given method body.
   *
   * @param name
   * @param body
   * @return
   */
  private static boolean isUniqueName(String name, Body body) {
    // check that none of the locals uses this name
    return body.getLocals().stream().noneMatch(l -> l.getName().equals(name));
  }
}
