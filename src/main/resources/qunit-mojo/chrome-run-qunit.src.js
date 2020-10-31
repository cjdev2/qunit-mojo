// url timeout
let {
    Chrome,
    Tab
} = require('chromate');

let process = require('process');
let readline = require('readline');
let split = require('split');
let async = require('async');

// let url, page, timeout, args = require('system').args;

let [node, theScript, timeout, ...args] = process.argv;

// if (typeof timeout !== 'number') {
//     timeout = 5000;
// }

let lineReader = (stream, callback) => {

}

// start a headless Chrome process
Chrome.start().then(chrome => {
    function addLogging() {

        QUnit.done(function (result) {
            let isFailure = (result.total === 0 || result.failed);

            let doneMessage = "Test URL: " + window.document.URL
                + ', Tests run: ' + result.total
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
                        data: {failureMessage, assertion}
                    })
                }

                console.error(failureMessage);
            }
        });
    }

    let handleResultWithPage = (page) => function handleResult(message) {
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
            }
        }
    }

    let urls = [];

    process.stdin.pipe(split(/\r?\n/)).on('data', file => {
        if (file.trim() !== "") {
            let page = new Tab({
                verbose: false,
                failonerror: false
            });
            page.on('testFailure', ({data}) => console.error(data.failureMessage));
            page.on('load', () => page.execute(addLogging));
            // page.on('console', (data) => console.log(data));

            let partitionval = file.match(/(test[/]+)javascript/);
            let partitionidx = partitionval.index + partitionval[1].length;
            let testPath = file.slice(partitionidx);
            let url = `http://localhost:8098/qunit-mojo/${testPath}`
            console.log(url);
            urls.push(url);
        }
    });

    let pages = [];
            page.open(url)
                .then(() => page.evaluate('typeof QUnit').then(res => {
                    if (res === 'undefined') {
                        console.log('QUnit not found');
                    }
                }))
                .catch(err => {
                    console.log('Tab.open error', err);
                })

            let thePromise = new Promise(
                (resolve, reject) => {
                    page.on('done', (message) => {
                        handleResultWithPage(page)(message);
                        page.close();
                        resolve();
                    });
                }
            );

            pages.push(thePromise);

        }
    });

    process.stdin.on('end', () => {
        Promise.all(pages).finally(() => {
            console.log('exiting');
            Chrome.kill();
            process.exit();
        });
    })
});
