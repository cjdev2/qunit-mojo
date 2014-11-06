package com.cj.qunit.mojo;

import static com.cj.qunit.mojo.fs.FilesystemFunctions.tempDirectory;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.util.log.JavaUtilLog;
import org.junit.Test;


public class QunitTestLocatorTest {

    @Test
    public void prependsTheWebRootToTheStaticFilesButOmitsTheLeadingSlash() {

        // given
        File dir = tempDirectory();

        write(dir, "somedir/Whatever.Qunit.html", "dummy content");
        QunitTestLocator locator = new QunitTestLocator();

        // when
        List<LocatedTest> results = locator.locateTests(Collections.singletonList(dir), "/foo/bar", "");

        // then
        assertEquals(1, results.size());
        assertEquals("foo/bar/somedir/Whatever.Qunit.html", results.get(0).relativePathToHtmlFile);
    }

    @Test
    public void worksWithWebRootsThatIncludeATrailingSlash() {

        // given
        File dir = tempDirectory();

        write(dir, "somedir/Whatever.Qunit.html", "dummy content");
        QunitTestLocator locator = new QunitTestLocator();

        // when
        List<LocatedTest> results = locator.locateTests(Collections.singletonList(dir), "/foo/bar/", "");

        // then
        assertEquals(1, results.size());
        assertEquals("foo/bar/somedir/Whatever.Qunit.html", results.get(0).relativePathToHtmlFile);
    }

    @Test
    public void worksWithRequirePrefixes() {

        // given
        File dir = tempDirectory();

        write(dir, "somedir/Whatever.qunit.js", "dummy content");
        QunitTestLocator locator = new QunitTestLocator();

        // when
        List<LocatedTest> results = locator.locateTests(Collections.singletonList(dir), "/foo/bar/", "/foo");

        // then
        assertEquals(1, results.size());
        LocatedTest result = results.get(0);
        assertEquals("qunit-mojo/somedir/Whatever.qunit.js", result.relativePathToHtmlFile);
        assertEquals("bar/somedir/Whatever.qunit", result.requireJsModuleName);
        assertEquals(new File(dir, "somedir/Whatever.qunit.js"), result.pathOnDisk);
    }


    @Test
    public void worksWithSingleSlashRequirePrefixes() {

        // given
        File dir = tempDirectory();

        write(dir, "somedir/Whatever.qunit.js", "dummy content");
        QunitTestLocator locator = new QunitTestLocator();

        // when
        List<LocatedTest> results = locator.locateTests(Collections.singletonList(dir), "/foo/bar/", "/");

        // then
        assertEquals(1, results.size());
        LocatedTest result = results.get(0);
        assertEquals("qunit-mojo/somedir/Whatever.qunit.js", result.relativePathToHtmlFile);
        assertEquals("foo/bar/somedir/Whatever.qunit", result.requireJsModuleName);
        assertEquals(new File(dir, "somedir/Whatever.qunit.js"), result.pathOnDisk);
    }

    @Test
    public void theMostSpecificBaseDirWins() {

        // given
        File dir = tempDirectory();

        write(dir, "somedir/Whatever.qunit.js", "dummy content");
        QunitTestLocator locator = new QunitTestLocator();

        List<File> dirs = Arrays.asList(
                                dir,
                                new File(dir, "somedir"));

        // when
        List<LocatedTest> results = locator.locateTests(dirs, "/foo/bar/", "");

        // then
        assertEquals(1, results.size());
        LocatedTest result = results.get(0);
        assertEquals("qunit-mojo/Whatever.qunit.js", result.relativePathToHtmlFile);
        assertEquals("foo/bar/Whatever.qunit", result.requireJsModuleName);
        assertEquals(new File(dir, "somedir/Whatever.qunit.js"), result.pathOnDisk);
    }


    @Test
    public void theMostSpecificBaseDirWinsForHandcodedFiles() {

        // given
        File dir = tempDirectory();

        write(dir, "somedir/WhateverQunitTest.html", "dummy content");
        QunitTestLocator locator = new QunitTestLocator();

        List<File> dirs = Arrays.asList(
                                dir,
                                new File(dir, "somedir"));

        // when
        List<LocatedTest> results = locator.locateTests(dirs, "/foo/bar/", "");

        // then
        assertEquals(1, results.size());
        LocatedTest result = results.get(0);
        assertEquals("foo/bar/WhateverQunitTest.html", result.relativePathToHtmlFile);
        assertEquals(null, result.requireJsModuleName);
        assertEquals(new File(dir, "somedir/WhateverQunitTest.html"), result.pathOnDisk);
    }

    @Test
    public void worksWithJavascript() {

        // given
        File dir = tempDirectory();

        write(dir, "somedir/Whatever.qunit.js", "dummy content");
        QunitTestLocator locator = new QunitTestLocator();

        // when
        List<LocatedTest> results = locator.locateTests(Collections.singletonList(dir), "/foo/bar/", "");

        // then
        assertEquals(1, results.size());
        LocatedTest result = results.get(0);
        assertEquals("qunit-mojo/somedir/Whatever.qunit.js", result.relativePathToHtmlFile);
        assertEquals("foo/bar/somedir/Whatever.qunit", result.requireJsModuleName);
        assertEquals(new File(dir, "somedir/Whatever.qunit.js"), result.pathOnDisk);
    }

    @Test
    public void worksWithCoffeescript() {

        // given
        File dir = tempDirectory();

        write(dir, "somedir/Whatever.qunit.coffee", "dummy content");
        QunitTestLocator locator = new QunitTestLocator();

        // when
        List<LocatedTest> results = locator.locateTests(Collections.singletonList(dir), "/foo/bar/", "");

        // then
        assertEquals(1, results.size());
        LocatedTest result = results.get(0);
        assertEquals("qunit-mojo/somedir/Whatever.qunit.coffee", result.relativePathToHtmlFile);
        assertEquals("cs!foo/bar/somedir/Whatever.qunit", result.requireJsModuleName);
        assertEquals(new File(dir, "somedir/Whatever.qunit.coffee"), result.pathOnDisk);
    }

    @Test
    public void worksWithJsx() {

        // given
        File dir = tempDirectory();

        write(dir, "somedir/Whatever.qunit.jsx", "dummy content");
        QunitTestLocator locator = new QunitTestLocator();

        // when
        List<LocatedTest> results = locator.locateTests(Collections.singletonList(dir), "/foo/bar/", "");

        // then
        assertEquals(1, results.size());
        LocatedTest result = results.get(0);
        assertEquals("qunit-mojo/somedir/Whatever.qunit.jsx", result.relativePathToHtmlFile);
        assertEquals("jsx!foo/bar/somedir/Whatever.qunit", result.requireJsModuleName);
        assertEquals(new File(dir, "somedir/Whatever.qunit.jsx"), result.pathOnDisk);
    }

    @Test
    public void theFilterMatchesTheTestNameOrThePathOrARegex() throws Exception {

        final String[] filters = {"bunnies", "somewhere-else", "regex:.*some.*nies.*"};

        for(String filter : filters){
            // given
            File projectDirectory = tempDirectory();

            {
                File srcTestDirectory = new File(projectDirectory, "src/test/somewhere");
                srcTestDirectory.mkdirs();
                FileUtils.writeStringToFile(new File(srcTestDirectory, "frogs.qunit.js"), "require([], function(){test('dummy test', function(){ok(true);});})");
            }

            {
                File srcTestDirectory = new File(projectDirectory, "src/test/somewhere-else");
                srcTestDirectory.mkdirs();
                FileUtils.writeStringToFile(new File(srcTestDirectory, "bunnies.qunit.js"), "require([], function(){test('dummy test', function(){ok(true);});})");
            }

            QunitTestLocator locator = new QunitTestLocator();

            // when
            List<LocatedTest> results = locator.locateTests(Collections.singletonList(projectDirectory), "/foo/bar/", "", filter);

            // then
            assertEquals(1, results.size());
            LocatedTest result = results.get(0);
            assertEquals("qunit-mojo/src/test/somewhere-else/bunnies.qunit.js", result.relativePathToHtmlFile);
            assertEquals("foo/bar/src/test/somewhere-else/bunnies.qunit", result.requireJsModuleName);
            assertEquals(new File(projectDirectory, "src/test/somewhere-else/bunnies.qunit.js"), result.pathOnDisk);
        }
    }

    private static void write(File baseDir, String path, String content){
        try {
            File where = new File(baseDir, path);
            where.getParentFile().mkdirs();
            FileUtils.writeStringToFile(where, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
