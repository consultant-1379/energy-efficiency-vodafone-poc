#! /bin/bash

# **********************************************************************************************************************
# Ericsson LMI                                    SCRIPT
# **********************************************************************************************************************
#
# (c) Ericsson LMI 2017 - All rights reserved.
#
# The copyright to the computer program(s) herein is the property of Ericsson LMI. The programs may be used and/or
# copied only with the written permission from Ericsson LMI or in accordance with the terms and conditions stipulated
# in the agreement/contract under which the program(s) have been supplied.
#
# **********************************************************************************************************************
# Name    : docker-compose.sh
# Date    : 30/11/2017
# Revision: 1.0
# Author  : ebuslor
# Purpose : This script is used to invoke docker-compose on Git Bash on Windows or Linux shell. It configures the Docker
#           environment automatically on Windows.
# **********************************************************************************************************************

is_git_bash() {
    uname -a | grep -i mingw | wc -l
}

configure() {
    if [ $(is_git_bash) == 1 ]; then
        echo "Configuring Docker to run on Git Bash..."
        if [ $("docker-machine" status) == "Stopped" ]; then
            docker-machine start
        fi
        eval $("docker-machine" env default)
    fi

    echo "------------------------------------------------------------------"
    docker --version
    docker ps --format "table {{.Names}}\t{{.Status}}"
    echo "------------------------------------------------------------------"
}

up() {
    configure
    echo "Starting Docker containers..."
    docker-compose up -d --build
}

down() {
    configure
    echo "Stopping Docker containers..."
    docker-compose down -v
}

case $1 in
    up)
        up
    ;;
    down)
        down
    ;;
    *)
        echo $"Usage: $0 <up|down>"
        exit 1
esac

exit 0