// url timeout
let {
    Chrome,
    Tab
} = require('chromate');

// let url, page, timeout, args = require('system').args;

let [node, theScript, url, timeout, ...args] = process.argv;

// if (typeof timeout !== 'number') {
//     timeout = 5000;
// }

// start a headless Chrome process
Chrome.start().then(chrome => {
    let page = new Tab({
        verbose: false,
        failonerror: false
    });
    page.on('testFailure', ({data}) => console.error(data.failureMessage));
    page.on('load', () => page.execute(addLogging));
    // page.on('console', (data) => console.log(data));

    function addLogging() {

        QUnit.done(function (result) {
            let isFailure = (result.total === 0 || result.failed);

            let doneMessage =  "Test URL: " + window.document.URL
                +', Tests run: ' + result.total
                + ', Passed: ' + result.passed
                + ', Failures: ' + result.failed
                + ', Time elapsed: ' + result.runtime + " ms"
                + (isFailure ? " <<< FAILURE!" : "");

            if (typeof window.__chromate === 'function') {
                window.__chromate({
                    event: 'done',
                    data: Object.assign({}, result, {message: doneMessage})
                });
            }
        });

        QUnit.log(function (assertion) {
            if (false === assertion.result) {

                var failureMessage = "*** Assertion FAILED!! Test URL: " + window.document.URL + " Test: [" + assertion.name + "]";
                if (assertion.message) {
                    failureMessage += " Message: [" + assertion.message + "]";
                }

                if (assertion.hasOwnProperty('expected')) {
                    failureMessage += " Expected: [" + assertion.expected + "] Actual: [" + assertion.actual + "]";
                }

                if (typeof window.__chromate === 'function') {
                    window.__chromate({
                        event: 'testFailure',
                        data: { failureMessage, assertion }
                    })
                }

                console.error(failureMessage);
            }
        });
    }

    function handleResult(message) {
        var result, failed;
        if (message) {
            if (message.event === 'done') {
                result = message.data;
                failed = !result || !result.total || result.failed;
                if (!result.total) {
                    console.error('No tests were executed. Are you loading tests asynchronously?');
                } else {
                    console.log(result.message);
                }

                page.close().then( () => {
                        Chrome.kill(chrome);
                        process.exit(failed ? 1 : 0);
                    }
                );
            }
        }
    }
    page.on('done', handleResult);

    console.log(url);
    page.open(url)
        .then(() => page.evaluate('typeof QUnit').then(res => {
            if (res === 'undefined') {
                console.log('QUnit not found');
                page.close().then( () => {
                        Chrome.kill(chrome);
                        process.exit();
                    }
                );
            }
        }))
        .catch(err => console.log('Tab.open error', err));


});
