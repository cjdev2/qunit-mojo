package com.cj.qunit.mojo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;

abstract class AbstractQunitMojo extends AbstractMojo {

    /**
     * @parameter default-value="${basedir}
     * @readonly
     * @required
     */
    private File basedir;

    /**
     * @parameter
     */
    private String webRoot = "";
    
    /**
     * @parameter
     */
    private String webPathToRequireDotJsConfig = null;

    /**
     * @parameter default-value=5000
     */
    private int timeout;
    
    /**
     * @parameter 
     */
    private List<String> extraPathsToServe = new ArrayList<String>();

    /**
     * @parameter 
     */
    private final List<String> dirsToExclude = new ArrayList<String>();
    
    protected String webPathToRequireDotJsConfig() {
		return webPathToRequireDotJsConfig;
	}

    protected int returnTimeout(){
	return timeout;
    }
    
    protected List<File> codePaths(){
        return QunitTestLocator.findCodePaths(basedir);
    }

    protected List<String> dirsToExclude(){
        return this.dirsToExclude;
    }
    
    public String webRoot() {
        return webRoot;
    }

    protected List<File> extraPathsToServe(){ 
        List<File> extraPathsToServe = new ArrayList<File>();

        for(String path : this.extraPathsToServe){
            extraPathsToServe.add(new File(basedir, path));
        }
        System.out.println("The extra paths are " + extraPathsToServe);
        System.out.println("dirsToExclude: " + this.dirsToExclude);
        return extraPathsToServe;
    }
}
