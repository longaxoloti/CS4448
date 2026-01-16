import java.io.PrintStream;
import java.util.*;
import MultiThread.*;
import Dispatcher.*;
import Process.*;

public class MainConcurrent {
    public static void main(String[] args) throws Exception {
        // wrap System.out to shows thread name in each line
        PrintStream orig = System.out;
        System.setOut(new PrefixedPrintStream(orig));

        // Job A1: CPU(5), RAM(100)
        List<Instruction> insA1 = Arrays.asList(new Instruction(Instruction.Type.CPU, 5));

        // Job A2: CPU(2) -> IO(3) -> CPU(1), RAM(200)
        List<Instruction> insA2 = Arrays.asList(
                new Instruction(Instruction.Type.CPU, 2),
                new Instruction(Instruction.Type.IO, 3),
                new Instruction(Instruction.Type.CPU, 1)
        );

        // Job A3: CPU(3), RAM(150)
        List<Instruction> insA3 = Arrays.asList(new Instruction(Instruction.Type.CPU, 3));

        List<PCB> jobA = Arrays.asList(
                new PCB(1, 2, 100, 0, insA1),
                new PCB(2, 1, 200, 1, insA2),
                new PCB(3, 3, 150, 2, insA3)
        );

        List<PCB> jobB = new ArrayList<>();
        // Job B1: CPU(4), RAM 300
        jobB.add(new PCB(11, 1, 300, 0, Arrays.asList(new Instruction(Instruction.Type.CPU, 4))));
        // Job B2: CPU(2) -> IO(5) -> CPU(2), RAM 400
        jobB.add(new PCB(12, 2, 400, 2, Arrays.asList(
                new Instruction(Instruction.Type.CPU, 2),
                new Instruction(Instruction.Type.IO, 5),
                new Instruction(Instruction.Type.CPU, 2)
        )));

        // Create modules
        ThreadModule modRR = new ThreadModule("RR", jobA, 2, 20L, false, true, 500);
        ThreadModule modPR = new ThreadModule("Prio", jobB, 0, 20L, true, true, 500);

        Thread tRR = new Thread(modRR, "[RR]");
        Thread tPR = new Thread(modPR, "[PRIO]");

        tRR.start();
        tPR.start();

        tRR.join();
        tPR.join();

        System.out.println("Both simulations finished.");
    }
}
