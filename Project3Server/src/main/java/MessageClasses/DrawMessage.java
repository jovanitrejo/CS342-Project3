package MessageClasses;

public class DrawMessage extends Message {
    private final long lengthInMinutes;
    private final int totalMoves;

    public DrawMessage(long length, int totalMoves) {
        lengthInMinutes = length;
        this.totalMoves = totalMoves;
    }

    public long getLengthInMinutes() { return this.lengthInMinutes; }
    public int getTotalMoves() { return this.totalMoves; }
}
