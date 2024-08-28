
GET http://localhost:8181/restconf/operational/ietf-network:networks/network/mini-link-topo/link/MINI-LINK-6691-1:WAN-1%2F1%2F1

{
    "ietf-network-topology:link": [
        {
            "link-id": "MINI-LINK-6691-1:WAN-1/1/1",
            "destination": {
                "dest-tp": "WAN-1/1/1",
                "dest-node": "MINI-LINK-6691-2"
            },
            "source": {
                "source-tp": "WAN-1/1/1",
                "source-node": "MINI-LINK-6691-1"
            },
            "ietf-l2-topology:l2-link-attributes": {
                "l2-topology-lag:lag-id-ref": "MINI-LINK-6691-1:lag-2",
                "l2-topology-lag:network-ref": "mini-link-topo",
                "rate": 125000000
            },
            "ietf-te-topology:te": {
                "state": {
                    "te-link-attributes": {
                        "te-default-metric": 1,
                        "name": "MINI-LINK-6691-1:WAN-1/1/1",
                        "max-link-bandwidth": "4661250",
                        "max-resv-link-bandwidth": "4661250",
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
}

>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

http://localhost:8181/restconf/operational/ietf-network:networks/network/mini-link-topo/link/MINI-LINK-6691-1:WAN-1%2F3%2F1


{
    "ietf-network-topology:link": [
        {
            "link-id": "MINI-LINK-6691-1:WAN-1/3/1",
            "destination": {
                "dest-tp": "WAN-1/3/1",
                "dest-node": "MINI-LINK-6691-2"
            },
            "source": {
                "source-tp": "WAN-1/3/1",
                "source-node": "MINI-LINK-6691-1"
            },
            "ietf-l2-topology:l2-link-attributes": {
                "l2-topology-lag:lag-id-ref": "MINI-LINK-6691-1:lag-2",
                "l2-topology-lag:network-ref": "mini-link-topo",
                "rate": 4661250
            },
            "ietf-te-topology:te": {
                "state": {
                    "te-link-attributes": {
                        "te-default-metric": 1,
                        "name": "MINI-LINK-6691-1:WAN-1/3/1",
                        "max-link-bandwidth": "4661250",
                        "max-resv-link-bandwidth": "4661250",
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
}

>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

GET http://localhost:8181/restconf/operational/ietf-network:networks/network/mini-link-topo/link/MINI-LINK-6691-1:WAN-1%2F3%2F2


{
    "ietf-network-topology:link": [
        {
            "link-id": "MINI-LINK-6691-1:WAN-1/3/2",
            "destination": {
                "dest-tp": "WAN-1/3/2",
                "dest-node": "MINI-LINK-6691-2"
            },
            "source": {
                "source-tp": "WAN-1/3/2",
                "source-node": "MINI-LINK-6691-1"
            },
            "ietf-l2-topology:l2-link-attributes": {
                "l2-topology-lag:lag-id-ref": "MINI-LINK-6691-1:lag-2",
                "l2-topology-lag:network-ref": "mini-link-topo",
                "rate": 4661250
            },
            "ietf-te-topology:te": {
                "state": {
                    "te-link-attributes": {
                        "te-default-metric": 1,
                        "name": "MINI-LINK-6691-1:WAN-1/3/2",
                        "max-link-bandwidth": "4661250",
                        "max-resv-link-bandwidth": "4661250",
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
}