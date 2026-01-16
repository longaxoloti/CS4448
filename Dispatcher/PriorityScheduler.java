package Dispatcher;
import Process.TCB;
import java.util.*;

public class PriorityScheduler implements Scheduler {
    private final PriorityQueue<TCB> queue;
    private final boolean isPreemptive;

    public PriorityScheduler(boolean smallerIsHigher, boolean isPreemptive) {
        this.isPreemptive = isPreemptive;
        this.queue = new PriorityQueue<>((t1, t2) -> {
            int cmp = Integer.compare(t1.getPriority(), t2.getPriority());
            return smallerIsHigher ? cmp : -cmp;
        });
    }

    @Override
    public void addThread(TCB t) { queue.add(t); }

    @Override
    public TCB nextThread(int currentTime) { return queue.poll(); }

    @Override
    public TCB peek() { return queue.peek(); }

    @Override
    public void onTick(int currentTime) {}

    @Override
    public boolean isPreemptive() { return isPreemptive; }
}