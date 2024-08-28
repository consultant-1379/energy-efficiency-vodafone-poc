define([
    './Type',
    'jscore/ext/mvp',
    './nodes',
    './bonding',
    'i18n!energyefficiency/dictionary.json',
    '../common'
], function (Type, mvp, nodes, bonding, dictionary, FC) {

    var TX_ARROW = '→'; // 0x2190
    var RX_ARROW = '←'; // 0x2192
    var BOTH_ARROWS = '⇄'; // 0x21c4
    var UNDEF = 4294967295;
    var noUndef = {segIdRx: 1, segIdTx: 1};

    var links = new Type({
        whichTab: 'links',
        title: 'Links',
        singularTitle: 'Link',
        attributes: [
            ['linkId', 'Link Id'],
            ['headId', '', {nodeId: true}],
            ['fromNodeLabel', 'From NE'],
            ['fromPort', 'From Port'],
            ['tailId', '', {nodeId: true}],
            ['toNodeLabel', 'To NE'],
            ['toPort', 'To Port'],
            ['scs', 'Sw. Cap.', {notInTable: true}],
            ['maxBdw', 'Max Bdw [Mbps]'],
            ['opState', 'Op. state'],
            ['bondingId', 'RL Bonding Id'],
            ['ifRef', 'ifRef', {notInTable: true}],
            ['linkType', 'Link Type', {notInTable: true}]
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
                this.collection.setModels([]);
                for (var i = 0; i < links.length; i++) {
                    var currLink = links[i];
                    var l2LinkAttributes = currLink['ietf-l2-topology:l2-link-attributes'];
                    if (!l2LinkAttributes || !l2LinkAttributes['l2-topology-lag:lag-config']) {
                        var currModel = new this.Model();
                        var srcNodeId = currLink.source["source-node"];
                        var srcTpId = currLink.source["source-tp"];
                        var dstNodeId = currLink.destination["dest-node"];
                        var dstTpId = currLink.destination["dest-tp"];
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
                        currModel.setAttribute("linkId", currLink["link-id"]);
                        currModel.setAttribute("headId", srcNodeId);
                        currModel.setAttribute("fromNodeLabel", srcNodeId);
                        currModel.setAttribute("tailId", dstNodeId);
                        currModel.setAttribute("toNodeLabel", dstNodeId);
                        currModel.setAttribute("fromPort", srcTpId);
                        currModel.setAttribute("toPort", dstTpId);
                        currModel.setAttribute("opState", currLink['ietf-te-topology:te'].state['oper-status']);

                        //NOT Used anymore
                        // if (currLink['ietf-l2-topology:l2-link-attributes']) {
                        //     currModel.setAttribute("lagId", currLink['ietf-l2-topology:l2-link-attributes']["l2-topology-lag:lag-id-ref"]);
                        // } else {
                        //     currModel.setAttribute("lagId", '');
                        // }

                        //TODO verify if could be done better, getting attributes from models.
                        if (bonding && bonding.collection && bonding.collection._collection &&
                            bonding.collection._collection.models && bonding.collection._collection.models.length > 0) {
                            var modelsBonding = bonding.collection._collection.models;
                            for (var j = 0; j < modelsBonding.length; j++) {
                                var memberLinksBonding = FC.splitStringToArray(modelsBonding[j].attributes.radioLinks, FC.COMMA_SPLIT_JOIN);
                                if (FC.arrayContains(memberLinksBonding, currLink["link-id"])) {
                                    currModel.setAttribute("bondingId", modelsBonding[j].id);
                                }
                            }
                        }

                        currModel.setAttribute("id", currLink["link-id"]);

                         var linkName = currLink["link-id"];
                         var supportingLink = currLink['supporting-link'];

                         if (supportingLink && supportingLink.length > 0) {
                             currModel.setAttribute("linkType", "bondingLogLink");
                         }
                         else if(linkName.indexOf('LAN') < 0){
                             currModel.setAttribute("linkType", "radioLink");
                         }
                         else {
                             currModel.setAttribute("linkType", "electricLink");
                         }

                        var ifRef = nodes.getInterfaceReference(srcNodeId, srcTpId);
                        currModel.setAttribute("ifRef", ifRef);


                        this.collection.addModel(currModel);
                    }
                }
            }
        },

        // findNode: FC.findNode(nodeId, nodeNames),
        // findTp: FC.findTp(nodeId, tpId, tpNames),

        TX_ARROW: TX_ARROW,
        RX_ARROW: RX_ARROW,
        BOTH_ARROWS: BOTH_ARROWS,
        UNDEF: UNDEF
    });

    var perDir = links.perDirection; // set to links.perDir after new()
    var mvpGetAttribute = mvp.Model.prototype.getAttribute;

    links.Model.prototype.getAttribute = function (attrib) {
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

    nodes.keepNameAligned(links.collection, 'headId', 'fromNodeLabel');
    nodes.keepNameAligned(links.collection, 'tailId', 'toNodeLabel');

    return links;
});