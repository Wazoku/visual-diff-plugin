package org.jenkinsci.plugins.visual_diff.comparison;

import hudson.DescriptorExtensionList;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Result;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.jenkinsci.plugins.visual_diff.data.BuildData;
import org.jenkinsci.plugins.visual_diff.utils.Artifacts;
import org.kohsuke.stapler.DataBoundConstructor;

public abstract class ComparisonDescribable implements Serializable, Describable<ComparisonDescribable>
{
  public static final String FAILED = "failed";
  public static final String UNSTABLE = "unstable";
  public static final String NOTHING = "nothing";
  private final String screenshotsPath;
  private final Boolean autoApprove;
  private final String markAs;
  private final int numberOfDifferences;
  private int newScreens;
  private int autoApprovedScreens;
  private int unknownScreens;
  private int screens;
  private int knownScreens;
  private int differentScreens;
  private int differentOkScreens;
  private List<String> differentScreenNames;
  
  @DataBoundConstructor
  public ComparisonDescribable(String screenshotsPath, Boolean autoApprove, String markAs, int numberOfDifferences)
  {
    this.screenshotsPath = screenshotsPath;
    this.autoApprove = autoApprove;
    this.markAs = markAs;
    this.numberOfDifferences = numberOfDifferences;
  }
  
  public String getScreenshotsPath()
  {
    return this.screenshotsPath;
  }
  
  public Boolean getAutoApprove()
  {
    return this.autoApprove;
  }
  
  public String getMarkAs()
  {
    return this.markAs;
  }
  
  public int getNumberOfDifferences()
  {
    return this.numberOfDifferences;
  }
  
  public List<String> getDifferentScreenNames()
  {
    return this.differentScreenNames;
  }
  
  public BuildData getBuildData()
  {
    return new BuildData(this.newScreens, this.autoApprovedScreens, this.unknownScreens, this.screens, this.knownScreens, this.differentScreens, this.differentOkScreens);
  }
  
  public FilePath[] archiveFiles(AbstractBuild build, Launcher launcher, BuildListener listener, FilePath dstDir) throws InterruptedException, IOException
  {
    listener.getLogger().println("Copy files...");
    
    FilePath[] files = build.getWorkspace().list(this.screenshotsPath);
    
    List<FilePath> buildScreens = new ArrayList();
    for (FilePath file : files)
    {
      FilePath archivedFile = dstDir.child(file.getName());
      file.copyTo(archivedFile);
      buildScreens.add(archivedFile);
    }
    return (FilePath[])buildScreens.toArray(new FilePath[buildScreens.size()]);
  }
  
  public void processAll(AbstractBuild build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException
  {
    this.newScreens = 0;
    this.autoApprovedScreens = 0;
    this.unknownScreens = 0;
    this.screens = 0;
    this.knownScreens = 0;
    this.differentScreens = 0;
    this.differentOkScreens = 0;
    
    this.differentScreenNames = new ArrayList();
    
    Artifacts artifacts = new Artifacts(build);
    FilePath buildScreensPath = artifacts.getBuildScreensPath();
    FilePath[] buildScreenPaths = archiveFiles(build, launcher, listener, buildScreensPath);
    
    listener.getLogger().println("Compare files...");
    for (FilePath buildScreenPath : buildScreenPaths) {
      processOne(build, launcher, listener, buildScreenPath);
    }
    if (this.differentScreens + this.unknownScreens >= this.numberOfDifferences)
    {
      listener.getLogger().println("Too many differences.");
      if ((this.markAs != null) && (this.markAs.equals("failed")))
      {
        build.setResult(Result.FAILURE);
      }
      else if ((this.markAs == null) || (this.markAs.equals("unstable")))
      {
        Result result = build.getResult();
        if ((result == null) || (result.isBetterThan(Result.UNSTABLE))) {
          build.setResult(Result.UNSTABLE);
        }
      }
    }
  }
  
  public void processOne(AbstractBuild build, Launcher launcher, BuildListener listener, FilePath buildScreenPath) throws InterruptedException, IOException
  {
    Artifacts artifacts = new Artifacts(build);
    
    this.screens += 1;
    if (artifacts.isApproved(buildScreenPath).booleanValue())
    {
      this.knownScreens += 1;
      
      FilePath approvedScreenPath = artifacts.getApprovedScreensPath().child(buildScreenPath.getName());
      FilePath buildDiffPath = artifacts.getBuildDiffsPath().child(buildScreenPath.getName());
      
      Boolean differenceFound = compareScreens(build, launcher, listener, buildScreenPath, approvedScreenPath, buildDiffPath);
      if (differenceFound.booleanValue())
      {
        this.differentScreens += 1;
        this.differentScreenNames.add(buildScreenPath.getName());
        listener.getLogger().println("Difference found in screen " + buildScreenPath.getName());
      }
      else if (buildDiffPath.exists())
      {
        this.differentOkScreens += 1;
        listener.getLogger().println("Difference found in screen " + buildScreenPath.getName() + ", but acceptable");
      }
    }
    else
    {
      this.newScreens += 1;
      if (this.autoApprove.booleanValue())
      {
        this.autoApprovedScreens += 1;
        
        listener.getLogger().println("Screen " + buildScreenPath.getName() + " does not exist. Auto-approve screen...");
        artifacts.approve(buildScreenPath);
      }
      else
      {
        this.unknownScreens += 1;
        listener.getLogger().println("Unknown screen found " + buildScreenPath.getName() + ".");
      }
    }
  }
  
  public Descriptor<ComparisonDescribable> getDescriptor()
  {
    return (ComparisonDescriptor)Hudson.getInstance().getDescriptor(getClass());
  }
  
  public static DescriptorExtensionList<ComparisonDescribable, Descriptor<ComparisonDescribable>> all()
  {
    return Hudson.getInstance().getDescriptorList(ComparisonDescribable.class);
  }
  
  public abstract Boolean compareScreens(AbstractBuild paramAbstractBuild, Launcher paramLauncher, BuildListener paramBuildListener, FilePath paramFilePath1, FilePath paramFilePath2, FilePath paramFilePath3) throws InterruptedException, IOException;
}
