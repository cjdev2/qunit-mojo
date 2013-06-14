package com.cj.qunit.mojo.fs;

import java.io.File;

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
}
