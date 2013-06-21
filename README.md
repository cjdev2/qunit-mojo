qunit-mojo
==========

A maven plugin for quickly and easily creating, refactoring and running qunit tests


Goals:

* qunit:test -> runs all your '*.qunit.js' files as qunit tests using phantomjs (or, optionally, qunit-test-driver)
* qunit:serve -> starts an http server and serves your code so you can run it in the browser

Example:

~~~~~ javascript
/* src/main/foo/my-first-test.qunit.js */
define([], function(){
    test("my first failing test", function(){
        ok(false);
    });
});
~~~~~

~~~~~ 
stu@knox:~/projects/my-web-project mvn qunit:test
[INFO] Scanning for projects...
[INFO] ------------------------------------------------------------------------
[INFO] Building my-web-project - com.cj.example:my-web-project:jar:0.0.1-SNAPSHOT
[INFO]    task-segment: [qunit:test]
[INFO] ------------------------------------------------------------------------
[INFO] [qunit:test {execution: default-cli}]
The extra paths are []
2013-06-21 14:03:04.270:INFO::Logging to STDERR via org.mortbay.log.StdErrLog
2013-06-21 14:03:04.272:INFO::jetty-6.1.24
2013-06-21 14:03:04.284:INFO::Started SocketConnector@0.0.0.0:8098
[INFO] Executing qunit tests on 1 thread(s) using phantomjs
[INFO] Running: my-first-test.qunit.js
Tests run: 1, Passed: 0, Failures: 1, Time elapsed: 22 ms <<< FAILURE!
2013-06-21 14:03:04.527:INFO::Stopped SocketConnector@0.0.0.0:8098
[INFO] ------------------------------------------------------------------------
[ERROR] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Problems found in 'my-first-test.qunit.js

[INFO] ------------------------------------------------------------------------
[INFO] For more information, run Maven with the -e switch
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 1 second
[INFO] Finished at: Fri Jun 21 14:03:04 PDT 2013
[INFO] Final Memory: 12M/302M
[INFO] ------------------------------------------------------------------------
~~~~~

Setup:

~~~~~ xml

<project>
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.cj.example</groupId>
    <artifactId>qunit-mojo-example</artifactId>
    <version>0.0.1-SNAPSHOT</version>

    <build>
        <plugins>
            <plugin>
                <groupId>com.cj.qunit.mojo</groupId>
                <artifactId>qunit-maven-plugin</artifactId>
                <version>2.0.0</version>
                <executions>
                    <execution>
                        <phase>test</phase>
                        <goals>
                            <goal>test</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
~~~~~
