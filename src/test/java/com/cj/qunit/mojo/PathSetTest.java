package com.cj.qunit.mojo;

import static com.cj.qunit.mojo.fs.FilesystemFunctions.tempDirectory;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class PathSetTest {
	javax.servlet.Filter foo;
	
    @Test
    public void moreSpecificPathsWin() throws Exception {
        // given
        File lessSpecific = tempDirectory();
        File moreSpecific = new File(lessSpecific, "foo");
        moreSpecific.mkdir();
        
        File file = new File(lessSpecific, "foo/file.txt");
        file.createNewFile();
        PathSet paths =  new PathSet(Arrays.asList(lessSpecific, moreSpecific));
        
        // when
        final List<File> rootsScanned = new ArrayList<File>();
        paths.scanFiles(new PathSet.Visitor() {
            
            @Override
            public void visit(File dir, File path) {
                rootsScanned.add(dir);
            }
        });
        
        // then
        assertEquals(1, rootsScanned.size());
        assertEquals(moreSpecific.getAbsolutePath(), rootsScanned.get(0).getAbsolutePath());
        
    }

}
