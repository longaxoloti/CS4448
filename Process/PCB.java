package Process;

public class PCB {
    private final int pID;
    private final int priority;
    private final int burstTime;
    private int remainingTime;
    private final int arrivalTime;
    private State state;

    private Integer startTime = null;
    private Integer completionTime = null;

    public PCB (int pID, int priority, int burstTime, int arrivalTime){
        this.pID = pID;
        this.priority = priority;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.arrivalTime = arrivalTime;
        this.state = State.NEW;
    }

    // ===== Getter =====
    public int getPID(){return pID;}
    public int getPriority(){return priority;}
    public int getBurstTime(){return burstTime;}
    public int getArrivalTime(){return arrivalTime;}
    public int getRemainingTime(){return remainingTime;}
    public State getState(){return state;}
    public Integer getStartTime(){return startTime;}
    public Integer getCompletionTime(){return completionTime;}

    // ===== Setter =====
    public void setRemainingTime(int remainingTime){
        this.remainingTime = remainingTime;
    }
    public void setState(State state){
        this.state = state;
    }
    public void setStartTime(int t){
        if (this.startTime == null) this.startTime = t;
    }
    public void setCompletionTime(int t){
        this.completionTime = t;
    }
    public void setRemaining(int t){
        this.remainingTime = t;
    }

    // Other functions
    public void decRemaining(){
        if (remainingTime > 0) remainingTime--;
    }
    public int getTurnaroundTime(){
        return (completionTime == null) ? -1 : (completionTime - arrivalTime);
    }
    public int getWaitingTime(){
        if (completionTime == null || startTime == null) return -1;
        return getTurnaroundTime() - burstTime;
    }

    @Override
    public String toString(){
        return String.format("PID = %d, priority = %d, burst = %d, remain = %d, arrival = %d, state = %s",
                pID, priority, burstTime, remainingTime, arrivalTime, state);
    }

    public static void main(String[] args){

    }
}
