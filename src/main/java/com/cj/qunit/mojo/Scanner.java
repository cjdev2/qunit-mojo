package com.cj.qunit.mojo;

import java.io.File;
import java.util.List;

public class Scanner {
	private final int port;
	private final String webPathToRequireDotJsConfig;
	private final List<File> codePaths;
	private final String webRoot;
	private final boolean cacheResults;
	private final String filterPattern;
	private final List<String> dirsToExclude;

	public Scanner(int port, String webPathToRequireDotJsConfig,
			List<File> codePaths, String webRoot, boolean cacheResults, String filterPattern, List<String> dirsToExclude) {
		super();
		this.port = port;
		this.webPathToRequireDotJsConfig = webPathToRequireDotJsConfig;
		this.codePaths = codePaths;
		this.webRoot = webRoot;
		this.cacheResults = cacheResults;
		this.filterPattern = filterPattern;
		this.dirsToExclude = dirsToExclude;
	}

	private List<LocatedTest> latestResults;

	public List<LocatedTest> findTests(){
		if(!cacheResults || latestResults==null){
			synchronized(this){
				final String requireConfigBaseUrl = QunitMavenRunner.requireConfigBaseUrl(webPathToRequireDotJsConfig, port);
				System.out.println("requireConfigBaseUrl is " + requireConfigBaseUrl);
				latestResults = new QunitTestLocator().locateTests(codePaths, webRoot, requireConfigBaseUrl, filterPattern, dirsToExclude);
			}
			
		}
		return latestResults;
    }
}
