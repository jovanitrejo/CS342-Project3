package MessageClasses;

public class MoveMessage extends Message {
    private static final long serialVersionUID = 42L;

    public final int row;
    public final int col;

    MoveMessage(int row, int col) {
        this.row = row;
        this.col = col;
    }
}
