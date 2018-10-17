package org.jenkinsci.plugins.visual_diff;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.tasks.BuildStepDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.jenkinsci.plugins.visual_diff.comparison.ComparisonDescribable;
import org.jenkinsci.plugins.visual_diff.data.BuildData;
import org.jenkinsci.plugins.visual_diff.utils.Artifacts;
import org.kohsuke.stapler.DataBoundConstructor;

public class Builder extends hudson.tasks.Builder
{
  private final List<ComparisonDescribable> comparisons;

  @DataBoundConstructor
  public Builder(List<ComparisonDescribable> comparisons)
  {
    this.comparisons = comparisons;
  }

  public List<ComparisonDescribable> getComparisons()
  {
    return this.comparisons;
  }

  public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException
  {
    List<String> differentScreenNames = new ArrayList();

    Artifacts artifacts = new Artifacts(build);
    artifacts.createFolders(listener);

    int knownScreensBefore = artifacts.getApprovedScreens().length;

    BuildData buildData = new BuildData();
    for (ComparisonDescribable comparison : this.comparisons)
    {
      comparison.processAll(build, launcher, listener);
      buildData.add(comparison.getBuildData());
      differentScreenNames.addAll(comparison.getDifferentScreenNames());
    }
    int knownScreensAfter = artifacts.getApprovedScreens().length;

    String[] differentScreenNamesArray = (String[])differentScreenNames.toArray(new String[differentScreenNames.size()]);

    build.addAction(new DataAction(buildData, knownScreensBefore, knownScreensAfter, differentScreenNamesArray));
    build.addAction(new CompareAction(build));

    return true;
  }

  public DescriptorImpl getDescriptor()
  {
    return (DescriptorImpl)super.getDescriptor();
  }

  @Extension
  public static final class DescriptorImpl extends BuildStepDescriptor<hudson.tasks.Builder>
  {
    public DescriptorImpl()
    {
      load();
    }

    public String getDisplayName()
    {
      return "Visual Diff";
    }

    public boolean isApplicable(Class<? extends AbstractProject> aClass)
    {
      return true;
    }

    public List<Descriptor<? extends ComparisonDescribable>> getComparisonDescribables(AbstractProject<?, ?> p)
    {
      List<Descriptor<? extends ComparisonDescribable>> list = new LinkedList();
      for (Descriptor<? extends ComparisonDescribable> rs : ComparisonDescribable.all()) {
        list.add(rs);
      }
      return list;
    }
  }
}
