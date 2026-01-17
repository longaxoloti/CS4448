package Process;
import java.util.*;

public class TCB {
    private final int tID;
    private final PCB parentPCB;
    private int priority;

    private List<Instruction> instructions;
    private int programCounter = 0;
    private State state;

    public TCB(int tID, PCB parentPCB, int priority, List<Instruction> instructions) {
        this.tID = tID;
        this.parentPCB = parentPCB;
        this.priority = priority;
        this.instructions = new ArrayList<>();
        for (Instruction i : instructions) {
            this.instructions.add(new Instruction(i.getType(), i.getDuration()));
        }
        this.state = State.NEW;
    }

    public TCB(TCB other, PCB newParent) {
        this.tID = other.tID;
        this.parentPCB = newParent;
        this.priority = other.priority;
        this.state = State.NEW;
        this.instructions = new ArrayList<>();
        for (Instruction i : other.instructions) {
            this.instructions.add(new Instruction(i.getType(), i.getDuration()));
        }
    }

    // === Getters/Setters ===
    public int getTID() { return tID; }
    public PCB getParentPCB() { return parentPCB; }
    public int getPriority() { return priority; }
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }
    public void setPriority(int p) { this.priority = p; }
    public List<Instruction> getInstructions() { return instructions; }

    // === Other functions ===
    public boolean isFinished() {
        return programCounter >= instructions.size();
    }

    public Instruction getCurrentInstruction() {
        if (isFinished()) return null;
        return instructions.get(programCounter);
    }

    public void processCurrentInstruction() {
        Instruction current = getCurrentInstruction();
        if (current != null) {
            current.decDuration();
            if (current.getDuration() == 0) {
                programCounter++;
            }
        }
    }

    public void increasePriority(int amount) {
        this.priority -= amount;
        if (this.priority < 0) this.priority = 0; // Giới hạn không cho âm
    }

    @Override
    public String toString() {
        return String.format("TID = %d (PID = %d) State = %s", tID, parentPCB.getPID(), state);
    }
}