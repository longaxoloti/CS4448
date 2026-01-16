import java.io.PrintStream;
import java.util.*;
import MultiThread.*;
import Process.*;

public class MainConcurrent {
    public static void main(String[] args) throws Exception {
        PrintStream orig = System.out;
        System.setOut(new PrefixedPrintStream(orig));

        // Process A
        PCB pA = new PCB(1, 600, 0);
        pA.addThread(new TCB(10, pA, 1, Arrays.asList(new Instruction(Instruction.Type.CPU, 5))));
        pA.addThread(new TCB(11, pA, 2, Arrays.asList(
                new Instruction(Instruction.Type.CPU, 2),
                new Instruction(Instruction.Type.IO, 3),
                new Instruction(Instruction.Type.CPU, 2)
        )));

        // Process B
        PCB pB = new PCB(2, 200, 1);
        pB.addThread(new TCB(20, pB, 1, Arrays.asList(new Instruction(Instruction.Type.CPU, 4))));

        // Process C
        PCB pC = new PCB(3, 300, 3);
        pC.addThread(new TCB(30, pC, 3, Arrays.asList(new Instruction(Instruction.Type.CPU, 3))));

        List<PCB> dataset = new ArrayList<>();
        dataset.add(pA);
        dataset.add(pB);
        dataset.add(pC);

        // Module 1: RR, RAM 1000MB
        ThreadModule modRR = new ThreadModule("RR", dataset, 2, 50L, false, true, 1000);

        // Module 2: Priority, RAM 700MB
        ThreadModule modPrio = new ThreadModule("PRIO", dataset, 0, 50L, true, true, 700);

        Thread t1 = new Thread(modRR, "[RR-SYS]");
        Thread t2 = new Thread(modPrio, "[PRIO-SYS]");

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("All simulations finished.");
    }
}