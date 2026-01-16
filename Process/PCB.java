package Process;
import java.util.*;

public class PCB {
    private final int pID;
    private final int memoryRequired;
    private final int arrivalTime;
    private List<TCB> threads = new ArrayList<>();
    private boolean isInRAM = false;

    public PCB(int pID, int memoryRequired, int arrivalTime) {
        this.pID = pID;
        this.memoryRequired = memoryRequired;
        this.arrivalTime = arrivalTime;
    }

    public void addThread(TCB t) {
        threads.add(t);
    }

    public List<TCB> getThreads() { return threads; }
    public int getPID() { return pID; }
    public int getMemoryRequired() { return memoryRequired; }
    public int getArrivalTime() { return arrivalTime; }

    public boolean isInRAM() { return isInRAM; }
    public void setInRAM(boolean inRAM) { isInRAM = inRAM; }

    @Override
    public String toString() {
        return String.format("Process %d [Mem: %dMB, Loc: %s, Threads: %d]",
                pID, memoryRequired, isInRAM ? "RAM" : "DISK", threads.size());
    }
}