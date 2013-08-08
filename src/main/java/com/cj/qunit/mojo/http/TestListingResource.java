package com.cj.qunit.mojo.http;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
        
        StringBuffer html = new StringBuffer("<html>" +
        		"<head><title>Qunit Tests</title><link rel=\"stylesheet\" href=\"/qunit-mojo/styles.css\" type=\"text/css\" media=\"screen\"/></head>" +
        		"<body><div class=\"all-files-link\">Qunit Tests</div>");
        
        Map<String, List<LocatedTest>> testsByParentDir = new TreeMap<String, List<LocatedTest>>();
        
        for(LocatedTest test : allTestFiles){
            final String parent = parentPath(test.relativePath);
            List<LocatedTest> tests = testsByParentDir.get(parent);
            if(tests==null){
                tests = new ArrayList<QunitTestLocator.LocatedTest>();
                testsByParentDir.put(parent, tests);
            }
            
            tests.add(test);
        }
        
        for(Map.Entry<String, List<LocatedTest>> entry : testsByParentDir.entrySet()){
            html.append("<div class=\"test-directory\">" + entry.getKey() + "</div>");
            for(LocatedTest test : entry.getValue()){
                html.append("<div class=\"test-file\" ><a href=\"" + test.relativePath + "\">" + lastPathSegment(test.name) + "</a></div>");
            }
        }
        html.append("</body></html");
        
        return OK(Html(html.toString()));
    }

    private String parentPath(final String path) {
        int idx = path.lastIndexOf('/');
        return idx==-1?path:path.substring(0, idx);
    }

    private String lastPathSegment(final String impliedJavascriptFile) {
        final String[] parts = impliedJavascriptFile.split("/");
        
        final String fileName = parts.length == 0 ? impliedJavascriptFile : parts[parts.length-1];
        return fileName;
    }
    
    private List<LocatedTest> findFiles() {
        List<LocatedTest> allTestFiles = new ArrayList<LocatedTest>();
        
        for(File path: paths){
            allTestFiles.addAll(new QunitTestLocator().locateTests(path, basePath));
        }
        return allTestFiles;
    }
}