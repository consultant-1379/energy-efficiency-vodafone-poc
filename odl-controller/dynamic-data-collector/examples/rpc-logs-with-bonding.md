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
                    "if-ref": "MINI-LINK-6691-2:WAN-1/1/1",
                    "current-bandwidth-capacity": 100547750,
                    "maximum-bandwidth-capacity": 100547750
                },
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
                    "if-ref": "MINI-LINK-6351-3:WAN-1/2",
                    "current-bandwidth-capacity": 73944375,
                    "maximum-bandwidth-capacity": 73944375
                },
                {
                    "if-ref": "MINI-LINK-6691-1:WAN-1/1/1",
                    "current-bandwidth-capacity": 100547750,
                    "maximum-bandwidth-capacity": 100547750
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
            "collection-interval" : 60,
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

>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

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
                        "index": 3,
                        "required-bandwidth-capacity": 100547750,
                        "request-date-and-time": "2017-11-17T12:18:50.492Z",
                        "nominal-output-power": 200,
                        "time-interval": 3,
                        "current-output-power": 200,
                        "timestamp": "2017-11-17T12:20:15.346Z",
                        "tx-traffic-rate": 73018750,
                        "current-bandwidth-capacity": 100547750,
                        "oper-status": "up"
                    },
                    {
                        "index": 2,
                        "required-bandwidth-capacity": 100547750,
                        "request-date-and-time": "2017-11-17T12:18:50.492Z",
                        "nominal-output-power": 200,
                        "time-interval": 2,
                        "current-output-power": 200,
                        "timestamp": "2017-11-17T12:19:55.340Z",
                        "tx-traffic-rate": 69701400,
                        "current-bandwidth-capacity": 100547750,
                        "oper-status": "up"
                    },
                    {
                        "index": 1,
                        "required-bandwidth-capacity": 100547750,
                        "request-date-and-time": "2017-11-17T12:18:50.492Z",
                        "nominal-output-power": 200,
                        "time-interval": 1,
                        "current-output-power": 200,
                        "timestamp": "2017-11-17T12:19:35.400Z",
                        "tx-traffic-rate": 75689150,
                        "current-bandwidth-capacity": 100547750,
                        "oper-status": "up"
                    }
                ]
            },
            {
                "if-ref": "MINI-LINK-6351-1:WAN-1/2",
                "collected-data": [
                    {
                        "index": 1,
                        "required-bandwidth-capacity": 64698250,
                        "request-date-and-time": "2017-11-17T12:18:50.272Z",
                        "nominal-output-power": 2511,
                        "time-interval": 1,
                        "current-output-power": 2511,
                        "timestamp": "2017-11-17T12:20:28.023Z",
                        "tx-traffic-rate": 58486325,
                        "current-bandwidth-capacity": 64698250,
                        "oper-status": "up"
                    }
                ]
            }
        ]
    }
}