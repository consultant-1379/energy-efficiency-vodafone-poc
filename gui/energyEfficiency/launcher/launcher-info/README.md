# HOW TO CHANGE HREF PATH FROM ENM LAUNCHER TO LOCALHOST OR OTHER HOSTS FOR EE APP (ENERGY EFFICIENCY)
# In the link below is explained the new way to add refs link to apps from ENM Launcher page:
# https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/sites/tor/Presentation_Server/latest/addAppToLauncher.html

1. If not present copy on vApp in the path '/ericsson/tor/data/apps/':
   a) the directory 'energyefficiency' that is inside the directory 'metadata' inside this directory 'energyefficiency',
   put the directory 'locales'.
    OR
   b) the the directory 'energyefficiency' that you can find in the shared area:
   "\\eemea.ericsson.se\eitgodfs01\TEAMS\DepQ41\ENM\Ratatouille\GUI\Launcher_ENM(directory_to_copy_to_vApp)\launcher".

2. In the shared area: "\\eemea.ericsson.se\eitgodfs01\TEAMS\DepQ41\ENM\Ratatouille\GUI\Launcher_ENM(directory_to_copy_to_vApp)":
   there are 2 rpms modified to be copied on the vApp and you have to stop and start httpd service, commands below:
   2.A) Connect to the vApp with VPN Client (ShrewSoft)
   2.B) Connect with a SSH Client to MS of the vApp:
        Host Name: 192.168.0.42
        User Name: root
        Port Number: 22
        Password = 12shroot
        
   After the Connections, use the below commands:
       2.1) cd /var/www/html/ENM_services
       2.2) createrepo .
       2.3) ssh litp-admin@svc-1
       2.4) su
       2.5) hagrp -offline Grp_CS_svc_cluster_httpd -any
       (NOTE: Before running the step 6 below run cmd: hagrp -state | grep httpd
       to verify if the service is really offline)
       2.6) virsh undefine httpd
       2.7) hagrp -online Grp_CS_svc_cluster_httpd -any

3. After launching ENM you will see the link to EE App (energyefficiency).


OPTIONAL changes:
Click F12 button from keyboard and on Console Tab of browser copy and paste the content of file 'changeEELinkFromLauncherToLocalhost.js' 
and click OK button from keyboard. If needed to change path rather then localhost change the value of "href_path" value.
