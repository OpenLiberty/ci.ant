#!/bin/bash

# Ensure script always executes from the repository root directory
cd $(dirname $0)/..

./mvnw -V clean install -DskipTests
MVN_RC=$?

exit $MVN_RC
