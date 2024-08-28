define([
    'jscore/core',
    'jscore/ext/net',
    'energyEfficiency/regions/details/Details',
    'energyEfficiency/regions/details/DetailsView',
//    'i18n!energyefficiency/dictionary.json',
//    'chartlib/charts/Column',
//    'container/api',
//    '../../widgets/radialChart/RadialChart',
    'energyEfficiency/datastore/datastore',
    'energyEfficiency/common'
], function (core, net, Details, View, datastore, FC) { // dictionary, Column, container, RadialChart,

    describe('Details', function() {
            var _sandbox, classUnderTest, eventBusStub, stub;

            beforeEach(function () {
                _sandbox = sinon.sandbox.create();

                classUnderTest = new Details();
                classUnderTest.view = _sandbox.stub(new View());
            });

            afterEach(function () {
                _sandbox.restore();
            });

            function arrangeEventBus() {
                // ARRANGE
                eventBusStub = sinon.createStubInstance(core.EventBus);
                classUnderTest.getContext = function() {
                    stub = sinon.createStubInstance(core.AppContext);
                    stub.eventBus = eventBusStub;
                    return stub;
                };
                classUnderTest.getEventBus = function() {
                    return eventBusStub;
                };
            }

            describe('onStart()', function () {
                it('should create and start Details region', function () {
                    // ARRANGE
                    arrangeEventBus();

                    // ACT
                    classUnderTest.onStart();


                    // ASSERT

                });
            });
    });

});