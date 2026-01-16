package MultiThread;
import java.util.*;
import Process.*;
import Dispatcher.*;

public class ThreadModule implements Runnable{
    private final String algo;
    private final List<PCB> jobs;
    private final int rrQuota;
    private final long tickSleepMillis;
    private final boolean priorityPreemptive;
    private final boolean smallerIsHigher;
    private final int totalMemory;

    public ThreadModule(String algo, List<PCB> jobs, int rrQuota, long tickSleepMillis, boolean priorityPreemptive, boolean smallerIsHigher, int totalMemory) {
        this.algo = algo;
        this.rrQuota = rrQuota;
        this.tickSleepMillis = tickSleepMillis;
        this.priorityPreemptive = priorityPreemptive;
        this.smallerIsHigher = smallerIsHigher;
        this.totalMemory = totalMemory;
        List<PCB> copy = new ArrayList<>();
        for (PCB p: jobs){
            copy.add(new PCB(p.getPID(), p.getPriority(), p.getMemoryRequired(), p.getArrivalTime(), p.getInstructions()));
        }
        this.jobs = copy;
    }

    @Override
    public void run() {
        try {
            if ("RR".equalsIgnoreCase(algo)) {
                Scheduler rr = new RoundRobinScheduler(rrQuota);
                Dispatcher d = new Dispatcher(jobs, rr, tickSleepMillis, totalMemory);
                d.run();
            }
            else {
                Scheduler pr = new PriorityScheduler(smallerIsHigher, priorityPreemptive);
                Dispatcher d = new Dispatcher(jobs, pr, tickSleepMillis, totalMemory);
                d.run();
            }
        }
        catch (Exception e) {
            System.out.printf("Exception in module %s: %s\n", algo, e.toString());
            e.printStackTrace(System.out);
        }
    }
}
