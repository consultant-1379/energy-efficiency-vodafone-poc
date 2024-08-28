
>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

POST http://localhost:8181/restconf/operations/radio-link-configurator:set-interface-capacity

{
	"input" : {
		 "network-ref": "mini-link-topo",
		 "if-ref": "MINI-LINK-6691-1:WAN-1/1/1",
		 "bandwidth-capacity" : 100547750
	}
}

POST http://localhost:8181/restconf/operations/radio-link-configurator:set-interface-capacity

{
	"input" : {
		 "network-ref": "mini-link-topo",
		 "if-ref": "MINI-LINK-6691-2:WAN-1/1/1",
		 "bandwidth-capacity" : 100547750
	}
}

>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

POST http://localhost:8181/restconf/operations/radio-link-configurator:set-interface-capacity

{
	"input" : {
		 "network-ref": "mini-link-topo",
		 "if-ref": "MINI-LINK-6691-1:WAN-1/1/1",
		 "bandwidth-capacity" : 10547750
	}
}


POST http://localhost:8181/restconf/operations/radio-link-configurator:set-interface-capacity

{
	"input" : {
		 "network-ref": "mini-link-topo",
		 "if-ref": "MINI-LINK-6691-2:WAN-1/1/1",
		 "bandwidth-capacity" : 10547750
	}
}
