package com.cj.qunit.mojo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.cj.qunit.mojo.fs.FilesystemFunctions;
import com.cj.qunit.mojo.fs.FilesystemFunctions.FileVisitor;

public class PathSet {
    private final List<File> paths;

    public PathSet(List<File> paths) {
        super();
        this.paths = paths;
    }
    
    private static class FileFound {
        private final File root;
        private final File file;
        
        public FileFound(File root, File file) {
            super();
            this.root = root;
            this.file = file;
        }
    }
    
    public static interface Visitor {
        void visit(File root, File child);
    }
    
    public void scanFiles(Visitor visitor){
        List<FileFound> locations = new ArrayList<FileFound>();
        for(File path : paths){
            System.out.println("Scanning path: " + path.getAbsolutePath());
            collectDirs(path, path, locations);
        }
        
        Set<File> files = new TreeSet<File>(new Comparator<File>() {
        	@Override
        	public int compare(File o1, File o2) {
        		return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
        	}
		});
        
        for(FileFound f : locations){
            files.add(f.file);
        }
        
        for(File f : files){
            List<FileFound> matches = new ArrayList<FileFound>();
            for(FileFound next : locations){
                if(next.file.getAbsolutePath().equals(f.getAbsolutePath())){
                    matches.add(next);
                }
            }
            
            Collections.sort(matches, new Comparator<FileFound>() {
                @Override
                public int compare(FileFound o1, FileFound o2) {
                    return o2.root.compareTo(o1.root);
                }
            });
            
            FileFound mostSpecific = matches.get(0);
            visitor.visit(mostSpecific.root, mostSpecific.file);
        }
    }
    
    private void collectDirs(File root, File d, List<FileFound> locations){
        if(d.isDirectory()){
            for(File child : d.listFiles()){
                if(child.isDirectory()){
                    collectDirs(root, child, locations);
                }else if(child.isFile()){
                    locations.add(new FileFound(root, child));
                }
            }
        }
    }
    
}
