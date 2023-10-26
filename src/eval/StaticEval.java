package eval;

import position.Position;
import position.Type;

public class StaticEval {
    static final int midGamePawnValue = 100;
    static final int midGameKnightValue = 330;
    static final int midGameBishopValue = 350;
    static final int midGameRookValue = 500;
    static final int midGameQueenValue = 900;

    static final int endGamePawnValue = 120;
    static final int endGameKnightValue = 300;
    static final int endGameBishopValue = 350;
    static final int endGameRookValue = 550;
    static final int endGameQueenValue = 1050;

    public static int evaluate(Position pos) {
        if (pos.gameState == Type.midGame)return midGameEvaluationOf(pos);
        return endGameEvaluationOf(pos);
    }
    private static int midGameEvaluationOf(Position pos) {//return a high value if good for player to move, low value if not
        int eval = midGameKingSafety(pos);
        if (eval==Integer.MIN_VALUE || eval==Integer.MAX_VALUE)return eval;
        eval += midGamePawnWeight(pos);
        eval += midGameKnightWeight(pos);
        eval += midGameBishopWeight(pos);
        eval += midGameRookWeight(pos);
        eval += midGameQueenWeight(pos);
        return eval;
    }
    private static int endGameEvaluationOf(Position pos) {
        int eval = endGameKingSafety(pos);
        if (eval==Integer.MIN_VALUE || eval==Integer.MAX_VALUE)return eval;
        eval += endGamePawnWeight(pos);
        eval += endGameKnightWeight(pos);
        eval += endGameBishopWeight(pos);
        eval += endGameRookWeight(pos);
        eval += endGameQueenWeight(pos);
        return eval;
    }

    //right now the code for evaluating the position is a bit repetitive, but I should keep it this way if I need to
    //tweak one thing in the future
    private static int midGamePawnWeight(Position pos) {
        int whitePawn = Type.White | Type.Pawn;
        int blackPawn = Type.Black | Type.Pawn;

        int ret = pos.numPieces[whitePawn] - pos.numPieces[blackPawn];
        ret *= midGamePawnValue;

        int wpLocationFactor=0;
        int bpLocationFactor=0;
        for (int i=0;i<pos.numPieces[whitePawn];i++) {
            int index = pos.pieceSquareList[whitePawn][i];
            wpLocationFactor += PieceWeights.midGameWhitePawnTable[pos.pieceSquareList[whitePawn][index]];
        }
        for (int i=0;i<pos.numPieces[blackPawn];i++) {
            int index = pos.pieceSquareList[blackPawn][i];
            bpLocationFactor += PieceWeights.midGameBlackPawnTable[pos.pieceSquareList[blackPawn][index]];
        }
        ret += wpLocationFactor - bpLocationFactor;
        return ret;
    }
    private static int midGameKnightWeight(Position pos) {
        int whiteKnight = Type.White | Type.Knight;
        int blackKnight = Type.Black | Type.Knight;

        int ret = pos.numPieces[whiteKnight] - pos.numPieces[blackKnight];
        ret *= midGameKnightValue;

        int wpLocationFactor=0;
        int bpLocationFactor=0;
        for (int i=0;i<pos.numPieces[whiteKnight];i++) {
            int index = pos.pieceSquareList[whiteKnight][i];
            wpLocationFactor += PieceWeights.whiteKnightTable[pos.pieceSquareList[whiteKnight][index]];
        }
        for (int i=0;i<pos.numPieces[blackKnight];i++) {
            int index = pos.pieceSquareList[blackKnight][i];
            bpLocationFactor += PieceWeights.blackKnightTable[pos.pieceSquareList[blackKnight][index]];
        }
        ret += wpLocationFactor - bpLocationFactor;
        return ret;
    }
    private static int midGameBishopWeight(Position pos) {
        int whiteBishop = Type.White | Type.Bishop;
        int blackBishop = Type.Black | Type.Bishop;

        int ret = pos.numPieces[whiteBishop] - pos.numPieces[blackBishop];
        ret *= midGameBishopValue;

        int wpLocationFactor=0;
        int bpLocationFactor=0;
        for (int i=0;i<pos.numPieces[whiteBishop];i++) {
            int index = pos.pieceSquareList[whiteBishop][i];
            wpLocationFactor += PieceWeights.whiteBishopTable[pos.pieceSquareList[whiteBishop][index]];
        }
        for (int i=0;i<pos.numPieces[whiteBishop];i++) {
            int index = pos.pieceSquareList[whiteBishop][i];
            bpLocationFactor += PieceWeights.blackBishopTable[pos.pieceSquareList[whiteBishop][index]];
        }
        ret += wpLocationFactor - bpLocationFactor;
        return ret;
    }
    private static int midGameRookWeight(Position pos) {
        int whiteRook = Type.White | Type.Rook;
        int blackRook = Type.Black | Type.Rook;

        int ret = pos.numPieces[whiteRook] - pos.numPieces[blackRook];
        ret *= midGameRookValue;

        int wpLocationFactor=0;
        int bpLocationFactor=0;
        for (int i=0;i<pos.numPieces[whiteRook];i++) {
            int index = pos.pieceSquareList[whiteRook][i];
            wpLocationFactor += PieceWeights.whiteRookTable[pos.pieceSquareList[whiteRook][index]];
        }
        for (int i=0;i<pos.numPieces[blackRook];i++) {
            int index = pos.pieceSquareList[blackRook][i];
            bpLocationFactor += PieceWeights.blackRookTable[pos.pieceSquareList[blackRook][index]];
        }
        ret += wpLocationFactor - bpLocationFactor;
        return ret;
    }
    private static int midGameQueenWeight(Position pos) {
        int whiteQueen = Type.White | Type.Queen;
        int blackQueen = Type.Black | Type.Queen;

        int ret = pos.numPieces[whiteQueen] - pos.numPieces[blackQueen];
        ret *= midGameQueenValue;

        int wpLocationFactor=0;
        int bpLocationFactor=0;
        for (int i=0;i<pos.numPieces[whiteQueen];i++) {
            int index = pos.pieceSquareList[whiteQueen][i];
            wpLocationFactor += PieceWeights.whiteQueenTable[pos.pieceSquareList[whiteQueen][index]];
        }
        for (int i=0;i<pos.numPieces[blackQueen];i++) {
            int index = pos.pieceSquareList[blackQueen][i];
            bpLocationFactor += PieceWeights.blackQueenTable[pos.pieceSquareList[blackQueen][index]];
        }
        ret += wpLocationFactor - bpLocationFactor;
        return ret;
    }
    private static int midGameKingSafety(Position pos) {
        if (pos.isCheckMate) {
            if (pos.whiteToMove) {//black got mated
                return Integer.MAX_VALUE;
            }
            else {//white got mated
                return Integer.MIN_VALUE;
            }
        }
        //evaluate white king safety
        int whiteKingSafety= PieceWeights.midGameWhiteKingTable[pos.pieceSquareList[Type.White | Type.King][0]];


        //evaluate black king safety
        int blackKingSafety= PieceWeights.midGameWhiteKingTable[pos.pieceSquareList[Type.Black | Type.King][0]];



        return whiteKingSafety-blackKingSafety;
    }

    private static int endGamePawnWeight(Position pos) {
        int whitePawn = Type.White | Type.Pawn;
        int blackPawn = Type.Black | Type.Pawn;

        int ret = pos.numPieces[whitePawn] - pos.numPieces[blackPawn];
        ret *= endGamePawnValue;

        int wpLocationFactor=0;
        int bpLocationFactor=0;
        for (int i=0;i<pos.numPieces[whitePawn];i++) {
            int index = pos.pieceSquareList[whitePawn][i];
            wpLocationFactor += PieceWeights.endGameWhitePawnTable[pos.pieceSquareList[whitePawn][index]];
        }
        for (int i=0;i<pos.numPieces[blackPawn];i++) {
            int index = pos.pieceSquareList[blackPawn][i];
            bpLocationFactor += PieceWeights.endGameBlackPawnTable[pos.pieceSquareList[blackPawn][index]];
        }
        ret += wpLocationFactor - bpLocationFactor;
        return ret;
    }
    private static int endGameKnightWeight(Position pos) {
        int whiteKnight = Type.White | Type.Knight;
        int blackKnight = Type.Black | Type.Knight;

        int ret = pos.numPieces[whiteKnight] - pos.numPieces[blackKnight];
        ret *= endGameKnightValue;

        int wpLocationFactor=0;
        int bpLocationFactor=0;
        for (int i=0;i<pos.numPieces[whiteKnight];i++) {
            int index = pos.pieceSquareList[whiteKnight][i];
            wpLocationFactor += PieceWeights.whiteKnightTable[pos.pieceSquareList[whiteKnight][index]];
        }
        for (int i=0;i<pos.numPieces[blackKnight];i++) {
            int index = pos.pieceSquareList[blackKnight][i];
            bpLocationFactor += PieceWeights.blackKnightTable[pos.pieceSquareList[blackKnight][index]];
        }
        ret += wpLocationFactor - bpLocationFactor;
        return ret;
    }
    private static int endGameBishopWeight(Position pos) {
        int whiteBishop = Type.White | Type.Bishop;
        int blackBishop = Type.Black | Type.Bishop;

        int ret = pos.numPieces[whiteBishop] - pos.numPieces[blackBishop];
        ret *= endGameBishopValue;

        int wpLocationFactor=0;
        int bpLocationFactor=0;
        for (int i=0;i<pos.numPieces[whiteBishop];i++) {
            int index = pos.pieceSquareList[whiteBishop][i];
            wpLocationFactor += PieceWeights.whiteBishopTable[pos.pieceSquareList[whiteBishop][index]];
        }
        for (int i=0;i<pos.numPieces[whiteBishop];i++) {
            int index = pos.pieceSquareList[whiteBishop][i];
            bpLocationFactor += PieceWeights.blackBishopTable[pos.pieceSquareList[whiteBishop][index]];
        }
        ret += wpLocationFactor - bpLocationFactor;
        return ret;
    }
    private static int endGameRookWeight(Position pos) {
        int whiteRook = Type.White | Type.Rook;
        int blackRook = Type.Black | Type.Rook;

        int ret = pos.numPieces[whiteRook] - pos.numPieces[blackRook];
        ret *= endGameRookValue;

        int wpLocationFactor=0;
        int bpLocationFactor=0;
        for (int i=0;i<pos.numPieces[whiteRook];i++) {
            int index = pos.pieceSquareList[whiteRook][i];
            wpLocationFactor += PieceWeights.whiteRookTable[pos.pieceSquareList[whiteRook][index]];
        }
        for (int i=0;i<pos.numPieces[blackRook];i++) {
            int index = pos.pieceSquareList[blackRook][i];
            bpLocationFactor += PieceWeights.blackRookTable[pos.pieceSquareList[blackRook][index]];
        }
        ret += wpLocationFactor - bpLocationFactor;
        return ret;
    }
    private static int endGameQueenWeight(Position pos) {
        int whiteQueen = Type.White | Type.Queen;
        int blackQueen = Type.Black | Type.Queen;

        int ret = pos.numPieces[whiteQueen] - pos.numPieces[blackQueen];
        ret *= endGameQueenValue;

        int wpLocationFactor=0;
        int bpLocationFactor=0;
        for (int i=0;i<pos.numPieces[whiteQueen];i++) {
            int index = pos.pieceSquareList[whiteQueen][i];
            wpLocationFactor += PieceWeights.whiteQueenTable[pos.pieceSquareList[whiteQueen][index]];
        }
        for (int i=0;i<pos.numPieces[blackQueen];i++) {
            int index = pos.pieceSquareList[blackQueen][i];
            bpLocationFactor += PieceWeights.blackQueenTable[pos.pieceSquareList[blackQueen][index]];
        }
        ret += wpLocationFactor - bpLocationFactor;
        return ret;
    }
    private static int endGameKingSafety(Position pos) {
        if (pos.isCheckMate) {
            if (pos.whiteToMove) {//black got mated
                return Integer.MAX_VALUE;
            }
            else {//white got mated
                return Integer.MIN_VALUE;
            }
        }
        //evaluate white king safety
        int whiteKingSafety= PieceWeights.endGameWhiteKingTable[pos.pieceSquareList[Type.White | Type.King][0]];


        //evaluate black king safety
        int blackKingSafety= PieceWeights.endGameWhiteKingTable[pos.pieceSquareList[Type.Black | Type.King][0]];



        return whiteKingSafety-blackKingSafety;
    }
}
