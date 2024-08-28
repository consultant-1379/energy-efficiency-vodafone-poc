#!/usr/bin/env bash
# contribute.sh
#  Adds the default metadata for the application to the Application Launcher
for app in 'energyefficiency'
do
    \cp -R /var/www/html/${app}/metadata/* /ericsson/tor/data/apps/
    if [ $? -ne 0 ]
    then
        logger -t "CLOUD INIT" "[${app} deploy] failed to copy application metadata files."
        exit 1
    fi
    mkdir -p /ericsson/tor/data/apps/${app}/locales/en-us/
    \cp /var/www/html/locales/en-us/${app}/app.json /ericsson/tor/data/apps/${app}/locales/en-us/
    if [ $? -ne 0 ]
    then
        logger -t "CLOUD INIT" "[${app} deploy] failed to copy en-us localization files."
        exit 1
    fi
    \cp /var/www/html/locales/en-us/${app}/app_actions.json /ericsson/tor/data/apps/${app}/locales/en-us/
    if [ $? -ne 0 ]
    then
        logger -t "CLOUD INIT" "[${app} deploy] failed to copy en-us actions localization files."
    fi

    chown -R jboss_user:jboss /ericsson/tor/data/apps/${app}
done