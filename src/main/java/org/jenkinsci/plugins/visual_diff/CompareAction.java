package org.jenkinsci.plugins.visual_diff;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.DirectoryBrowserSupport;
import hudson.util.ChartUtil;
import hudson.util.ColorPalette;
import hudson.util.ShiftedCategoryAxis;
import java.awt.BasicStroke;
import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.ListIterator;
import javax.servlet.ServletException;
import org.jenkinsci.plugins.visual_diff.utils.Artifacts;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleEdge;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class CompareAction implements Action, Serializable
{
  private static final long serialVersionUID = -5986348230372792726L;
  private final AbstractBuild<?, ?> build;

  public CompareAction(AbstractBuild<?, ?> build)
  {
    this.build = build;
  }

  public String getDisplayName()
  {
    return "Visual-Diff Report";
  }

  public String getIconFileName()
  {
    return "graph.gif";
  }

  public String getUrlName()
  {
    return "vdiff";
  }

  public Artifacts getArtifacts()
  {
    return new Artifacts(this.build);
  }

  public String getCssClass(FilePath file) throws InterruptedException, IOException
  {
    Artifacts artifacts = getArtifacts();
    StringBuilder sb = new StringBuilder();
    if (artifacts.isApproved(file).booleanValue()) {
      sb.append(" type-approved");
    } else {
      sb.append(" type-not-approved");
    }
    if (artifacts.hasBuildScreen(file).booleanValue()) {
      sb.append(" type-build");
    } else {
      sb.append(" type-not-build");
    }
    return sb.toString();
  }

  public void doBuildScreens(StaplerRequest req, StaplerResponse rsp) throws InterruptedException, IOException, ServletException
  {
    DirectoryBrowserSupport dbs = new DirectoryBrowserSupport(this, "Build-Screens");
    dbs.serveFile(req, rsp, getArtifacts().getBuildScreensPath(), "graph.gif", false);
  }

  public void doBuildDiffs(StaplerRequest req, StaplerResponse rsp) throws InterruptedException, IOException, ServletException
  {
    DirectoryBrowserSupport dbs = new DirectoryBrowserSupport(this, "Build-Diffs");
    dbs.serveFile(req, rsp, getArtifacts().getBuildDiffsPath(), "graph.gif", false);
  }

  public void doApprovedScreens(StaplerRequest req, StaplerResponse rsp) throws InterruptedException, IOException, ServletException
  {
    DirectoryBrowserSupport dbs = new DirectoryBrowserSupport(this, "Approved-Screens");
    dbs.serveFile(req, rsp, getArtifacts().getApprovedScreensPath(), "graph.gif", false);
  }

  public void doApprove(@QueryParameter String name) throws InterruptedException, IOException, ServletException
  {
    getArtifacts().approve(name);
  }

  public void doDelete(@QueryParameter String name) throws InterruptedException, IOException, ServletException
  {
    getArtifacts().getApprovedPath(name).delete();
  }

  public void doDeleteAll(@QueryParameter String name) throws InterruptedException, IOException, ServletException
  {
    FilePath[] approvedScreens = getArtifacts().getApprovedScreens();
    for (FilePath approvedScreen : approvedScreens) {
      approvedScreen.delete();
    }
  }

  protected JFreeChart createPerformanceChart(CategoryDataset dataSet)
  {
    JFreeChart chart = ChartFactory.createLineChart("Performance", null, "Screens", dataSet, PlotOrientation.VERTICAL, true, true, false);

    LegendTitle legend = chart.getLegend();
    legend.setPosition(RectangleEdge.RIGHT);

    chart.setBackgroundPaint(Color.white);

    CategoryPlot plot = chart.getCategoryPlot();

    plot.setBackgroundPaint(Color.WHITE);
    plot.setOutlinePaint(null);
    plot.setRangeGridlinesVisible(true);
    plot.setRangeGridlinePaint(Color.black);

    CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
    plot.setDomainAxis(domainAxis);
    domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
    domainAxis.setLowerMargin(0.0D);
    domainAxis.setUpperMargin(0.0D);
    domainAxis.setCategoryMargin(0.0D);

    NumberAxis rangeAxis = (NumberAxis)plot.getRangeAxis();
    rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

    LineAndShapeRenderer renderer = (LineAndShapeRenderer)plot.getRenderer();
    renderer.setBaseStroke(new BasicStroke(2.0F));
    ColorPalette.apply(renderer);

    return chart;
  }

  protected CategoryDataset createDataSet() throws InterruptedException, IOException
  {
    DefaultCategoryDataset ds = new DefaultCategoryDataset();

    List<? extends AbstractBuild<?, ?>> builds = this.build.getProject().getBuilds();
    for (ListIterator iterator = builds.listIterator(builds.size()); iterator.hasPrevious();)
    {
      AbstractBuild<?, ?> currentBuild = (AbstractBuild)iterator.previous();

      DataAction dataAction = (DataAction)currentBuild.getAction(DataAction.class);
      if (dataAction != null)
      {
        Integer number = Integer.valueOf(currentBuild.number);
        String buildNumber = "#" + number;

        Artifacts buildArtifacts = new Artifacts(currentBuild);

        ds.addValue(dataAction.getBuildData().getNewScreens(), "new", buildNumber);
        ds.addValue(dataAction.getBuildData().getAutoApprovedScreens(), "approved", buildNumber);
        ds.addValue(dataAction.getBuildData().getUnknownScreens(), "unknown", buildNumber);
        ds.addValue(dataAction.getBuildData().getScreens(), "screens", buildNumber);
        ds.addValue(dataAction.getBuildData().getKnownScreens(), "known", buildNumber);
        ds.addValue(dataAction.getBuildData().getDifferentScreens(), "diff", buildNumber);
        ds.addValue(dataAction.getBuildData().getDifferentOkScreens(), "diff/ok", buildNumber);
        ds.addValue(dataAction.getKnownScreensBefore(), "known-before", buildNumber);
        ds.addValue(dataAction.getKnownScreensAfter(), "known-after", buildNumber);
      }
    }
    return ds;
  }

  public void doGraph(StaplerRequest req, StaplerResponse rsp) throws InterruptedException, IOException, ServletException
  {
    ChartUtil.generateGraph(req, rsp, createPerformanceChart(createDataSet()), 800, 400);
  }
}
