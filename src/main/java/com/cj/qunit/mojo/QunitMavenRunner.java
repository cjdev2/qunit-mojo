package com.cj.qunit.mojo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.util.log.Logger;

import com.cj.qunit.mojo.http.WebServerUtils;
import com.cj.qunitTestDriver.QUnitTestPage;
import com.gargoylesoftware.htmlunit.BrowserVersion;

public class QunitMavenRunner {
    public enum Runner{
        HTMLUNIT{
            String runTest(
                    final WebServerUtils.JettyPlusPortPlusScanner jetty,
                    final LocatedTest test,
                    final String name,  int testTimeout,
                    final Listener listener,
                    boolean verbose,
                    boolean preserveTempFiles,
                    int retryCount) {

                QUnitTestPage page = new QUnitTestPage(jetty.port, test.relativePathToHtmlFile, testTimeout, BrowserVersion.FIREFOX_17, true);
                page.assertTestsPass();
                return null;
            }
        },
        PHANTOMJS{

            String runTest(
                    final WebServerUtils.JettyPlusPortPlusScanner jetty,
                    final LocatedTest test,
                    final String name,  int testTimeout,
                    final Listener listener,
                    boolean verbose,
                    boolean preserveTempFiles,
                    int retryCount) {

                try {
                    String message = null;
                    for (int run = 0; run <= retryCount; run++) {
                        File f = File.createTempFile("phantomjs-run-qunit", ".js");

                        if (!preserveTempFiles) { f.deleteOnExit(); }

                        FileUtils.write(f, IOUtils.toString(getClass().getResourceAsStream("/qunit-mojo/phantomjs-run-qunit.js")));

                        String baseUrl = "http://localhost:" + jetty.port;
                        String url = baseUrl + "/" + test.relativePathToHtmlFile;
                        String[] command = {
                                "phantomjs",
                                f.getAbsolutePath(),
                                url,
                                Integer.toString(testTimeout)};

                        Process phantomjs = new ProcessBuilder().redirectErrorStream(true).command(command).start();

                        String logMessage = "Executing " + mkString(command, " ");

                        if (verbose) { listener.info(logMessage); } else { listener.debug(logMessage); }

                        copyStreamAsyncOneByteAtATime(phantomjs.getInputStream(), System.out);
                        copyStreamAsyncOneByteAtATime(phantomjs.getErrorStream(), System.err);

                        final int exitCode = phantomjs.waitFor();

                        logMessage = "Exit code " + exitCode + " for " + name;

                        if (verbose) { listener.info(logMessage); } else { listener.debug(logMessage); }

                        if (exitCode == 0) {
                            return null;
                        }
                        if (exitCode < 128) {
                            return "Problems found in " + name;
                        }
                        // apparently when exitCode is >128 phantomjs crashed with error exitCode-128  (e.g. 139-128=11 or SIGSEGV)
                        message = "Problems found in " + name + " (Crash? " + exitCode + ")";
                    }
                    return message;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        },

        CHROME{
            
            String runTest(
                    final WebServerUtils.JettyPlusPortPlusScanner jetty,
                    final LocatedTest test,
                    final String name,  int testTimeout,
                    final Listener listener,
                    boolean verbose,
                    boolean preserveTempFiles,
                    int retryCount) {
                
                try {
                    String message = null;
                    for (int run = 0; run <= retryCount; run++) {
                        File f = File.createTempFile("chrome-run-qunit", ".js");

                        if (!preserveTempFiles) { f.deleteOnExit(); }

                        FileUtils.write(
                                f,
                                IOUtils.toString(getClass().getResourceAsStream("/qunit-mojo/chrome-run-qunit.js"))
                        );
                    
                        String baseUrl = "http://localhost:" + jetty.port;
                        String url = baseUrl + "/" + test.relativePathToHtmlFile;
                        String[] command = {
                                "node",
                                f.getAbsolutePath(),
                                url,
                                Integer.toString(testTimeout)};
                    
                        Process chrome = new ProcessBuilder().redirectErrorStream(true).command(command).start();

                        String logMessage = "Executing " + mkString(command, " ");

                        if (verbose) { listener.info(logMessage); } else { listener.debug(logMessage); }
                    
                        copyStreamAsyncOneByteAtATime(chrome.getInputStream(), System.out);
                        copyStreamAsyncOneByteAtATime(chrome.getErrorStream(), System.err);
                    
                        final int exitCode = chrome.waitFor();

                        logMessage = "Exit code " + exitCode + " for " + name;
                    
                        if (verbose) { listener.info(logMessage); } else { listener.debug(logMessage); }

                        if (exitCode == 0) {
                            return null;
                        }
                        if (exitCode < 128) {
                            return "Problems found in " + name;
                        }
                        // apparently when exitCode is >128 phantomjs crashed with error exitCode-128  (e.g. 139-128=11 or SIGSEGV)
                        message = "Problems found in " + name + " (Crash? " + exitCode + ")";
                    }
                    return message;
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
                final WebServerUtils.JettyPlusPortPlusScanner jetty,
                final LocatedTest test,
                final String name,  int testTimeout,
                final Listener listener,
                boolean verbose,
                boolean preserveTempFiles,
                int retryCount);
    }
    
    public static interface Listener {
        void warn(String info);
        void info(String info);
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
    final boolean verbose;
    final boolean preserveTempFiles;
    final int retryCount;

    public QunitMavenRunner() {
        this(1, Runner.HTMLUNIT, false, false, 0);
    }
    
    public QunitMavenRunner(int numThreads, Runner runner, boolean verbose, boolean preserveTempFiles, int retryCount) {
        super();
        this.numThreads = numThreads;
        this.runner = runner;
        this.verbose = verbose;
        this.preserveTempFiles = preserveTempFiles;
        this.retryCount = retryCount;
    }
        
    public static String requireConfigBaseUrl(String webPathToRequireDotJsConfig, int port){
        
        try {
        	
            if(webPathToRequireDotJsConfig == null || webPathToRequireDotJsConfig.isEmpty()) return "";
            
            final String code = IOUtils.toString(new URL("http://localhost:" + port + webPathToRequireDotJsConfig).openStream());
            Rhino rhino = new Rhino();
            rhino.eval(code);
            final String result = rhino.eval("require.baseUrl");
            final String baseUrl =  result == null ? "" : result;
            System.out.println("baseUrl is " + baseUrl);
            return baseUrl;
            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public List<String> run(final String webRoot, final List<File> codePaths, final String filter, final List<File> extraPathsToServe, final String webPathToRequireDotJsConfig, final Listener log, final int testTimeout, Logger jettyLog) {
        final String requireDotJsConfig;

        final String normalizedWebRoot = normalizedWebRoot(webRoot);

        if(webPathToRequireDotJsConfig!=null && webPathToRequireDotJsConfig.trim().equals("")){
            requireDotJsConfig = null;
        }else{
            requireDotJsConfig = webPathToRequireDotJsConfig;
        }

        validateJsConfigpath(normalizedWebRoot, codePaths, extraPathsToServe, requireDotJsConfig);

        final WebServerUtils.JettyPlusPortPlusScanner jetty = WebServerUtils.launchHttpServer(normalizedWebRoot, codePaths, extraPathsToServe, requireDotJsConfig, jettyLog, true, filter);

        try {

            final List<String> problems = new ArrayList<String>(); 

            final List<File> allPaths = new ArrayList<File>(codePaths);
            allPaths.addAll(extraPathsToServe);
            
            final List<LocatedTest> allTests = jetty.scanner.findTests();

            final List<LocatedTest> testsRemaining = prioritizeJsTests(allTests);

            if (verbose) {
                log.info(String.format("found %d test files", testsRemaining.size()));
            }

            log.info("Executing qunit tests on " + numThreads + " thread(s) using " + runner.toString().toLowerCase());
            
            runInParallel(numThreads, log, new Runnable(){
                public void run() {
                    while(true){
                        final LocatedTest test;

                        synchronized(testsRemaining){
                            if (verbose) { log.info(testsRemaining.size() + " tests remaining"); }

                            test = (testsRemaining.size() > 0) ? testsRemaining.remove(0) : null;
                        }

                        if(test==null){
                            break;
                        }

                        final String relativePathToDetectedFile = test.relativePathToDetectedFile;
                        log.runningTest(relativePathToDetectedFile);

                        String problem = null;
                        try {
                            problem = runner.runTest(jetty, test, relativePathToDetectedFile, testTimeout, log, verbose, preserveTempFiles, retryCount);
                        } catch (Throwable m){
                            problem = "Problems found in '" + relativePathToDetectedFile +"':\n"+m.getMessage();
                        }   

                        if(problem!=null){
                            synchronized(problems){
                                log.warn(problem);
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

    private List<LocatedTest> prioritizeJsTests(List<LocatedTest> tests) {
        final Map<String, LocatedTest> finalResults = new HashMap<String, LocatedTest>();
        //add all the non-js files
        for(LocatedTest test : tests) {
            if(test.type != QunitTestLocator.TestType.JAVASCRIPT && test.type != QunitTestLocator.TestType.HANDCRAFTEDHTML) {
                String withoutPlugin = test.requireJsModuleName.replace(test.type.getRequireJsPluginPrefix(), "");
                finalResults.put(withoutPlugin, test);
            }
        }
        //overwrite with js files
        for(LocatedTest test : tests) {
            if(test.type == QunitTestLocator.TestType.JAVASCRIPT) {
                finalResults.put(test.requireJsModuleName, test);
            }
        }

        ArrayList<LocatedTest> locatedTests = new ArrayList<LocatedTest>(finalResults.values());

        // hand crafted tests don't have a requireJsModuleName, so we can't put them in a map!
        for(LocatedTest test : tests) {
            if(test.type == QunitTestLocator.TestType.HANDCRAFTEDHTML) {
                locatedTests.add(test);
            }
        }

        return locatedTests;
    }


    private void runInParallel(int numThreads, Listener log, final Runnable runnable) {

        List<Thread> threads = new ArrayList<Thread>();

        for(int x=0;x<numThreads;x++){
            Thread t = new Thread("qunit-test-runner-" + x){
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
                if (verbose) {
                    log.info("joining on " + t.getName());
                }
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        
    }

    private String normalizedWebRoot(final String webRoot) {
        return webRoot.endsWith("/") ? webRoot.substring(0, webRoot.length()-1) : webRoot;
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

            StringBuilder text = new StringBuilder("You configured a require.js configuration path of \"" + webPathToRequireDotJsConfig + "\".  However, it doesn't seem to exist.  Here's where I looked for it:");
            for(File path : placesLooked){
                text.append("\n    ").append(path.getAbsolutePath()).append("\n");
            }
            throw new RuntimeException(text.toString());
        }
    }


}
