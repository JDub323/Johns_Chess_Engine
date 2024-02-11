package position;

public class Type {
    public static final byte EMPTY = 0;
    public static final byte PAWN = 1;
    public static final byte KNIGHT = 2;
    public static final byte BISHOP = 3;
    public static final byte ROOK = 4;
    public static final byte QUEEN = 5;
    public static final byte KING = 6;

    public static final byte WHITE = 0;
    public static final byte BLACK = 8;
    //can use bitwise operators to create white/black pieces
    //ie: White|Knight or Black|King

    public static final long WHITE_CAN_CS = Constants.g1;//the toSquare when I make a castling move
    public static final long WHITE_CAN_CL = Constants.c1;
    public static final long BLACK_CAN_CS = Constants.g8;
    public static final long BLACK_CAN_CL = Constants.c8;
    public static final long NOT_WHITE_CAN_CS = ~WHITE_CAN_CS;
    public static final long NOT_WHITE_CAN_CL = ~WHITE_CAN_CL;
    public static final long NOT_BLACK_CAN_CS = ~BLACK_CAN_CS;
    public static final long NOT_BLACK_CAN_CL = ~BLACK_CAN_CL;

    //move types
    public static final byte NORMAL_MOVE =0;
    public static final byte EN_PASSANT =1;
    public static final byte DOUBLE_PAWN_MOVE =2;
    public static final byte CASTLES =3; //toSquare is where the king will be, fromSquare is where the king is
    public static final byte PAWN_PROMOTES_TO_Q =4;
    public static final byte PAWN_PROMOTES_TO_N =5;
    public static final byte PAWN_PROMOTES_TO_B =6;
    public static final byte PAWN_PROMOTES_TO_R =7;
    public static final byte ILLEGAL_MOVE = 0;

    //possible states the game could be in
    public static final byte MID_GAME =0;
    public static final byte END_GAME =1;
    public static final byte BLACK_IS_CHECKMATED =2;
    public static final byte WHITE_IS_CHECKMATED =3;
    public static final byte GAME_IS_A_DRAW =4;

    //pin directions
    //odd numbers are bishop directions, evens are rook directions
    public static final byte DIR_RIGHT =0;
    public static final byte DIR_UPRIGHT =1;
    public static final byte DIR_UP =2;
    public static final byte DIR_UPLEFT =3;
    public static final byte DIR_LEFT =4;
    public static final byte DIR_DOWNLEFT =5;
    public static final byte DIR_DOWN =6;
    public static final byte DIR_DOWNRIGHT =7;
}
