package org.jenkinsci.plugins.visual_diff.data;

import java.io.Serializable;

public class BuildData
  implements Serializable
{
  private static final long serialVersionUID = -5986348230372792725L;
  private int newScreens = 0;
  private int autoApprovedScreens = 0;
  private int unknownScreens = 0;
  private int screens = 0;
  private int knownScreens = 0;
  private int differentScreens = 0;
  private int differentOkScreens = 0;
  
  public BuildData() {}
  
  public BuildData(int newScreens, int autoApprovedScreens, int unknownScreens, int screens, int knownScreens, int differentScreens, int differentOkScreens)
  {
    this.newScreens = newScreens;
    this.autoApprovedScreens = autoApprovedScreens;
    this.unknownScreens = unknownScreens;
    this.screens = screens;
    this.knownScreens = knownScreens;
    this.differentScreens = differentScreens;
    this.differentOkScreens = differentOkScreens;
  }
  
  public int getNewScreens()
  {
    return this.newScreens;
  }
  
  public int getAutoApprovedScreens()
  {
    return this.autoApprovedScreens;
  }
  
  public int getUnknownScreens()
  {
    return this.unknownScreens;
  }
  
  public int getScreens()
  {
    return this.screens;
  }
  
  public int getKnownScreens()
  {
    return this.knownScreens;
  }
  
  public int getDifferentScreens()
  {
    return this.differentScreens;
  }
  
  public int getDifferentOkScreens()
  {
    return this.differentOkScreens;
  }
  
  public void add(BuildData data)
  {
    this.newScreens += data.getNewScreens();
    this.autoApprovedScreens += data.getAutoApprovedScreens();
    this.unknownScreens += data.getUnknownScreens();
    this.screens += data.getScreens();
    this.knownScreens += data.getKnownScreens();
    this.differentScreens += data.getDifferentScreens();
    this.differentOkScreens += data.getDifferentOkScreens();
  }
}
