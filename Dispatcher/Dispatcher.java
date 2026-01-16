package Dispatcher;
import Process.*;
import java.util.*;

public class Dispatcher {
    private final List<PCB> allJobs;
    private final Scheduler scheduler;

    private final List<PCB> blockedQueue = new ArrayList<>();
    private final List<PCB> suspendQueue = new ArrayList<>();

    private int currentTime = 0;
    private PCB running = null;
    private int rrRemaining = 0;
    private long tickSleepMillis;

    private final int totalMemory;
    private int usedMemory = 0;

    public Dispatcher(List<PCB> jobs, Scheduler scheduler){
        this(jobs, scheduler, 0, 999999);
    }

    public Dispatcher(List<PCB> jobs, Scheduler scheduler, long tickSleepMillis, int totalMemory){
        this.allJobs = new ArrayList<>(jobs);
        this.allJobs.sort(Comparator.comparingInt(PCB::getArrivalTime));
        this.scheduler = scheduler;
        this.tickSleepMillis = tickSleepMillis;
        this.totalMemory = totalMemory;
    }

    private void admitArrivals(){
        Iterator<PCB> it = allJobs.iterator();
        while (it.hasNext()){
            PCB p = it.next();
            if (p.getArrivalTime() <= currentTime){
                // Put to RAM
                if (usedMemory + p.getMemoryRequired() <= totalMemory) {
                    allocateMemory(p);
                    p.setState(State.READY);
                    scheduler.addProcess(p);
                    System.out.printf("Time %d: PID %d -> READY (RAM)\n", currentTime, p.getPID());
                }
                else {
                    // RAM full
                    p.setState(State.READY_SUSPENDED);
                    suspendQueue.add(p);
                    System.out.printf("Time %d: PID %d -> READY/SUSPENDED (RAM)\n", currentTime, p.getPID());
                }
                it.remove();
            }
            else break;
        }
    }

    private void processIO() {
        // BLOCKED
        Iterator<PCB> it = blockedQueue.iterator();
        while (it.hasNext()){
            PCB p = it.next();
            p.processCurrentInstruction(); // decrease I/O duration

            // If current I/O done, move to the next instr
            Instruction next = p.getCurrentInstruction();
            if (next == null || next.getType() == Instruction.Type.CPU) {
                p.setState(State.READY);
                scheduler.addProcess(p);
                it.remove();
                System.out.printf("Time %d: PID %d finished I/O -> READY\n", currentTime, p.getPID());
            }
        }

        // BLOCKED/SUSPENDED
        for (PCB p: suspendQueue) {
            if (p.getState() == State.BLOCKED_SUSPENDED){
                p.processCurrentInstruction();
                Instruction next = p.getCurrentInstruction();
                if (next == null || next.getType() == Instruction.Type.CPU){
                    p.setState(State.READY_SUSPENDED);
                    System.out.printf("Time %d: PID %d finished I/O (Disk) -> READY/SUSPENDED\n", currentTime, p.getPID());
                }
            }
        }
    }

    private void swapper() {
        // Find READY/SUSPENDED process
        PCB candidate = null;
        for (PCB p: suspendQueue) {
            if (p.getState() == State.READY_SUSPENDED) {
                candidate = p;
                break;
            }
        }

        if (candidate != null) {
            // RAM enough
            if (usedMemory + candidate.getMemoryRequired() <= totalMemory) {
                swapIn(candidate);
            }
            // RAM full
            else {
                PCB victim = findVictimToSwapOut(candidate.getMemoryRequired());
                if (victim != null) {
                    swapOut(victim);
                    // try swap in
                    if (usedMemory + candidate.getMemoryRequired() <= totalMemory) {
                        swapIn(candidate);
                    }
                }
            }
        }
    }

    private void swapIn(PCB p) {
        suspendQueue.remove(p);
        allocateMemory(p);
        p.setState(State.READY);
        scheduler.addProcess(p);
        System.out.printf("Time %d: SWAP IN PID %d\n", currentTime, p.getPID());
    }

    private void swapOut(PCB p) {
        if (p.getState() == State.BLOCKED) {
            blockedQueue.remove(p);
            p.setState(State.BLOCKED_SUSPENDED);
            freeMemory(p);
            suspendQueue.add(p);
            System.out.printf("Time %d: SWAP OUT PID %d (Victim)\n", currentTime, p.getPID());
        }
    }

    private PCB findVictimToSwapOut(int neededMem) {
        // find process with 'neededMem' memory in blocked queue
        for (PCB p: blockedQueue){
            if (p.getMemoryRequired() >= neededMem) return p;
        }
        if (!blockedQueue.isEmpty()) return blockedQueue.get(0);
        return null;
    }

    private void allocateMemory(PCB p){usedMemory += p.getMemoryRequired();}
    private void freeMemory(PCB p){usedMemory -= p.getMemoryRequired();}

    private void sleepIfNeeded(){
        if (tickSleepMillis > 0){
            try{
                Thread.sleep(tickSleepMillis);
            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }
    }

    public void run(){
        while(true) {
            admitArrivals();
            processIO();
            swapper();

            // if no process is running, pick one
            if (running == null) {
                running = scheduler.nextProcess(currentTime);
                if (running != null) {
                    running.setState(State.RUNNING);
                    running.setStartTime(currentTime);
                    System.out.printf("Time %d: DISPATCH %s\n", currentTime, running);
                    if (scheduler instanceof RoundRobinScheduler) {
                        rrRemaining = ((RoundRobinScheduler) scheduler).getQuota();
                    }
                }
            }

            if (running != null) {
                Instruction currentOp = running.getCurrentInstruction();

                // Running CPU instr
                if (currentOp != null && currentOp.getType() == Instruction.Type.CPU) {
                    running.processCurrentInstruction();
                    currentTime++;

                    scheduler.onTick(currentTime);

                    if (running.isFinished()) {
                        running.setState(State.TERMINATED);
                        running.setCompletionTime(currentTime);
                        freeMemory(running);

                        System.out.printf("Time %d: %s TERMINATED\n", currentTime, running);
                        running = null;
                    }
                    // Check I/O instr
                    else if (running.getCurrentInstruction().getType() == Instruction.Type.IO) {
                        running.setState(State.BLOCKED);
                        blockedQueue.add(running);

                        System.out.printf("Time %d: PID %d Request I/O -> BLOCKED\n", currentTime, running.getPID());
                        running = null;
                    }
                    // Check RR Quota
                    else if (scheduler instanceof RoundRobinScheduler) {
                        rrRemaining--;
                        if (rrRemaining <= 0) {
                            running.setState(State.READY);
                            ((RoundRobinScheduler) scheduler).addProcess(running);

                            System.out.printf("Time %d: RR Quota expired -> PID %d READY\n", currentTime, running.getPID());
                            running = null;
                        }
                    }
                    // Check priority
                    else if (scheduler.isPreemptive() && scheduler instanceof PriorityScheduler) {
                        PriorityScheduler ps = (PriorityScheduler) scheduler;
                        PCB best = ps.peek();
                        if (best != null) {
                            int runningPrio = running.getPriority();
                            int bestPrio = best.getPriority();
                            // smaller means higher priority
                            if (bestPrio < runningPrio) {
                                running.setState(State.READY);
                                scheduler.addProcess(running);
                                System.out.printf("Time %d: [PREEMPT] PID %d (Prio=%d) preempted by PID %d (Prio=%d)\n",
                                        currentTime, running.getPID(), runningPrio, best.getPID(), bestPrio);
                                running = null;
                            }
                        }
                    }
                }

                // Running I/O instr
                else if (currentOp != null && currentOp.getType() == Instruction.Type.IO) {
                    running.setState(State.BLOCKED);
                    blockedQueue.add(running);
                    running = null;
                }
            }
            else {
                // CPU idle
                if (allJobs.isEmpty() && running == null && blockedQueue.isEmpty()&& suspendQueue.isEmpty() && schedulerHasNoProcess()) {
                    break;
                }
                System.out.printf("Time %d: CPU idle\n", currentTime);
                currentTime++;
            }
            sleepIfNeeded();
        }
        System.out.printf("Simulation finished at t = %d\n", currentTime);
    }

    private boolean schedulerHasNoProcess() {
        return scheduler.nextProcess(currentTime) == null;
    }
}