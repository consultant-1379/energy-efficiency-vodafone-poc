define([
    './nodes',
    './links',
    './bonding',
    './lags',
    './elines',
    './statusMessages'
], function (nodes, links, bonding, lags, elines, statusMessages) {

    return {
        nodes: nodes,
        bonding: bonding,
        links: links,
        statusMessages: statusMessages,
        eod: null, // END types to cycle, should be at the end.
        lags: lags,
        elines: elines // Elines is after eod (last type call), because if needed (for now not visible) has it's own rest call.

        // getNodeByIntfId: function (addr, as) {
        //     for (var i = 0; i < this.nodes.collection._collection.models.length; ++i) {
        //         var model = this.nodes.collection._collection.models[i];
        //         var tps = model.getAttribute("tps");
        //         if (tps) {
        //             for (var j = 0; j < tps.length; ++j) {
        //                 var tp = tps[j];
        //                 if (!tp) {
        //                     continue;
        //                 }
        //                 if (as && tp["as-number"] !== as) {
        //                     continue;
        //                 }
        //                 if (tp.name.split(':')[1] === addr) {
        //                     return model;
        //                 }
        //             }
        //         }
        //     }
        //     return undefined;
        // },
        //
        // getTpName: function (nodeModel, addr) {
        //     var tps = nodeModel.getAttribute("tps");
        //     if (tps) {
        //         for (var j = 0; j < tps.length; ++j) {
        //             var tp = tps[j];
        //             if (tp && tp.name.split(':')[1] === addr) {
        //                 return tp.name;
        //             }
        //         }
        //     }
        //     return undefined;
        // },
        //
        // /**
        //  * Return the operational/administrative status of the given object (if applicable). Operational status takes
        //  * precedence over administrative status (a down/locked object shall be reported as 'down').
        //  * If not applicable or up/unlocked it returns empty string.
        //  * @param {type} model
        //  * @returns {String}
        //  */
        // getStatus: function (model) {
        //     return model.getAttribute("opStatus");
        // }

    };

});