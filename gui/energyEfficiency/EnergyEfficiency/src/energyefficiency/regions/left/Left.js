define([
    'jscore/core',
    './LeftView',
    'widgets/Tree',
    'jscore/ext/net',
    'container/api',
    '../../types/types',
    'i18n!energyefficiency/dictionary.json'
], function (core, View, Tree, net, container, types, dictionary) {

    return core.Region.extend({

        View: View,

        init: function (options) {
            this.options = options || {};
        },

        loadTree: function (data) {
            if (this.treeWidget) {
                this.treeWidget.destroy();
            }

            this.treeWidget = new Tree({
                items: this.createTopologyTree(data)
            });
            this.treeWidget.attachTo(this.view.getTree());

            //// For now publish nothing, when an item is selected from the network topology.
            // this.treeWidget.addEventHandler('itemselect', function (item) {
            //     this.getContext().eventBus.publish("itemSelected", item);
            // }.bind(this));

            //// For now no context in the network topology (right-click on mouse).
            // this.treeWidget.getElement().addEventHandler('contextmenu', function (e) {
            //     e.preventDefault();
            //     if (this.treeWidget.getSelectedItem()) {
            //         container.getEventBus().publish('contextmenu:show', e, this.changeContextMenu.call(this));
            //     }
            // }.bind(this));
        },

        createTopologyTree: function (data) {
            var itemsOfTopologyTree = [];
            var networkRoot = {};

            networkRoot.label = dictionary.get("left.network");
            networkRoot.icon = "network";
            networkRoot.type = "network";
            itemsOfTopologyTree.push(networkRoot);

            if (data.network && data.network[0] && data.network[0].node) {
                itemsOfTopologyTree[0].children = [];
                var dataNodes = data.network[0].node;
                for (var i = 0; i < dataNodes.length; i++) {
                    itemsOfTopologyTree[0].children.push({
                        label: dataNodes[i]["node-id"],
                        icon: 'microwave', //{prefix: 'ebIcon', name: 'microwave', opacity: "0.4"},
                        type: "node"
                    });

                    var dataNodesPorts = dataNodes[i]["ietf-network-topology:termination-point"];
                    itemsOfTopologyTree[0].children[i].children = [];

                    var ports = [];
                    for (var j = 0; j < dataNodesPorts.length; j++) {
                        ports.push({
                            label: dataNodesPorts[j]["tp-id"],
                            type: "port"
                        });
                    }

                    itemsOfTopologyTree[0].children[i].children.push({
                        label: "ports",
                        type: "port",
                        children: ports
                    });
                }
            }
            return itemsOfTopologyTree;
        }

        //// ContextMenu
        // changeContextMenu: function() {
        //     var item = this.treeWidget.getSelectedItem().options.item;
        //     var actions = [];
        //     if (item.label !== "Network") {
        //         actions.push({
        //             color: 'grey',
        //             name: dictionary.removeNode,
        //             action: this.removeNode.bind(this)
        //         });
        //     }
        //     actions.push({type:'separator'
        //     });
        //     actions.push({color: 'grey',
        //         name: dictionary.createNode,
        //         action: this.createNode.bind(this)
        //     });
        //     return actions;
        // },
        // createNode:function (item) {
        //     //alert("Not yet implemented.");
        //     this.getEventBus().publish('appchange:createnode', 'node-creation');
        // },
        // addNode:function (item) {
        //     alert("Not yet implemented.");
        // },
        // removeNode:function (item) {
        //     var destination = this.treeWidget.find({label: "Network"});
        //     destination.addItem(this.treeWidget.getSelectedItem().options.item);
        //     //this.deleteArea();
        // },

    });

});