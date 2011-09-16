/*
 * Copyright 2004-2008 H2 Group. Licensed under the H2 License, Version 1.0 (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.test.unit;

import java.sql.SQLException;
import java.util.Random;
import org.h2.store.DataHandler;
import org.h2.store.FileStore;
import org.h2.test.TestBase;
import org.h2.util.FileUtils;
import org.h2.value.Value;

/**
 * Tests the in-memory file system.
 */
public class TestFile extends TestBase implements DataHandler {

    public void test() throws Exception {
        doTest(false);
        doTest(true);
    }

    private void doTest(boolean compress) throws Exception {
        byte[] magic = new byte[0];
        int len = getSize(1000, 10000);
        Random random = new Random();
        FileStore mem = null, file = null;
        byte[] buffMem = null;
        byte[] buffFile = null;
        String prefix = compress ? "memLZF:" : "memFS:";
        FileUtils.delete(prefix + "test");
        FileUtils.delete("~/test");

        // config.traceTest = true;

        for (int i = 0; i < len; i++) {
            if (buffMem == null) {
                int l = 1 + random.nextInt(1000);
                buffMem = new byte[l];
                buffFile = new byte[l];
            }
            if (file == null) {
                mem = FileStore.open(this, prefix + "test", "rw", magic);
                file = FileStore.open(this, "~/test", "rw", magic);
            }
            check(file.getFilePointer(), mem.getFilePointer());
            check(file.length(), mem.length());
            int x = random.nextInt(100);
            if ((x -= 20) < 0) {
                if (file.length() > 0) {
                    long pos = random.nextInt((int) (file.length() / 16)) * 16;
                    trace("seek " + pos);
                    mem.seek(pos);
                    file.seek(pos);
                }
            } else if ((x -= 20) < 0) {
                trace("close");
                mem.close();
                file.close();
                mem = null;
                file = null;
            } else if ((x -= 20) < 0) {
                if (buffFile.length > 16) {
                    random.nextBytes(buffFile);
                    System.arraycopy(buffFile, 0, buffMem, 0, buffFile.length);
                    int off = random.nextInt(buffFile.length - 16);
                    int l = random.nextInt((buffFile.length - off) / 16) * 16;
                    trace("write " + off + " " + l);
                    mem.write(buffMem, off, l);
                    file.write(buffFile, off, l);
                }
            } else if ((x -= 20) < 0) {
                if (buffFile.length > 16) {
                    int off = random.nextInt(buffFile.length - 16);
                    int l = random.nextInt((buffFile.length - off) / 16) * 16;
                    l = (int) Math.min(l, file.length() - file.getFilePointer());
                    trace("read " + off + " " + l);
                    Exception a = null, b = null;
                    try {
                        file.readFully(buffFile, off, l);
                    } catch (Exception e) {
                        a = e;
                    }
                    try {
                        mem.readFully(buffMem, off, l);
                    } catch (Exception e) {
                        b = e;
                    }
                    if (a != b) {
                        if (a == null || b == null) {
                            error("only one threw an exception");
                        }
                    }
                    check(buffMem, buffFile);
                }
            } else if ((x -= 10) < 0) {
                trace("reset buffers");
                buffMem = null;
                buffFile = null;
            } else {
                int l = random.nextInt(10000) * 16;
                long p = file.getFilePointer();
                file.setLength(l);
                mem.setLength(l);
                trace("setLength " + l);
                if (p > l) {
                    file.seek(l);
                    mem.seek(l);
                }
            }
        }
        if (mem != null) {
            mem.close();
            file.close();
        }
        FileUtils.delete("inmemory:test");
        FileUtils.delete("~/test");
    }

    public int allocateObjectId(boolean needFresh, boolean dataFile) {
        return 0;
    }

    public void checkPowerOff() throws SQLException {
    }

    public void checkWritingAllowed() throws SQLException {
    }

    public int compareTypeSave(Value a, Value b) throws SQLException {
        return 0;
    }

    public String createTempFile() throws SQLException {
        return null;
    }

    public void freeUpDiskSpace() throws SQLException {
    }

    public int getChecksum(byte[] data, int start, int end) {
        return 0;
    }

    public String getDatabasePath() {
        return null;
    }

    public String getLobCompressionAlgorithm(int type) {
        return null;
    }

    public Object getLobSyncObject() {
        return null;
    }

    public int getMaxLengthInplaceLob() {
        return 0;
    }

    public boolean getTextStorage() {
        return false;
    }

    public void handleInvalidChecksum() throws SQLException {
    }

    public FileStore openFile(String name, String mode, boolean mustExist) throws SQLException {
        return null;
    }

}