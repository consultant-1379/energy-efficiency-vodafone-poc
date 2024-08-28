define([
    'jscore/core',
    'energyEfficiency/regions/main/Main',
    'energyEfficiency/regions/main/MainView'

], function (core, Main, View) {
    describe('Main', function() {
        var _sandbox, classUnderTest, eventBusStub, stub;

        beforeEach(function () {
            _sandbox = sinon.sandbox.create();

            classUnderTest = new Main();
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
            it('should create and start sub-Regions', function () {
                // ARRANGE
                arrangeEventBus();

                // ACT
                classUnderTest.onStart();

                // ASSERT

            });
        });

    });

});