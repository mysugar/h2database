/*
 * Copyright 2004-2008 H2 Group. Multiple-Licensed under the H2 License, 
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.store.fs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.HashMap;

import org.h2.message.Message;
import org.h2.util.IOUtils;
import org.h2.util.RandomUtils;

/**
 * This file system keeps files fully in memory.
 * There is an option to compress file blocks to safe memory.
 */
public class FileSystemMemory extends FileSystem {

    private static final FileSystemMemory INSTANCE = new FileSystemMemory();
    private static final HashMap MEMORY_FILES = new HashMap();

    private FileSystemMemory() {
        // don't allow construction
    }

    public static FileSystemMemory getInstance() {
        return INSTANCE;
    }

    public long length(String fileName) {
        return getMemoryFile(fileName).length();
    }

    public void rename(String oldName, String newName) {
        oldName = normalize(oldName);
        newName = normalize(newName);
        FileObjectMemory f = getMemoryFile(oldName);
        f.setName(newName);
        synchronized (MEMORY_FILES) {
            MEMORY_FILES.remove(oldName);
            MEMORY_FILES.put(newName, f);
        }
    }

    public boolean createNewFile(String fileName) {
        if (exists(fileName)) {
            return false;
        }
        // creates the file (not thread safe)
        getMemoryFile(fileName);
        return true;
    }

    public boolean exists(String fileName) {
        fileName = normalize(fileName);
        synchronized (MEMORY_FILES) {
            return MEMORY_FILES.get(fileName) != null;
        }
    }

    public void delete(String fileName) {
        fileName = normalize(fileName);
        synchronized (MEMORY_FILES) {
            MEMORY_FILES.remove(fileName);
        }
    }

    public boolean tryDelete(String fileName) {    
        fileName = normalize(fileName);
        synchronized (MEMORY_FILES) {
            MEMORY_FILES.remove(fileName);
        }
        return true;
    }

    public String createTempFile(String name, String suffix, boolean deleteOnExit, boolean inTempDir) {
        name += ".";
        for (int i = 0;; i++) {
            String n = name + (RandomUtils.getSecureLong() >>> 1) + suffix;
            if (!exists(n)) {
                // creates the file (not thread safe)
                getMemoryFile(n);
                return n;
            }
        }
    }

    public String[] listFiles(String path) {
        synchronized (MEMORY_FILES) {
            String[] list = new String[MEMORY_FILES.size()];
            FileObjectMemory[] l = new FileObjectMemory[MEMORY_FILES.size()];
            MEMORY_FILES.values().toArray(l);
            for (int i = 0; i < list.length; i++) {
                list[i] = l[i].getName();
            }
            return list;
        }
    }

    public void deleteRecursive(String fileName) throws SQLException {
        throw Message.getUnsupportedException();
    }

    public boolean isReadOnly(String fileName) {
        return false;
    }

    public String normalize(String fileName) {
        fileName = fileName.replace('\\', '/');
        int idx = fileName.indexOf(":/");
        if (idx > 0) {
            fileName = fileName.substring(0, idx + 1) + fileName.substring(idx + 2);
        }
        return fileName;
    }

    public String getParent(String fileName) {
        fileName = normalize(fileName);
        int idx = fileName.lastIndexOf('/');
        if (idx < 0) {
            idx = fileName.indexOf(':') + 1;
        }
        return fileName.substring(0, idx);
    }

    public boolean isDirectory(String fileName) {
        // TODO in memory file system currently doesn't support directories
        return false;
    }

    public boolean isAbsolute(String fileName) {
        // TODO relative files are not supported
        return true;
    }

    public String getAbsolutePath(String fileName) {
        // TODO relative files are not supported
        return normalize(fileName);
    }

    public long getLastModified(String fileName) {
        return getMemoryFile(fileName).getLastModified();
    }

    public boolean canWrite(String fileName) {
        return true;
    }

    public void copy(String original, String copy) throws SQLException {
        try {
            OutputStream out = openFileOutputStream(copy, false);
            InputStream in = openFileInputStream(original);
            IOUtils.copyAndClose(in, out);
        } catch (IOException e) {
            throw Message.convertIOException(e, "Can not copy " + original + " to " + copy);
        }
    }

    public void createDirs(String fileName) {
        // TODO directories are not really supported
    }

    public String getFileName(String name) {
        // TODO directories are not supported
        return name;
    }

    public boolean fileStartsWith(String fileName, String prefix) {
        fileName = normalize(fileName);
        prefix = normalize(prefix);
        return fileName.startsWith(prefix);
    }

    public OutputStream openFileOutputStream(String fileName, boolean append) throws SQLException {
        try {
            FileObjectMemory obj = getMemoryFile(fileName);
            obj.seek(0);            
            return new FileObjectOutputStream(obj, append);
        } catch (IOException e) {
            throw Message.convertIOException(e, fileName);
        }
    }

    public InputStream openFileInputStream(String fileName) {
        FileObjectMemory obj = getMemoryFile(fileName);
        obj.seek(0);
        return new FileObjectInputStream(obj);
    }

    public FileObject openFileObject(String fileName, String mode) {
        FileObjectMemory obj = getMemoryFile(fileName);
        obj.seek(0);
        return obj;
    }

    private FileObjectMemory getMemoryFile(String fileName) {
        fileName = normalize(fileName);
        synchronized (MEMORY_FILES) {
            FileObjectMemory m = (FileObjectMemory) MEMORY_FILES.get(fileName);
            if (m == null) {
                boolean compress = fileName.startsWith(FileSystem.PREFIX_MEMORY_LZF);
                m = new FileObjectMemory(fileName, compress);
                MEMORY_FILES.put(fileName, m);
            }
            return m;
        }
    }

}
