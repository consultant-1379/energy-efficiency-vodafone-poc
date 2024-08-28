if (typeof define !== 'function') {
    var define = require('../../../acceptance/node_modules/amdefine')(module);
}

define(function() {
    return {
        "ietf-network-topology:link": [
            {
                "link-id": "MINI-LINK-6351-3:WAN-1/2",
                "destination": {
                    "dest-tp": "WAN-1/2",
                    "dest-node": "MINI-LINK-6351-4"
                },
                "source": {
                    "source-tp": "WAN-1/2",
                    "source-node": "MINI-LINK-6351-3"
                },
                "ietf-te-topology:te": {
                    "state": {
                        "te-link-attributes": {
                            "te-default-metric": 1,
                            "name": "MINI-LINK-6351-3:WAN-1/2",
                            "max-link-bandwidth": "73944375",
                            "admin-status": "up"
                        },
                        "oper-status": "up"
                    },
                    "config": {
                        "te-link-attributes": {
                            "admin-status": "up"
                        }
                    }
                }
            }
        ]
    };
});