package optimizations.ollir.data;

public class VarLifeTime {
    private final int start;
    private int end;

    public VarLifeTime(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }
}
