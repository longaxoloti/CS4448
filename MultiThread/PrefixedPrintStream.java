package MultiThread;

import java.io.*;
import java.util.*;

public class PrefixedPrintStream extends PrintStream {
    private final PrintStream delegate;

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

    private String prefix() {
        String t = Thread.currentThread().getName();
        return t == null ? "" : t + " ";
    }

    @Override
    public synchronized void println(String s) {
        delegate.println(prefix() + s);
    }

    public synchronized void println(Object o) {
        delegate.println(prefix() + String.valueOf(o));
    }

    public synchronized void print(String s){
        delegate.print(prefix() + s);
    }

    public synchronized PrintStream printf(String format, Object... args) {
        //prefix once, then formatted message
        String msg = String.format(format, args);
        delegate.println(prefix() + msg);
        return this;
    }

    public synchronized PrintStream format(String format, Object... args){
        return printf(format, args);
    }

    // fallbacks for other print/println overloads
    public void println(){delegate.println(prefix());}
    public void println(boolean x){println(String.valueOf(x));}
    public void println(char x){println(String.valueOf(x));}
    public void println(int x){println(String.valueOf(x));}
    public void println(long x){println(String.valueOf(x));}
    public void println(float x){println(String.valueOf(x));}
    public void println(double x){println(String.valueOf(x));}
    public void println(char[] x){println(new String(x));}
}
