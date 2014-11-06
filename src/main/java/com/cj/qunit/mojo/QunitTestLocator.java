package com.cj.qunit.mojo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class QunitTestLocator {
    public enum TestType {
        JAVASCRIPT(""),
        COFFEESCRIPT("cs"),
        HANDCRAFTEDHTML(""),
        JSX("jsx");
        private final String requirePlugin;

        TestType(String requirePlugin) {
            this.requirePlugin = requirePlugin;
        }
        public String getRequireJsPluginPrefix() {
            return requirePlugin.isEmpty() ?  "" : requirePlugin + "!" ;
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

    private String normalizedWebRoot(final String webRoot) {
        return webRoot.endsWith("/") ? webRoot.substring(0, webRoot.length()-1) : webRoot;
    }

    public List<LocatedTest> locateTests(final List<File> wheres, final String webRootRaw, final String requireBaseUrl){
        return locateTests(wheres, webRootRaw, requireBaseUrl, null);
    }

    public List<LocatedTest> locateTests(final List<File> wheres, final String webRootRaw, final String requireBaseUrl, final String filter){
        if(wheres==null) throw new NullPointerException();

        final String webRoot = normalizedWebRoot(webRootRaw);

        final List<LocatedTest> results = new ArrayList<LocatedTest>();

        class Foo{
        	final File where, path;

			public Foo(File where, File path) {
				super();
				this.where = where;
				this.path = path;
			}

        }

        final List<Foo> foos = new ArrayList<Foo>();

        long start = System.currentTimeMillis();
        new PathSet(wheres).scanFiles(new PathSet.Visitor() {
            @Override
            public void visit(File where, File path) {
            	foos.add(new Foo(where, path));
            }
        });

        System.out.println("took " + ((System.currentTimeMillis()-start)/1000));
        for(Foo foo : foos){
        	final File dir = foo.where;
        	final File path = foo.path;
        	final String name = path.getName();
            final String relativePath = stripLeadingSlash(path.getAbsolutePath().replaceAll(Pattern.quote(dir.getAbsolutePath()), ""));
            final String root = webRoot.equals("")?"":addTrailingSlashIfMissing(stripLeadingSlash(webRoot));
            try {
                if(name.matches(".*Qunit.*\\.html")){
                    LocatedTest locatedTest = new LocatedTest(relativePath, TestType.HANDCRAFTEDHTML, addTrailingSlashIfMissing(root) + relativePath, null, path);
                    if (locatedTest.matchesFilter(filter)) {
                        System.out.println("Found handcoded test " + dir + " ----> " + relativePath );
                        results.add(locatedTest);
                    }
                }else {
                    final TestType type;
                    final String extension;

                    if(name.endsWith(".qunit.js")){
                        extension = ".js";
                        type = TestType.JAVASCRIPT;
                    }else if(name.endsWith(".qunit.coffee")){
                        extension = ".coffee";
                        type = TestType.COFFEESCRIPT;
                    } else if(name.endsWith(".qunit.jsx")){
                        extension = ".jsx";
                        type = TestType.JSX;
                    }else {
                        type = null;
                        extension = null;
                    }
                    if(type!=null) {
                        final String requireJsName = requireJsName(requireBaseUrl, relativePath, root, extension);
                        LocatedTest locatedTest = new LocatedTest(relativePath, type, "qunit-mojo/" + relativePath, requireJsName, path);
                        if (locatedTest.matchesFilter(filter)) {
                            System.out.println("Found test " + dir + " ----> " + relativePath + "  (" + requireJsName + ")" );
                            results.add(locatedTest);
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return results;
    }

    private String requireJsName(final String requireBaseUrl, final String relativePath, final String root, final String suffix) {
//    	System.out.println("base url is " + requireBaseUrl);
        final String expectedPrefix;

        if(requireBaseUrl.isEmpty()){
        	expectedPrefix = "";
        }else if(requireBaseUrl.equals("/")){
        	expectedPrefix = "";
        }else{
        	expectedPrefix = addTrailingSlashIfMissing(stripLeadingSlash(requireBaseUrl));
        }
//        System.out.println("root is " + root);
        final String x = minusPrefix(expectedPrefix, root + minusSuffix(relativePath, suffix));
        return x;
    }

    private String minusPrefix(final String prefix, final String str) {
        if(!str.startsWith(prefix)) throw new RuntimeException(str + " doesn't start with " + prefix);
        return str.substring(prefix.length());
    }

    private String minusSuffix(final String str, final String suffix) {
        if(!str.endsWith(suffix)) throw new RuntimeException(str + " doesn't end with " + suffix);
        return str.substring(0, str.length() - suffix.length());
    }
    private String addTrailingSlashIfMissing(String webRoot) {
        return webRoot.endsWith("/") ? webRoot : webRoot + "/";
    }

    private String stripLeadingSlash(String webRoot) {
        return webRoot.startsWith("/") ? webRoot.substring(1) : webRoot;
    }
}
