define([
    'jscore/core'
], function (core)  {

    // Will execute before every test as it is outside of a describe block.
    beforeEach(function () {
        // PhantomJS workaround for setInterval/setTimeout failing to execute callback (CDS-4464)
        for (var i = 0; i < 100000; i++) {
            clearInterval(i);
            clearTimeout(i);
        }
    });

    // Will execute after every test
    afterEach(function () {
        // Remove orphaned Component List elements
        var componentLists = document.getElementsByClassName('elWidgets-ComponentList');
        for (var i = 0; i < componentLists.length; i++) {
            componentLists[i].parentNode.removeChild(componentLists[i]);
        }
        // Remove orphaned Dialog elements
        var dialogs = document.getElementsByClassName('ebDialog');
        for (var j = 0; j < dialogs.length; j++) {
            dialogs[j].parentNode.removeChild(dialogs[j]);
        }
    });

    describe('PhantomJS 1.9.x Workaround', function () {
        it('Empty test required to prevent crash when separating beforeEach into separate file from test.', function () {
            console.log('   ' + window.navigator.userAgent);
            if (window.callPhantom) {
                console.log('    PhantomJS version: ' + (window.navigator.userAgent.indexOf('PhantomJS/1.')?'1.x.x':'2.x.x+'));
            }
        });
    });
});