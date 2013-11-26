package com.cj.qunit.mojo;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoFailureException;

import com.cj.qunit.mojo.QunitMavenRunner.Runner;
import com.cj.qunit.mojo.jetty.JettyMavenLogger;

/**
 * @phase test
 * @goal test
 */
public class QunitMavenRunnerMojo extends AbstractQunitMojo {
    
    /**
     * @parameter expression="${qunit.numThreads}"
     */
    public Integer numThreads = 1;
    
    /**
     * @parameter expression="${qunit.runner}" default-value=PHANTOMJS
     */
    public String runner;
    
    /**
     * @parameter expression="${qunit.filter}"
     */
    public String filterPattern;
    
    /**
     * @parameter expression="${qunit.verbose}" default-value=false
     */
    public Boolean verbose;

    /**
     * @parameter expression="${qunit.preserveTempFiles}" default-value=false
     */
    public Boolean preserveTempFiles;

    /**
     * @parameter expression="${qunit.retryCount}"
     */
    public Integer retryCount = 0;

    public void execute() throws MojoFailureException {
        if(shouldSkipTests()) return;
        
        getLog();
        
        final List<String> filesRun = new ArrayList<String>();
        final QunitMavenRunner.Listener listener = new QunitMavenRunner.Listener() {

            String maybePrependThreadName(String s) {
                return verbose ? "[" + Thread.currentThread().getName() + "] " + s : s;
            }

            @Override
            public synchronized void runningTest(String relativePath) {
                String message = "Running: " + relativePath;
                getLog().info(maybePrependThreadName(message));
                filesRun.add(relativePath);
            }
            @Override
            public synchronized void debug(String info) {
                getLog().debug(maybePrependThreadName(info));
            }

            @Override
            public synchronized void warn(String info) {
                getLog().warn(maybePrependThreadName(info));
            }

            @Override
            public synchronized void info(String info) {
                getLog().info(maybePrependThreadName(info));
            }
        };

        
        final Runner runner = Runner.valueOf(this.runner.toUpperCase());
        
        final List<String> problems = new QunitMavenRunner(numThreads, runner, verbose, preserveTempFiles, retryCount).run(
                                            webRoot(), 
                                            codePaths(), 
                                            filterPattern,
                                            extraPathsToServe(), 
                                            webPathToRequireDotJsConfig(), 
                                            listener, 
                                            returnTimeout(),
                                            new JettyMavenLogger("foobar", getLog()));
        
        if(!problems.isEmpty()){
            StringBuilder problemsString = new StringBuilder();
            
            for(String next : problems){
                problemsString.append(next);
                problemsString.append('\n');
            }

            throw new MojoFailureException(problemsString.toString());
        }else{
            getLog().info("Ran qunit on " + filesRun.size() + " files");
        }
    }

    private boolean shouldSkipTests() {
        boolean skipTests = false;
        
        final String[] skipFlags = {"maven.test.skip", "skipTests", "qunit.skip"};
        
        for(String skipFlag : skipFlags){
            String value = System.getProperty(skipFlag);
            if(value!=null && !value.trim().toLowerCase().equals("false")){
                getLog().warn("###########################################################################");
                getLog().warn("## Skipping Qunit tests because the \"" + skipFlag + "\" property is set.");
                getLog().warn("###########################################################################");
                skipTests = true;
                break;
            }
        }
        return skipTests;
    }
}

