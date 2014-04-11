package com.cj.qunit.mojo.http;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.httpobjects.HttpObject;
import org.httpobjects.Request;
import org.httpobjects.Response;

import com.cj.qunit.mojo.LocatedTest;
import com.cj.qunit.mojo.Scanner;

class TestListingResource extends HttpObject {
    private final List<File> paths;
    private final String basePath;
    private final String webPathToRequireDotJsConfig;
    private final Scanner scanner;
    
    public TestListingResource(String pathPattern, String basePath, String webPathToRequireDotJsConfig, List<File> paths, Scanner scanner) {
        super(pathPattern);
        this.basePath = basePath;
        this.paths = paths;
        this.webPathToRequireDotJsConfig = webPathToRequireDotJsConfig;
        this.scanner = scanner;
    }
    
    @Override
    public Response get(Request req) {

        List<LocatedTest> allTestFiles = scanner.findTests();
        
        StringBuffer html = new StringBuffer("<html>" +
        		"<head><title>Qunit Tests</title><link rel=\"stylesheet\" href=\"/qunit-mojo/styles.css\" type=\"text/css\" media=\"screen\"/></head>" +
        		"<body><div class=\"all-files-link\">Qunit Tests</div>");
        
        Map<String, List<LocatedTest>> testsByParentDir = new TreeMap<String, List<LocatedTest>>();
        
        for(LocatedTest test : allTestFiles){
            final String parent = test.groupName();
            
            List<LocatedTest> tests = testsByParentDir.get(parent);
            if(tests==null){
                tests = new ArrayList<LocatedTest>();
                testsByParentDir.put(parent, tests);
            }
            
            tests.add(test);
        }
        
        for(Map.Entry<String, List<LocatedTest>> entry : testsByParentDir.entrySet()){
            html.append("<div class=\"test-directory\">" + entry.getKey() + "</div>");
            for(LocatedTest test : entry.getValue()){
                html.append("<div class=\"test-file\" >" +
                		        "<a href=\"" + test.relativePathToHtmlFile + "\">" + lastPathSegment(test.relativePathToDetectedFile) + "</a>" +
                		    "</div>");
            }
        }
        html.append("</body></html");
        
        return OK(Html(html.toString()));
    }


    private String lastPathSegment(final String impliedJavascriptFile) {
        final String[] parts = impliedJavascriptFile.split("/");
        
        final String fileName = parts.length == 0 ? impliedJavascriptFile : parts[parts.length-1];
        return fileName;
    }
    
}