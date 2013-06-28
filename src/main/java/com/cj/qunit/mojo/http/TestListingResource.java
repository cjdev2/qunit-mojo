package com.cj.qunit.mojo.http;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.httpobjects.HttpObject;
import org.httpobjects.Request;
import org.httpobjects.Response;

import com.cj.qunit.mojo.QunitTestLocator;
import com.cj.qunit.mojo.QunitTestLocator.LocatedTest;;

class TestListingResource extends HttpObject {
    private final List<File> paths;
    private final String basePath;
    
    public TestListingResource(String pathPattern, String basePath, List<File> paths) {
        super(pathPattern);
        this.basePath = basePath;
        this.paths = paths;
    }
    
    @Override
    public Response get(Request req) {

        List<LocatedTest> allTestFiles = findFiles();
        Collections.sort(allTestFiles, new Comparator<LocatedTest>() {
            @Override
            public int compare(LocatedTest a, LocatedTest b) {
                return a.relativePath.compareTo(b.relativePath);
            }
        });
        
        StringBuffer html = new StringBuffer("<html><body><h1>Qunit Tests</h1>");
        for(LocatedTest test : allTestFiles){
            html.append("<div><a href=\"" + test.relativePath + "\">" + test.name + "</a></div>");
        }
        html.append("</body></html");
        
        return OK(Html(html.toString()));
    }

    private List<LocatedTest> findFiles() {
        List<LocatedTest> allTestFiles = new ArrayList<LocatedTest>();
        
        for(File path: paths){
            allTestFiles.addAll(new QunitTestLocator().locateTests(path, basePath));
        }
        return allTestFiles;
    }
}