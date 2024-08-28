#!/bin/bash

##############################
# Install UISDK dependencies #
##############################

function installdeps {
  if [ -d "$1" ]; then
    cd $1
    cdt2 package install --autofill
    cd ..
  fi
}
installdeps energyefficiency

######################################
# Build and install local dependency #
######################################

cd grammarparsinglibrary/
node -i ../energyefficiency/energyefficiency.g4
cd ..

#######################
# Link local packages #
#######################

function linklib {
  if [ -d "$1" ]; then
    cd energyefficiency
    cdt2 package link ../$1
    cd ..
  fi
}

# linklib ....
