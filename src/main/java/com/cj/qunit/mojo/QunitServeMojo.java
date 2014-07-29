package com.cj.qunit.mojo;

import org.apache.maven.plugin.MojoFailureException;

import com.cj.qunit.mojo.http.WebServerUtils;
import com.cj.qunit.mojo.http.WebServerUtils.JettyPlusPortPlusScanner;
import com.cj.qunit.mojo.jetty.JettyMavenLogger;


/**
 * @phase test
 * @goal serve
 */
public class QunitServeMojo extends AbstractQunitMojo {
    
    /**
     * @parameter expression="${qunit.filter}"
     */
    public String filterPattern;

    public void execute() throws MojoFailureException {
        JettyPlusPortPlusScanner jetty = WebServerUtils.launchHttpServer(webRoot(), codePaths(), extraPathsToServe(), super.webPathToRequireDotJsConfig(),
                new JettyMavenLogger("foobar", getLog()), false, filterPattern);
        
        getLog().info("Server started: visit http://localhost:" + jetty.port + " to run your tests.");
        Object o = new Object();
        try {
            synchronized(o){
                o.wait();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}

