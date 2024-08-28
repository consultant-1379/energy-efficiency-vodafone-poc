FROM armdocker.rnd.ericsson.se/proj_oss_releases/enm/jboss-dps

# Install the latest ENM ISO version of our JBoss dependencies
#RUN enm ++ ERICtopologycollections_CXP9030498
#RUN enm ++ ERICcmeditcommon_CXP9035095
#RUN enm ++ ERICtopologysearch_CXP9030474
RUN enm ++ ERICcmread_CXP9030316
RUN enm ++ ERICmodelinformationservice_CXP9031071
RUN enm ++ ERICpersistentobjectservice_CXP9031015
RUN enm ++ ERICpib_CXP9030194
RUN enm ++ ERICps_CXP9030203

# CM Components are not installed on the deployments folder, so we need to move then.
RUN mv /opt/ericsson/com.ericsson.oss.services.cm/*.ear /ericsson/3pp/jboss/standalone/deployments

COPY standalone-enm.xml $JBOSS_HOME/standalone/configuration/standalone-enm.xml

HEALTHCHECK --start-period=240s --interval=30s --retries=16 CMD curl --output /dev/null --silent --head --fail -H "Accept: application/json" -H "X-Tor-UserID: administrator" http://energyefficiency_jboss:8080

ENTRYPOINT ["entrypoint.sh"]
CMD ["-IP", "-s", "jboss"]
