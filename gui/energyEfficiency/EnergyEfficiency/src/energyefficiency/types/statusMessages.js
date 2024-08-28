define([
    './Type',
    'i18n!energyefficiency/dictionary.json'
], function (Type, dictionary) {

    var statusMessages = new Type({
        whichTab: 'statusMessages',
        title: "Log",
        singularTitle: "Message",
        attributes: [
            ['time', 'Time', {width: 200}],
            ['message', 'Message'],
            ['status', 'Status', {width: 200}]  // will be "success", "warning", "error"
        ],
        nextId: 1,

        addSuccess: function (message) {
            this.addMessage(message, "success");
        },

        addWarning: function (message) {
            this.addMessage(message, "warning");
        },

        addError: function (message) {
            this.addMessage(message, "error");
        },

        addNotification: function (notif) {
            var currModel = new this.Model();
            currModel.setAttribute("id", this.nextId++);
            // Try to compensate for the difference in behavior between win and odl
            if (currModel.id === undefined) {
                currModel.id = currModel.getAttribute("id");
            }
            var date = new Date(notif.time);
            currModel.setAttribute("time", date.toLocaleString());
            currModel.setAttribute("message", 'Data change for ' + notif.path);
            currModel.setAttribute("status", notif.operation);
            this.collection.addModel(currModel);
            this.sortCollection();
        },

        addMessage: function (message, status) {
            var currModel = new this.Model();
            currModel.setAttribute("id", this.nextId++);

            // Try to compensate for the difference in behavior between win and odl
            if (currModel.id === undefined) {
                currModel.id = currModel.getAttribute("id");
            }

            var now = new Date();
            currModel.setAttribute("time", now.toLocaleString());
            currModel.setAttribute("message", message);
            currModel.setAttribute("status", status);

            this.collection.addModel(currModel);
            this.sortCollection();
        },

        sortCollection: function () {
            this.collection.sort(function (model1, model2) {
                if (model1.id < model2.id) {
                    return 1;
                } else if (model1.id > model2.id) {
                    return -1;
                } else {
                    return 0;
                }
            });
        },

        clear: function () {
            this.collection.setModels([]);
            this.nextId = 1;
        }
    });

    return statusMessages;

});