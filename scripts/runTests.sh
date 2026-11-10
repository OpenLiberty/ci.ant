#!/bin/bash

# Ensure script always executes from the repository root directory
cd $(dirname $0)/..

WLP_VERSION="${WLP_VERSION:-26.0.0_03}"
WLP_LICENSE="${WLP_LICENSE:-L-CJVT-WW2FFS}"

./mvnw -V verify -Ponline-its \
    -D"invoker.streamLogs"=true \
    -DwlpVersion="${WLP_VERSION}" \
    -DwlpLicense="${WLP_LICENSE}" \
    -e
MVN_RC=$?

exit $MVN_RC
