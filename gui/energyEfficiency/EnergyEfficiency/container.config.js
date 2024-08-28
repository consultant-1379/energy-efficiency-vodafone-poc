define({
    name: "ENM",
    defaultApp: "energyefficiency",
    properties: {
        help: {
            helpCenter: true,
            search: true,
            i18n: {
                locales: ['en-us']
            }
        },
        helpbutton: {
            helpCenter: true,
            aboutDialog: true,
            i18n: {
                locales: ['en-us']
            }
        },
        helpsearch: {
            url: '/help-search/rest/help/search',
            i18n: {
                locales: ['en-us']
            }
        },
        systemtime: {
            url: '/rest/system/time'
        },
        helplib: {
            i18n: {
                locales: ['en-us']
            }
        }
    },
    components: [
        {
            path: "helpbutton"
        },
        {
            path: "contextmenu"
        },
        {
            path: "systemtime"
        },
        {
            path: "navigation"
        },
        {
            path: "flyout"
        }
    ]

});