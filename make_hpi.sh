#!/bin/bash

version=$(grep "Plugin-Version: " src/META-INF/MANIFEST.MF | grep "[0-9\.]*" -o)

# Update verion number with following:
# grep "0.5.7" -l | xargs sed 's/0\.5\.7/0\.5\.8/g' -i

# Create hpi zip file
(
cd src || exit
zip -r "../visual-diff-$version.hpi" *
)
