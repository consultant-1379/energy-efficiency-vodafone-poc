#!/bin/bash
# Use first parameter to set a specific port
# e.g. <scriptname> 8585
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PORT=8585
if [ ! -z "$1" ]; then
    PORT=$1
fi
cd ${SCRIPT_DIR}/
. proxyCookieUpdate.sh
MODULES=()
# Preconfigured modules
if [ ! -z "$ADDITIONAL_MODULES" ]; then
    MODULES+=( "${ADDITIONAL_MODULES[@]}" )
fi
MODULE_LIST=$(IFS=, ; echo "${MODULES[*]}")
echo "modl=<${MODULE_LIST}>"
if [ ! -z "$MODULE_LIST" ]; then
    cdt2 serve -m $MODULE_LIST -p $PORT --proxy-port 8181 --proxy-config proxyConfig.json
else
    cdt2 serve -p $PORT --proxy-port 8181 --proxy-config proxyConfig.json
fi
