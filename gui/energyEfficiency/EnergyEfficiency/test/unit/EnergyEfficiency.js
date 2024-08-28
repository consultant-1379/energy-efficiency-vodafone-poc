/*global define, describe, before, after, beforeEach, afterEach, it, expect */
define([
    'jscore/ext/net',
    'jscore/core',
    'layouts/TopSection',
    'layouts/MultiSlidingPanels',
    'energyEfficiency/EnergyEfficiency',
    'energyEfficiency/regions/left/Left',
    'energyEfficiency/regions/details/Details',
    'energyEfficiency/regions/main/Main'

], function (net, core, TopSection, MultiSlidingPanels, EnergyEfficiency, Left, Details, Main) {
    'use strict';

    describe('EnergyEfficiency UNIT Tests', function () {
    
        var classUnderTest, _sandbox, eventBusStub, stub;

        beforeEach(function () {
            _sandbox = sinon.sandbox.create();
            classUnderTest = new EnergyEfficiency();

            classUnderTest.options = {
                breadcrumb : [
                    {
                        name : "ENM",
                        url : "#energyefficiency"
                    },
                    {
                        name : "Energy Efficiency",
                        url : "#energyefficiency",
                        app : "energyefficiency",
                        children : []
                    }
                ],
                namespace : "energyefficiency",
                properties : {
                    title : "Energy Efficiency"
                }
            };

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

        it('EnergyEfficiency should be defined', function () {
            expect(classUnderTest).to.be.defined;
        });

        describe('onStart', function() {
            it('for the sake of code coverage', function() {
                // ARRANGE
                _sandbox.stub(TopSection.prototype);
                _sandbox.stub(classUnderTest, 'onDatastoreLoaded');
                _sandbox.stub(classUnderTest, 'onDatastoreUpdated');
                _sandbox.stub(classUnderTest, 'refreshDatastore');
                _sandbox.stub(classUnderTest, 'hideAndShowRightPanel');
                _sandbox.stub(classUnderTest, 'setupGui');
                arrangeEventBus();

                // ACT
                classUnderTest.onStart();

                // ASSERT
                expect(eventBusStub.subscribe.callCount).to.equal(4);
                expect(eventBusStub.subscribe.getCall(0).calledWith('dataStoreLoaded')).to.equal(true);
                expect(eventBusStub.subscribe.getCall(1).calledWith('dataStoreUpdated')).to.equal(true);
                expect(eventBusStub.subscribe.getCall(2).calledWith('reloadDataStore')).to.equal(true);
                expect(eventBusStub.subscribe.getCall(3).calledWith('rowTabSelectForDetails')).to.equal(true);
                expect(eventBusStub.publish.callCount).to.equal(0);

            });
        });

        describe('setupGui', function() {
            it('for the sake of code coverage', function() {
                // ARRANGE
                var showMainWindow = _sandbox.stub(classUnderTest, 'showMainWindow');
                var readTopology = _sandbox.stub(classUnderTest, 'readTopology');

                //ACT
                classUnderTest.setupGui();

                // ASSERT
                expect(eventBusStub.subscribe.callCount).to.equal(4);
                expect(eventBusStub.subscribe.getCall(0).calledWith('dataStoreLoaded')).to.equal(true);
                expect(eventBusStub.subscribe.getCall(1).calledWith('dataStoreUpdated')).to.equal(true);
                expect(eventBusStub.subscribe.getCall(2).calledWith('reloadDataStore')).to.equal(true);
                expect(eventBusStub.subscribe.getCall(3).calledWith('rowTabSelectForDetails')).to.equal(true);
            });
        });

        describe('publishEventsForDataStoreLoaded', function() {
            it('should publish events for datastore loaded', function() {
                // ARRANGE
                arrangeEventBus();

                //ACT
                classUnderTest.publishEventsForDataStoreLoaded();

                // ASSERT
                expect(eventBusStub.publish.callCount).to.equal(3);
                expect(eventBusStub.publish.getCall(0).calledWith('updateTabs')).to.equal(true);
                expect(eventBusStub.publish.getCall(1).calledWith('wait-end')).to.equal(true);
                expect(eventBusStub.publish.getCall(2).calledWith('dataStoreLoaded')).to.equal(true);
                expect(eventBusStub.unsubscribe.callCount).to.equal(1);
                expect(eventBusStub.unsubscribe.getCall(0).calledWith('stopProgress')).to.equal(true);
            });
        });

        describe('showMainWindow', function() {
            it('should show the main region', function() {
                // ARRANGE
                arrangeEventBus();
                var topSectionStub = _sandbox.stub(TopSection.prototype);
                classUnderTest.topSection = topSectionStub;
//                classUnderTest.multiSlidingPanels = 'multiSlidingPanels';
//                _sandbox.stub(TopSection.prototype, 'attachTo', function () {});
//                _sandbox.stub(TopSection.prototype, 'setContent', function () {});
//                _sandbox.spy(TopSection.prototype, 'init');

                var viewStub = {
                    getContent: function () {},
                    getElement: function () {}
                };
                classUnderTest.view = viewStub;
                _sandbox.spy(viewStub, 'getContent');
                _sandbox.spy(viewStub, 'getElement');

                //ACT
                classUnderTest.showMainWindow();

                // ASSERT
                expect(TopSection.prototype.attachTo.callCount).to.equal(1);
                expect(TopSection.prototype.setContent.callCount).to.equal(1);
            });
        });


    });

});