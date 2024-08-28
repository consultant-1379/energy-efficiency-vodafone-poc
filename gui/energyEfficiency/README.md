## The User Story Acceptance Tests (USAT)

The USAT tests uses Selenium Webdrivers to interact with each type of browser. The type of browser is selectable via Maven profiles.
The following profiles are available:

 - **browser-chrome** - this is the default browser profile.
 - **browser-firefox**

It's not necessary to set profiles to select webdrivers for each operational system, Maven will pick up the appropriate one automatically.

In addition to the browser profiles, there are profiles for managing and interacting with different environments:

 - **env-local**: it runs the tests in the local browser. The application should be running locally.
 - **env-docker**: it runs the tests pointing to the Docker environment.
 - **run-docker**: it starts/stops the Docker containers before/after tests. It does not run the tests.

The profile `headless` selects the headless mode of some browsers like Chrome and Firefox.

The Docker environment is defined in the `docker-compose.yml` in the project root.

All the Maven commands in this section must be executed in the following folder: `testsuite/usat-functional`.


### Running tests with Chrome locally

Pre-requisite: Start the application (Mock Server).

```
mvn verify -P env-local
```

### Running tests with Chrome in headless mode locally

Pre-requisite: Start the application (Mock Server).

```
mvn verify -P env-local,headless
```

### Running tests with Firefox locally

Pre-requisite: Start the application (Mock Server).

```
mvn verify -P env-local,browser-firefox
```

### Running tests automatically with Chrome, application and Selenium on Docker

```
mvn verify -P run-docker,env-docker
```

### Debugging the application

If you want to debug the application under test, or just want to see the browser being automated, you may connect to
the Selenium server using VNC.
You may downloaded the [VNC Viewer](https://www.realvnc.com/en/connect/download/viewer).

1. Start the application, from the gui folder:
`./docker-compose.sh up`

2. Connect to the Selenium server, use the following URLs in the VNC Viewer:
- On Windows: 192.168.99.100:5900
- On Linux: localhost:5900
- The default password for VNC on Selenium container is `secret`.

3. Run the tests from the folder `testsuite/usat-functional`:
`mvn verify -P env-docker`

========================================================================================================================

+-------------------------------------+
Relevant pom.xml Do(s) and DO Don't(s)
+-------------------------------------+

Do(s)
==========================================

DO use inheritance and integration pom.xml
DO use ${project.version}

Don't(s)
==========================================

DO NOT change the groupId/artifactId ...

CXP Module
==========================================
A valid CXP number must be used, if you want to release you project.
To change the CXP number search for 1234567 in your poms and change it
to the appropriate CXP number, which can be requested from TOR CI EXECUTION TEAM.

COMMAND TO CHANGE THE CXP NUMBER:
find . -name pom.xml -exec sed -i "s/old cxp/new cxp/g" {} \;
and also change the cxp number from the cxp module folder just under your root directory
of this project.  

+----------------------------+
BUILDING
+----------------------------+

- By default build is for devel-environment (no LITP specific implementation modules)
- For production environment you should do a build with activated 'production' profile (mvn clean install -P production_env)
(Please check pom.xml in /ear/ submodule


+----------------------------+
DEPLOYING TO JBOSS
+----------------------------+

System property JBOSS_HOME should point to the installation folder of JBoss.

Once you started JBoss you have to execute mvn jboss-as:deploy to deploy service


+----------------------------+
LINKS FOR MORE USEFUL INFO.
+----------------------------+

TOR CI Execution Main Confluence Page
=====================================

http://confluence-oss.lmera.ericsson.se/display/TORCI/TOR+CI+Execution

Getting Started with CI
=======================

http://confluence-oss.lmera.ericsson.se/display/TORCI/Getting+Started+with+CI
