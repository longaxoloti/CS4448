import Dispatcher.*;
import Process.*;

import java.util.*;
public class Problem1 {
    public static void main(String[] args){
        List<PCB> jobs = Arrays.asList(
                new PCB(1, 2, 5, 0),
                new PCB(2, 1, 3, 1),
                new PCB(3, 3, 2, 2),
                new PCB(4, 2, 4, 3)
        );
        System.out.println("=== Round Robin (q = 2) ===");
        Scheduler rr = new RoundRobinScheduler(2);
        Dispatcher d1 = new Dispatcher(jobs, rr);
        d1.run_no_thread();

        System.out.println("\n=== Priority Preemptive (smaller = better) ===");
        List<PCB> jobs2 = Arrays.asList(
                new PCB(1, 2, 5, 0),
                new PCB(2, 1, 3, 1),
                new PCB(3, 3, 2, 2),
                new PCB(4, 2, 4, 3)
        );
        Scheduler pr = new PriorityScheduler(true, true);
        Dispatcher d2 = new Dispatcher(jobs2, pr);
        d2.run_no_thread();
    }
}
