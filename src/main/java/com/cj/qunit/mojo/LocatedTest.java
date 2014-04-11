package com.cj.qunit.mojo;

import java.io.File;
import java.util.regex.Pattern;

import com.cj.qunit.mojo.QunitTestLocator.TestType;

public class LocatedTest {
    public final String relativePathToDetectedFile;
    public final TestType type;
    public final String relativePathToHtmlFile;
    public final String requireJsModuleName;
    public final File pathOnDisk;
    
    public LocatedTest(String relativePathToDetectedFile, TestType type,
            String relativePathToHtmlFile, String requireJsModuleName,
            File pathOnDisk) {
        super();
        this.relativePathToDetectedFile = relativePathToDetectedFile;
        this.type = type;
        this.relativePathToHtmlFile = relativePathToHtmlFile;
        this.requireJsModuleName = requireJsModuleName;
        this.pathOnDisk = pathOnDisk;
        if(pathOnDisk==null) throw new NullPointerException();
    }
    
    public String groupName(){
    	if(type==TestType.HANDCRAFTEDHTML){
    		return parentPath(relativePathToDetectedFile);
    	}else{
    		return parentPath(requireJsModuleName);
    	}
    }
    

    private String parentPath(final String path) {
        int idx = path.lastIndexOf('/');
        return idx==-1?"":path.substring(0, idx);
    }

    public boolean matchesFilter(String filterRegex) {
        final boolean result;
        if(filterRegex==null){
            result = true;
        }else if(filterRegex.startsWith("regex:")){
            final String patternText = filterRegex.replaceFirst("regex:", "");
            result = Pattern.compile(patternText).matcher(this.relativePathToDetectedFile).matches();
        }else{
            result = this.relativePathToDetectedFile.contains(filterRegex);
        }
        return result;
    }

}