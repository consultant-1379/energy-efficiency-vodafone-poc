>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

POST http://localhost:8181/restconf/operations/dynamic-data-collector:get-interface-list

{
    "input" : {
        "all-interfaces": ""
    }
}

{
    "output": {
        "result-ok": true,
        "interfaces": {
            "network-ref": "mini-link-topo",
            "interface-list": [
                {
                    "if-ref": "MINI-LINK-6351-2:WAN-1/2",
                    "current-bandwidth-capacity": 17954625,
                    "maximum-bandwidth-capacity": 17954625
                },
                {
                    "if-ref": "MINI-LINK-6351-1:WAN-1/2",
                    "current-bandwidth-capacity": 17954625,
                    "maximum-bandwidth-capacity": 17954625
                },
                {
                    "if-ref": "MINI-LINK-6351-4:WAN-1/2",
                    "current-bandwidth-capacity": 73944375,
                    "maximum-bandwidth-capacity": 73944375
                },
                {
                    "if-ref": "MINI-LINK-6691-2:WAN-1/1/2",
                    "current-bandwidth-capacity": 5962375,
                    "maximum-bandwidth-capacity": 5962375
                },
                {
                    "if-ref": "MINI-LINK-6691-2:lag-2:WAN-1/1/1",
                    "current-bandwidth-capacity": 13984875,
                    "maximum-bandwidth-capacity": 98338500
                },
                {
                    "if-ref": "MINI-LINK-6691-1:WAN-1/1/2",
                    "current-bandwidth-capacity": 5962375,
                    "maximum-bandwidth-capacity": 5962375
                },
                {
                    "if-ref": "MINI-LINK-6691-1:lag-2:WAN-1/1/1",
                    "current-bandwidth-capacity": 13984875,
                    "maximum-bandwidth-capacity": 98338500
                },
                {
                    "if-ref": "MINI-LINK-6351-3:WAN-1/2",
                    "current-bandwidth-capacity": 73944375,
                    "maximum-bandwidth-capacity": 73944375
                }
            ]
        }
    }
}

>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

POST http://localhost:8181/restconf/operations/dynamic-data-collector:set-interface-rate-monitoring

{
    "input" : {
        "network-ref": "mini-link-topo",
        "if-ref": "MINI-LINK-6691-1:WAN-1/1/1",
        "collection-interval" : 30,
        "monitoring-enable" : true,
        "history-length" : 2
    }
}

{
    "output": {
        "result-ok": true
    }
}


>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

POST http://localhost:8181/restconf/operations/dynamic-data-collector:get-interface-monitored-data

{
    "input" : {
        "network-ref": "mini-link-topo",
        "if-ref": "MINI-LINK-6691-1:WAN-1/1/1"
    }
}

{
    "output": {
        "nominal-output-power": 2052,
        "time-interval": 69,
        "current-output-power": 2052,
        "timestamp": "2017-10-19T09:21:41.620Z",
        "result-ok": true,
        "tx-traffic-rate": 1278466,
        "current-bandwidth-capacity": 100547750
    }
}


>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

>>> SOLO PER DEBUG <<<<<

GET http://localhost:8181/restconf/operational/dynamic-data-collector:historical-data

{
    "historical-data": {
        "history": [
            {
                "if-ref": "MINI-LINK-6691-1:WAN-1/1/1",
                "collected-data": [
                    {
                        "index": 8,
                        "nominal-output-power": 2052,
                        "time-interval": 68,
                        "current-output-power": 2052,
                        "timestamp": "2017-10-19T09:21:11.487Z",
                        "tx-traffic-rate": 1274866,
                        "current-bandwidth-capacity": 100547750
                    },
                    {
                        "index": 7,
                        "nominal-output-power": 2052,
                        "time-interval": 67,
                        "current-output-power": 2052,
                        "timestamp": "2017-10-19T09:20:41.541Z",
                        "tx-traffic-rate": 1276666,
                        "current-bandwidth-capacity": 100547750
                    },
                    {
                        "index": 10,
                        "nominal-output-power": 2052,
                        "time-interval": 70,
                        "current-output-power": 2052,
                        "timestamp": "2017-10-19T09:22:11.475Z",
                        "tx-traffic-rate": 1276666,
                        "current-bandwidth-capacity": 100547750
                    },
                    {
                        "index": 9,
                        "nominal-output-power": 2052,
                        "time-interval": 69,
                        "current-output-power": 2052,
                        "timestamp": "2017-10-19T09:21:41.620Z",
                        "tx-traffic-rate": 1278466,
                        "current-bandwidth-capacity": 100547750
                    },
                    {
                        "index": 4,
                        "nominal-output-power": 2052,
                        "time-interval": 64,
                        "current-output-power": 2052,
                        "timestamp": "2017-10-19T09:19:11.525Z",
                        "tx-traffic-rate": 531400,
                        "current-bandwidth-capacity": 100547750
                    },
                    {
                        "index": 3,
                        "nominal-output-power": 2052,
                        "time-interval": 63,
                        "current-output-power": 2052,
                        "timestamp": "2017-10-19T09:18:41.495Z",
                        "tx-traffic-rate": 533333,
                        "current-bandwidth-capacity": 100547750
                    },
                    {
                        "index": 6,
                        "nominal-output-power": 2052,
                        "time-interval": 66,
                        "current-output-power": 2052,
                        "timestamp": "2017-10-19T09:20:11.534Z",
                        "tx-traffic-rate": 580000,
                        "current-bandwidth-capacity": 100547750
                    },
                    {
                        "index": 5,
                        "nominal-output-power": 2052,
                        "time-interval": 65,
                        "current-output-power": 2052,
                        "timestamp": "2017-10-19T09:19:41.467Z",
                        "tx-traffic-rate": 531933,
                        "current-bandwidth-capacity": 100547750
                    },
                    {
                        "index": 0,
                        "nominal-output-power": 2052,
                        "time-interval": 60,
                        "current-output-power": 2052,
                        "timestamp": "2017-10-19T09:17:11.577Z",
                        "tx-traffic-rate": 1159083,
                        "current-bandwidth-capacity": 100547750
                    },
                    {
                        "index": 2,
                        "nominal-output-power": 2052,
                        "time-interval": 62,
                        "current-output-power": 2052,
                        "timestamp": "2017-10-19T09:18:11.497Z",
                        "tx-traffic-rate": 530000,
                        "current-bandwidth-capacity": 100547750
                    },
                    {
                        "index": 1,
                        "nominal-output-power": 2052,
                        "time-interval": 61,
                        "current-output-power": 2052,
                        "timestamp": "2017-10-19T09:17:41.485Z",
                        "tx-traffic-rate": 533333,
                        "current-bandwidth-capacity": 100547750
                    },
                    {
                        "index": 12,
                        "nominal-output-power": 2052,
                        "time-interval": 72,
                        "current-output-power": 2052,
                        "timestamp": "2017-10-19T09:23:11.490Z",
                        "tx-traffic-rate": 1254050,
                        "current-bandwidth-capacity": 100547750
                    },
                    {
                        "index": 11,
                        "nominal-output-power": 2052,
                        "time-interval": 71,
                        "current-output-power": 2052,
                        "timestamp": "2017-10-19T09:22:41.473Z",
                        "tx-traffic-rate": 1276666,
                        "current-bandwidth-capacity": 100547750
                    },
                    {
                        "index": 14,
                        "nominal-output-power": 2052,
                        "time-interval": 59,
                        "current-output-power": 2052,
                        "timestamp": "2017-10-19T09:16:41.421Z",
                        "tx-traffic-rate": 1150000,
                        "current-bandwidth-capacity": 100547750
                    },
                    {
                        "index": 13,
                        "nominal-output-power": 2052,
                        "time-interval": 58,
                        "current-output-power": 2052,
                        "timestamp": "2017-10-19T09:16:14.718Z",
                        "tx-traffic-rate": 1406666,
                        "current-bandwidth-capacity": 100547750
                    }
                ]
            }
        ]
    }
}