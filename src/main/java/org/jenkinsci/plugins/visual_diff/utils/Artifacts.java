package org.jenkinsci.plugins.visual_diff.utils;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;

import java.io.File;
import java.io.IOException;

import org.jenkinsci.plugins.visual_diff.DataAction;

public class Artifacts
{
  private final AbstractBuild<?, ?> build;
  
  public Artifacts(AbstractBuild<?, ?> build)
  {
    this.build = build;
  }
  
  public FilePath getBuildArtifactsPath()
  {
    return new FilePath(new File(this.build.getRootDir().getAbsolutePath())).child("vDiff");
  }
  
  public FilePath getBuildScreensPath()
  {
    return getBuildArtifactsPath().child("screens");
  }
  
  public FilePath[] getBuildScreens() throws InterruptedException, IOException
  {
    return getBuildScreensPath().list("*");
  }
  
  public Boolean hasBuildScreen(String name) throws InterruptedException, IOException
  {
    return Boolean.valueOf(getBuildScreensPath().child(name).exists());
  }
  
  public Boolean hasBuildScreen(FilePath file) throws InterruptedException, IOException
  {
    return hasBuildScreen(file.getName());
  }
  
  public FilePath getBuildDiffsPath()
  {
    return getBuildArtifactsPath().child("diffs");
  }
  
  public FilePath[] getBuildDiffs() throws InterruptedException, IOException
  {
    return getBuildDiffsPath().list("*");
  }
  
  public Boolean hasBuildDiff(String name) throws InterruptedException, IOException
  {
    return Boolean.valueOf(getBuildDiffsPath().child(name).exists());
  }
  
  public Boolean hasBuildDiff(FilePath file) throws InterruptedException, IOException
  {
    return hasBuildDiff(file.getName());
  }
  
  public FilePath getApprovedArtifactsPath()
  {
    return new FilePath(this.build.getProject().getRootDir()).child("vDiff");
  }
  
  public FilePath getApprovedScreensPath()
  {
    return getApprovedArtifactsPath().child("screens");
  }
  
  public FilePath[] getApprovedScreens() throws InterruptedException, IOException
  {
    return getApprovedScreensPath().list("*");
  }
  
  public FilePath getApprovedPath(String name) throws InterruptedException, IOException
  {
    return getApprovedScreensPath().child(name);
  }
  
  public Boolean isApproved(String name) throws InterruptedException, IOException
  {
    return Boolean.valueOf(getApprovedPath(name).exists());
  }
  
  public Boolean isApproved(FilePath file) throws InterruptedException, IOException
  {
    return isApproved(file.getName());
  }
  
  public void approve(FilePath file) throws InterruptedException, IOException
  {
    file.copyTo(getApprovedScreensPath().child(file.getName()));
    getBuildDiffsPath().child(file.getName()).delete();
  }
  
  public void approve(String name) throws InterruptedException, IOException
  {
    approve(getBuildScreensPath().child(name));
  }
  
  public void createFolders(BuildListener listener) throws InterruptedException, IOException
  {
    _createIfNoExist(getBuildArtifactsPath(), listener);
    _createIfNoExist(getBuildScreensPath(), listener);
    _createIfNoExist(getBuildDiffsPath(), listener);
    _createIfNoExist(getApprovedArtifactsPath(), listener);
    _createIfNoExist(getApprovedScreensPath(), listener);
  }
  
  protected FilePath _createIfNoExist(FilePath path, BuildListener listener) throws InterruptedException, IOException
  {
    if (!path.exists())
    {
      if (listener != null) {
        listener.getLogger().println("Create folder: " + path.getRemote());
      }
      path.mkdirs();
    }
    return path;
  }
  
  public Boolean hasThresholdDiff(String name)
  {
    DataAction action = (DataAction)this.build.getAction(DataAction.class);
    if (action == null) {
      return Boolean.valueOf(false);
    }
    return action.screenIsDifferent(name);
  }
  
  public Boolean hasThresholdDiff(FilePath file)
  {
    return hasThresholdDiff(file.getName());
  }
}
