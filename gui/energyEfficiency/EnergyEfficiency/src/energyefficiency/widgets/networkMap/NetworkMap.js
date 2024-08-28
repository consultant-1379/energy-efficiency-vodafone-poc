define([
    'jscore/core',
    './NetworkMapView',
    '../../types/types',
    'jscore/handlebars/handlebars',
    './transform/Transform',
    '../../common',
    'mapping/Map',
    'mapping/Leaflet',
    'mapping/ZoomControls',
    '../mapMenu/MapMenu',
    'i18n!energyefficiency/dictionary.json',
    '../../common'
    //, './leaflet.textpath'
], function (core, View, types, Handlebars, Transform, CF, thisMap, L, ZoomControls, MapMenu, dictionary, FC) { // , Ltext

    /**
     * 'NetworkMap' shows a map with NEs, links and bonding, with selection, highlighting, and a menu to perform various actions.
     * It is by far the biggest class in the project (with a mix of display logic and business logic), based on the
     * 'mapping' CDS library (SDKUI), and uses Leaflet ('L') directly to draw NEs, links and bonding. If any of the NE has a
     * longitude or latitude the (Openstreetmap) background map is shown, at 30% opacity. It is possible to switch the
     * background Map: Off or 100% opacity, it is hard-wired in 'types/types', and "knows" about their attributes and how they
     * are related. Links and bonding are 'L.polyline'-s. Normally they are single lines, but in case of multiple links between the same pair of
     * nodes they are true polylines. 'highlight(whichTab, id)' highlights NE/links/bonding, with 'this.highlighters[whichTab]'
     * providing the method to do the highlighting. In the end that always causes nodes and/or links to be highlighted.
     * NE are highlighted by adding a '&-highlight-{hl,start,finish}' class whichTab causes a border image to be shown;
     * Links and Bonding are highlighted by changing the style, i.e. the color and/or the dash style. From the 'MapMenu' you
     * can create new NE, links or traffic demands, place the network on the world map, add/remove the background map, zoom to fit.
     * @class widgets/NetworkMap
     * @extends core.Widget
     */
    var DEFAULT_CENTER = [44.425, 8.863]; // Default center of the map (if nothing is read from the local storage) : Genoa
    var MIN_ZOOM = 3;           // Minimum zoom level allowed
    var MAX_ZOOM = 18;          // Maximum zoom level allowed
    var NO_TEXT_ZOOM = 6;       // Zoom level under whichTab no text is shown
    var DEFAULT_ZOOM = 8;       // Default zoom level (if nothing is read from the local storage)
    var HIGHLIGHT_SIZE = 25;    // radius of highlight circle

    // Zoomlevel: 0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17   18   19
    var wSize = [1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3];   // weight of the link at level x
    var wbSize = [6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8, 8, 8];   // weight of the bonding logical link at level x
    var hSize = [0.1, 0.1, 0.3, 0.1, 0.4, 0.5, 0.6, 1.0, 1.1, 1.2, 1.2, 1.2, 1.2, 1.2, 1.3, 1.3, 1.3, 1.3, 1.3, 1.3];   // factor to HIGHLIGHT_SIZE at level x
    var iSize = [3, 4, 6, 8, 10, 15, 20, 25, 30, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35];   // size of the node icon at level x
    // var fSize = [1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.5, 0.5, 0.5, 0.0, 0.0, 0.0, 0.0, 0.0];    // font size factor at level x

    // *** Highlight params
    var weight_3 = 3;

    var classNameLabel = "eaEnergyEfficiency-wNetworkMap-NE-label-";

    // "http://{s}.tile.osm.org/{z}/{x}/{y}.png" // Orignal
    // "http://{s}.tiles.wmflabs.org/bw-mapnik/{z}/{x}/{y}.png" // Black&White
    var openStreetMapTileLayer = 'https://korona.geog.uni-heidelberg.de/tiles/roadsg/x={x}&y={y}&z={z}'; //Grey

    var NodeIcon = L.DivIcon.extend({
        options: {iconSize: [25, 25]},
        initialize: function (options) {
            L.Util.setOptions(this, options);
            this.options.className = 'ebIcon ebIcon_' + options.nodeType + ' eaEnergyEfficiency-wNetworkMap-NE-icon'; // ORIGINAL
            // this.options.className = "eaEnergyEfficiency-wNetworkMap-NE-icon-" + options.nodeType; //TODO - to remove
        }
    });

    var nodeIcon = function (options) {
        return new NodeIcon(options);
    };

    var NodeLabelIcon = L.DivIcon.extend({
        options: {className: classNameLabel + DEFAULT_ZOOM},
        initialize: function (options) {
            L.Util.setOptions(this, options);
        }
    });

    var nodeLabelIcon = function (options) {
        return new NodeLabelIcon(options);
    };

    var fitOnMapTransform = function (map, NEs) {
        var dataBounds = NEs.length ? L.bounds(NEs[0].pos, NEs[0].pos) : L.bounds([-10, -10], [800, 500]);
        for (var i = 1; i < NEs.length; ++i) {
            dataBounds.extend(NEs[i].pos);
        }
        return Transform.fitBoundsTransform(dataBounds, map.getBounds(), {
            minSize: 500,
            padding: 0.1
        });
    };

    var getIncidentLinks = function (links, endPoint) {
        var incident = [];
        for (var id in links) {
            if (links[id].to === endPoint || links[id].from === endPoint) {
                incident.push(links[id]);
            }
        }
        return incident;
    };

    var storeMapState = function (e) {
        var map = e.target;
        localStorage.setItem('mapCenter', map.getCenter().lat + '/' + map.getCenter().lng);
        localStorage.setItem('mapZoom', map.getZoom());
    };

    // INIT Widget
    return core.Widget.extend({

        View: View,

        init: function (options) {
            //TODO - to verify
            this.getDefaults();

            if (!options.zoom) {
                var zoom = localStorage.getItem("mapZoom");
                if (zoom) {
                    options.zoom = +zoom;
                } else { // default 7
                    options.zoom = DEFAULT_ZOOM;
                }
            }
            if (!options.center) {
                var coord = localStorage.getItem("mapCenter");
                if (coord && coord.split("\/").length >= 2) {
                    var center = {'lat': +coord.split("\/")[0], 'lng': +coord.split("\/")[1]};
                    options.center = [center.lat, center.lng];
                } else { // Default -> Genoa
                    options.center = DEFAULT_CENTER;
                }
            }
            if (!options.minZoom) {
                options.minZoom = MIN_ZOOM;
            }
            if (!options.maxZoom) {
                options.maxZoom = MAX_ZOOM;
            }
            if (!options.url && !options.wms) {
                var osmOpacity = localStorage.getItem("osmOpacity");
                if (osmOpacity === null) {
                    this.osmOpacity = 1.0; // default
                } else {
                    this.osmOpacity = +osmOpacity; // force number
                }
                options.url = openStreetMapTileLayer;
            }

            if (!options.height) {
                options.height = options.standalone ? CF.availableHeight() : 200;
            }
            if (this.options.standalone) {
                core.Window.addEventHandler('resize', this.invalidateSize.bind(this));
            }

            this.options = options;

            // Avoid Zoom on doubleClick
            this.options.doubleClickZoom = false;

            this.map = new thisMap(options);
            this.map.addEventHandler("load", this.onMapLoaded.bind(this));

            var lkLabels = localStorage.getItem("lkLabels");
            if (lkLabels === null) {
                this.lkLabels = 0; // default
            } else {
                this.lkLabels = +lkLabels; // force number
            }
            // Controls for changing the zoom of the map
            var zoomControls = new ZoomControls();
            this.map.addComponent(zoomControls);
            this.highlighted = [];
            this.highlightedBounds = new L.LatLngBounds();
            this.mapOpened = true;
        },

        onViewReady: function () {
            this.getElement().setStyle({
                height: this.options.height
            });
        },

        onAttach: function () {
            this.map.attachTo(this.getElement());
        },

        onMapLoaded: function () {
            // Map has been drawn, and the layers are available.
            var renderer = this.map.getRenderer();
            if (!renderer) {
                return;
            }
            renderer.eachLayer(function (layer) {
                if (layer.options.tileSize) {
                    this.tileLayer = this.greyTiles = layer;
                }
            }.bind(this));

            if (this.tileLayer) {
                this.tileLayer.setOpacity(this.osmOpacity);
            }
            this.osmTiles = L.tileLayer(openStreetMapTileLayer, {maxZoom: MAX_ZOOM});

            this.linksLayer = L.featureGroup();
            this.linksLayer.addTo(renderer);

            this.linkIconsLayer = L.featureGroup();
            this.linkIconsLayer.addTo(renderer);

            this.nodesLayer = L.featureGroup();
            this.nodesLayer.addTo(renderer);

            this.nodeLabelsLayer = L.featureGroup();
            this.nodeLabelsLayer.addTo(renderer);

            types.nodes.collection.addEventHandler('change', this.updateNode.bind(this));
            types.links.collection.addEventHandler('change', this.addLinks.bind(this));

            /*
             * MapMenu is a Leaflet control that shows a menu button, opening to a drop down menu (on Top Right of the Map).
             */
            if (!this.menu) {
                this.menu = new MapMenu({
                    items: [
                        {
                            title: dictionary.get("networkMap.backgroundMapTitle"),
                            radio: [
                                {
                                    description: dictionary.get("networkMap.off"),
                                    value: 0
                                },
                                {
                                    description: dictionary.get("networkMap.faint"),
                                    value: 0.6
                                },
                                {
                                    description: dictionary.get("networkMap.opaque"),
                                    value: 1
                                }
                            ],
                            getter: function () {
                                return this.osmOpacity > 0.9 ? dictionary.get("networkMap.opaque") :
                                    this.osmOpacity > 0 ? dictionary.get("networkMap.faint") : dictionary.get("networkMap.off");
                            }.bind(this),
                            setter: this.showBackground.bind(this)
                        },
                        {
                            title: dictionary.get("networkMap.zoomToFitTitle"),
                            action: this.zoomToFit.bind(this)
                        }
                        // ,
                        // { // TODO - for mapping-library version 1.0.0 does not exists work anymore (due to change of Leaflet library v. 1.2.0 used by mapping-library).
                        //     title: dictionary.get("networkMap.linklabelsTitle"),
                        //     radio: [
                        //         {
                        //             description: "none",
                        //             value: 0
                        //         },
                        //         {
                        //             description: "linkId",
                        //             value: 1
                        //         }
                        //     ],
                        //     getter: function () {
                        //         switch (this.lkLabels) {
                        //             case 0:
                        //                 return "none";
                        //             case 1:
                        //                 return "linkId";
                        //             default:
                        //                 return "none";
                        //         }
                        //     }.bind(this),
                        //     setter: function (val) {
                        //         this.lkLabels = +val;
                        //         localStorage.setItem('lkLabels', +val);
                        //         this.reload();
                        //         this.reHighlightSelected();
                        //     }.bind(this)
                        // }

                    ],
                    cancel: this.unhighlight.bind(this)
                });
            }
            renderer.addControl(this.menu);

            renderer.on('zoomend', function (e) {
                for (var i = 0; i < types.nodes.collection._collection.models.length; ++i) {
                    var model = types.nodes.collection._collection.models[i];
                    this.updateNode(model);
                }
                //TODO - To verify
                storeMapState(e);
            }.bind(this)); // save to localStorage

            renderer.on('dragend', storeMapState);

            this.reload();
        },

        reload: function () { // double fetch
            this.getDefaults(); //TODO - To verify
            this.drawTopology();
        },

        getHighlightStyle: function (weight, color) {
            if (!weight) {
                weight = weight_3;
            }
            if (!color) {
                color = FC.BLU_COLOR;
            }
            return {
                color: color,
                weight: weight
            };
        },

        getNeCoordinates: function (model) {
            var newXY = {};
            if (!model) {
                return newXY;
            }
            var coord = localStorage.getItem("ne-" + model.getAttribute("name"));
            if (coord && coord.split("\/").length >= 2) {
                var newLatLng = {
                    lat: parseFloat(+coord.split("/")[0]),
                    lng: parseFloat(+coord.split("/")[1])
                };
                if (this.transform) {
                    newXY = this.transform.toXY(newLatLng);
                }
            }
            var x = model.getAttribute("x");
            var y = model.getAttribute("y");
            var name = model.getAttribute("name");
            if (!newXY.x || !newXY.y) {
                newXY.x = x;
                newXY.y = y;
            } else {
                types.nodes.lastPositions[name] = newXY;
                if (x !== newXY.x || y !== newXY.y) {
                    model.setAttribute("x", newXY.x);
                    model.setAttribute("y", newXY.y);
                }
            }
            return newXY;
        },

        getApCoordinates: function (model) {
            var newXY = {};
            if (!model) return newXY;
            var coord = localStorage.getItem("ap-" + model.getAttribute("name"));
            if (coord && coord.split("\/").length >= 2) {
                var newLatLng = {
                    lat: parseFloat(+coord.split("/")[0]),
                    lng: parseFloat(+coord.split("/")[1])
                };
                newXY = this.transform.toXY(newLatLng);
            }
            if (!newXY.x || !newXY.y) {
                newXY.x = model.getAttribute('x');
                newXY.y = model.getAttribute('y');
            }
            return newXY;
        },

        drawTopology: function () {
            var renderer = this.map.getRenderer();
            if (!renderer) {
                return;
            }
            if (!this.nodesLayer) {
                this.onMapLoaded();
            }
            this.nodesLayer.clearLayers();
            this.nodeLabelsLayer.clearLayers();
            this.linksLayer.clearLayers();

            this.links = {};
            this.perPair = {}; // 'n1/n2' => link id
            this.pairKey = []; //  id => key of perPair

            var NEs = [];
            var curMap = this;
            types.nodes.collection.each(function (model) {
                var coord = curMap.getNeCoordinates(model);
                NEs.push({
                    id: model.getAttribute("name"),
                    pos: L.point(coord.x, coord.y),
                    cid: model.cid
                });
            });
            this.NEs = {};
            if (!this.transform) {
                this.transform = fitOnMapTransform(renderer, NEs);
            }
            for (var i = 0; i < NEs.length; ++i) {
                this.setOrUpdateMarker(NEs[i].id, NEs[i].cid);
            }
            this.addLinks();
        },

        showBackground: function (osmOpacity) {
            this.osmOpacity = +osmOpacity; // force numeric
            localStorage.setItem('osmOpacity', osmOpacity);
            if (osmOpacity) {
                this.setTiles(this.osmTiles, osmOpacity);
            } else {
                this.setTiles(this.greyTiles, 1.0);
            }
        },

        setTiles: function (tileLayer, opacity) {
            tileLayer.setOpacity(opacity);
            if (this.tileLayer !== tileLayer) {
                var map = this.map.getRenderer();
                map.removeLayer(this.tileLayer);
                map.addLayer(tileLayer);
                this.tileLayer = tileLayer;
            }
        },

        setTransform: function (transform) {
            this.transform = transform;
            sessionStorage.setItem('mapTransform', JSON.stringify(transform));
        },

        zoomToLatLngAndSetTransform: function (NEs) {
            var transform = Transform.fitWithoutRotation(NEs);
            this.setTransform(transform);
            var latLngs = [];
            for (var i in NEs) {
                var p = NEs[i];
                if (NEs[i].latLng) {
                    latLngs.push(p.latLng);
                } else {
                    latLngs.push(transform.toLatLng(p.pos));
                }
            }
            var bounds = L.latLngBounds(latLngs);
            bounds.pad(0.1);
            // FitBounds on init can freeze the map, but with 'reset: true' we should not get any animation.
            this.map.getRenderer().fitBounds(bounds, {reset: true});
        },

        setOrUpdateMarker: function (id, cid) {
            var model = types.nodes.collection.getModel(cid);
            if (!model) {
                return undefined;
            }
            var name = model.getAttribute("name");
            if (!name) {
                return undefined;
            }
            var marker = this.NEs[name];
            if (marker) {
                this.nodeLabelsLayer.removeLayer(marker.options.labelMarker);
                this.nodesLayer.removeLayer(marker);
            }
            var x = model.getAttribute('x');
            var y = model.getAttribute('y');

            var labelMarker = null;
            if (this.map.getRenderer().getZoom() > NO_TEXT_ZOOM) { // No label under zoom level NO_TEXT_ZOOM
                var newLabelLatLng = this.transform.toLatLng(L.point(x, y)); // L.point(x - name.length * fSize[this.getActualZoomLevel()], y)
                labelMarker = L.marker(newLabelLatLng, {
                    icon: nodeLabelIcon({
                        html: Handlebars.Utils.escapeExpression(name),
                        className: classNameLabel + this.getActualZoomLevel()
                    })
                });
                labelMarker.addTo(this.nodeLabelsLayer);
            }
            var newLatLng = this.transform.toLatLng(L.point(x, y));
            marker = L.marker(newLatLng, {
                title: name,
                icon: nodeIcon({
                    nodeType: (model.getAttribute('nodeType') === "Unknown") ? "X" : model.getAttribute('nodeType'),
                    iconSize: [iSize[this.getActualZoomLevel()], iSize[this.getActualZoomLevel()]]
                }),
                draggable: true,
                nodeId: cid,
                labelMarker: labelMarker
            });
            marker.on('dragstart', this.onDragStart.bind(this));
            marker.on('drag', this.onDrag.bind(this));
            marker.on('dragend', this.onDragEnd.bind(this));

            var popupContent = "<h3>" + name + "</h3>";
            popupContent += dictionary.get("ipAddress") + model.getAttribute("ipAddress") + "</br>";
            popupContent += dictionary.get("operationState") + model.getAttribute("nodeOpState") + "</br>";
            marker.bindPopup(popupContent, {closeButton: true});

            this.nodesLayer.addLayer(marker);
            this.NEs[name] = marker;
            return marker;
        },

        // TODO - Not Used
        // addToPolyLine: function (points) {
        //     var polyLine = [];
        //     points.forEach(function (vertex) {
        //         polyLine.push(vertex.toLatLong());
        //     });
        //     return polyLine;
        // },

        addLinks: function () {
            var id;
            var NEs = this.NEs;
            if (!NEs) {
                return;
            } // too early
            var links = this.links; // link Id -> {line, from, to }
            var seenIds = {};
            var todo = {}; // set 'n1/n2' => this.perPair['n1/n2']
            var pairKey, portsKey;

            types.links.collection.each(function (model) {
                id = model.getAttribute("id");
                seenIds[id] = true;
                if (links[id]) {
                    return;
                }
                var headId = model.getAttribute("headId");
                var tailId = model.getAttribute("tailId");

                var fromPort = model.getAttribute("fromPort");
                var toPort = model.getAttribute("toPort");

                // var from = NEs[headId];
                // var to = NEs[tailId];

                // markers
                if (!NEs[headId] || !NEs[tailId]) {
                    return;
                } // next, please

                if (headId < tailId) {
                    pairKey = headId + '/' + tailId;
                    portsKey = fromPort + '/' + toPort;
                } else {
                    pairKey = tailId + '/' + headId;
                    portsKey = toPort + '/' + fromPort;
                }
                this.pairKey[id] = pairKey;
                this.perPair[pairKey] = this.perPair[pairKey] || [];
                this.perPair[pairKey].push({
                    id: id,
                    cid: model.cid,
                    ports: portsKey // to check for single Links
                });
                todo[pairKey] = this.perPair[pairKey];
            }.bind(this));

            for (id in links) {
                if (!seenIds[id]) {
                    this.deleteLinkById(id, true);
                }
            }
            for (pairKey in todo) {
                this.addLinksPerPair(todo[pairKey]);
            }
        },

        addLinksPerPair: function (linkIds) {
            // normal case
            var linksLayer = this.linksLayer;
            var links = this.links;
            var collection = types.links.collection;
            var toRemove = [];
            var i;
            for (i = 0; i < linkIds.length; ++i) {
                if (!collection.getModel(linkIds[i].cid)) {
                    toRemove.push(i);
                }
            }
            // remove in reverse order
            while (toRemove.length) {
                var idx = toRemove.pop();
                linkIds.splice(idx, 1);
            }
            if (!linkIds.length) {
                return;
            } // only dead models
            // 'Join' bidirectional links
            var nofPortsConn = {};
            for (i = 0; i < linkIds.length; ++i) {
                nofPortsConn[linkIds[i].ports] = (nofPortsConn[linkIds[i].ports] || 0) + 1;
            }
            var nLinks = Object.keys(nofPortsConn).length;
            // number of (bidirectional or unidirectional) links
            //var width = 6 + 1.5 * nLinks;
            var width = 30 + 1.5 * nLinks;
            var fanoutLength = width;
            var halfDx = nLinks > 1 ? width / 2 / (nLinks - 1) : 1;
            var model = collection.getModel(linkIds[0].cid);
            if (!model) {
                return;
            }
            var headId = model.getAttribute("headId");
            var tailId = model.getAttribute("tailId");
            var from = this.NEs[headId];
            var to = this.NEs[tailId];
            var transform = this.transform;
            if (!from || !to) {
                return;
            }
            var fromLatLng = from.getLatLng();
            var toLatLng = to.getLatLng();
            var nearFromXY, nearToXY, normalXY;
            if (nLinks > 1) {  // prepare
                // need to shift. right from low Id node
                if (headId > tailId) {
                    shift = -shift;
                }
                var fromXY = transform.toXY(fromLatLng);
                var toXY = transform.toXY(toLatLng);
                var distance = fromXY.distanceTo(toXY);
                var vecXY = toXY.subtract(fromXY);
                normalXY = L.point(vecXY.y / distance, -vecXY.x / distance);
                if (distance <= 2 * fanoutLength) {
                    // triangle: center, no 'nearTo
                    nearFromXY = (fromXY.add(toXY)).divideBy(2);  // center
                } else { // paralellogram
                    nearFromXY = fromXY.add(vecXY.multiplyBy(fanoutLength / distance));
                    nearToXY = toXY.subtract(vecXY.multiplyBy(fanoutLength / distance));
                }
            }
            var iLine = 0;
            var doneLink = {};
            for (i = 0; i < linkIds.length; ++i) {
                var id = linkIds[i].id;
                if (doneLink[linkIds[i].ports]) {
                    links[id] = doneLink[linkIds[i].ports];
                    links[id].id2 = id;
                    continue;
                }
                if (links[id]) {
                    this.linksLayer.removeLayer(this.links[id].line);
                }

                var shift = (2 * iLine - (nLinks - 1)) * halfDx;
                // -0.5 width at 0, +0.5 width at N-1; should be === 0 for i = (nlinks -1)/2
                ++iLine;
                var latLngs;
                if (shift === 0 || fromLatLng.equals(toLatLng)) {
                    latLngs = [fromLatLng, toLatLng];
                } else {
                    var shiftXY = normalXY.multiplyBy(shift);
                    latLngs = [fromLatLng, transform.toLatLng(nearFromXY.add(shiftXY))];
                    if (nearToXY) {
                        latLngs.push(transform.toLatLng(nearToXY.add(shiftXY)));
                    }
                    latLngs.push(toLatLng);
                }
                var linkType = collection.getModel(id).attributes.linkType;
                var style = nofPortsConn[linkIds[i].ports] === 1 ?
                    {
                        color: FC.GREY_DEEP_COLOR,
                        dashArray: "3, 7", // Single Link Display
                        weight: wSize[this.getActualZoomLevel()],
                        opacity: 1.0
                    } :
                    {
                        color: FC.GREY_DEEP_COLOR,
                        weight: (linkType === "bondingLogLink") ? wbSize[this.getActualZoomLevel()] : wSize[this.getActualZoomLevel()],
                        //dashArray: (linkType === "radioLink") ? "0.5, 4" : "1", //.........
                        dashArray: (linkType === "radioLink") ? "10, 5" : "1", //_ _ _ _ _
                        opacity: 1.0
                    };

                var polyline = L.polyline(latLngs, style);

                //TODO - For mapping-library version 1.0.0 does not exists work anymore (due to change of Leaflet library v. 1.2.0 used by mapping-library).
                // var label = "";
                // switch (this.lkLabels) {
                //     case 1:
                //         label += model.getAttribute("linkId");
                //         break;
                //     default:
                //         break;
                // }
                // if (label === "undefined") {
                //     label = "";
                // }
                // var z = this.getActualZoomLevel();
                // if (z > this.getZoomLevel(NO_TEXT_ZOOM)) {
                //     polyline.setText(label, {
                //         center: true,
                //         below: true,
                //         offset: -5,
                //         orientation: fromLatLng.lng > toLatLng.lng ? 'flip' : 0,
                //         attributes: {
                //             class: classNameLabel + z
                //         }
                //     });
                // }

                //TODO - Not used for now - Double click on link to highlight/unhighlight it, but it doesn't select the row in table (not working well).
                // polyline.on('dblclick', this.doubleClickHandler('links', id));

                polyline.addTo(linksLayer);
                links[id] = {id: id, from: from, to: to, line: polyline};
                doneLink[linkIds[i].ports] = links[id]; // reuse for other direction
            }
        },

        // TODO - Not Used
        // clickHandler: function (id) {
        //     // return function (e) {
        //     //     e.originalEvent.stopPropagation();
        //     //     e.originalEvent.preventDefault();
        //     //     this.trigger('neSelected', id);
        //     // }.bind(this);
        // },

        //NOT Used for now
        // selectNodeHandler: function (whichTab, attrib, second) {
        //     return function (e) {
        //         var nodeId = e.layer.options.nodeId;
        //         if (!nodeId) {
        //             return;
        //         } // just in case
        //         if (second) {
        //             this.highlightNodes([nodeId], 'finish');
        //             this.selectedNodes[attrib] = nodeId;
        //             this.trigger('selected', whichTab, [0], this.selectedNodes);
        //         } else {
        //             this.unhighlight();
        //             this.selectedNodes = {};
        //             this.selectedNodes[attrib] = nodeId;
        //             this.highlightNodes([nodeId], 'start');
        //         }
        //     }.bind(this);
        // },

        // //Not used for now - Double click on link to highlight/unhighlight it, but it doesn't select the row in table (not working well).
        // doubleClickHandler: function (whichTab, id) {
        //     return function (e) {
        //         e.originalEvent.stopPropagation();
        //         e.originalEvent.preventDefault();
        //         //TODO - OK for tab link (instead of double verse link). For tabs Nodes and Bonding???
        //         if (this.tabSelected && this.tabSelected === FC.linksLabel && this.tabSelectedIds !== id) {
        //             this.trigger('selected', whichTab, [id], this.transform.toXY(e.latlng));
        //             this.highlight(whichTab, id);
        //         }
        //     }.bind(this);
        // },

        //TODO - to verify
        to20_980Transform: function () {
            // transform such that the NEs fit in [20, 980] x [ 20, 980]
            // if there are no poins, or if there is only 1 point, use the current map zoom for guidance.
            var map;
            var bounds = new L.LatLngBounds();

            for (var id in this.NEs) {
                bounds.extend(this.NEs[id].getLatLng());
            }
            if (!bounds.isValid() || (
                bounds.getWest() === bounds.getEast() &&
                bounds.getNorth() === bounds.getSouth())) {
                map = this.map.getRenderer();
                return new Transform({
                    latLng: map.getCenter(),
                    xy: L.point(500, 500),
                    size: 256 * Math.pow(2, map.getZoom())
                });
            } else { //normal case
                return Transform.fitBoundsTransform(
                    L.bounds([20, 20], [960, 960]),
                    bounds,
                    {bigger: 'xy'} // fit latLng points inside that square
                );
            }
        },

        updateNode: function (model) {
            if (!this.NEs) {
                return;
            }
            var nodeName = model.getAttribute("name");
            if (!nodeName) {
                return;
            }
            var marker = this.NEs[nodeName];
            var newMarker = this.setOrUpdateMarker(model.id, model.cid);
            if (marker && newMarker) {
                this.updateIncidentLinks(marker, newMarker);
            }
            this.reHighlightSelected();
        },

        reHighlightSelected: function () {
            this.isReHeiglight = true;
            // Re-highlight what was selected before ofter the drag&drop or zoom.
            if (this.tabSelectedIds && this.tabSelectedIds.length > 0) {
                var selectedIds = this.tabSelected === FC.bondingLabel ? this.tabSelectedIds[0] : this.tabSelectedIds;
                this.highlight(this.tabSelected, selectedIds);
            }
            if (this.selectedLinkDown) {
                this.highlight(this.tabSelected, this.selectedLinkDown);
            }
            this.isReHeiglight = false;
        },

        updateIncidentLinks: function (marker, newMarker) {
            if (!marker) {
                return;
            }
            var i, link;
            // var newLatLng = newMarker.getLatLng();
            var incident = getIncidentLinks(this.links, marker);
            for (i = 0; i < incident.length; ++i) {
                link = incident[i];
                if (link.from === marker) {
                    link.from = newMarker;
                } else {
                    link.to = newMarker;
                }
            }
            var done = {};
            for (i = 0; i < incident.length; ++i) {
                link = incident[i];
                var pairKey = this.pairKey[incident[i].id];
                if (!pairKey) { // link to AP
                    link.line.setLatLngs([link.from.getLatLng(), link.to.getLatLng()]);
                } else if (!done[pairKey]) {
                    var linkIds = this.perPair[pairKey];
                    this.addLinksPerPair(linkIds);
                    done[pairKey] = 1;

                }
            }
        },

        onDragStart: function (e) {
            this._currentDragLatLng = e.target.getLatLng();
        },

        onDrag: function (e) {
            var transform = this.transform;
            var newLatLng = e.target.getLatLng();
            if (!newLatLng.equals(this._currentDragLatLng)) {
                this.updateIncidentLinks(e.target, e.target);
                if (e.target.options.labelMarker) {
                    var xy = transform.toXY(newLatLng);
                    var labelLatLng = transform.toLatLng(xy.subtract([2 * e.target.options.title.length, 0]));
                    e.target.options.labelMarker.setLatLng(labelLatLng);
                }
                if (e.target.options.highlightMarker) {
                    e.target.options.highlightMarker.setLatLng(newLatLng);
                }
                this._currentDragLatLng = newLatLng;
            }
        },

        onDragEnd: function (e) {
            var transform = this.transform;
            var id = e.target.options.nodeId;
            var model;
            var nodeDragged = false;
            if (id) {
                model = types.nodes.collection.getModel(id);
                nodeDragged = true;
            } else {
                return;
            }
            if (e.target.options.labelMarker) {
                var xy = transform.toXY(e.target.getLatLng());
                var labelLatLng = transform.toLatLng(xy.subtract([2 * e.target.options.title.length, 0]));
                e.target.options.labelMarker.setLatLng(labelLatLng);
            }
            if (e.target.options.highlightMarker) {
                e.target.options.highlightMarker.setLatLng(e.target.getLatLng());
            }
            if (model) {
                var newLatLng = e.target.getLatLng();
                var newXY = this.transform.toXY(newLatLng);

                model.setAttribute({x: newXY.x, y: newXY.y});
                if (this.map.getRenderer()) {
                    var newCenter = this.map.getRenderer().getCenter();
                    // mapCenter - localStorage
                    localStorage.setItem("mapCenter", newCenter.lat + "/" + newCenter.lng);
                }
                //TODO - nodeDragged - localStorage - to be verified
                localStorage.setItem((nodeDragged ? "ne-" : "ap-") + model.getAttribute("name"), newLatLng.lat + "/" + newLatLng.lng);
                // Save new position in case of subsequent refresh
                var nodeName = model.getAttribute("name");
                if (nodeName) {  // should always be so
                    types.nodes.lastPositions[nodeName] = {
                        x: newXY.x,
                        y: newXY.y
                    };
                }
            }
            //this.reHighlightSelected();
        },

        unhighlight: function () {
            for (var i = 0; i < this.highlighted.length; ++i) {
                var v = this.highlighted[i];
                if (v.whichTab === 'nodes') {
                    this.nodesLayer.removeLayer(v.object);
                } else if (v.whichTab === FC.linksLabel || v.whichTab === FC.bondingLabel || v.whichTab === FC.lagsLabel) {
                    var dashArray = v.object.options.dashArray;
                    if (dashArray === "0.5, 4") {  //..........
                        dashArray = "10, 5"; //___________
                    }
                    else if (dashArray === "0.5, 8") {  //o o o o o o
                        dashArray = "1";
                    }

                    v.object.setStyle({
                        color: FC.GREY_DEEP_COLOR,
                        dashArray: dashArray
                    });
                }
            }
            this.highlighted = [];
            this.highlightedBounds = undefined;
        },

        setOrExtendHighlightBounds: function (bounds) {
            if (this.bounds) {
                this.bounds.extend(bounds);
            } else {
                this.bounds = bounds;
            }
        },

        highlightLags: function (ids, style) {
            this.tabSelected = FC.lagsLabel;
            this.tabSelectedIds = ids;
            if (!style || style === undefined) {
                style = this.getHighlightStyle();
            }
            if (FC.isStringVar(ids)) {
                link = this.links[ids];
                if (link) {
                    this.highlightExistingLink(link, style);
                }
            } else {
                for (var i = 0; i < ids.length; ++i) {
                    var link = this.links[ids[i]];
                    if (link) {
                        this.highlightExistingLink(link, style);
                    }
                }
            }
        },

        //Used to highlight only bonding phisical link (RL Bonding label)
        highlightBonding: function (ids, style) {
            this.tabSelected = FC.bondingLabel;
            this.tabSelectedIds = ids;
            if (!style || style === undefined) {
                style = this.getHighlightStyle();
            }
            var link;
            var bothPhisicalLinksDown = true;

            //Highlight physical links
            var bondingLinks = this.tabSelectedIds[0] ? this.tabSelectedIds[0] : this.tabSelectedIds;
            for (var i = 0; i < bondingLinks.length; ++i) {
                link = this.links[bondingLinks[i]];
                if (link) {
                    var opState = types[FC.linksLabel].collection.getModel(bondingLinks[i]).attributes.opState;
                    var styleToSet = (opState === FC.OP_STATUS_DOWN) ?
                        {
                            color: FC.GREY_COLOR, //this.map.highlightGreyColor;
                            weight: wSize[this.getActualZoomLevel()],
                            dashArray: "0.5, 4" //.........
                        } :
                        {
                            color: FC.BLU_COLOR,
                            weight: wSize[this.getActualZoomLevel()],
                            dashArray: "10, 5" //_ _ _ _ _
                        };

                    this.highlightExistingLink(link, styleToSet);

                    if (opState !== FC.OP_STATUS_DOWN) {
                        bothPhisicalLinksDown = false;
                    }
                }
            }

            //Highlight logical link
            var bondingId = types.links.collection.getModel(bondingLinks[0]).attributes.bondingId;
            if (bondingId) {
                var logicalLinkColor = FC.BLU_COLOR;
                var logicalLinkDashArray = "1";
                if (bothPhisicalLinksDown === true) {
                    types.bonding.collection.getModel(bondingId).setAttribute("opState", FC.OP_STATUS_DOWN);

                    logicalLinkColor = FC.GREY_COLOR; //this.map.highlightGreyColor;
                    logicalLinkDashArray = "0.5, 8"; //o o o o o o
                }

                var styleToSet4LogicalLink =
                    {
                        color: logicalLinkColor,
                        weight: wbSize[this.getActualZoomLevel()],
                        dashArray: logicalLinkDashArray
                    };

                link = this.links[bondingId];
                this.highlightExistingLink(link, styleToSet4LogicalLink);
            }
        },

        highlightLinks: function (ids, style) {
            this.tabSelected = FC.linksLabel;
            this.tabSelectedIds = ids;
            if (!style || style === undefined) {
                style = this.getHighlightStyle(weight_3);
            }
            if (FC.isStringVar(ids)) {
                link = this.links[ids];
                if (link) {
                    this.highlightExistingLink(link, style);
                }
            } else {
                for (var i = 0; i < ids.length; ++i) {
                    var link = this.links[ids[i]];
                    if (link) {
                        this.highlightExistingLink(link, style);
                    }
                }
            }
        },

        highlightExistingLink: function (link, style) {
            link.line.setStyle(style);
            /*var linkType = "bondingLogLink";
             if (this.tabSelected === FC.linksLabel) {
             linkType = types[this.tabSelected].collection.getModel(link.id).attributes.linkType;
             }
             else if (this.tabSelected === FC.bondingLabel) {
             linkType = "radioLink";
             }

             var dashArray = "1";
             if(style.color === FC.GREY_COLOR){
             dashArray = style.dashArray;
             }
             else if(linkType === "radioLink"){
             dashArray = "10, 5"; //_ _ _ _ _
             }

             link.line.setStyle({
             color: style.color,
             //weight: (linkType === "bondingLogLink") ? style.weight + 3 : style.weight,
             weight: (linkType === "bondingLogLink") ? wbSize[this.getActualZoomLevel()] : wSize[this.getActualZoomLevel()],
             dashArray: dashArray
             });*/
            this.highlighted.push({
                whichTab: this.tabSelected,
                object: link.line
            });
            this.setOrExtendHighlightBounds(link.line.getBounds());
        },

        // highlightElines: function (ids, style) {
        //     this.tabSelected = "elines";
        //     ids = types.elines.collection.getModel(ids).getAttribute("eLinePath").split(", ");
        //     this.tabSelectedIds = ids;
        //     if (!style) {
        //         style = this.getHighlightStyle();
        //     }
        //     for (var i = 0; i < ids.length; ++i) {
        //         if (types.lags.findLinkIdByLagId(ids[i]) !== undefined) {
        //             ids[i] = types.lags.findLinkIdByLagId(ids[i]);
        //         }
        //         var link = this.links[ids[i]];
        //         if (link) {
        //             this.highlightExistingLink(link, style);
        //         }
        //     }
        // },

        highlightNodes: function (ids) {
            this.tabSelected = "nodes";
            if (typeof ids !== 'object') {
                ids = [ids];
            }
            this.tabSelectedIds = ids;
            for (var i = 0; i < ids.length; ++i) {
                var neName = types.nodes.collection.getModel(ids[i]).getAttribute("name");
                var nemarker = this.NEs[neName];
                var marker = L.circleMarker(nemarker.getLatLng(), {
                    color: FC.BLU_COLOR,
                    stroke: false,
                    fillOpacity: 0.5
                });
                this.nodesLayer.addLayer(marker);
                marker.setRadius(HIGHLIGHT_SIZE * hSize[this.getActualZoomLevel()]);
                if (marker) {
                    // marker.setIcon(nodeIcon({suffix: '-hl'}));
                    this.highlighted.push({whichTab: 'nodes', object: marker});
                    // var xy = this.transform.toXY(marker.getLatLng());
                    // this.setOrExtendHighlightBounds(L.latLngBounds(
                    //     this.transform.toLatLng(xy.add([30, 30])),
                    //     this.transform.toLatLng(xy.subtract([30, 30]))
                    // ));
                }
            }
        },

        highlighters: {

            nodes: function (id, style) {
                this.highlightNodes([id]);
            },

            links: function (id, style) {
                this.highlightLinks([id], style);
            },

            bonding: function (id, style) {
                this.highlightBonding([id], style);
            },

            lags: function (id, style) {
                this.highlightLags([id], style);
            }

            // NOT Used
            // elines: function (id, style) {
            //     this.highlightElines([id], style);
            // }
        },

        highlight: function (whichTab, id, unhighlight, style) {
            this.unhighlight();

            if (whichTab === FC.linksLabel && style) {
                this.currentStyle = style;
            } else if (whichTab === FC.bondingLabel && style) {
                this.currentStyle = style;
            } else if (!this.isReHeiglight) {
                this.currentStyle = this.getHighlightStyle();
            }

            if (unhighlight === true || !unhighlight) {
                if (this.map.selectedItem) {
                    this.map.selectedItem = undefined;
                }
            }

            if (!id) {
                return;
            }

            this.highlighted = [];
            this.bounds = undefined;
            this.map.selectedItem = {whichTab: whichTab, id: id};
            var highlighter = this.highlighters[whichTab];
            if (!highlighter) {
                return;
            }
            highlighter.call(this, id, this.currentStyle);
            this.panToHighlights();
        },

        panToHighlights: function () {
            if (!this.highlightedBounds) {
                return;
            }
            var map = this.map.getRenderer();
            var mapBounds = map.getBounds();
            var reducedMapBounds = L.latLngBounds(mapBounds.getSouthWest(), mapBounds.getNorthEast());
            reducedMapBounds.pad(-0.2); // better not too near to the edges.

            var bounds = this.highlightedBounds;

            map.fitBounds(bounds);

            //// TODO - To verify - Not working well.
            // if (reducedMapBounds.contains(bounds)) {
            //     return;
            // } // fits => done
            // var currentZoom = map.getZoom();
            // var maxZoom = map.getBoundsZoom(bounds);
            // if (currentZoom > maxZoom) { // does not fit => zoom
            //     map.fitBounds(bounds);
            // } else {
            //     // will fit => pan (x and/or y as needed)
            //     var oldCenter = map.getCenter();
            //     // get combined bounding box -- but make sure to copy.
            //     var fullBounds = L.latLngBounds(bounds.getSouthWest(), bounds.getNorthEast());
            //     fullBounds.extend(reducedMapBounds);
            //     /*
            //      For each of E/W, N/S: fullBounds either equals mapBounds in that direction, or differs on one side.
            //      If it differs, we need to move in that direction:
            //      map           |----|                          |-------|
            //      full          |----------|               |------------|
            //      => shift by   fullEast - mapEast (>0)    fullWest - mapWest (<0)
            //      */
            //     var newLng = oldCenter.lng + fullBounds.getEast() - reducedMapBounds.getEast() + fullBounds.getWest() - reducedMapBounds.getWest();
            //     var newLat = oldCenter.lat + fullBounds.getNorth() - reducedMapBounds.getNorth() + fullBounds.getSouth() - reducedMapBounds.getSouth();
            //     map.panTo([newLat, newLng]);
            // }
        },

        panBy: function (dx, dy) {
            var renderer = this.map.getRenderer();
            if (renderer) {
                renderer.invalidateSize(false);
                renderer.panBy(L.point(dx, dy), {animate: false});
                this.panToHighlights(); // make sure they are still in?
            }
        },

        closeMap: function () {
            var renderer = this.map.getRenderer();
            if (renderer) {
                renderer.remove();
            }
            this.mapOpened = false;
        },

        zoomToFit: function () {
            this.highlightedBounds = new L.LatLngBounds();
            for (var i in this.NEs) { // this.NEs = {} (not [])
                this.highlightedBounds.extend(this.NEs[i].getLatLng());
            }
            this.panToHighlights();
        },

        invalidateSize: function () {
            // window resized. let the map known, and check a few more times to be sure.
            if (this.options.standalone) {
                this.getElement().setStyle({height: CF.availableHeight()});
            }
            var renderer = this.map.getRenderer();
            if (!renderer) {
                return;
            }
            this.currentHeight = this.view.getElement().getProperty('offsetHeight');
            renderer.invalidateSize();
            this.heightCheckCount = 4;
            window.requestAnimationFrame(this.recheckHeight.bind(this));
        },

        recheckHeight: function () {
            var newHeight = this.view.getElement().getProperty('offsetHeight');
            --this.heightCheckCount;
            if (newHeight !== this.currentHeight) {
                this.invalidateSize(); // and start again
            } else if (this.heightCheckCount) { // check again a bit later
                window.requestAnimationFrame(this.recheckHeight.bind(this));
            }
        },

        // /**
        //  * @method drawApLink
        //  * @param {type} fromMarker
        //  * @param {type} toMarker
        //  * @param {type} color
        //  * @param {type} weight
        //  */
        // // Not Used for now
        // drawApLink: function (fromMarker, toMarker, color, weight) {
        //     if (!color) {
        //         color = FC.GREY_DEEP_COLOR;
        //     }
        //     var polyline = L.polyline([fromMarker.getLatLng(), toMarker.getLatLng()], {
        //             color: color,
        //             weight: (weight && weight > 0) ? weight : wSize[this.getActualZoomLevel()],
        //             opacity: 1.0
        //         }
        //     );
        //     // Not used for now - Double click on link to highlight/unhighlight it, but it doesn't select the row in table (not working well).
        //     // polyline.on('dblclick', this.doubleClickHandler('links', id));
        //     polyline.addTo(this.linksLayer);
        //     return polyline;
        // },

        //TODO - to verify
        getDefaults: function () {
            var defCenter = localStorage.getItem('defaultCenter');
            if (defCenter) {
                DEFAULT_CENTER = [parseFloat(defCenter.split('/')[0]), parseFloat(defCenter.split('/')[1])];
            }

            var minZoom = localStorage.getItem('minZoom');
            if (minZoom) {
                MIN_ZOOM = +minZoom;
            }

            var maxZoom = localStorage.getItem('maxZoom');
            if (maxZoom) {
                MAX_ZOOM = +maxZoom;
            }

            var defZoom = localStorage.getItem('defZoom');
            if (defZoom) {
                DEFAULT_ZOOM = +defZoom;
            }

            var ntZoom = localStorage.getItem('ntZoom');
            if (ntZoom) {
                NO_TEXT_ZOOM = +ntZoom;
            }
        },

        getActualZoomLevel: function () {
            var ret = this.map.getRenderer().getZoom() + 7 - DEFAULT_ZOOM;
            if (ret < 0) {
                ret = 0;
            }
            if (ret > 19) {
                ret = 19;
            }
            return ret;
        },

        getZoomLevel: function (l) {
            var ret = l + 7 - DEFAULT_ZOOM;
            if (ret < 0) {
                ret = 0;
            }
            if (ret > 19) {
                ret = 19;
            }
            return ret;
        },

        setSelectedTabAndIds: function (tab, ids) {
            this.tabSelected = tab;
            this.tabSelectedIds = ids;
        },

        removeSelectedBefore: function () {
            this.tabSelected = "";
            this.tabSelectedIds = undefined;
        }

    });

});