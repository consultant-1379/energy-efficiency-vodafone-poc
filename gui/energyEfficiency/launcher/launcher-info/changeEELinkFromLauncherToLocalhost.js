var href_path = "http://localhost:8585";
changeHref(document.getElementsByTagName('a'));
function changeHref(anchors) {
    for (var i = 0; i < anchors.length; i++) {
        if (anchors[i].text.indexOf("Energy Efficiency") !== -1) {
            var a = document.createElement("a");
            a.href = href_path+"/#energyefficiency";
            a.setAttribute('target', "_blank");
            a.setAttribute('title', "Launch Energy Efficiency");
            a.innerHTML = "Energy Efficiency<span class='eaLauncher-ListItem-acronym'> (EE)</span>" +
                "<span class='eaLauncher-ListItem-externalIcon'><i class='ebIcon ebIcon_externalApp'></i></span>";
            anchors[i].parentNode.replaceChild(a, anchors[i]);
        }
    }
}