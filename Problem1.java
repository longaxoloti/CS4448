import Dispatcher.*;
import Process.*;

import java.util.*;
public class Problem1 {
    public static void main(String[] args){

        System.out.println("=== Round Robin (Time Quantum = 2, RAM = 500MB) ===");

        List<Instruction> ins1 = Arrays.asList(new Instruction(Instruction.Type.CPU, 4));
        List<Instruction> ins2 = Arrays.asList(
                new Instruction(Instruction.Type.CPU, 2),
                new Instruction(Instruction.Type.IO, 5),
                new Instruction(Instruction.Type.CPU, 2)
        );
        List<Instruction> ins3 = Arrays.asList(new Instruction(Instruction.Type.CPU, 5));

        List<PCB> jobs = Arrays.asList(
                new PCB(1, 2, 100, 0, ins1),
                new PCB(2, 1, 250, 1, ins2),
                new PCB(3, 3, 200, 2, ins3)
        );
        Scheduler rr = new RoundRobinScheduler(2);
        Dispatcher d1 = new Dispatcher(jobs, rr, 0, 500);
        d1.run();

        System.out.println("\n=== Priority Preemptive (smaller = better, RAM = 1000MB) ===");

        List<Instruction> ins4 = Arrays.asList(new Instruction(Instruction.Type.CPU, 10));
        List<Instruction> ins5 = Arrays.asList(new Instruction(Instruction.Type.CPU, 3));
        List<PCB> jobs2 = Arrays.asList(
                new PCB(4, 2, 100, 0, ins4),
                new PCB(5, 1, 100, 2, ins5)
        );
        Scheduler pr = new PriorityScheduler(true, true);
        Dispatcher d2 = new Dispatcher(jobs2, pr, 0, 1000);
        d2.run();
    }
}
