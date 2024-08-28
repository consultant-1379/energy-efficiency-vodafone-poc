/**
 *    Define all REST mocks here that are required to run an application in a positive scenario
 *    (all known REST endpoints work).
 */

define([
    'test/resources/restMock/data/collections'
], function(collections) {

    return {
        /**
         * @param server - sinon server object (any configuration of the server should be done inside
         * the test case.
         */
        applyScenario: function(server) {
            var administratorUserPermissions = [
                {
                    "user": "administrator",
                    "role": "ADMINISTRATOR",
                    "targetGroup": "ALL"
                },
                {
                    "user": "administrator",
                    "role": "SECURITY_ADMIN",
                    "targetGroup": "ALL"
                }
            ];

            var operatorUserPermissions = [{
                "user": "operator",
                "role": "OPERATOR",
                "targetGroup": "ALL"
            }];

            var userProfile = {
                "username": "administrator",
                "firstName": "security",
                "lastName": "admin",
                "email": "security@administor.com",
                "userType": "enmUser",
                "status": "enabled",
                "_id": "administrator",
                "_rev": "11",
                "isMemberOf": "SECURITY_ADMIN,ADMINISTRATOR",
                "lastLogin": "20150901145848+0000"
            };

            server.autoRespond = true;
            server.respondImmediately = true;

            server.respondWith("/editprofile", this.validResponse(userProfile, "/editprofile"));
            server.respondWith("/oss/idm/usermanagement/users/administrator/privileges", this.validResponse(administratorUserPermissions));
            server.respondWith("/oss/idm/usermanagement/users/operator/privileges", this.validResponse(operatorUserPermissions));

//            server.respondWith("/", this.validResponse(collections));

            server.respondWith("/rest/system/time", this.validResponse({
                "timestamp":new Date().getTime(),
                "utcOffset":1.0,
                "timezone":"IST",
                "serverLocation":"Europe/Dublin"
            }));
        },

        validResponse: function(source) {
            return [200, { "Content-Type": "application/json" }, JSON.stringify(source)];
        },

        serverRespond: function(server) {
            for (var respondIndex = 0; respondIndex < 4; respondIndex++) {
                server.respond();
            }
        }
    };
});