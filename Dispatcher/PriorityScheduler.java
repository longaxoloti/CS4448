package Dispatcher;
import Process.TCB;
import java.util.*;

public class PriorityScheduler implements Scheduler {
    private final PriorityQueue<TCB> queue;
    private final boolean isPreemptive;
    private int tickCounter = 0;
    private final int AGING_INTERVAL = 5;

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
    public void onTick(int currentTime) {
        tickCounter++;
        if (tickCounter >= AGING_INTERVAL) {
            tickCounter = 0;
            List<TCB> tempList = new ArrayList<>();
            while (!queue.isEmpty()) {
                tempList.add(queue.poll());
            }

            for (TCB t: tempList) {
                t.increasePriority(1);
            }
            queue.addAll(tempList);
        }
    }

    @Override
    public boolean isPreemptive() { return isPreemptive; }
}