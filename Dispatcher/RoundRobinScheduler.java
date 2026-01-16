package Dispatcher;
import Process.TCB;
import java.util.*;

public class RoundRobinScheduler implements Scheduler {
    private final Queue<TCB> queue = new LinkedList<>();
    private final int timeQuantum;

    public RoundRobinScheduler(int timeQuantum) {
        this.timeQuantum = timeQuantum;
    }

    @Override
    public void addThread(TCB t) {
        queue.add(t);
    }

    @Override
    public TCB nextThread(int currentTime) {
        return queue.poll();
    }

    @Override
    public TCB peek() { return queue.peek(); }

    @Override
    public void onTick(int currentTime) {}

    @Override
    public boolean isPreemptive() { return true; } // RR luôn preemptive theo time quantum

    public int getQuota() { return timeQuantum; }
}