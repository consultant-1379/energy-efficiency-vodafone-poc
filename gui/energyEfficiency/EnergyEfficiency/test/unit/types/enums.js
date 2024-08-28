/*global define */
define([], function () {

    // Not used for now.

    var EnumType = function (names, negOne, wantedNames) {
        var wanted, i;
        if (negOne) {
            names[-1] = negOne;
        }
        this.names = names;

        if (wantedNames) {
            wanted = {};
            for (i = 0; i < wantedNames.length; ++i) {
                wanted[wantedNames[i]] = true;
            }
        }

        var items = [];
        var itemFor = [];
        if (negOne && (!wanted || wanted[negOne])) {
            var negOneItem = {name: negOne, value: -1};
            items.push(negOneItem);
            itemFor[-1] = negOneItem;
        }
        for (i = 0; i < names.length; ++i) {
            if (names[i] && (!wanted || wanted[names[i]])) {
                var item = {name: names[i], value: i};
                items.push(item);
                itemFor[i] = item;
            }
        }
        this.items = items;
        this.itemFor = itemFor;
    };

    EnumType.prototype.isValid = function (value) {
        return !!this.names[value];
    };

    //TODO
    var enums = {};

    return enums;
});