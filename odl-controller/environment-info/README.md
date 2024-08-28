## TO BUILD AND RUN THE ODL CONTROLLER on UBUNTU ##

1. Install apache maven 3.3.9 or later version

     cd /usr/local
     wget http://www-eu.apache.org/dist/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz
     sudo tar xzf apache-maven-3.3.9-bin.tar.gz
     sudo ln -s apache-maven-3.3.9 apache-maven

2. Install JAVA 8

     sudo add-apt-repository ppa:webupd8team/java
     sudo apt-get update
     sudo apt-get install oracle-java8-installer

3. setting JAVA environment variable to compile and run with JAVA 8, run bash shell

     bash
     export JAVA_HOME=/usr/lib/jvm/java-8-oracle
     export JRE_HOME=/usr/lib/jvm/java-8-oracle/jre

4. make sure git has proper global configured user name and email:

     git config –-list

     git config –-global user.name <username>
     git config –-global user.email <email address>
     git config –-list

5. check-out the energy-efficiency-vodafone-poc.git from Ericsson gerrit

     git clone ssh://<username>@gerrit.ericsson.se:29418/OSS-PROTO/com.ericsson.oss.mediation/energy-efficiency-vodafone-poc.git

6. configure git in terms of commit-msg:

    (cd energy-efficiency-vodafone-poc && curl -kLo `git rev-parse --git-dir`/hooks/commit-msg https://<username>@gerrit.ericsson.se/tools/hooks/commit-msg; chmod +x `git rev-parse --git-dir`/hooks/commit-msg)
    
7. Copy the environment-info/settings.xml file in your .m2 directory

     cd energy-efficiency-vodafone-poc/odl-controller
     copy environment-info/settings.xml /home/guest/.m2
     
    NOTE: you may also skip this step and use the mvn -gs option detailed ahead 

8. From odl-controller folder, run:

     mvn clean install -DskipTests

   NOTE: if you do not want to copy the settings.xml into .m2 directory (step #3), you may run:

     mvn -gs environment-info/settings.xml clean install -DskipTests

9. Once the odl controller software build is successfully finished, from odl-controller folder run:

     ./start-up/odl-start

    You should see the ODL controller karaf container starting and in a short time you can interact
    with the ODL NBI RESTconf interface