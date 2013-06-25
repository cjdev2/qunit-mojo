package com.cj.qunit.mojo.http;

import java.io.File;
import java.util.List;

import org.httpobjects.HttpObject;
import org.httpobjects.Request;
import org.httpobjects.Response;

import com.cj.qunit.mojo.QunitTestLocator;

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

        StringBuffer html = new StringBuffer("<html><body><h1>Qunit Tests</h1>");
        
        for(File path: paths){
            for(QunitTestLocator.LocatedTest test: new QunitTestLocator().locateTests(path, basePath)){
                html.append("<div><a href=\"" + test.relativePath + "\">" + test.name + "</a></div>");
            }
        }
        
        html.append("</body></html");
        
        return OK(Html(html.toString()));
    }
}