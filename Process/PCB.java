package Process;
import java.util.*;

public class PCB {
    private final int pID;
    private final int priority;
    private final int memoryRequired;
    private final int arrivalTime;

    private List<Instruction> instructions;
    private int programCounter = 0;

    private State state;
    private Integer startTime = null;
    private Integer completionTime = null;

    public PCB (int pID, int priority, int memoryRequired, int arrivalTime, List<Instruction> instructions) {
        this.pID = pID;
        this.priority = priority;
        this.memoryRequired = memoryRequired;
        this.arrivalTime = arrivalTime;

        this.instructions = new ArrayList<>();
        for (Instruction i: instructions){
            this.instructions.add(new Instruction(i.getType(), i.getDuration()));
        }
        this.state = State.NEW;
    }

    // ===== Getter =====
    public int getPID(){return pID;}
    public int getPriority(){return priority;}
    public int getMemoryRequired(){return memoryRequired;}
    public int getArrivalTime(){return arrivalTime;}
    public State getState(){return state;}
    public Integer getStartTime(){return startTime;}
    public Integer getCompletionTime(){return completionTime;}
    public List<Instruction> getInstructions() { return instructions; }

    public int getBurstTime(){
        return instructions.stream()
                .filter(i -> i.getType() == Instruction.Type.CPU)
                .mapToInt(Instruction::getDuration)
                .sum();
    }
    public int getRemainingTime(){
        int rem = 0;
        for (int i = programCounter; i < instructions.size(); i++){
            Instruction ins = instructions.get(i);
            if (ins.getType() == Instruction.Type.CPU) {
                rem += ins.getDuration();
            }
        }
        return rem;
    }
    public int getWaitingTime() {
        if (completionTime == null) return 0;
        return 0;
    }
    public int getTurnaroundTime() {
        return (completionTime == null) ? 0 : (completionTime - arrivalTime);
    }

    public Instruction getCurrentInstruction(){
        if (isFinished()) return null;
        return instructions.get(programCounter);
    }

    // ===== Setter =====
    public void setState(State state){
        this.state = state;
    }
    public void setStartTime(int t){
        if (this.startTime == null) this.startTime = t;
    }
    public void setCompletionTime(int t){
        this.completionTime = t;
    }

    // Other functions
//    public int getWaitingTime(){
//        if (completionTime == null || startTime == null) return -1;
//        return getTurnaroundTime() - (getBurstTime() - getRemainingTime());
//    }
    public boolean isFinished() {
        return programCounter >= instructions.size();
    }

    public void processCurrentInstruction() {
        Instruction current = getCurrentInstruction();
        if (current != null){
            current.decDuration();
            if (current.getDuration() == 0){
                programCounter++;
            }
        }
    }

    @Override
    public String toString(){
        String located = "RAM";
        if (state == State.READY_SUSPENDED || state == State.BLOCKED_SUSPENDED) located = "DISK";
        else if (state == State.NEW) located = "-";
        return String.format("PID = %d, [%s] Mem = %d, State = %s, NextOp = %s",
                pID, located, memoryRequired, state, getCurrentInstruction());
    }
}
