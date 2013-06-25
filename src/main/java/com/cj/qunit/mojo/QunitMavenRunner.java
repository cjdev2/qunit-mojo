package com.cj.qunit.mojo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.cj.qunit.mojo.QunitTestLocator.LocatedTest;
import com.cj.qunit.mojo.http.WebServerUtils;
import com.cj.qunitTestDriver.QUnitTestPage;
import com.gargoylesoftware.htmlunit.BrowserVersion;

public class QunitMavenRunner {
    public enum Runner{
        HTMLUNIT{
            String runTest(
                    final WebServerUtils.JettyPlusPort jetty,
                    final LocatedTest test,
                    final String name,  int testTimeout,
                    final Listener listener) {

                QUnitTestPage page = new QUnitTestPage(jetty.port, test.relativePath, testTimeout, BrowserVersion.FIREFOX_17, true);
                page.assertTestsPass();
                return null;
            }
        }, 
        PHANTOMJS{
            
            String runTest(
                    final WebServerUtils.JettyPlusPort jetty,
                    final LocatedTest test,
                    final String name,  int testTimeout,
                    final Listener listener) {
                
                try {
                    File f = File.createTempFile("phantomjs-run-qunit", ".js");
                    f.deleteOnExit();
                    FileUtils.write(f, IOUtils.toString(getClass().getResourceAsStream("/qunit-mojo/phantomjs-run-qunit.js")));
                    
                    String baseUrl = "http://localhost:" + jetty.port;
                    String url = baseUrl + "/" + test.relativePath;
                    String[] command = {
                            "phantomjs",
                            f.getAbsolutePath(),
                            url,
                            Integer.toString(testTimeout)};
                    
                    Process phantomjs = new ProcessBuilder().redirectErrorStream(true).command(command).start();
                    
                    listener.debug("Executing " + mkString(command, " "));
                    
                    copyStreamAsyncOneByteAtATime(phantomjs.getInputStream(), System.out);
                    copyStreamAsyncOneByteAtATime(phantomjs.getErrorStream(), System.err);
                    
                    final int exitCode = phantomjs.waitFor();
                    
                    listener.debug("Exit code " + exitCode);

                    if (exitCode!=0){
                        return "Problems found in '" + name;
                    }
                    
                    return null;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        
        private static <T> String mkString(T[] items, String separator){
            StringBuilder text = new StringBuilder();
            for(T next : items){
                if(text.length()>0){
                    text.append(separator);
                }
                text.append(next);
            }
            return text.toString();
        }
        
        private static void copyStreamAsyncOneByteAtATime(final InputStream in, final OutputStream out){
            new Thread(){
                public void run() {
                    try {
                        for(int b = in.read();b!=-1;b = in.read()){
                            out.write(b);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    
                };
            }.start();
            
        }
        
        abstract String runTest(
                final WebServerUtils.JettyPlusPort jetty,
                final LocatedTest test,
                final String name,  int testTimeout,
                final Listener listener);
        }
    
    public static interface Listener {
        void initInfo(String info);
        void runningTest(String relativePath);
        void debug(String info);
    }

    private static <T> List<T> concat(List<T> ... lists){
        final List<T> result = new ArrayList<T>();

        for(List<T> list : lists){
            result.addAll(list);
        }

        return result;
    }
    
    final int numThreads;
    final Runner runner;
    
    public QunitMavenRunner() {
        this(1, Runner.HTMLUNIT);
    }
    
    public QunitMavenRunner(int numThreads, Runner runner) {
        super();
        this.numThreads = numThreads;
        this.runner = runner;
    }
    
    public boolean matches(final LocatedTest test, final String filterRegex) {
        final boolean result;
        if(filterRegex==null){
            result = true;
        }else if(filterRegex.startsWith("regex:")){
            final String patternText = filterRegex.replaceFirst("regex:", "");
            result = Pattern.compile(patternText).matcher(test.name).matches();
        }else{
            result = test.name.contains(filterRegex);
        }
        return result;
    }
    
    public List<String> run(final String webRoot, final List<File> codePaths, final List<File> extraPathsToServe, final String webPathToRequireDotJsConfig, final Listener log, final int testTimeout) {
        return this.run(webRoot, codePaths, null, extraPathsToServe, webPathToRequireDotJsConfig, log, testTimeout);
    }
    
    public List<String> run(final String webRoot, final List<File> codePaths, final String filter, final List<File> extraPathsToServe, final String webPathToRequireDotJsConfig, final Listener log, final int testTimeout) {
        final String requireDotJsConfig;

        final String normalizedWebRoot = normalizedWebRoot(webRoot);

        if(webPathToRequireDotJsConfig!=null && webPathToRequireDotJsConfig.trim().equals("")){
            requireDotJsConfig = null;
        }else{
            requireDotJsConfig = webPathToRequireDotJsConfig;
        }


        validateJsConfigpath(normalizedWebRoot, codePaths, extraPathsToServe, requireDotJsConfig);

        final WebServerUtils.JettyPlusPort jetty = WebServerUtils.launchHttpServer(normalizedWebRoot, codePaths, extraPathsToServe, requireDotJsConfig);

        try{

            final List<String> problems = new ArrayList<String>(); 
            
            
            final List<LocatedTest> allTests = new ArrayList<LocatedTest>();
            
            for(File codePath : codePaths){
                for(QunitTestLocator.LocatedTest test: new QunitTestLocator().locateTests(codePath, normalizedWebRoot)){
                     if(matches(test, filter)){
                         allTests.add(test);
                     }
                }
            };
            
            final List<LocatedTest> testsRemaining = new ArrayList<LocatedTest>(allTests);

            log.initInfo("Executing qunit tests on " + numThreads + " thread(s) using " + runner.toString().toLowerCase());
            
            runInParallel(numThreads, new Runnable(){
                public void run() {
                    while(true){
                        final LocatedTest test;
                        synchronized(testsRemaining){
                            if(testsRemaining.size()>0){
                                test = testsRemaining.get(0);
                                testsRemaining.remove(0);
                            }else{
                                test = null;
                            }
                        }

                        if(test==null){
                            break;
                        }

                        final String name = test.name;
                        log.runningTest(name);

                        String problem = null;
                        try {
                            problem = runner.runTest(jetty, test, name, testTimeout, log);
                        } catch (Throwable m){
                            problem = "Problems found in '" + name +"':\n"+m.getMessage();
                        }   

                        if(problem!=null){
                            synchronized(problems){
                                problems.add(problem);
                            }
                        }
                    }
                }
                
            });
            
            return problems;
        }finally{
            try {
                jetty.server.stop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    
    private void runInParallel(int numThreads, final Runnable runnable) {

        List<Thread> threads = new ArrayList<Thread>();

        for(int x=0;x<numThreads;x++){

            Thread t = new Thread(){
                @Override
                public void run() {
                    runnable.run();
                }

            };

            threads.add(t);
            t.start();

        }
        
        for(Thread t : threads){
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
    }

    private String normalizedWebRoot(final String webRoot) {
        final String normalizedWebRoot = webRoot.endsWith("/") ? webRoot.substring(0, webRoot.length()-1) : webRoot;
        return normalizedWebRoot;
    }

    @SuppressWarnings("unchecked")
    private void validateJsConfigpath(final String webRoot, final List<File> codePaths,
            final List<File> extraPathsToServe,
            final String webPathToRequireDotJsConfig) {

        if(webPathToRequireDotJsConfig==null) return;

        boolean found = false;

        final String relativeFilesystemPathToRequireDotJsConfig = webPathToRequireDotJsConfig.replaceFirst(Pattern.quote(webRoot), "");

        List<File> placesLooked = new ArrayList<File>();
        for(File codeDir : concat(codePaths, extraPathsToServe)){
            final File config = new File(codeDir, relativeFilesystemPathToRequireDotJsConfig);
            placesLooked.add(config);
            if(config.exists()){
                found = true; 
            }
        }

        if(!found){

            StringBuffer text = new StringBuffer("You configured a require.js configuration path of \"" + webPathToRequireDotJsConfig + "\".  However, it doesn't seem to exist.  Here's where I looked for it:");
            for(File path : placesLooked){
                text.append("\n    " + path.getAbsolutePath() + "\n");
            }
            throw new RuntimeException(text.toString());
        }
    }


}
