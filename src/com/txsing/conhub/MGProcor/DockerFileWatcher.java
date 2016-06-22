package com.txsing.conhub.MGProcor;

import com.txsing.conhub.ult.Constants;
import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;
import java.nio.file.attribute.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Watch Docker directories for changes to files.
 */
public class DockerFileWatcher extends Thread{

    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private final boolean recursive;
    private boolean trace = false;
    private String type;
    private String dir;

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    /**
     * 
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.format("register: %s\n", dir);
            } else if (!dir.equals(prev)) {
                System.out.format("update: %s -> %s\n", prev, dir);
            }
        }
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Creates a WatchService and registers the given directory
     */
    DockerFileWatcher(String dir, String type, boolean recursive) throws IOException {
        this.dir = dir;
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey, Path>();
        this.recursive = recursive;
        this.type = type;
        if (recursive) {
            System.out.format("Scanning %s ...\n", dir);
            registerAll(Paths.get(dir));
            System.out.println("Done.");
        } else {
            register(Paths.get(dir));
        }

        // enable trace after initial registration
        this.trace = true;
    }

    /**
     * Process all events for keys queued to the watcher
     */
    void processEvents() {
        Logger logger = Logger.getLogger("com.txsing.conhub.mgprocor");
        logger.log(Level.INFO, "DIR: {0} MONITORED SUCCESSFULLY", dir);
        
        for (;;) {
            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                // TBD - provide example of how OVERFLOW event is handled
                if (kind == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);

                //event detected
                //System.out.format("%s: %s\n", event.kind().name(), child);
                if(type.equals("image") && !child.toString().endsWith("tmp") ){
                    logger.log(Level.INFO, "IMAGE SYNC TRIGGERED");
                    
                    Synchro.syncImamge(child.toString()
                            .substring(Constants.DOCKER_PATH_IMAGE.length()),
                            event.kind().name());
                    
                    Synchro.syncRepo();
                }
                
                if(type.equals("container") 
                        && !event.kind().name().equals("ENTRY_MODIFY")){
                    logger.log(Level.INFO, "CONTAINER SYNC TRIGGERED");
                    Synchro.syncContainer(child.toString()
                            .substring(Constants.DOCKER_PATH_CONTAINER.length()),
                            event.kind().name());
                }
                
                // if directory is created, and watching recursively, then
                // register it and its sub-directories
                if (recursive && (kind == ENTRY_CREATE)) {
                    try {
                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                            registerAll(child);
                        }
                    } catch (IOException x) {
                        // ignore to keep sample readbale
                    }
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }
    
    public String getType(){
        return type;
    }

    @Override
    public void run(){
        processEvents();
    }
}
