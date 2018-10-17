package org.jenkinsci.plugins.visual_diff;

import hudson.model.InvisibleAction;
import java.io.Serializable;
import org.jenkinsci.plugins.visual_diff.data.BuildData;

public class DataAction extends InvisibleAction implements Serializable
{
  private static final long serialVersionUID = -5986348230372792724L;
  private BuildData buildData;
  private int knownScreensBefore = 0;
  private int knownScreensAfter = 0;
  private String[] differentScreens;

  public DataAction(BuildData buildData, int knownScreensBefore, int knownScreensAfter, String[] differentScreens)
  {
    this.buildData = buildData;
    this.knownScreensBefore = knownScreensBefore;
    this.knownScreensAfter = knownScreensAfter;
    this.differentScreens = differentScreens;
  }

  public BuildData getBuildData()
  {
    return this.buildData;
  }

  public int getKnownScreensBefore()
  {
    return this.knownScreensBefore;
  }

  public int getKnownScreensAfter()
  {
    return this.knownScreensAfter;
  }

  public String[] getDifferentScreens()
  {
    return this.differentScreens;
  }

  public Boolean screenIsDifferent(String name)
  {
    for (String curr : this.differentScreens) {
      if (curr.equals(name)) {
        return Boolean.valueOf(true);
      }
    }
    return Boolean.valueOf(false);
  }
}
