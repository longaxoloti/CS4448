package Dispatcher;
import Process.PCB;
import java.util.*;

public class PriorityScheduler implements Scheduler{
    private final PriorityQueue<PCB> pq;
    private final boolean smallerIsHigher;
    private final boolean preemptive;

    public PriorityScheduler(boolean smallerIsHigher, boolean preemptive){
        this.smallerIsHigher = smallerIsHigher;
        this.preemptive = preemptive;
        pq = new PriorityQueue<>((a, b) -> {
            int cmp = Integer.compare(a.getPriority(), b.getPriority());
            return smallerIsHigher ? cmp : -cmp;
        });
    }

    @Override
    public void addProcess(PCB p) {
        pq.add(p);
    }

    public void onTick(int currentTime) {

    }

    public PCB nextProcess(int currentTime) {
        return pq.poll();
    }

    public boolean isPreemptive() { return preemptive;}

    // peek highest-priority
    public PCB peek() { return pq.peek(); }
}
