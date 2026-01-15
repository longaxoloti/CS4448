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

        List<PCB> jobA = Arrays.asList(
                new PCB(1, 2, 5, 0),
                new PCB(2, 1, 3, 1),
                new PCB(3, 3, 2, 2)
        );

        List<PCB> jobB = Arrays.asList(
                new PCB(11, 1, 4, 0),
                new PCB(12, 2, 6, 2),
                new PCB(13, 1, 2, 3)
        );

        // Create modules
        ThreadModule modRR = new ThreadModule("RR", jobA, 2, 20L, false, true);
        ThreadModule modPR = new ThreadModule("Prio", jobB, 0, 20L, true, true);

        Thread tRR = new Thread(modRR, "[RR]");
        Thread tPR = new Thread(modPR, "[PRIO]");

        tRR.start();
        tPR.start();

        tRR.join();
        tPR.join();

        System.out.println("Both simulations finished.");
    }
}
