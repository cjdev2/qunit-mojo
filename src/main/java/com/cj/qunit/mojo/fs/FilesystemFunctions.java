package com.cj.qunit.mojo.fs;

import java.io.File;
import java.io.IOException;

public class FilesystemFunctions {

    public static interface FileVisitor{
        void visit(File path);
    }
    public static void scanFiles(File path, FileVisitor visitor){
        if(path.isDirectory()){
            for(File next : path.listFiles()){
                scanFiles(next, visitor);
            }
        }else{
            visitor.visit(path);
        }
    }
    
    public static File tempDirectory(){
        try {
            File d = File.createTempFile("whatever", ".dir");
            if(!d.delete() || !d.mkdir()){
                throw new RuntimeException("Could not create temporary directory at " + d.getAbsolutePath());
            }
            
            return d;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
