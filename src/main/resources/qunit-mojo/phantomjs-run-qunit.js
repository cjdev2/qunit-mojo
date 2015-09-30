/*global phantom:false, require:false, console:false, window:false, QUnit:false */


function versionCheck(){
    var version = phantom.version, 
    versionString = version.major + "." + version.minor + "." + version.patch;

    if(version.major < 1 || (version.major ===1 && version.minor < 9)){
        console.error("Required phantomjs >= 1.9.0, but found " + versionString);
        phantom.exit(1);
    }
}

(function () {
    'use strict';

    versionCheck();
    
    var url, page, timeout, args = require('system').args;
    
    if (args.length < 2 || args.length > 3) {
        console.error('Usage:\n  phantomjs runner.js [url-of-your-qunit-testsuite] [timeout-in-seconds]');
        phantom.exit(1);
    }

    
    
    
    url = args[1];
    page = require('webpage').create();
    if (args[2] !== undefined) {
        timeout = parseInt(args[2], 10);
    }

    page.onConsoleMessage = function (msg) {
        console.log(msg);
    };

    page.onInitialized = function () {
        page.evaluate(watchForFinishedTests);
    };

    page.onCallback = function (message) {
        var result,
            failed;

        if (message && message.name === 'QUnit.done') {
            result = message.data;
            failed = !result || result.failed || (result.total === 0);

                // YES! This would be AWESOME! Unfortunately, this always spits out to the current working directory
                //                  so we'll need to parse out/pass in the test name and target directory
//            if (failed) {
//                page.render('target/surefire-reports/failure.jpg');
//            }

            phantom.exit(failed ? 1 : 0);
        }
    };

    page.open(url, function (status) {
        if (status !== 'success') {
            console.error('Unable to access network: ' + status);
            phantom.exit(1);
        } else {
            var qunitMissing = page.evaluate(function () {
                return (typeof QUnit === 'undefined' || !QUnit);
            });

            if (qunitMissing) {
                console.error('Test URL: ' + url + ' - The `QUnit` object is not present on this page.');
                phantom.exit(1);
            }

            // Set a timeout on the test running, otherwise tests with async problems will hang forever
            if (typeof timeout === 'number') {
                setTimeout(function () {
                    console.error('Test URL: ' + url + ' - The specified timeout of ' + timeout + ' ms has expired. Aborting...');
                    phantom.exit(1);
                }, timeout);
            }
        }
    });

    function watchForFinishedTests() {
        window.document.addEventListener('DOMContentLoaded', function () {
            QUnit.done(function (result) {
                var isFailure = (result.total === 0 || result.failed);

                console.log("Test URL: " + window.document.URL +', Tests run: ' + result.total + ', Passed: ' + result.passed + ', Failures: ' + result.failed + ', Time elapsed: ' + result.runtime + " ms" + (isFailure ? " <<< FAILURE!" : ""));

                if (typeof window.callPhantom === 'function') {
                    window.callPhantom({
                        'name': 'QUnit.done',
                        'data': result
                    });
                }
            });

            QUnit.log(function (assertion) {
                if (false === assertion.result) {

                    var failureMessage = "*** Assertion FAILED!! Test URL: " + window.document.URL + " Test: [" + assertion.name + "]";

                    if (assertion.message) {
                        failureMessage += " Message: [" + assertion.message + "]";
                    }

                    if (assertion.expected) {
                        failureMessage += " Expected: [" + assertion.expected + "] Actual: [" + assertion.actual + "]";
                    }

                    console.error(failureMessage);
                }
            });

        }, false);

    }
})();
