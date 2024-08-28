/**
 *  Css Name Spaces : provides a centralized point where css data is stored for use with
 *  Unit,Integration and Acceptance test
 *
 *  When editing this file consider:
 *      - Variable naming to be intuitive when using code completion by the developer.
 *      - Every property must be an object "{}" to allow extensibility.
 *      - Widgets may have namespaces independent from app or regions.
 *      - This library is to be used with either Unit, Integration or Acceptance lvl tests .
 */

// use amdefine library to ensure nodejs can also use this module.
if (typeof define !== 'function') {
    var define = require('../acceptance/node_modules/amdefine')(module);
}

define([

],function(){

    var app = {};
    var layout = {};

    app.val = "eaEnergyEfficiency";
    layout.val = "elLayouts";


    /**  Header (not a region just a child of app element) */
    app.header = {};
    app.header.val = app.val + "-header";

    app.contentHolder = {};
    app.contentHolder.val = app.val + '-content';

    layout.topSection = {};
    layout.topSection.val = layout.val + '-TopSection';
    layout.topSectionBreadCrumb = {};
    layout.topSectionBreadCrumb.val = layout.topSection.val + '-breadcrumb';
    layout.topSectionPlaceholder = {};
    layout.topSectionPlaceholder.val = layout.topSection.val + '-placeholder';
    layout.topSectionTitle = {};
    layout.topSectionTitle.val = layout.topSection.val + '-title';

    app.main = {};
    app.main.val = app.val + "-rMain";

    app.mainContent = {};
    app.mainContent.val = app.main.val + '-content';

    app.tableLib = {};
    app.tableLib.val = "elTablelib";
    app.table = {};
    app.table.val = app.tableLib.val + "-Table";
    app.table = {};
    app.table.val = app.tableLib.val + "-Table";
    app.table.header = {};
    app.table.header.val = app.table.val + "-header";
    app.table.body = {};
    app.table.body.val = app.table.val + "-body";
    app.tableRow = {};
    app.tableRow.val = "ebTableRow";
    app.tableCheckbox = {};
    app.tableCheckbox.val = "ebCheckbox";

    layout.actionBar = {};
    layout.actionBar.val = layout.val + '-QuickActionBar';
    layout.actionBarContents = {};
    layout.actionBarContents.val = layout.actionBar.val + '-contents';
    layout.actionBarItems = {};
    layout.actionBarItems.val = layout.val + '-ActionBarItem';

    app.ebDialog = {};
    app.ebDialog.val = 'ebDialog';

    app.ebDialogBox = {};
    app.ebDialogBox.val = 'ebDialogBox';
    app.ebDialogBox.errorBox = {};
    app.ebDialogBox.errorBox.val = app.ebDialogBox.val + '-contentBlock_type_error';
    app.ebDialogBox.primaryText = {};
    app.ebDialogBox.primaryText.val = app.ebDialogBox.val + '-primaryText';
    app.ebDialogBox.secondaryText = {};
    app.ebDialogBox.secondaryText.val = app.ebDialogBox.val + '-secondaryText';

    app.layout = layout;

    return app;
});
