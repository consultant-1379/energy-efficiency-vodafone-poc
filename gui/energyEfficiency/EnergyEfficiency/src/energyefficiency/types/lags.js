define([
    './Type',
    'jscore/ext/mvp',
    './nodes',
    'i18n!energyefficiency/dictionary.json',
    '../common'
], function (Type, mvp, nodes, dictionary, FC) {

    var TX_ARROW = '→';     // 0x2190
    var RX_ARROW = '←';     // 0x2192
    var BOTH_ARROWS = '⇄';  // 0x21c4
    var UNDEF = 4294967295;
    var noUndef = {segIdRx: 1, segIdTx: 1};

    var lags = new Type({
        whichTab: FC.lagsLabel,
        title: 'Lags',
        singularTitle: 'Lag',
        attributes: [
            ['lagId', 'Lag Id'],
            ['masterLink', 'Master Link'],
            ['otherLinks', 'Other Links'],
            ['maxBdw', 'Max Bdw [Mbps]'],
            ['opState', 'Op. state'],
            ['headId', '', {nodeId: true, notInTable: true}],
            ['tailId', '', {nodeId: true, notInTable: true}],
            ['fromPort', 'From Port', {notInTable: true}],
            ['toPort', 'To Port', {notInTable: true}],
            ['ifRef', 'ifRef', {notInTable: true}]
        ],

        collectionFromController: function (data) {
            return this.collectionForCtrl(data);
        },

        collectionForCtrl: function (data) {
            if (data === undefined || !data || !data.network[0]) {
                return;
            }
            var links = data.network[0]['ietf-network-topology:link'];

            // check if links is not none
            if (links.length !== null) {
                var currModel;
                this.collection.setModels([]);

                this.linksOfLag = []; // All memberlinks of lags

                var l2TopoLag = "l2-topology-lag:";
                for (var i = 0; i < links.length; i++) {
                    var currLink = links[i];
                    var l2LinkAttributes = currLink['ietf-l2-topology:l2-link-attributes'];
                    if (l2LinkAttributes && l2LinkAttributes[l2TopoLag + 'lag-config']) {
                        currModel = new this.Model();

                        var srcNodeId = currLink.source["source-node"];
                        var srcTpId = currLink.source["source-tp"];
                        var dstNodeId = currLink.destination["dest-node"];
                        var dstTpId = currLink.destination["dest-tp"];

                        currModel.setAttribute("lagId", l2LinkAttributes[l2TopoLag + 'lag-config']['lag-id']);
                        var memberLink = l2LinkAttributes[l2TopoLag + 'lag-config']['member-link'];

                        currModel.setAttribute("id", l2LinkAttributes[l2TopoLag + 'lag-config']['lag-id']);

                        var otherLinksByComma = [];
                        for (var j = 0; j < memberLink.length; j++) {
                            this.linksOfLag.push(memberLink[j]["link-id"]);
                            if (memberLink[j].master === "true" || memberLink[j].master === true) {
                                currModel.setAttribute("masterLink", memberLink[j]['link-id']);
                                // currModel.setAttribute("id", memberLink[j]['link-id']);
                            } else if (memberLink[j].master === "false" || memberLink[j].master === false) {
                                otherLinksByComma.push(memberLink[j]["link-id"]);
                            }
                        }
                        currModel.setAttribute("otherLinks", otherLinksByComma.join(", "));
                        currModel.setAttribute("opState", currLink['ietf-te-topology:te'].state['oper-status']);

                        var maxbdw;
                        var teLinkAttributes = currLink['ietf-te-topology:te'].state['te-link-attributes'];
                        if (teLinkAttributes) {
                            if (teLinkAttributes['max-link-bandwidth']) {
                                maxbdw = teLinkAttributes['max-link-bandwidth'] * 8 / 1000000;
                            }
                        }
                        if (maxbdw !== undefined) {
                            currModel.setAttribute("maxBdw", maxbdw);
                        }

                        currModel.setAttribute("headId", srcNodeId);
                        currModel.setAttribute("tailId", dstNodeId);
                        currModel.setAttribute("fromPort", srcTpId);
                        currModel.setAttribute("toPort", dstTpId);

                        var ifRef = nodes.getInterfaceReference(srcNodeId, srcTpId);
                        currModel.setAttribute("ifRef", ifRef);


                        this.collection.addModel(currModel);
                    }
                }
            }
        },

        // findNode: FC.findNode(nodeId, nodeNames),
        //
        // findTp: FC.findTp(nodeId, tpId, tpNames),

        /**
         * Search an return the linkId of the lagId passed as param, if not present return undefined.
         *
         * @param lagId
         * @returns {*} linkId || undefined
         */
        findLinkIdByLagId: function (lagId) {
            var model = this.collection.search(lagId, ["lagId"]);
            var linkId;
            if(model.length > 0 && model[0].lagId !== lagId) {
                linkId = model[0].id;
            }
            return linkId;
        },

        TX_ARROW: TX_ARROW,
        RX_ARROW: RX_ARROW,
        BOTH_ARROWS: BOTH_ARROWS,
        UNDEF: UNDEF,

        getLinksOfLag: function() {
            return this.linksOfLag;
        }
    });

    var perDir = lags.perDirection; // set to lags.perDir after new()
    var mvpGetAttribute = mvp.Model.prototype.getAttribute;

    lags.Model.prototype.getAttribute = function (attrib) {
        if (perDir[attrib]) {
            var Tx = this.getAttribute(attrib + 'Tx');
            var Rx = this.getAttribute(attrib + 'Rx');
            if (Tx === Rx) {
                return Tx;
            } else {
                return '→ ' + Tx + ' ← ' + Rx;
            }
        } else {
            var value = mvpGetAttribute.call(this, attrib);
            if (noUndef[attrib] && +value === UNDEF) { // +: as number
                value = '';
            }
            return value;
        }
    };

    nodes.keepNameAligned(lags.collection, 'headId', 'fromNodeLabel');
    nodes.keepNameAligned(lags.collection, 'tailId', 'toNodeLabel');

    return lags;
});