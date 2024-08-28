define([
    './Type',
    'i18n!energyefficiency/dictionary.json'
], function (Type, dictionary) {

    var mergeNodes = function (aNodes, bNodes) {
        var cNode = {};
        if (Array.isArray(aNodes)) {
            cNode = [];
        }
        if (typeof bNodes === "string") {
            return bNodes;
        }
        for (var aNodeIt in bNodes) {
            var aNode = aNodes[aNodeIt];
            var bNode = bNodes[aNodeIt];
            if (aNode) {
                if (bNode) {
                    cNode[aNodeIt] = mergeNodes(aNode, bNode);
                } else {
                    cNode[aNodeIt] = aNode;
                }
            } else {
                cNode[aNodeIt] = bNode;
            }
        }
        return cNode;
    };

    var nodes = new Type({
        whichTab: 'nodes',
        title: dictionary.get('networkElements'),
        singularTitle: dictionary.get('networkElement'),
        attributes: [
            ['name', dictionary.get('node.name')],                  //, {width: 200}
            ['ipAddress', dictionary.get('node.ipAddress')],        //, {width: 150}
            ['nodeProducer', dictionary.get('node.manufacturer')],  //, {width: 150}
            ['nodeOpState', dictionary.get('node.opState')],        //, {width: 100}
            ['latitude', 'Latitude', {notInTable: true}],
            ['longitude', 'Longitude', {notInTable: true}],
            ['x', 'X', {notInTable: true}],
            ['y', 'Y', {notInTable: true}]
        ],

        collectionFromController: function (data) {
            return this.collectionForCtrl(data);
        },

        collectionForCtrl: function (data) {
            if (data === undefined || !data || !data.network[0]) {
                return;
            }
            this.collection.setModels([]);

            for (var i = 0; i < data.network[0].node.length; i++) {
                var currentNode = data.network[0].node[i];
                var currModel = new this.Model();

                currModel.setAttribute("node-id", currentNode['node-id']);
                currModel.setAttribute('ipAddress', currentNode['ietf-l2-topology:l2-node-attributes']['management-address'][0]);
                currModel.setAttribute("name", currentNode['ietf-l2-topology:l2-node-attributes'].name);
                currModel.setAttribute("nodeOpState", currentNode['ietf-te-topology:te'].state['oper-status']);
                currModel.setAttribute("nodeType", dictionary.get('microwave'));
                currModel.setAttribute("nodeProducer", dictionary.get('ericsson'));
                currModel.setAttribute("id", currentNode['node-id']);
                currModel.setAttribute("ifRefs", currentNode["ietf-network-topology:termination-point"]);

                this.updatePositions(currModel); // Positional attrs (for map)

                this.collection.addModel(currModel);
            }
        },

        updatePositions: function (currModel) {
            // Positional attrs (for map)
            var lastPosition = this.lastPositions[currModel.getAttribute("name")];
            if (lastPosition) {
                currModel.setAttribute("x", lastPosition.x);
                currModel.setAttribute("y", lastPosition.y);
            } else {
                var xx = Math.floor((Math.random() * 600) + 1);
                var yy = Math.floor((Math.random() * 600) + 1);

                currModel.setAttribute("x", xx);
                currModel.setAttribute("y", yy);
                this.lastPositions[currModel.getAttribute("name")] = {
                    x: xx,
                    y: yy
                };
            }
            currModel.setAttribute("latitude", 1000);
            currModel.setAttribute("longitude", 2000);
        },

        clearPositions: function () {
            for (var i = 0; i < this.collection._collection.models.length; ++i) {
                var currModel = this.collection._collection.models[i];
                var xx = Math.floor((Math.random() * 600) + 1);
                var yy = Math.floor((Math.random() * 600) + 1);
                currModel.setAttribute("x", xx);
                currModel.setAttribute("y", yy);
                this.lastPositions[currModel.getAttribute("name")] = {
                    x: xx,
                    y: yy
                };
                currModel.setAttribute("latitude", 1000);
                currModel.setAttribute("longitude", 2000);
            }
        },

        getInterfaceReference: function (srcNodeId, srcTpId) {
            var collections = this.collection._collection.models;
            for (var m = 0; m < collections.length; m++) {
                if (collections[m].getAttribute('id') === srcNodeId) {
                    var ifRefsNode = collections[m].getAttribute("ifRefs");
                    if (ifRefsNode) {
                        for (var n = 0; n < ifRefsNode.length; n++) {
                            if (ifRefsNode[n]['tp-id'] === srcTpId) {
                                return ifRefsNode[n]["network-topology-interfaces:if-ref"];
                            }
                        }
                    }
                }
            }
        }

    });

    /*
     * Check whether name exists, if not emtpy returns id of the node with that name or zero
     */
    nodes.nameExists = function (name) {
        return this.attribExists('name', name);
    };

    nodes.nodeItems = function () {
        // array-ify id, to avoid matching the name against the value: otherwise if we happen to have a node with id = '3', name = '5' we'll run into trouble
        var items = [];
        this.collection.each(function (model) {
            items.push({
                name: model.getAttribute('name'),
                value: [model.getAttribute('id')]
            });
        });
        return items;
    };

    nodes.keepNameAligned = function (collection, idAttrib, nameAttrib) {
        nodes.collection.addEventHandler('change:name', function (model) {
            var nodeId = '' + model.getAttribute('id'); // stringify
            var nodeName = model.getAttribute('name');
            collection.each(function (model) {
                if ('' + model.getAttribute(idAttrib) === nodeId) {
                    model.setAttribute(nameAttrib, nodeName);
                }
            });
        });
    };

    // Management of coordinates
    nodes.lastPositions = {}; // each item in form "name" : {x: ..., y: ...}

    return nodes;
});