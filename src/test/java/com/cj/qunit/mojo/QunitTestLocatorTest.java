package com.cj.qunit.mojo;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.cj.qunit.mojo.QunitTestLocator.LocatedTest;

public class QunitTestLocatorTest {

    @Test
    public void prependsTheWebRootToTheStaticFilesButOmitsTheLeadingSlash() {

        // given
        File dir = tempDirectory();
        
        write(dir, "somedir/Whatever.Qunit.html", "dummy content");
        QunitTestLocator locator = new QunitTestLocator();
        
        // when
        List<LocatedTest> results = locator.locateTests(dir, "/foo/bar");
        
        // then
        assertEquals(1, results.size());
        assertEquals("foo/bar/somedir/Whatever.Qunit.html", results.get(0).relativePath);
    }
    
    @Test
    public void worksWithWebRootsThatIncludeATrailingSlash() {

        // given
        File dir = tempDirectory();
        
        write(dir, "somedir/Whatever.Qunit.html", "dummy content");
        QunitTestLocator locator = new QunitTestLocator();
        
        // when
        List<LocatedTest> results = locator.locateTests(dir, "/foo/bar/");
        
        // then
        assertEquals(1, results.size());
        assertEquals("foo/bar/somedir/Whatever.Qunit.html", results.get(0).relativePath);
    }

    @Test
    public void worksWithCoffeescript() {

        // given
        File dir = tempDirectory();

        write(dir, "somedir/Whatever.qunit.coffee", "dummy content");
        QunitTestLocator locator = new QunitTestLocator();

        // when
        List<LocatedTest> results = locator.locateTests(dir, "/foo/bar/");

        // then
        assertEquals(1, results.size());
        assertEquals("somedir/Whatever.qunit.coffee.Qunit.html", results.get(0).relativePath);
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

    private static File tempDirectory(){
        try {
            File d = File.createTempFile("whatever", ".dir");
            if(!d.delete() || !d.mkdir()){
                throw new RuntimeException("Could not create temporary directory at " + d.getAbsolutePath());
            }
            
            return d;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
