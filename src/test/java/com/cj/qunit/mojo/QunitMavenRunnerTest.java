package com.cj.qunit.mojo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.util.log.JavaUtilLog;
import org.junit.Test;

import com.cj.qunit.mojo.QunitMavenRunner;

public class QunitMavenRunnerTest {
    
    private static void write(File baseDir, String path, String content){
        try {
            File where = new File(baseDir, path);
            where.getParentFile().mkdirs();
            FileUtils.writeStringToFile(where, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    
    @Test
    public void theFilterMatchesTheTestNameOrThePathOrARegex() throws Exception {
        final String[] filters = {"bunnies", "somewhere-else", "regex:.*some.*nies.*"};
        
        for(String filter : filters){
            // given
            File projectDirectory = tempDirectory();
            
            {
                File srcMainHtmlDirectory = new File(projectDirectory, "src/test/somewhere");
                srcMainHtmlDirectory.mkdirs();
                FileUtils.writeStringToFile(new File(srcMainHtmlDirectory, "frogs.qunit.js"), "require([], function(){test('dummy test', function(){ok(true);});})");
            }
            
            {
                File srcMainHtmlDirectory = new File(projectDirectory, "src/test/somewhere-else");
                srcMainHtmlDirectory.mkdirs();
                FileUtils.writeStringToFile(new File(srcMainHtmlDirectory, "bunnies.qunit.js"), "require([], function(){test('dummy test', function(){ok(true);});})");
            }
            
            
            QunitMavenRunner runner = new QunitMavenRunner();
            FakeLog log = new FakeLog();
            
            // when
            List<String> problems;
            Exception t;
            try {
                problems = runner.run("", Collections.singletonList(projectDirectory), filter, Collections.<File>emptyList(), "", log, 5000, new JavaUtilLog());
                t = null;
            } catch (Exception e) {
                t = e;
                t.printStackTrace();
                problems = Collections.emptyList();
            }
            
            // then
            Assert.assertTrue("The plugin should not blow up", t == null);
            Assert.assertEquals(0, problems.size());
            Assert.assertEquals(1, log.pathsRun.size());
            Assert.assertEquals("src/test/somewhere-else/bunnies.qunit.js", log.pathsRun.get(0));
        }
    }
    
    @Test
    public void theRunnerProvidesRequireDotJs() throws Exception {
        // given
        File projectDirectory = tempDirectory();
        File srcMainHtmlDirectory = new File(projectDirectory, "src/test/whatever");
        srcMainHtmlDirectory.mkdirs();
        
        FileUtils.writeStringToFile(new File(srcMainHtmlDirectory, "Whatever.qunit.js"), "require([], function(){module('mytests');test('mytest', function(){ok(true);});})");
        
        QunitMavenRunner runner = new QunitMavenRunner();
        FakeLog log = new FakeLog();
        
        // when
        List<String> problems;
        Exception t;
        try {
            problems = runner.run("", Collections.singletonList(projectDirectory), null, Collections.<File>emptyList(), "", log, 5000, new JavaUtilLog());
            t = null;
        } catch (Exception e) {
            t = e;
            t.printStackTrace();
            problems = Collections.emptyList();
        }
        
        // then
        Assert.assertTrue("The plugin should not blow up", t == null);
        Assert.assertEquals(0, problems.size());
        Assert.assertEquals(1, log.pathsRun.size());
        Assert.assertEquals("src/test/whatever/Whatever.qunit.js", log.pathsRun.get(0));
    }
    
    @Test
    public void theWebRootIsConfigurable() throws Exception {
        // given
        
        File projectDirectory = tempDirectory();
        
        write(projectDirectory, "src/main/whatever/my-require-config.js",
                "var require = {" + 
                        "baseUrl: '/path/to/my/app/'" +  
                "};");

        write(projectDirectory, "src/main/whatever/a.js",
                "define(function(){" +
                "    return 'I am module a';" +
                "});");
        
        write(projectDirectory, "src/test/whatever/somedir/Whatever.qunit.js",
                "require(['a'], function(a){" +
                "    module('mytests');" +
                "    test('mytest', function(){" +
                "        equal(a, 'I am module a');" +
                "    });" +
                "});");
        
        QunitMavenRunner runner = new QunitMavenRunner();
        FakeLog log = new FakeLog();
        
        // when
        List<String> problems;
        Exception t;
        try {
            problems = runner.run("/path/to/my/app/", 
                                  Arrays.asList(
                                            new File(projectDirectory, "src/main/whatever"),
                                            new File(projectDirectory, "src/test/whatever")),
                                  null,
                                  Collections.<File>emptyList(), "/path/to/my/app/my-require-config.js", log, 5000, new JavaUtilLog());
            t = null;
        } catch (Exception e) {
            t = e;
            t.printStackTrace();
            problems = Collections.emptyList();
        }
        
        // then
        Assert.assertTrue("The plugin should not blow up", t == null);
        for(String p : problems){
            System.err.println(p);
        }
        Assert.assertEquals(0, problems.size());
        Assert.assertEquals(1, log.pathsRun.size());
        Assert.assertEquals("somedir/Whatever.qunit.js", log.pathsRun.get(0));
   
    }
    
    @Test
    public void nullRequireDotJsConfigFileDefaultsToSomethingUsableToNestedCode() throws Exception {
        // given
        
        for(String nullPath: new String[]{null, ""}){
            File projectDirectory = tempDirectory();
            
            write(projectDirectory, "src/main/whatever/a.js",
                    "define(function(){" +
                    "    return 'I am module a';" +
                    "});");
            
            write(projectDirectory, "src/test/whatever/somedir/Whatever.qunit.js",
                    "require(['a'], function(a){" +
                    "    module('mytests');" +
                    "    test('mytest', function(){" +
                    "        equal(a, 'I am module a');" +
                    "    });" +
                    "});");
            
            QunitMavenRunner runner = new QunitMavenRunner();
            FakeLog log = new FakeLog();
            
            // when
            List<String> problems;
            Exception t;
            try {
                problems = runner.run("", 
                                      Arrays.asList(
                                                new File(projectDirectory, "src/main/whatever"),
                                                new File(projectDirectory, "src/test/whatever")), 
                                      null,
                                      Collections.<File>emptyList(), nullPath, log, 5000, new JavaUtilLog());
                t = null;
            } catch (Exception e) {
                t = e;
                t.printStackTrace();
                problems = Collections.emptyList();
            }
            
            // then
            Assert.assertTrue("The plugin should not blow up", t == null);
            for(String p : problems){
                System.err.println(p);
            }
            Assert.assertEquals(0, problems.size());
            Assert.assertEquals(1, log.pathsRun.size());
            Assert.assertEquals("somedir/Whatever.qunit.js", log.pathsRun.get(0));
        }
       
    }
    
    
    @Test
    public void nonexistentRequireDotJsConfigFilesAreUnacceptable() throws Exception {
        // given
        File projectDirectory = tempDirectory();
        
        QunitMavenRunner runner = new QunitMavenRunner();
        FakeLog log = new FakeLog();
        
        // when
        Exception err;
        try {
            runner.run("", Collections.singletonList(projectDirectory), null, Collections.<File>emptyList(), "/some-nonexistent-file.js", log, 5000, new JavaUtilLog());
            err = null;
        } catch (Exception e) {
            err = e;
        }
        
        // then
        Assert.assertNotNull(err);
        Assert.assertEquals(RuntimeException.class.getName(), err.getClass().getName());
        Assert.assertEquals(
                "You configured a require.js configuration path of \"/some-nonexistent-file.js\".  However, it doesn't seem to exist.  Here's where I looked for it:\n"+
                "    " + projectDirectory.getAbsolutePath() + "/some-nonexistent-file.js", err.getMessage().trim());
    }
    
    @Test
    public void findsTestJsFilesUnderTheSrcTestDirectory() throws Exception {
        // given
        File projectDirectory = tempDirectory();
        File srcMainHtmlDirectory = new File(projectDirectory, "src/test/whatever");
        srcMainHtmlDirectory.mkdirs();
        
        FileUtils.writeStringToFile(new File(srcMainHtmlDirectory, "Whatever.qunit.js"), 
        									"module('mytests');" +
        									"test('mytest', function(){ok(true);});");
        
        QunitMavenRunner runner = new QunitMavenRunner();
        FakeLog log = new FakeLog();
        
        // when
        List<String> problems;
        Exception t;
        try {
            problems = runner.run("", Collections.singletonList(projectDirectory), null, Collections.<File>emptyList(), "", log, 5000, new JavaUtilLog());
            t = null;
        } catch (Exception e) {
            t = e;
            problems = Collections.emptyList();
        }
        
        // then
        System.out.println(srcMainHtmlDirectory.getAbsolutePath());
        Assert.assertTrue("The plugin should not blow up", t == null);
        for(String problem: problems){
            System.out.println("PROBLEM: " + problem);
        }
        Assert.assertEquals(0, problems.size());
        Assert.assertEquals(1, log.pathsRun.size());
        Assert.assertEquals("src/test/whatever/Whatever.qunit.js", log.pathsRun.get(0));
    }


    @Test
    public void findsTestCoffeescriptFilesUnderTheSrcTestDirectory() throws Exception {
        // given
        File projectDirectory = tempDirectory();
        File srcMainHtmlDirectory = new File(projectDirectory, "src/test/whatever");
        srcMainHtmlDirectory.mkdirs();

        FileUtils.writeStringToFile(new File(srcMainHtmlDirectory, "Whatever.qunit.coffee"), "test 'mytest', -> ok(true) \n");

        QunitMavenRunner runner = new QunitMavenRunner();
        FakeLog log = new FakeLog();

        // when
        List<String> problems;
        Exception t;
        try {
            problems = runner.run("", Collections.singletonList(projectDirectory), null, Collections.<File>emptyList(), "", log, 5000, new JavaUtilLog());
            t = null;
        } catch (Exception e) {
            t = e;
            problems = Collections.emptyList();
        }

        // then
        System.out.println(srcMainHtmlDirectory.getAbsolutePath());
        Assert.assertTrue("The plugin should not blow up", t == null);
        for(String problem : problems){
        	System.out.println("PROBLEM: " + problem);
        }
        Assert.assertEquals(0, problems.size());
        Assert.assertEquals(1, log.pathsRun.size());
        Assert.assertEquals("src/test/whatever/Whatever.qunit.coffee", log.pathsRun.get(0));
    }
    
    @Test
    public void findsQunitHtmlFilesUnderTheSrcTestDirectory() throws Exception {
        // given
        File projectDirectory = tempDirectory();
        File srcMainHtmlDirectory = new File(projectDirectory, "src/test/whatever");
        srcMainHtmlDirectory.mkdirs();
        
        for(String name : new String[]{"SomeQunitTest.html", "jquery-1.8.2.min.js", "qunit-1.11.0.css", "qunit-1.11.0.js"}){
            copyToDiskFromClasspath(srcMainHtmlDirectory, name);
        }
        
        QunitMavenRunner runner = new QunitMavenRunner();
        FakeLog log = new FakeLog();
        
        // when
        List<String> problems;
        Exception t;
        try {
            problems = runner.run("", Collections.singletonList(projectDirectory), null, Collections.<File>emptyList(), "", log, 5000, new JavaUtilLog());
            t = null;
        } catch (Exception e) {
            t = e;
            problems = Collections.emptyList();
        }
        
        // then
        System.out.println(srcMainHtmlDirectory.getAbsolutePath());
        Assert.assertTrue("The plugin should not blow up", t == null);
        Assert.assertEquals(0, problems.size());
        Assert.assertEquals(1, log.pathsRun.size());
        Assert.assertEquals("src/test/whatever/SomeQunitTest.html", log.pathsRun.get(0));
    }
    

    @Test
    public void theBuildFailsWhenATestFails() throws Exception {
        // given
        File projectDirectory = tempDirectory();
        File srcMainHtmlDirectory = new File(projectDirectory, "src/test/html");
        srcMainHtmlDirectory.mkdirs();
        
        for(String name : new String[]{"SomeFailingQunitTest.html", "jquery-1.8.2.min.js", "qunit-1.11.0.css", "qunit-1.11.0.js"}){
            copyToDiskFromClasspath(srcMainHtmlDirectory, name);
        }
        
        QunitMavenRunner runner = new QunitMavenRunner();
        FakeLog log = new FakeLog();
        
        // when
        List<String> problems;
        Throwable t;
        try {
            problems = runner.run("", Collections.singletonList(projectDirectory), null, Collections.<File>emptyList(), "", log, 5000, new JavaUtilLog());
            t = null;
        } catch (Throwable e) {
            t = e;
            problems = Collections.emptyList();
            t.printStackTrace();
        }
        
        // then
        Assert.assertNull("The build should not have failed", t);
        Assert.assertEquals(1, log.pathsRun.size());
        Assert.assertEquals("src/test/html/SomeFailingQunitTest.html", log.pathsRun.get(0));
        
        Assert.assertEquals(1, problems.size());
        String problem = problems.get(0);
        System.out.println("PROBLEM: " + problem);
        Assert.assertTrue(problem.startsWith("Problems found in 'src/test/html/SomeFailingQunitTest.html'"));
        Assert.assertTrue(problem.contains("module with failing test in it: this test left intentionally failing"));
        Assert.assertTrue(problem.contains("0 assertions of 1 passed, 1 failed"));
    }
    
    private static class FakeLog implements QunitMavenRunner.Listener {
        List<String> pathsRun = new ArrayList<String>();
        
        @Override
        public void runningTest(String relativePath) {
            pathsRun.add(relativePath);
        }
        
        @Override
        public void debug(String info) {
        }

        @Override
        public void warn(String info) {
        }

        @Override
        public void info(String info) {
        }
        
    }
    
    private void copyToDiskFromClasspath(File srcMainHtmlDirectory, String name)
            throws IOException {
        FileUtils.write(new File(srcMainHtmlDirectory, name), readClasspathResourceAsString("/" + name));
    }

    private String readClasspathResourceAsString(String name) throws IOException {
        return IOUtils.toString(getClass().getResourceAsStream(name));
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
