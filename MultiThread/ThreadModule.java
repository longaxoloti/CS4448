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

    public ThreadModule(String algo, List<PCB> jobs, int rrQuota, long tickSleepMillis, boolean priorityPreemptive, boolean smallerIsHigher) {
        this.algo = algo;
        List<PCB> copy = new ArrayList<>();
        for (PCB p: jobs){
            copy.add(new PCB(p.getPID(), p.getPriority(), p.getBurstTime(), p.getArrivalTime()));
        }
        this.jobs = copy;
        this.rrQuota = rrQuota;
        this.tickSleepMillis = tickSleepMillis;
        this.priorityPreemptive = priorityPreemptive;
        this.smallerIsHigher = smallerIsHigher;
    }

    @Override
    public void run() {
        try {
            if ("RR".equalsIgnoreCase(algo)) {
                Scheduler rr = new RoundRobinScheduler(rrQuota);
                Dispatcher d = new Dispatcher(jobs, rr, tickSleepMillis);
                d.run_multi_thread();
            }
            else {
                Scheduler pr = new PriorityScheduler(smallerIsHigher, priorityPreemptive);
                Dispatcher d = new Dispatcher(jobs, pr, tickSleepMillis);
                d.run_multi_thread();
            }
        }
        catch (Exception e) {
            System.out.printf("Exception in module %s: %s\n", algo, e.toString());
            e.printStackTrace(System.out);
        }
    }
}
