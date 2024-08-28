define([
    './Type',
    'i18n!energyefficiency/dictionary.json',
    '../common'
], function (Type, dictionary, FC) {

    var elines = new Type({
        whichTab: 'elines',
        title: 'E-Lines',
        singularTitle: 'E-Line',
        attributes: [
            ['eLineId', 'E-Line Id'],
            ['eLinePath', 'E-Line Path']
        ],

        collectionFromController: function (data) {
            return this.collectionForCtrl(data);
        },

        collectionForCtrl: function (data) {
            if (data === undefined || !data && !data['e-lines']) {
                return;
            }
            this.collection.setModels([]);

            for (var i = 0; i < data['e-lines']['e-line'].length; i++) {
                var currentELine = data['e-lines']['e-line'][i];
                var currModel = new this.Model();

                currModel.setAttribute("eLineId", currentELine['e-line-id']);

                var unorderedLinkPaths = currentELine["link-path"];

                var orderedLinkPaths = FC.sortArrayByKey(unorderedLinkPaths, 'index');

                var linkPathsByComma = [];
                for (var j = 0; j < orderedLinkPaths.length; j++) {
                    linkPathsByComma.push(orderedLinkPaths[j]["link-id"]);
                }
                currModel.setAttribute('eLinePath', linkPathsByComma.join(", "));

                currModel.setAttribute("id", currentELine['e-line-id']);

                this.collection.addModel(currModel);
            }
        }

    });
    return elines;

});