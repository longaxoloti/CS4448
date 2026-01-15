package Dispatcher;

import Process.PCB;

public interface Scheduler {
    void addProcess(PCB p);
    void onTick(int currentTime);
    PCB nextProcess(int currentTime);
    boolean isPreemptive();
}