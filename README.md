visual-diff-plugin
==================

The original repo (https://github.com/ablage/visual-diff-plugin) contains a hpi file which differs from the checked in code.

Rather then use the supplied source code (which when compiled, didn't seem to render correctly) I've extracted the hpi file into a new `dev` branch and modified it to fix a couple of js & css bugs and better match Wazoku's needs.

Use the `make_hpi.sh` script to repackage the hpi file which can be uploaded to Jenkins

# External tools needed
Install the perceptualdiff package with:

```yum install perceptualdiff```

Add the path of the binary to the system configuration for the visual-diff plugin, and the plugin should be ready to run.
