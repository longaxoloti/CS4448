package MultiThread;

import java.io.*;
import java.util.*;

public class PrefixedPrintStream extends PrintStream {
    private final PrintStream delegate;

    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";

    public PrefixedPrintStream(PrintStream delegate){
        super(new OutputStream(){
            @Override
            public void write(int b) throws IOException {
                delegate.write(b);
            }

            public void write(byte[] b, int off, int len) throws IOException {
                delegate.write(b, off, len);
            }
        }, true);
        this.delegate = delegate;
    }

    private String getColor(String threadName) {
        if (threadName.contains("RR")) return CYAN;
        if (threadName.contains("PRIO")) return YELLOW;
        return RESET;
    }

    private String prefix() {
        String t = Thread.currentThread().getName();
        String color = getColor(t);
        return (t == null ? "" : color + t + " ");
    }

    @Override
    public synchronized void println(String s) {
        delegate.println(prefix() + s + RESET);
    }

    public synchronized void println(Object o) {
        delegate.println(prefix() + String.valueOf(o) + RESET);
    }

    public synchronized void print(String s){
        delegate.print(prefix() + s + RESET);
    }

    public synchronized PrintStream printf(String format, Object... args) {
        //prefix once, then formatted message
        String msg = String.format(format, args);
        delegate.println(prefix() + msg + RESET);
        return this;
    }

    public synchronized PrintStream format(String format, Object... args){
        return printf(format, args);
    }

    // fallbacks for other print/println overloads
    public void println(){delegate.println(prefix() + RESET);}
    public void println(boolean x){println(String.valueOf(x));}
    public void println(char x){println(String.valueOf(x));}
    public void println(int x){println(String.valueOf(x));}
    public void println(long x){println(String.valueOf(x));}
    public void println(float x){println(String.valueOf(x));}
    public void println(double x){println(String.valueOf(x));}
    public void println(char[] x){println(new String(x));}
}
