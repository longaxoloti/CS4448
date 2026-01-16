package MultiThread;
import java.util.*;
import Process.*;
import Dispatcher.*;

public class ThreadModule implements Runnable {
    private final String algo;
    private final List<PCB> jobs; // Bản sao riêng
    private final int rrQuota;
    private final long tickSleepMillis;
    private final boolean priorityPreemptive;
    private final boolean smallerIsHigher;
    private final int totalMemory;

    public ThreadModule(String algo, List<PCB> originalJobs, int rrQuota, long tickSleepMillis,
                        boolean priorityPreemptive, boolean smallerIsHigher, int totalMemory) {
        this.algo = algo;
        this.rrQuota = rrQuota;
        this.tickSleepMillis = tickSleepMillis;
        this.priorityPreemptive = priorityPreemptive;
        this.smallerIsHigher = smallerIsHigher;
        this.totalMemory = totalMemory;

        // Deep copy PCB & TCB
        this.jobs = new ArrayList<>();
        for (PCB original : originalJobs) {
            PCB pCopy = new PCB(original.getPID(), original.getMemoryRequired(), original.getArrivalTime());
            for (TCB tOriginal : original.getThreads()) {
                TCB tCopy = new TCB(tOriginal, pCopy);
                pCopy.addThread(tCopy);
            }
            this.jobs.add(pCopy);
        }
    }

    @Override
    public void run() {
        try {
            if ("RR".equalsIgnoreCase(algo)) {
                Scheduler rr = new RoundRobinScheduler(rrQuota);
                Dispatcher d = new Dispatcher(jobs, rr, tickSleepMillis, totalMemory);
                d.run();
            } else {
                Scheduler pr = new PriorityScheduler(smallerIsHigher, priorityPreemptive);
                Dispatcher d = new Dispatcher(jobs, pr, tickSleepMillis, totalMemory);
                d.run();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}