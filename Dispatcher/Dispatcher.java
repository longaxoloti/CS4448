package Dispatcher;
import Process.*;
import java.util.*;

public class Dispatcher {
    private final List<PCB> jobQueue;
    private final Scheduler scheduler;
    private int currentTime = 0;
    private PCB running = null;
    private int rrRemaining = 0;
    private long tickSleepMillis;

    public Dispatcher(List<PCB> jobs, Scheduler scheduler){
        this.jobQueue = new ArrayList<>(jobs);
        this.jobQueue.sort(Comparator.comparingInt(PCB::getArrivalTime));
        this.scheduler = scheduler;
    }

    public Dispatcher(List<PCB> jobs, Scheduler scheduler, long tickSleepMillis){
        this.jobQueue = new ArrayList<>(jobs);
        this.jobQueue.sort(Comparator.comparingInt(PCB::getArrivalTime));
        this.scheduler = scheduler;
        this.tickSleepMillis = tickSleepMillis;
    }

    private void admitArrivals(){
        Iterator<PCB> it = jobQueue.iterator();
        while (it.hasNext()){
            PCB p = it.next();
            if (p.getArrivalTime() <= currentTime){
                p.setState(State.READY);
                scheduler.addProcess(p);
                it.remove();
                System.out.printf("Current time = %d: ADMIT %s\n", currentTime, p);
            }
            else break;
        }
    }

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
        while(true){
            admitArrivals();
            // if no process is running, pick one
            if (running == null){
                running = scheduler.nextProcess(currentTime);
                if (running != null){
                    running.setState(State.RUNNING);
                    running.setStartTime(currentTime);
                    System.out.printf("Current time = %d: DISPATCH %s\n", currentTime, running);
                    // if RR, set rrRemaining
                    if (scheduler instanceof RoundRobinScheduler){
                        rrRemaining = ((RoundRobinScheduler) scheduler).getQuota();
                    }
                }
            }

            // Execute one tick
            if (running != null) {
                running.decRemaining();
                currentTime++;

                sleepIfNeeded();

                // allow scheduler to update internal state if needed
                scheduler.onTick(currentTime);
                admitArrivals();

                // check termination
                if (running.getRemainingTime() == 0){
                    running.setState(State.TERMINATED);
                    running.setCompletionTime(currentTime);
                    System.out.printf("Current time = %d: TERMINATED %s (turn around = %d, waiting = %d\n",
                            currentTime, running, running.getTurnaroundTime(), running.getWaitingTime());
                    running = null;
                }
                else{
                    // Check quota expiration
                    if (scheduler instanceof RoundRobinScheduler){
                        rrRemaining--;
                        if (rrRemaining <= 0){
                            running.setState(State.READY);
                            ((RoundRobinScheduler) scheduler).addProcess(running);
                            System.out.printf("Current time = %d: RR quota expired -> preempt %s\n", currentTime, running);
                            running = null;
                        }
                    }
                    // Check preemption
                    else if (scheduler.isPreemptive() && scheduler instanceof PriorityScheduler){
                        PriorityScheduler ps = (PriorityScheduler) scheduler;
                        PCB best = ps.peek();
                        if (best != null) {
                            int cmp = Integer.compare(best.getPriority(), running.getPriority());
                            if ((cmp < 0 && ps.isPreemptive()) || (cmp != 0 && ps.isPreemptive())){
                                //preempt
                                running.setState(State.READY);
                                ps.addProcess(running);
                                System.out.printf("Current time = %d: PREEMPT %s due to %s\n", currentTime, running, best);
                                running = null;
                            }
                        }
                    }
                }
            }
            else{
                // idle
                if (jobQueue.isEmpty() && (scheduler.nextProcess(currentTime) == null)){
                    // finished (jobQueue and ready are empty)
                    break;
                }
                System.out.printf("Current time = %d: CPU IDLE\n", currentTime);
                currentTime++;

                sleepIfNeeded();
            }
        }
        System.out.printf("Simulation finished at t = %d\n", currentTime);
    }
}