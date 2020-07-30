package org.objectweb.asm;

public class MissingJarEntryException extends Exception {
    String msg;

    public MissingJarEntryException(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "MissingJarEntryException: " + msg;
    }
}
