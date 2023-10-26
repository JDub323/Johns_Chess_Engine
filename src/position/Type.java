package position;

public class Type {
    public static final byte Empty = 0;
    public static final byte Pawn = 1;
    public static final byte Knight = 2;
    public static final byte Bishop = 3;
    public static final byte Rook = 4;
    public static final byte Queen = 5;
    public static final byte King = 6;

    public static final byte White = 0;
    public static final byte Black = 8;
    //can use bitwise operators to create white/black pieces
    //ie: White|Knight or Black|King

    public static final long whiteCanCS= Constants.g1;//the toSquare when I make a castling move
    public static final long whiteCanCL= Constants.c1;
    public static final long blackCanCS= Constants.g8;
    public static final long blackCanCL= Constants.c8;
    public static final long notWhiteCanCS= ~whiteCanCS;
    public static final long notWhiteCanCL= ~whiteCanCL;
    public static final long notBlackCanCS= ~blackCanCS;
    public static final long notBlackCanCL= ~blackCanCL;

    //move types
    public static final byte normalMove=0;
    public static final byte enPassant=1;
    public static final byte doublePawnMove=2;
    public static final byte castles=3; //toSquare is where the king will be, fromSquare is where the king is
    public static final byte pawnPromotesToQ=4;
    public static final byte pawnPromotesToN=5;
    public static final byte pawnPromotesToB=6;
    public static final byte pawnPromotesToR=7;

    //possible states the game could be in
    public static final byte midGame=0;
    public static final byte endGame=1;
    public static final byte blackIsCheckmated=2;
    public static final byte whiteIsCheckmated=3;
    public static final byte gameIsADraw=4;

    //pin directions
    public static final byte pinRight=0;
    public static final byte pinUpRight=1;
    public static final byte pinUp=2;
    public static final byte pinUpLeft=3;
    public static final byte pinLeft=4;
    public static final byte pinDownLeft=5;
    public static final byte pinDown=6;
    public static final byte pinDownRight=7;
}
