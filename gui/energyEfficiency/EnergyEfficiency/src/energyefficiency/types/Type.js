define([
    'jscore/ext/mvp',
    './enums',
    '../datastore/datastore',
    '../common'
], function (mvp, enums, datastore, FC) {

    var types = {};

    var compareNumerically = function (a, b) {
        return a - b;
    };

    var UNDEF = 4294967295;
    var noUndef = function (value) {
        return (+value === UNDEF) ? "" : value;
    };

    // hex to decimal == parseInt(<value>, 16)
    var toHex = function (decValue) {
        if (!decValue) {
            return "";
        }
        var hexValue = (decValue >>> 0).toString(16);
        if (hexValue.length && 1) {
            hexValue = '0' + hexValue;
        }
        return hexValue;
    };

    var displayType = function (value, options) {
        if (!value) {
            return "";
        }
        var childType = types[options.isType];
        var childModel = childType && childType.collection.getModel(value);
        return childModel ? childModel.getDescription() : "[" + value + "]";
    };

    var displayEnum = function (value, options) {
        return enums[options.enumType].names[value];
    };

    var displayBool = function (value) {
        return value ? '\u2714' : ''; // '✔' U+2714 heavy check mark
    };

    var parse_attributes = function (obj, attributes) {
        // attributes is [ [ attrib, title, options ]
        obj.attributes = {};
        obj.columns = [];
        obj.inputs = {};
        obj.perDirection = [];
        obj.nodeFields = []; // { id, label }
        obj.enumFields = []; // attrib
        obj.boolFields = []; // attrib
        var nodeId;
        for (var i = 0; i < attributes.length; ++i) {
            var attribute = attributes[i];
            var attrib = attribute[0];
            var title = attribute[1];
            var options = attribute[2] || {};
            if (nodeId) {
                obj.nodeFields.push({id: nodeId, label: attrib});
                options.nodeLabel = true;
                nodeId = undefined;
            }
            if (options.nodeId) {
                nodeId = attrib;
            } // & use next
            if (options.enumType) {
                obj.enumFields.push(attrib);
                options.displayFunction = displayEnum;
            } else if (options.isBool) {
                obj.boolFields.push(attrib);
                options.displayFunction = displayBool;
            } else if (options.isType) {
                obj.typeFields.push(attrib);
                options.displayFunction = displayType;
            } else if (options.inHex) {
                options.displayFunction = toHex;
            } else if (options.noUndef) {
                options.displayFunction = noUndef;
            }
            options.attribute = attrib;
            obj.attributes[attrib] = options;
            if (!title && !options.nodeId) continue;
            // no name => no input/output, but keep nodeId-s

            options.title = title;
            if (options.sort === 'numeric') {
                options.compare = compareNumerically;
            }
            if (!options.notInTable && !options.nodeId) {
                obj.columns.push(options); // or selected?
            }
            if (options.directional) {
                obj.inputs[attrib] = options;
                obj.inputs[attrib + "Tx"] = options;
                obj.inputs[attrib + "Rx"] = options;
                obj.attributes[attrib + "Tx"] = options;
                obj.attributes[attrib + "Rx"] = options;
                obj.perDirection[attrib] = 1;
            } else if (!options.notInput) {
                obj.inputs[attrib] = options;
            }
        }
    };

    var TypeModel = mvp.Model.extend({
        displayAttribute: function (attrib) {
            var value = this.getAttribute(attrib);
            var options = this.attributeOptions[attrib];
            if (!options) {
                return value;
            }
            return options.displayFunction ? options.displayFunction(value, options) : value;
        },

        inputAttribute: function (attrib) {
            var value = this.getAttribute(attrib);
            var options = this.attributeOptions[attrib];
            if (options.enumType) {
                if (value === undefined || value === '') {
                    return {};
                } else {
                    return {
                        name: enums[options.enumType].names[value],
                        value: value
                    };
                }
            } else {
                return value;
            }
        },
        getDescription: function () {
            return this.getAttribute('name');
        }
    });

    var Type = function (options) {
        parse_attributes(this, options.attributes);  // Add attributes to this (create this.attributes)
        var attributeOptions = this.attributes;

        delete options.attributes;
        this.url = '';
        var url = '';

        var Model = TypeModel.extend({
            url: url,
            attributeOptions: attributeOptions
        });

        this.Model = Model;

        var Collection = mvp.Collection.extend({
            url: url,
            Model: Model
        });

        this.Collection = Collection;
        this.collection = new Collection();

        this.empty = {
            attributeOptions: attributeOptions,
            getAttribute: function (attribute) {
                return "";
            },
            inputAttribute: TypeModel.prototype.inputAttribute,
            displayAttribute: function (attribute) {
                return "";
            }
        };

        for (var child in options) {
            this[child] = options[child];
        }

        if (options.collectionFromController) {
            this.collectionFromController = options.collectionFromController;
        } else {
            this.collectionFromController = function (data) {
            };  // default version, does nothing
        }
        this.selectedId = 0;
        types[options.whichTab] = this; // register, for use in isType fields
    };

    Type.prototype.attribExists = function (attrib, value) {
        if (!value) {
            return 0;
        }
        var existing = this.collection.findModel(function (model) {
            return model.getAttribute(attrib) === value;
        });
        return existing ? existing.getAttribute("id") : 0;
    };

    TypeModel.prototype.getNested = function (attrib) {
        var fields = attrib.split('.');
        var value = this.getAttribute(fields[0]);
        for (var i = 1; i < fields.length; ++i) {
            if (typeof value !== "object") {
                return undefined;
            }
            value = value[fields[i]];
        }
        return value;
    };
    Type.prototype.adjustItems = function (items) {
    };

    Type.prototype.comboItems = function () {
        // array-ify id, to avoid matching the name against the value: otherwise if we happen to have say a node with id = '3',
        // name = '5' we'll run into trouble
        var seen = {};
        var items = [];
        var itemFor = this.itemFor = {};
        this.collection.each(function (model) {
            var id = model.getAttribute('id');
            var desc = model.getDescription();
            if (seen[desc]) {
                ++seen[desc];
                // but check: we have "a" and "a [2]" we want "a [3]"
                while (seen[desc + ' [' + seen[desc] + ']']) {
                    ++seen[desc];
                }
                desc = desc + ' [' + seen[desc] + ']';
            } else {
                seen[desc] = 1;
            }
            var item = {value: [id], name: desc};
            items.push(item);
            itemFor[id] = item;
        });
        this.adjustItems(items);
        return items;
    };

    Type.prototype.comboItemFor = function (id) {
        if (!this.itemFor) {
            this.comboItems();
        } // will set itemFor
        return this.itemFor[id];
    };

    Type.prototype.getType = function (typeName) {
        return types[typeName];
    };

    return Type;
});