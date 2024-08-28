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

    var bonding = new Type({
        whichTab: FC.bondingLabel,
        title: 'RL Bonding',
        singularTitle: 'RL Bonding',
        attributes: [
            ['bondingId', 'RL Bonding Id'],
            ['radioLinks', 'Radio Links'],
            ['maxBdw', 'Max Bdw [Mbps]'],
            ['opState', 'Op. state'],
            ['headId', '', {nodeId: true, notInTable: true}],
            ['tailId', '', {nodeId: true, notInTable: true}],
            ['fromPort', 'From Port', {notInTable: true}],
            ['toPort', 'To Port', {notInTable: true}],
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
                var currModel;
                this.collection.setModels([]);

                this.linksOfBonding = new Map(); // Map memberlinks of bonding

                for (var i = 0; i < links.length; i++) {
                    var currLink = links[i];
                    var supportingLink = currLink['supporting-link'];

                    if (supportingLink && supportingLink.length > 0) {
                        currModel = new this.Model();
                        var linksOfBonding = [];

                        var srcNodeId = currLink.source["source-node"];
                        var srcTpId = currLink.source["source-tp"];
                        var dstNodeId = currLink.destination["dest-node"];
                        var dstTpId = currLink.destination["dest-tp"];

                        currModel.setAttribute("linkType", "bondingLogLink");
                        currModel.setAttribute("bondingId", currLink['link-id']);
                        currModel.setAttribute("id", currLink['link-id']);

                        var memberLink = supportingLink;
                        var radioLinksByComma = [];
                        for (var j = 0; j < memberLink.length; j++) {
                            linksOfBonding.push(memberLink[j]["link-ref"]);
                            radioLinksByComma.push(memberLink[j]["link-ref"]);
                        }

                        this.linksOfBonding[currLink['link-id']] = linksOfBonding;

                        currModel.setAttribute("radioLinks", FC.joinArrayToString(radioLinksByComma, FC.COMMA_SPLIT_JOIN));
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

        TX_ARROW: TX_ARROW,
        RX_ARROW: RX_ARROW,
        BOTH_ARROWS: BOTH_ARROWS,
        UNDEF: UNDEF,

        getLinksOfBonding: function (id) {
            return this.linksOfBonding[id];
        }
    });

    var perDir = bonding.perDirection; // set to bonding.perDir after new()
    var mvpGetAttribute = mvp.Model.prototype.getAttribute;

    bonding.Model.prototype.getAttribute = function (attrib) {
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

    nodes.keepNameAligned(bonding.collection, 'headId', 'fromNodeLabel');
    nodes.keepNameAligned(bonding.collection, 'tailId', 'toNodeLabel');

    return bonding;
});