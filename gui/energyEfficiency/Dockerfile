FROM armdocker.rnd.ericsson.se/proj_oss_releases/enm/base-ui

#=========================================================================
# Install our UI application
#=========================================================================

ENV ROOT_UI_DIR "/var/www/html/"

COPY . ${ROOT_UI_DIR}

WORKDIR ${ROOT_UI_DIR}

# Clean up any local modifications to the cdt environment
# Convert line endings
# Install dependencies from npm
RUN find -name .cdt | xargs rm -rf \
 && &>- find . -type f -exec dos2unix {} \; \
 && &>- ./docker-ui-install.sh

#=========================================================================
# Install our application metadata locally
#=========================================================================

COPY ./launcher/metadata/energyefficiency ${ROOT_UI_DIR}/energyefficiency/metadata/energyefficiency

COPY ./energyefficiency/locales ${ROOT_UI_DIR}/locales/

#=========================================================================
# Instruct the container to start serving our client packages
#=========================================================================

EXPOSE 8585

HEALTHCHECK --start-period=240s --interval=30s --retries=16 CMD curl --output /dev/null --silent --head --fail http://netex_ui:8585/rest/system/time

ENTRYPOINT ["./docker-ui-start.sh"]
