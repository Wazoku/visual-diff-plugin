visual-diff-plugin
==================

A Jenkins plug-in to manage differences in screenshots

Screenshots that are taken during a build are compared with previously approved images and possibly trigger build status changes. The screenshots can be compared with each other through a comparison page which highlights differences.


Differences chart:

![Image](images/Chart.png?raw=true)



Screenshot comparison reports:

![Image](images/Report.png?raw=true)



Highlights to show differences:

![Image](images/Difference_Highlighting.png?raw=true)

# Modifications by Wazoku
This has been modified from the original repo. The [original hpi file](https://github.com/Wazoku/visual-diff-plugin/commit/b0fc7b0631481f23905124b38ea27e7be74c56db) added to this repo contained a different (more recent?) version of the code. That package has been decompiled over the 'older' version here. The UI and Java code has been very slightly modified to better support our usecase.

# External tools needed
The current version still needs the perceptualdiff bin to do the differences. In a future version, this may be replaced with a native java version.
Install the perceptualdiff package with:

```yum install perceptualdiff```

Add the path of the binary to the system configuration for the visual-diff plugin, and the plugin should be ready to run.

# Build

Make sure [maven](https://maven.apache.org/) is installed.

    mvn install
