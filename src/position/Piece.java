package position;

public class Piece {
    int square;
    short pieceType;
    short color;

    public Piece(int square, short pieceType, short color) {
        this.square = square;
        this.pieceType = pieceType;
        this.color = color;
    }
}
