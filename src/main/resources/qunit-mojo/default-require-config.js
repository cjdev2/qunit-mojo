var require = {
	baseUrl: "/",
    paths: {
        cs: '/qunit-mojo/cs-0.4.3',
        jsx: '/qunit-mojo/jsx-0.4.0',
        react: '/qunit-mojo/react-0.12.0',
        JSXTransformer: '/qunit-mojo/JSXTransformer-0.12.0-rc1',
        'coffee-script': '/qunit-mojo/coffee-script-1.6.3',
        qunit: '/qunit-mojo/qunit-1.11.0',
        jquery: '/qunit-mojo/jquery-1.8.2.min'
    },
    shim: {
        react: {
            exports: 'React',
            deps: ['/qunit-mojo/shimBind']
        },
        jquery: {
            exports: '$'
        },
        qunit:{
             exports: 'QUnit',
             init: function() {
                 QUnit.config.autoload = false;
                 QUnit.config.autostart = false;
             }
        }
    },
    jsx: {
        fileExtension: '.jsx'
    }
};
