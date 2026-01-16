package Dispatcher;
import Process.*;
import java.util.*;

public class Dispatcher {
    private final List<PCB> allProcesses;
    private final Scheduler scheduler;
    private final int totalMemory;
    private int usedMemory = 0;
    private final long tickSleepMillis;

    private TCB runningThread = null;
    private int currentTime = 0;
    private int rrRemaining = 0;

    private final List<TCB> blockedThreads = new ArrayList<>();
    private final List<PCB> suspendedProcesses = new ArrayList<>();

    public Dispatcher(List<PCB> processes, Scheduler scheduler, long tickSleepMillis, int totalMemory) {
        this.allProcesses = new ArrayList<>(processes);
        this.allProcesses.sort(Comparator.comparingInt(PCB::getArrivalTime));
        this.scheduler = scheduler;
        this.tickSleepMillis = tickSleepMillis;
        this.totalMemory = totalMemory;
    }

    private void checkArrivals() {
        Iterator<PCB> it = allProcesses.iterator();
        while (it.hasNext()) {
            PCB p = it.next();
            if (p.getArrivalTime() <= currentTime) {
                if (usedMemory + p.getMemoryRequired() <= totalMemory) {
                    // RAM enough
                    allocateMemory(p);
                    p.setInRAM(true);
                    for (TCB t : p.getThreads()) {
                        t.setState(State.READY);
                        scheduler.addThread(t);
                    }
                    System.out.printf("Time %d: [NEW PROC] PID %d loaded to RAM. %d threads ready.\n",
                            currentTime, p.getPID(), p.getThreads().size());
                } else {
                    // RAM full
                    p.setInRAM(false);
                    suspendedProcesses.add(p);
                    System.out.printf("Time %d: [NEW PROC] PID %d -> Suspended (Disk) - Not enough RAM.\n", currentTime, p.getPID());
                }
                it.remove();
            } else break;
        }
    }

    // I/O Handling
    private void processIO() {
        Iterator<TCB> it = blockedThreads.iterator();
        while (it.hasNext()) {
            TCB t = it.next();
            t.processCurrentInstruction();

            Instruction next = t.getCurrentInstruction();
            if (next == null || next.getType() == Instruction.Type.CPU) {
                t.setState(State.READY);
                if (t.getParentPCB().isInRAM()) {
                    scheduler.addThread(t);
                    System.out.printf("Time %d: [IO-DONE] TID %d (P%d) -> READY\n", currentTime, t.getTID(), t.getParentPCB().getPID());
                } else {
                    System.out.printf("Time %d: [IO-DONE] TID %d finished I/O but P%d is Suspended.\n", currentTime, t.getTID(), t.getParentPCB().getPID());
                }
                it.remove();
            }
        }
    }

    // Swapper (Memory Manager)
    private void swapper() {
        // Swap In
        if (!suspendedProcesses.isEmpty()) {
            PCB candidate = suspendedProcesses.get(0); // FIFO
            if (usedMemory + candidate.getMemoryRequired() <= totalMemory) {
                suspendedProcesses.remove(0);
                allocateMemory(candidate);
                candidate.setInRAM(true);
                for (TCB t : candidate.getThreads()) {
                    // Check state
                    if (t.getState() != State.TERMINATED) {
                        t.setState(State.READY);
                        scheduler.addThread(t);
                    }
                }
                System.out.printf("Time %d: [SWAP-IN] PID %d moved from Disk to RAM.\n", currentTime, candidate.getPID());
            }
        }
    }

    private void allocateMemory(PCB p) { usedMemory += p.getMemoryRequired(); }
    private void freeMemory(PCB p) { usedMemory -= p.getMemoryRequired(); }

    // --- MAIN RUN LOOP ---
    public void run() {
        TCB lastRunning = null;

        while (true) {
            checkArrivals();
            processIO();
            swapper();

            if (runningThread == null) {
                runningThread = scheduler.nextThread(currentTime);

                if (runningThread != null) {
                    // == CONTEXT SWITCH SIMULATION ==
                    if (lastRunning != null && lastRunning.getParentPCB() != runningThread.getParentPCB()) {
                        System.out.printf("Time %d: [CTX-SWITCH] Process Switch (P%d -> P%d) - Heavy Overhead.\n",
                                currentTime, lastRunning.getParentPCB().getPID(), runningThread.getParentPCB().getPID());
                        currentTime++;
                    } else if (lastRunning != null && lastRunning != runningThread) {
                        System.out.printf("Time %d: [CTX-SWITCH] Thread Switch (TID %d -> TID %d) - Light Overhead.\n",
                                currentTime, lastRunning.getTID(), runningThread.getTID());
                    }

                    runningThread.setState(State.RUNNING);
                    if (scheduler instanceof RoundRobinScheduler) {
                        rrRemaining = ((RoundRobinScheduler) scheduler).getQuota();
                    }
                    System.out.printf("Time %d: [DISPATCH] TID %d (P%d) Running.\n", currentTime, runningThread.getTID(), runningThread.getParentPCB().getPID());
                }
            }

            // Execution
            if (runningThread != null) {
                lastRunning = runningThread;
                Instruction op = runningThread.getCurrentInstruction();

                if (op != null && op.getType() == Instruction.Type.CPU) {
                    runningThread.processCurrentInstruction();
                    currentTime++;

                    // Thread Finished
                    if (runningThread.isFinished()) {
                        runningThread.setState(State.TERMINATED);
                        System.out.printf("Time %d: [TERM] TID %d Finished.\n", currentTime, runningThread.getTID());

                        checkProcessTermination(runningThread.getParentPCB());
                        runningThread = null;
                    }
                    // I/O Request
                    else if (runningThread.getCurrentInstruction().getType() == Instruction.Type.IO) {
                        runningThread.setState(State.BLOCKED);
                        blockedThreads.add(runningThread);
                        System.out.printf("Time %d: [IO-REQ] TID %d -> BLOCKED\n", currentTime, runningThread.getTID());
                        runningThread = null;
                    }
                    // Round Robin Timeout
                    else if (scheduler instanceof RoundRobinScheduler) {
                        rrRemaining--;
                        if (rrRemaining <= 0) {
                            runningThread.setState(State.READY);
                            scheduler.addThread(runningThread);
                            System.out.printf("Time %d: [RR-TIME] TID %d -> READY\n", currentTime, runningThread.getTID());
                            runningThread = null;
                        }
                    }
                    // Preemption (Priority)
                    else if (scheduler.isPreemptive() && scheduler instanceof PriorityScheduler) {
                        TCB best = scheduler.peek();
                        if (best != null && best.getPriority() < runningThread.getPriority()) {
                            runningThread.setState(State.READY);
                            scheduler.addThread(runningThread);
                            System.out.printf("Time %d: [PREEMPT] TID %d preempted by TID %d\n", currentTime, runningThread.getTID(), best.getTID());
                            runningThread = null;
                        }
                    }
                }
            } else {
                // CPU IDLE
                currentTime++;
                if (allProcesses.isEmpty() && suspendedProcesses.isEmpty() && blockedThreads.isEmpty() && schedulerHasNoThread()) {
                    break;
                }
            }

            if (tickSleepMillis > 0) {
                try { Thread.sleep(tickSleepMillis); } catch (Exception e) {}
            }
        }
        System.out.println("Simulation finished at time " + currentTime);
    }

    private void checkProcessTermination(PCB p) {
        boolean allDone = true;
        for (TCB t : p.getThreads()) {
            if (t.getState() != State.TERMINATED) {
                allDone = false;
                break;
            }
        }
        if (allDone) {
            System.out.printf("Time %d: [PROC-TERM] All threads of PID %d done. Freeing %dMB RAM.\n",
                    currentTime, p.getPID(), p.getMemoryRequired());
            freeMemory(p);
        }
    }

    private boolean schedulerHasNoThread() { return scheduler.nextThread(currentTime) == null; }
}