import Dispatcher.*;
import Process.*;
import java.util.*;

public class Problem1 {
    public static void main(String[] args) {
        System.out.println("=== SINGLE THREAD OS SIMULATION (Threads + Memory + IO) ===");

        // === Process 1 (400MB) ===
        PCB p1 = new PCB(1, 400, 0);
        // Thread 1.1: Render UI
        List<Instruction> t1Ins = Arrays.asList(new Instruction(Instruction.Type.CPU, 4));
        p1.addThread(new TCB(101, p1, 2, t1Ins));
        // Thread 1.2: Download File (IO heavy)
        List<Instruction> t2Ins = Arrays.asList(
                new Instruction(Instruction.Type.CPU, 1),
                new Instruction(Instruction.Type.IO, 5),
                new Instruction(Instruction.Type.CPU, 1)
        );
        p1.addThread(new TCB(102, p1, 1, t2Ins));

        // === Process 2 (300MB) ===
        PCB p2 = new PCB(2, 300, 2);

        List<Instruction> t3Ins = Arrays.asList(new Instruction(Instruction.Type.CPU, 6));
        p2.addThread(new TCB(201, p2, 1, t3Ins));

        List<PCB> jobs = new ArrayList<>();
        jobs.add(p1);
        jobs.add(p2);

        // Total 1000MB RAM in Main Memory
        Scheduler rr = new RoundRobinScheduler(3);
        Dispatcher dispatcher = new Dispatcher(jobs, rr, 0, 1000);
        dispatcher.run();
    }
}