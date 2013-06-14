package com.cj.qunit.mojo;

import static com.cj.qunit.mojo.fs.FilesystemFunctions.scanFiles;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import com.cj.qunit.mojo.fs.FilesystemFunctions.FileVisitor;

public class QunitTestLocator {
    public static class LocatedTest {
        public final String name;
        public final String relativePath;

        private LocatedTest(String name, String relativePath) {
            super();
            this.name = name;
            this.relativePath = relativePath;
        }

    }

    public static List<File> findCodePaths(File basedir) {
        List<File> codePaths = new ArrayList<File>();
        File sourceDir = new File(basedir, "src");
        if(sourceDir.exists()){
	        for(File next : new File(basedir, "src").listFiles()){
	            if(next.isDirectory()){
	                codePaths.addAll(Arrays.asList(next.listFiles()));
	            }
	        }
		}
	
        return codePaths;
    }

    public List<LocatedTest> locateTests(final File where){
    	if(where==null) throw new NullPointerException();
    	
        final List<LocatedTest> results = new ArrayList<QunitTestLocator.LocatedTest>();

        scanFiles(where, new FileVisitor(){
            @Override
            public void visit(File path) {
                final String name = path.getName();
                final String s = path.getAbsolutePath().replaceAll(Pattern.quote(where.getAbsolutePath()), "");
                final String relativePath = s.startsWith("/")?s.substring(1):s;
                
                if(name.matches(".*Qunit.*\\.html")){
                    results.add(new LocatedTest(relativePath, relativePath));
                }else if(name.endsWith(".qunit.js")){
                    results.add(new LocatedTest(relativePath, relativePath + ".Qunit.html"));
                }
            }
        });

        return results;
    }
}
