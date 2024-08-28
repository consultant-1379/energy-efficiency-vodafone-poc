define([
    'jscore/core',
    'energyEfficiency/regions/left/Left',
    'energyEfficiency/regions/left/LeftView'
//    'widgets/Tree',
//    'jscore/ext/net',
//    'container/api',
//    'energyEfficiency/types/types'
], function (core, Left, View) { // Tree, net, container, types

    describe('Left', function() {
            var _sandbox, classUnderTest, eventBusStub, stub;

            beforeEach(function () {
                _sandbox = sinon.sandbox.create();

                classUnderTest = new Left();
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
                it('should create and start Left region', function () {
                    // ARRANGE
                    arrangeEventBus();


                    // ACT
                    classUnderTest.onStart();


                    // ASSERT


                });
            });

    });

});