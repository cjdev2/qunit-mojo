// url timeout
let {
    Chrome,
    Tab
} = require('chromate');

var url, page, timeout, args = require('system').args;

// start a headless Chrome process
Chrome.start().then(chrome => {
    let page = new Tab({
        verbose: true,
        failonerror: false
    });
page.on('console', msg => console.error(msg));
page.on('load', () => page.execute(addLogging));

function addLogging() {
    QUnit.done(function (result) {
        console.log('\n' + 'Took ' + result.runtime + 'ms to run ' + result.total + ' tests. ' + result.passed + ' passed, ' + result.failed + ' failed.');

        if (typeof window.__chromate === 'function') {
            window.__chromate({
                event: 'done',
                data: result
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
}

page.on('done', handleResult);

function handleResult(message) {
    var result, failed;
    if (message) {
        if (message.event === 'done') {
            result = message.data;
            failed = !result || !result.total || result.failed;
            if (!result.total) {
                console.error('No tests were executed. Are you loading tests asynchronously?');
            }
            process.exit(failed ? 1 : 0);
        }
    }
}

page.open('http://localhost:8098/qunit-mojo/javascript/account/advertiser/programterms/edit/actionterms.qunit.js')
    .then(() => page.evaluate('typeof QUnit').then(res => {
    if (res === 'undefined') {
    console.log('QUnit not found');
    page.close().then(exit);
}
}))
.catch(err => console.log('Tab.open error', err));


});
