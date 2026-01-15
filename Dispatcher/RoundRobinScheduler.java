package Dispatcher;
import Process.PCB;
import java.util.*;

public class RoundRobinScheduler implements Scheduler{
    private final Deque<PCB> q = new ArrayDeque<>();
    private final int quota;
    private int currentSliceRemaining = 0; //maintained by dispatcher

    public RoundRobinScheduler(int quota){
        if (quota <= 0) throw new IllegalArgumentException("quota>0");
        this.quota = quota;
    }

    public int getQuota(){return quota;}

    @Override
    public void addProcess(PCB p){
        q.addLast(p);
    }

    public void onTick(int currentTime){

    }

    public PCB nextProcess(int currentTime){
        return q.pollFirst();
    }

    public boolean isPreemptive(){return true;}
}
