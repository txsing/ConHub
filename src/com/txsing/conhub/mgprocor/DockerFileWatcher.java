package com.txsing.conhub.mgprocor;

import com.txsing.conhub.ult.Constants;
import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;
import java.nio.file.attribute.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;

/**
 * Watch Docker directories for changes to files.
 */
public class DockerFileWatcher extends Thread {

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
        //System.out.printf("LOG(INFO): DIR \"%s\" MONITORED\n", dir);
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
                if (type.equals("image") && !child.toString().contains("tmp")) {
                    //System.out.println("LOG(INFO): IMAGE SYNC TRIGGERED");
                    /* docker pull a new image of which the image id is new, then
                    signal is set to false, cos the repo of that image will be
                    synced along with the image itself.
                    However, if docker pull a new image of which the image id is
                    already existing (the content is the same, the image is new
                    in terms of "name") in this case, the singal is set to true */
                    ReadWriteLock rwl = new ReentrantReadWriteLock();
                    Lock writeLock = rwl.writeLock();
                    writeLock.lock();
                    try {
                        Synchro.getInstance().SIGNAL_SYNC_REPO = false;
                    } finally {
                        writeLock.unlock();
                    }

                    try {
                        Synchro.syncImamge(child.toString()
                                .substring(Constants.DOCKER_PATH_IMAGE.length()),
                                event.kind().name());
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                        System.err.println("LOG(ERROR): Failed to sync image");
                    }

                } else if (type.equals("repo")
                        && child.toString().endsWith("repositories.json")) {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    if (Synchro.getInstance().SIGNAL_SYNC_REPO == true) {
                        System.out.println("LOG(INFO): REPO SYNC TRIGGERED");
                        try {
                            Synchro.getInstance().syncRepo();
                        } catch (Exception e) {
                            System.err.println(e.getMessage());
                            System.err.println("LOG(ERROR): Failed to sync whole repo");
                        }

                    } else {
                        if(!Synchro.getInstance().SIGNAL_SYNC_REPO_FST){
                            //System.out.println("LOG(DEBUG): No repo sync: step 1");
                            Synchro.getInstance().SIGNAL_SYNC_REPO_FST = true;
                        }else{
                            if(!Synchro.getInstance().SIGNAL_SYNC_REPO_SEC){
                                //System.out.println("LOG(DEBUG): No repo sync: step 2");
                                Synchro.getInstance().SIGNAL_SYNC_REPO_SEC = true;
                            }                                
                        }
                    }
                    if(Synchro.getInstance().SIGNAL_SYNC_REPO_FST 
                            && Synchro.getInstance().SIGNAL_SYNC_REPO_SEC){
                        //System.out.println("LOG(DEBUG): No repo sync: step 3");
                         Synchro.getInstance().SIGNAL_SYNC_REPO = true;
                         Synchro.getInstance().SIGNAL_SYNC_REPO_FST = false;
                         Synchro.getInstance().SIGNAL_SYNC_REPO_SEC = false;
                    }
                       
                } else if (type.equals("container")
                        && !event.kind().name().equals("ENTRY_MODIFY")) {
                    System.out.println("LOG(INFO): CONTAINER SYNC TRIGGERED");
                    try {
                        Synchro.syncContainer(child.toString()
                                .substring(Constants.DOCKER_PATH_CONTAINER.length()),
                                event.kind().name());
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                        //e.printStackTrace();
                        System.err.println("LOG(ERROR): Failed to sync container");
                    }

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

    public String getType() {
        return type;
    }

    @Override
    public void run() {
        processEvents();
    }
}
