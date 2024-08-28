/*global define, describe, before, after, beforeEach, afterEach, it, expect */
define([
    'energyefficiency/EnergyEfficiency'
], function (EnergyEfficiency) {
    'use strict';

    describe('EnergyEfficiency BIT Tests', function () {

        it('EnergyEfficiency should be defined', function () {
            expect(EnergyEfficiency).not.to.be.undefined;
        });

    });

});