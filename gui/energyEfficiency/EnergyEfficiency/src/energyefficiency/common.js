define([
    'widgets/Dialog',
    'widgets/EditableField',
    'widgets/ProgressBar',
    'tablelib/Table',
    'tablelib/plugins/FixedHeader',
    'tablelib/plugins/Selection',
    'widgets/Notification',
    'i18n!energyefficiency/dictionary.json'
], function (Dialog, EditableField, ProgressBar, Table, FixedHeader, Selection, Notification, dictionary) {

    var INTERVAL_CALL_REST_MIL_SEC = 30000; // Interval for REST Calls 15s for now, Will be in EEE response call.
    var AUTO_DISMISS_DURATION_TOAST = 5000; // Interval for auto-dismissing info-message toast.

    return {
        lagsLabel: "lags",
        linksLabel: "links",
        nodesLabel: "nodes",
        bondingLabel: "bonding",
        detailsLabel: "details",
        savingsLabel: "savings",
        USERNAME_ODL: "admin",
        PASSWORD_ODL: "admin",
        CONFIG_URI: "/restconf/config",
        TOPOLOGY_URI: "/ietf-network:networks/network/",
        MIB_OBJECT_OID_URI: "/snmp-agent:mib-object-oid",
        VPN_SERVICE_POC_URI: "/vpn-service-poc:e-lines",
        OPERATIONAL_URI: "/restconf/operational",
        OPERATIONS_URI: "/restconf/operations",
        NETWORK_REF_MN_TOPO: "mini-link-topo",
        MODE_CURRENT_DAY: "CURRENT_DAY",
        PREVIOUS_DAY: "PREVIOUS_DAY",
        MODE_SAVING: "SAVING",
        COMMA_SPLIT_JOIN: ', ',
        SLASH: '/',
        SLASH_ENCODED: '%2F',
        //COLORS
        GREEN_COLOR: '#032600',
        BLU_COLOR: '#0d43ff',
        BLU_DARK_COLOR: '#0002b4',
        GREY_COLOR: '#535353',
        GREY_DEEP_COLOR: '#6b6b6b',
        YELLOW_COLOR: '#fabb00',
        OP_STATUS_UP: "up",
        OP_STATUS_DOWN: "down",

        getIntervalCall: function () {
            return INTERVAL_CALL_REST_MIL_SEC;
        },

        setIntervalCall: function (newInterval) {
            INTERVAL_CALL_REST_MIL_SEC = newInterval;
        },

        getDurationToast: function () {
            return AUTO_DISMISS_DURATION_TOAST;
        },

        setDurationToast: function (newInterval) {
            AUTO_DISMISS_DURATION_TOAST = newInterval;
        },

        // GUI facilities

        // /**
        //  * Speed up animations
        //  * @param {type} selector
        //  */
        // turnOffAnimation: function (selector) {
        //     var noanim = selector + " {";
        //     ["-webkit-", "-moz-", "-o-", "-ms-", ""].forEach(function (prefix) {
        //         // transition: none or 0s breaks zooming in leafletjs; and transform: none breaks panning
        //         // noanim += prefix + "transition: none !important; ";
        //         // noanim += prefix + "animation:  none !important; ";
        //         // noanim += prefix + "transform:  none !important; ";
        //         // so just speed up the animations:
        //         noanim += prefix + "transition: 0.01s !important; ";
        //         noanim += prefix + "animation:  0s !important; ";
        //         noanim += prefix + "transform:  0s !important; ";
        //     });
        //     noanim += '}';
        //     var div = document.createElement('div');
        //     div.id = 'divAnim';
        //     div.innerHTML = '<span></span><style>' + noanim + '</style>';
        //     document.getElementsByTagName('head')[0].appendChild(div.childNodes[1]);
        // },
        //
        // turnOnAnimation: function () {
        //     var div = document.getElementById('divAnim');
        //     if (div) {
        //         div.parentNode.removeChild(div);
        //     }
        // },
        //
        // //Hide system bar saving space
        // noSystemBar: function () {
        //     var div = document.createElement('div');
        //     div.innerHTML = '<span></span><style> .eaContainer-SystemBarHolder ' +
        //         ' { display: none !important; height: 0px; visibility: hidden;} ' +
        //         ' .eaContainer-SystemBar { height: 0px !important } ' +
        //         ' .eaContainer-applicationHolder { top: 0px !important; } </style>';
        //     document.getElementsByTagName('head')[0].appendChild(div.childNodes[1]);
        // },
        //
        // addSystemBar: function () {
        //     var head = document.getElementsByTagName('head')[0];
        //     if (head) {
        //         head.removeChild(head.lastChild);
        //     }
        // },
        //
        // /**
        //  * Set the title of the document
        //  * @param {type} msg
        //  */
        // setTitle: function (msg) {
        //     document.title = msg;
        // },
        //
        // /**
        //  * Generate a GUI event, writing its structure down to the local storage this will trigger an event to be
        //  * captured and forwarded according to the registered forwarding function.
        //  * Eventually the "guiEvent" entry in the data store is cleaned up.
        //  * @param {type} event
        //  */
        // sendGuiEvent: function (event) {
        //     if (this.isEmptyObject(event)) {
        //         return;
        //     }
        //     localStorage.setItem("guiEvent", JSON.stringify(event));
        //     localStorage.setItem("guiEvent", "");
        // },

        /**
         * Return the height of the current window minus to space allocated to the header bar, if any
         * @return {Window.innerHeight/Number}
         */
        availableHeight: function () { // for full window, less header
            var height = window.innerHeight;
            var headerBar = document.getElementsByClassName("eaContainer-SystemBarHolder");
            if (headerBar) {
                height -= headerBar[0].clientHeight;
            }
            return height;
        },

        /**
         * Return the screen height in pixel of the area available to the referred element
         * @param {type} ref - The reference element to be measured
         * @returns {Number} - The height in pixel
         */
        elementHeight: function (ref) {
            var el = ref.getElement()._getHTMLElement();
            var rect = el.getBoundingClientRect();
            return window.innerHeight - rect.top;
        },

        /**
         * Display an Info Notification Message.
         * @param {type} message - Content message text
         * @param {type} el - Element to be attached to
         */
        showToast: function (message, el) {
            if (this.notification) {
                this.notification.destroy();
            }
            var color = 'green';
            this.notification = new Notification({
                label: message,
                icon: 'tick',
                color: color,
                showCloseButton: true,
                showAsToast: true,
                autoDismiss: true,
                autoDismissDuration: this.getDurationToast()
            });
            this.notification.attachTo(el);
        },

        /**
         * Hide an Info Notification Message, if present.
         */
        hideToast: function () {
            if (this.notification) {
                this.notification.destroy();
            }
        },

        /**
         * Display an error Dialog
         * @param {type} hdr - Header text
         * @param {type} ctx - Content message
         */
        errorDialog: function (hdr, ctx) {
            if (!hdr) {
                hdr = dictionary.get("dialogServerErrorHeader");
            }
            if (!ctx) {
                ctx = dictionary.get("dialogErrorServiceContent");
            }

            var dialog = new Dialog({
                header: hdr,
                visible: true,
                type: 'error',
                content: ctx
            });
            dialog.setButtons([{
                caption: dictionary.get("close"),
                action: function () {
                    dialog.destroy();
                }
            }]);
            dialog.show();
        },

        /**
         * Display an error Dialog from REST Call XHR
         * @param {type} xhr - Header text and body
         */
        errorDialogFromXHR: function (data, xhr) {
            if (xhr.getStatusText() !== 'abort') { // if the request was aborted by the user, a different request will follow. Take no further action
                this.destroyDialog();
                var errorMessage = this.getServerMessage(xhr.getStatus(), xhr.getResponseText());
                this.dialog = new Dialog({
                    header: errorMessage.userMessage.title,
                    visible: true,
                    type: 'error',
                    content: errorMessage.userMessage.body
                });
                this.dialog.setButtons([{
                    caption: dictionary.get("close"),
                    action: this.destroyDialog.bind(this)
                }]);
                this.dialog.show();
            }
        },

        destroyDialog: function () {
            if (this.dialog) {
                this.dialog.destroy();
            }
        },

        getServerMessage: function (statusCode, responseText) {
            var serverMessage;
            switch (statusCode) {
                case 404:
                    serverMessage = {
                        userMessage: {
                            title: dictionary.get('unknownServerErrorHeader'),
                            body: dictionary.get('unknownServerError'),
                            shortBody: dictionary.get('unknownServerErrorShort')
                        }
                    };
                    break;
                default:
                    serverMessage = {
                        userMessage: {
                            title: dictionary.get('dialogServerErrorHeader'),
                            body: dictionary.get('dialogServerErrorBody'),
                            shortBody: dictionary.get('dialogServerErrorBody')
                        }
                    };
            }
            return serverMessage;
        },

        /**
         * Display a simple OK/Cancel dialog, invoking the okAction function if OK button is pressed
         * @param {type} hdr - The header text
         * @param {type} ctx - The content text
         * @param {type} okAction - The action function to be invoked when ok button is pressed
         * @param {type} okObj - The object (this) where the okAction lives
         */
        queryDialog: function (hdr, ctx, okAction, okObj) {
            var dialog = new Dialog({
                header: hdr,
                visible: true,
                type: "information",
                content: ctx
            });
            dialog.setButtons([
                {
                    caption: dictionary.get("ok"),
                    action: function () {
                        okAction.call(this);
                        dialog.hide();
                    }.bind(okObj)
                },
                {
                    caption: dictionary.get("cancel"),
                    action: function () {
                        dialog.hide();
                    }.bind(this)
                }
            ]);
            dialog.show();
        },

        /**
         * Display a simple search dialog, waiting for the user to enter a text to be searched and then invoking the
         * searchAction passing the edited text to it.
         * @param {type} hdr - The header text
         * @param {type} prompt - The prompt text
         * @param {type} searchAction - The inputAction function
         * @param {type} searchObj - The object (this) where the inputAction lives
         */
        searchDialog: function (hdr, prompt, searchAction, searchObj) {
            var input = new EditableField({});
            var dialog = new Dialog({
                header: hdr,
                visible: true,
                closable: true,
                type: "information",
                content: prompt,
                optionalContent: input
            });
            dialog.setButtons([
                {
                    caption: dictionary.get("search"),
                    action: function () {
                        var nSel = searchAction.call(this, input.getValue());
                        dialog.setContent(dictionary.get('foundMatches').replace('$1', nSel));
                    }.bind(searchObj)
                },
                {
                    caption: dictionary.get("cancel"),
                    action: function () {
                        dialog.hide();
                    }.bind(this)
                }]);
            dialog.show();
        },

        /**
         * Show a selection dialog where a table is shown with possible options.
         * The user shall select one of them and the relevant action shall be invoked with the selected item passed to it.
         * @param {type} hdr - header of the dialog
         * @param {type} prompt - Prompt text
         * @param {type} obj - The object with the table to be shown
         * @param {type} columns - The columns to be shown
         * @param {type} selectAction - The action invoked upon selectin
         * @param {type} selectObj - The "this" of the select Action
         */
        selectDialog: function (hdr, prompt, obj, columns, selectAction, selectObj) {
            var input = new Table({
                modifiers: [{name: 'striped'}],
                columns: columns,
                tooltips: true,
                plugins: [new FixedHeader({height: 150, maxHeight: 150}),
                    new Selection({
                        checkboxes: false,
                        selectableRows: true,
                        multiselect: false,
                        bind: false
                    })]
            });
            for (var j = 0; j < obj.length; j++) {
                input.addRow(obj[j]);
            }
            var dialog = new Dialog({
                header: hdr,
                visible: true,
                closable: true,
                type: "information",
                content: prompt,
                optionalContent: input
            });

            dialog.setButtons([
                {
                    caption: dictionary.get("select"),
                    action: function () {
                        var rows = dialog.options.optionalContent.getSelectedRows();
                        if (rows) {
                            selectAction.call(selectObj, rows[0].options.model);
                        }
                        dialog.hide();
                    }.bind(this)
                },
                {
                    caption: dictionary.get("cancel"),
                    action: function () {
                        dialog.hide();
                    }.bind(this)
                }]);
            dialog.show();
        },

        /**
         * Display a generic input dialog, waiting for the user to enter a text and then invoking the inputAction
         * passing the edited text to it.
         * @param {type} hdr - The header text
         * @param {type} prompt - The prompt text
         * @param {type} inputBtn - The text of the input button
         * @param {type} inputAction - The inputAction function
         * @param {type} inputObj - The object (this) where the inputAction lives
         */
        inputDialog: function (hdr, prompt, inputBtn, inputAction, inputObj) {
            var input = new EditableField({});

            var dialog = new Dialog({
                header: hdr,
                visible: true,
                closable: true,
                type: "information",
                content: prompt,
                optionalContent: input
            });

            dialog.setButtons([
                {
                    caption: inputBtn,
                    action: function () {
                        inputAction.call(this, input.getValue());
                        dialog.hide();
                    }.bind(inputObj)
                },
                {
                    caption: dictionary.get("cancel"),
                    action: function () {
                        dialog.hide();
                    }.bind(this)
                }]);
            dialog.show();
        },

        // Variable to store the progress dialog info
        progressDialog: undefined,

        /**
         * Start the progress dialog used to show a lengthy operation
         * @param message - The message to show on the dialog
         * @param inc - The percentage of increment of the progress bar at any step of the operation
         */
        startProgress: function (message, inc) {
            var did = sessionStorage.getItem('dialog');
            if (!did) {
                did = 1;
            } else {
                did++;
            }
            sessionStorage.setItem('dialog', did);

            if (!this.progressDialog) {
                var progress = new ProgressBar();
                this.progressDialog = new Dialog({
                    header: dictionary.get("opInProgress"),
                    visible: true,
                    type: "information",
                    did: did,
                    content: message,
                    optionalContent: progress
                });
                this.progressDialog.setButtons([{
                    caption: dictionary.get("cancel"),
                    action: function () {
                        if (this.progressDialog.isVisible()) {
                            var progress = this.progressDialog.options.optionalContent;
                            progress.setValue(0);
                            this.getEventBus().publish('stopProgress', this.progressDialog.options.did);
                            this.progressDialog.value = 0;
                            this.progressDialog.hide();
                        }
                    }.bind(this)
                }]);
            } else {
                this.progressDialog.setContent(message);
            }
            this.progressDialog.value = 0;
            this.progressDialog.inc = inc;
            this.progressDialog.options.optionalContent.setValue(0);
            this.progressDialog.show();
        },

        /*
         * Close the progress dialog and reset all the relevant variables.
         */
        endProgress: function () {
            if (!this.progressDialog) {
                return;
            }
            if (!this.progressDialog.isVisible()) {
                return;
            }
            var progress = this.progressDialog.options.optionalContent;
            progress.setValue(0);
            this.progressDialog.value = 0;
            this.progressDialog.hide();
        },

        /**
         * Perform a step of the progress incrementing the progress bar of the default value configured at
         * initialization or setting its new value to the passed parameter.
         * @param val - The next value of the progress bar (%). If undefined, the progress bar is added the default increment.
         */
        stepProgress: function (val) {
            if (!this.progressDialog) {
                return;
            }
            if (!this.progressDialog.isVisible()) {
                return;
            }
            var progress = this.progressDialog.options.optionalContent;
            if (val === undefined) {
                this.progressDialog.value += this.progressDialog.inc;
            } else {
                this.progressDialog.value = val;
            }
            progress.setValue(this.progressDialog.value);
        },

        // /**
        //  * Save a set of data in JSON format onto a data file located in ~/Download. All files start with energyefficiency prefix.
        //  * @param {type} data - The data to be saved : must be non undefined
        //  * @param {type} filename - The filename : if undefined, it defaults to data.json
        //  */
        // saveData: function (data, filename) {
        //     if (!data) {
        //         return;
        //     }
        //     if (!filename) {
        //         filename = 'data.json';
        //     }
        //     if (typeof data === "object") {
        //         data = JSON.stringify(data, undefined, 4);
        //     }
        //     var blob = new Blob([data], {type: 'text/json'});
        //     var e = new MouseEvent('click', {'region': window});
        //     // document.createEvent('MouseEvents');
        //     var a = document.createElement('a');
        //
        //     a.download = 'energyefficiency/' + filename;
        //     a.href = window.URL.createObjectURL(blob);
        //     a.dataset.downloadurl = ['text/json', a.download, a.href].join(':');
        //     //e.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);
        //     a.dispatchEvent(e);
        // },

        // String facilities

        /**
         * Return the version "uncamelized" of the given string. Capital
         * letters are replaced with an hyphen plus the relevant lower case
         * @param {type} string
         * @returns {string}
         */
        unCamelCase: function (string) { // uppercase -> hyphen-lc
            return string.replace(/([A-Z])/g, function (all, letter) {
                return "-" + letter.toLowerCase();
            });
        },

        decodeb64String: function (b64) {
            if (!b64 || b64.length % 4) {
                return b64;
            } // not base64 encoded
            var s;
            try {
                s = atob(b64);
            } catch (err) { // already decoded ?
                return b64;
            }
            return s;
        },

        decodePccAddress: function (b64) {
            if (!b64 || b64.length % 4) {
                return b64;
            } // not base64 encoded
            var s;
            try {
                s = atob(b64);
            } catch (err) { // already decoded ?
                return b64;
            }
            if (s.length === 4) {
                return s.charCodeAt(0) + "." + s.charCodeAt(1) + "." + s.charCodeAt(2) + "." + s.charCodeAt(3);
            } else {
                return b64; // not IPv4 address ?
            }
        },

        // Objects facilities

        /**
         * Check if an object has methods/members or not
         * @param {type} obj
         * @returns {Boolean}
         */
        isEmptyObject: function (obj) {
            return Object.keys(obj).length === 0 && obj.constructor === Object;
        },

        /**
         * Sort the array by Key (given in input).
         * @param {type} arrayObject unordered
         * @param {type} key name to be ordered
         * @returns {Array} array ordered
         */
        sortArrayByKey: function (arrayObject, key) {
            return arrayObject.sort(function (a, b) {
                var x = a[key], y = b[key];
                return ((x < y) ? -1 : ((x > y) ? 1 : 0));
            });
        },

        arrayContains: function (arrayList, searchParameter) {
            return (arrayList.indexOf(searchParameter) > -1);
        },

        splitStringToArray: function (string, reg) {
            return string.split(reg);
        },

        joinArrayToString: function (array, reg) {
            return array.join(reg);
        },

        isNumber: function (n) {
            return !isNaN(parseFloat(n)) && isFinite(n);
        },

        getMaxNumberFromArray: function (arrayObject) {
            var max = Math.max.apply(null, arrayObject);
            return this.isNumber(max) ? max : 0;
        },

        getMaxNumber: function (n, m) {
            var max = Math.max(n, m);
            return this.isNumber(max) ? max : 0;
        },

        replaceChar: function (word, from, to) {
            return word.replace(new RegExp(from, 'g'), to);
        },

        isStringVar: function (myVar) {
            return typeof myVar === 'string' || myVar instanceof String;
        },

        isArrayVar: function (myVar) {
            return Array.isArray(myVar);
        },

        removeIdFromArray: function (id, array) {
            if (array.indexOf(id) !== -1) {
                return array.splice(index, 1);
            }
        }


        // ,
        // findNode: function (nodeId, nodeNames) {
        //     var index = -1;
        //     for (var i = 0; i < nodeNames.length; i++) {
        //         var obj = nodeNames[i];
        //         if (obj.nodeId === nodeId) {
        //             index = i;
        //             break;
        //         }
        //     }
        //     return index;
        // },
        //
        // findTp: function (nodeId, tpId, tpNames) {
        //     for (var i = 0; i < tpNames.length; i++) {
        //         var obj = tpNames[i];
        //         if (obj.nodeId === nodeId) {
        //             for (var j = 0; j < obj.tps.length; j++) {
        //                 var tp = obj.tps[j];
        //                 if (tp.tpId === tpId) {
        //                     if (tp.tpName === "Not Defined") {
        //                         return undefined;
        //                     } else {
        //                         return tp.tpName;
        //                     }
        //                 }
        //             }
        //         }
        //     }
        //     return undefined;
        // }

    };

});