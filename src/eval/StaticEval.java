package eval;

import position.Position;
import position.Type;

public class StaticEval {
    static final int pawnValue = 100;
    static final int knightValue = 300;
    static final int bishopValue = 300;
    static final int rookValue = 500;
    static final int queenValue = 900;

    static byte[][] PieceLocations = new byte[15][];
    static {
        PieceLocations[Type.White | Type.Pawn]= new byte[8];
        PieceLocations[Type.White | Type.Knight]= new byte[2];
        PieceLocations[Type.White | Type.Bishop]= new byte[2];
        PieceLocations[Type.White | Type.Rook]= new byte[2];
        PieceLocations[Type.White | Type.Queen]= new byte[2];
        PieceLocations[Type.White | Type.King]= new byte[1];
        PieceLocations[Type.Black | Type.Pawn]= new byte[8];
        PieceLocations[Type.Black | Type.Knight]= new byte[2];
        PieceLocations[Type.Black | Type.Bishop]= new byte[2];
        PieceLocations[Type.Black | Type.Rook]= new byte[2];
        PieceLocations[Type.Black | Type.Queen]= new byte[2];
        PieceLocations[Type.Black | Type.King]= new byte[1];
    }
    static byte[] pieceCount = new byte[15];


    public static int evaluationOf(Position pos) {//return a high value if good for player to move, low value if not
        //find and save each piece position
        int eval = kingSafety(pos);
        if (eval==Integer.MIN_VALUE || eval==Integer.MAX_VALUE)return eval;
        eval += pawnWeight();
        eval += knightWeight();
        eval += bishopWeight();
        eval += rookWeight();
        eval += queenWeight();
        return eval;
    }

    public static int pawnWeight() {
        int whitePawn = Type.White | Type.Pawn;
        int blackPawn = Type.Black | Type.Pawn;

        int ret = pieceCount[whitePawn] - pieceCount[blackPawn];
        ret *= pawnValue;

        int wpLocationFactor=0;
        int bpLocationFactor=0;
        for (int i=0;i<pieceCount[whitePawn];i++) {
            wpLocationFactor += PieceWeights.whitePawnTable[PieceLocations[whitePawn][pieceCount[whitePawn]]];
        }
        for (int i=0;i<pieceCount[blackPawn];i++) {
            bpLocationFactor += PieceWeights.blackPawnTable[PieceLocations[blackPawn][pieceCount[blackPawn]]];
        }
        ret += wpLocationFactor - bpLocationFactor;
        return ret;
    }

    public static int knightWeight() {
        int whiteKnight = Type.White | Type.Knight;
        int blackKnight = Type.Black | Type.Knight;

        int ret = pieceCount[whiteKnight] - pieceCount[blackKnight];
        ret *= knightValue;

        int wpLocationFactor=0;
        int bpLocationFactor=0;
        for (int i=0;i<pieceCount[whiteKnight];i++) {
            wpLocationFactor += PieceWeights.whiteKnightTable[PieceLocations[whiteKnight][pieceCount[whiteKnight]]];
        }
        for (int i=0;i<pieceCount[blackKnight];i++) {
            bpLocationFactor += PieceWeights.blackKnightTable[PieceLocations[blackKnight][pieceCount[blackKnight]]];
        }
        ret += wpLocationFactor - bpLocationFactor;
        return ret;
    }

    public static int bishopWeight() {
        int whiteBishop = Type.White | Type.Bishop;
        int blackBishop = Type.Black | Type.Bishop;

        int ret = pieceCount[whiteBishop] - pieceCount[blackBishop];
        ret *= bishopValue;

        int wpLocationFactor=0;
        int bpLocationFactor=0;
        for (int i=0;i<pieceCount[whiteBishop];i++) {
            wpLocationFactor += PieceWeights.whiteBishopTable[PieceLocations[whiteBishop][pieceCount[whiteBishop]]];
        }
        for (int i=0;i<pieceCount[blackBishop];i++) {
            bpLocationFactor += PieceWeights.blackBishopTable[PieceLocations[blackBishop][pieceCount[blackBishop]]];
        }
        ret += wpLocationFactor - bpLocationFactor;
        return ret;
    }

    public static int rookWeight() {
        int whiteRook = Type.White | Type.Rook;
        int blackRook = Type.Black | Type.Rook;

        int ret = pieceCount[whiteRook] - pieceCount[blackRook];
        ret *= rookValue;

        int wpLocationFactor=0;
        int bpLocationFactor=0;
        for (int i=0;i<pieceCount[whiteRook];i++) {
            wpLocationFactor += PieceWeights.whiteRookTable[PieceLocations[whiteRook][pieceCount[whiteRook]]];
        }
        for (int i=0;i<pieceCount[blackRook];i++) {
            bpLocationFactor += PieceWeights.blackRookTable[PieceLocations[blackRook][pieceCount[blackRook]]];
        }
        ret += wpLocationFactor - bpLocationFactor;
        return ret;
    }

    public static int queenWeight() {
        int whiteQueen = Type.White | Type.Queen;
        int blackQueen = Type.Black | Type.Queen;

        int ret = pieceCount[whiteQueen] - pieceCount[blackQueen];
        ret *= queenValue;

        int wpLocationFactor=0;
        int bpLocationFactor=0;
        for (int i=0;i<pieceCount[whiteQueen];i++) {
            wpLocationFactor += PieceWeights.whiteQueenTable[PieceLocations[whiteQueen][pieceCount[whiteQueen]]];
        }
        for (int i=0;i<pieceCount[blackQueen];i++) {
            bpLocationFactor += PieceWeights.blackQueenTable[PieceLocations[blackQueen][pieceCount[blackQueen]]];
        }
        ret += wpLocationFactor - bpLocationFactor;
        return ret;
    }

    public static int kingSafety(Position pos) {
        if (pos.isCheckMate) {
            if (pos.whiteToMove) {//black got mated
                return Integer.MAX_VALUE;
            }
            else {//white got mated
                return Integer.MIN_VALUE;
            }
        }
        //evaluate white king safety
        int whiteKingSafety= PieceWeights.whiteKingTable[PieceLocations[Type.White | Type.King][0]];


        //evaluate black king safety
        int blackKingSafety= PieceWeights.whiteKingTable[PieceLocations[Type.White | Type.King][0]];



        return whiteKingSafety-blackKingSafety;
    }


    public static void findAllPieceLocations(Position pos) {
        for (int i=0;i<64;i++) {
            byte piece = (byte) pos.squareCentricPos[i];
            PieceLocations[piece][pieceCount[piece]] = piece;
            pieceCount[piece]++;
        }
    }
}
