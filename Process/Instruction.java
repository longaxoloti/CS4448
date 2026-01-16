package Process;

public class Instruction {
    public enum Type {
        CPU,
        IO
    }

    private final Type type;
    private int duration;

    public Instruction(Type type, int duration){
        this.type = type;
        this.duration = duration;
    }

    public Type getType(){ return type;}
    public int getDuration(){ return duration; }
    public void decDuration(){
        if (duration > 0) duration--;
    }

    @Override
    public String toString(){
        return type + "(" + duration + ")";
    }
}
