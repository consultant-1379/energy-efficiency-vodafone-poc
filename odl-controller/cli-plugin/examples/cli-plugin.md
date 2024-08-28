POST http://localhost:8181/restconf/operations/cli-plugin:invoke-command

{ 
  "input" : {
    "ip-address" : "10.42.142.9",
    "user" : "admin",
    "password" : "Ericsson1",
    "command" : "config slot 1 ct 1 selected-min-acm 4_QAM selected-max-acm 128_QAM"
  }
}


POST http://localhost:8181/restconf/operations/cli-plugin:invoke-command

{ 
  "input" : {
    "ip-address" : "10.42.142.9",
    "user" : "admin",
    "password" : "Ericsson1",
    "command" : "config slot 1 ct 1 selected-max-acm 128_QAM"
  }
}


POST http://localhost:8181/restconf/operations/cli-plugin:invoke-command

{ 
  "input" : {
    "ip-address" : "10.42.142.9",
    "user" : "admin",
    "password" : "Ericsson1",
    "command" : "config slot 1 ct 1 selected-min-acm 16_QAM"
  }
}


POST http://localhost:8181/restconf/operations/cli-plugin:invoke-command

{ 
  "input" : {
    "ip-address" : "10.42.142.9",
    "user" : "admin",
    "password" : "Ericsson1",
    "command" : "config slot 1 ct 1 target-input-power-far-end -33"
  }
}
