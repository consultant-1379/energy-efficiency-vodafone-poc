/**
 *  App Constants : provides a centralized point where application parameters are stored (it is advised
 *  to store params that are likely to change during application's lifetime.
 *
 *  When editing this file consider:
 *      - Variable naming to be intuitive when using code completion by the developer.
 *      - Every property must be an object "{}" to allow extensibility.
 *      - This library is to be used with either Unit, Integration or Acceptance lvl tests.
 */

// use amdefine library to ensure nodejs can also use this module.
if (typeof define !== 'function') {
    var define = require('../acceptance/node_modules/amdefine')(module);
}

define([

],function(){
    return {

          //TODO
//        restPath : "/restPath/"


    };
});
