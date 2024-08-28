define([
    './nodes',
    './links',
    './bonding'
], function (nodes, links, bonding) {

    return {
        nodes: nodes,
        bonding: bonding,
        links: links,
        eod: null // END types to cycle, should be at the end.

    };

});