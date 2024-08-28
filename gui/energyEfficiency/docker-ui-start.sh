#!/usr/bin/env bash
scriptname=`basename "$0"`
echo "[$scriptname] Setup UI"

# Copy metadata into jboss shared dir
sed -i 's/\${enm.configuration.data.dir}/\/ericsson\/tor\/data/' ./launcher/scripts/contribute.sh \
&& ./launcher/scripts/contribute.sh \
&& echo "[$scriptname] Metadata installed"

cd energyefficiency

echo "[$scriptname] Serving UI"
# Serve local UI
# - Use proxy to route energyefficiency REST requests to docker
# - Route any requests targeting non-energyefficiency services to the express module
exec cdt2 serve --proxy-config ../docker-proxy-config.json -m ../docker-external-services.js