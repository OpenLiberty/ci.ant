#!/bin/bash

# No artifacts to build for ci.ant. (It has no dependencies to clone and the build 
# happens in runTests.sh. This script must exist because the Jenkins pipeline calls 
# it for every repository.
exit 0
