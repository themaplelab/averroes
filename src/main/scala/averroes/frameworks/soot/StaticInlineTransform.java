package averroes.frameworks.soot;

import soot.Transform;

public class StaticInlineTransform extends Transform {
  public StaticInlineTransform(String phaseName) {
    super(phaseName, new StaticInliner());
  }

  @Override
  public String getDeclaredOptions() {
    return "enabled rerun-jb insert-null-checks insert-redundant-casts allowed-modifier-changes expansion-factor max-container-size max-inlinee-size ";
  }

  @Override
  public String getDefaultOptions() {
    //        return "enabled:true rerun-jb:true insert-null-checks:true insert-redundant-casts:true
    // allowed-modifier-changes:unsafe expansion-factor:3 max-container-size:5000
    // max-inlinee-size:20 ";
    return "enabled:true rerun-jb:true insert-null-checks:true insert-redundant-casts:true allowed-modifier-changes:unsafe expansion-factor:30 max-container-size:50000 max-inlinee-size:50000 ";
  }
}
