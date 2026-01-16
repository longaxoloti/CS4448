package Dispatcher;

import Process.TCB;

public interface Scheduler {
    void addThread(TCB t);
    void onTick(int currentTime);
    TCB nextThread(int currentTime);
    TCB peek();
    boolean isPreemptive();
}