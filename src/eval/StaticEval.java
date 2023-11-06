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
        switch (pos.gameState) {
            case Type.midGame -> {
                return midGameEvaluationOf(pos);
            }
            case Type.endGame -> {
                return endGameEvaluationOf(pos);
            }
            case Type.blackIsCheckmated -> {
                return Integer.MIN_VALUE+1;
            }
            case Type.whiteIsCheckmated -> {
                return Integer.MAX_VALUE-1;
            }
            default -> {//Type.gameIsADraw
                return 0;
            }
        }
    }
    private static int midGameEvaluationOf(Position pos) {//return a high value if good for player to move, low value if not
        int eval = midGameKingSafety(pos);
        eval += midGamePawnWeight(pos);
        eval += midGameKnightWeight(pos);
        eval += midGameBishopWeight(pos);
        eval += midGameRookWeight(pos);
        eval += midGameQueenWeight(pos);
        if (pos.whiteToMove)return eval;
        return -eval;
    }
    private static int endGameEvaluationOf(Position pos) {
        int eval = endGameKingSafety(pos);
        eval += endGamePawnWeight(pos);
        eval += endGameKnightWeight(pos);
        eval += endGameBishopWeight(pos);
        eval += endGameRookWeight(pos);
        eval += endGameQueenWeight(pos);
        if (pos.whiteToMove)return eval;
        return -eval;
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
            int square = pos.pieceSquareList[whitePawn][i];
            wpLocationFactor += PieceWeights.midGameWhitePawnTable[square];
        }
        for (int i=0;i<pos.numPieces[blackPawn];i++) {
            int square = pos.pieceSquareList[blackPawn][i];
            bpLocationFactor += PieceWeights.midGameBlackPawnTable[square];
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
            int square = pos.pieceSquareList[whiteKnight][i];
            wpLocationFactor += PieceWeights.whiteKnightTable[square];
        }
        for (int i=0;i<pos.numPieces[blackKnight];i++) {
            int square = pos.pieceSquareList[blackKnight][i];
            bpLocationFactor += PieceWeights.blackKnightTable[square];
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
            int square = pos.pieceSquareList[whiteBishop][i];
            wpLocationFactor += PieceWeights.whiteBishopTable[square];
        }
        for (int i=0;i<pos.numPieces[whiteBishop];i++) {
            int square = pos.pieceSquareList[whiteBishop][i];
            bpLocationFactor += PieceWeights.blackBishopTable[square];
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
            int square = pos.pieceSquareList[whiteRook][i];
            wpLocationFactor += PieceWeights.whiteRookTable[square];
        }
        for (int i=0;i<pos.numPieces[blackRook];i++) {
            int square = pos.pieceSquareList[blackRook][i];
            bpLocationFactor += PieceWeights.blackRookTable[square];
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
            int square = pos.pieceSquareList[whiteQueen][i];
            wpLocationFactor += PieceWeights.whiteQueenTable[square];
        }
        for (int i=0;i<pos.numPieces[blackQueen];i++) {
            int square = pos.pieceSquareList[blackQueen][i];
            bpLocationFactor += PieceWeights.blackQueenTable[square];
        }
        ret += wpLocationFactor - bpLocationFactor;
        return ret;
    }
    private static int midGameKingSafety(Position pos) {
        //evaluate white king safety
        int whiteKingSafety= PieceWeights.midGameWhiteKingTable[pos.pieceSquareList[Type.White | Type.King][0]];


        //evaluate black king safety
        int blackKingSafety= PieceWeights.midGameBlackKingTable[pos.pieceSquareList[Type.Black | Type.King][0]];



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
            int square = pos.pieceSquareList[whitePawn][i];
            wpLocationFactor += PieceWeights.endGameWhitePawnTable[square];
        }
        for (int i=0;i<pos.numPieces[blackPawn];i++) {
            int square = pos.pieceSquareList[blackPawn][i];
            bpLocationFactor += PieceWeights.endGameBlackPawnTable[square];
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
            int square = pos.pieceSquareList[whiteKnight][i];
            wpLocationFactor += PieceWeights.whiteKnightTable[square];
        }
        for (int i=0;i<pos.numPieces[blackKnight];i++) {
            int square = pos.pieceSquareList[blackKnight][i];
            bpLocationFactor += PieceWeights.blackKnightTable[square];
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
            int square = pos.pieceSquareList[whiteBishop][i];
            wpLocationFactor += PieceWeights.whiteBishopTable[square];
        }
        for (int i=0;i<pos.numPieces[whiteBishop];i++) {
            int square = pos.pieceSquareList[whiteBishop][i];
            bpLocationFactor += PieceWeights.blackBishopTable[square];
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
            int square = pos.pieceSquareList[whiteRook][i];
            wpLocationFactor += PieceWeights.whiteRookTable[square];
        }
        for (int i=0;i<pos.numPieces[blackRook];i++) {
            int square = pos.pieceSquareList[blackRook][i];
            bpLocationFactor += PieceWeights.blackRookTable[square];
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
            int square = pos.pieceSquareList[whiteQueen][i];
            wpLocationFactor += PieceWeights.whiteQueenTable[square];
        }
        for (int i=0;i<pos.numPieces[blackQueen];i++) {
            int square = pos.pieceSquareList[blackQueen][i];
            bpLocationFactor += PieceWeights.blackQueenTable[square];
        }
        ret += wpLocationFactor - bpLocationFactor;
        return ret;
    }
    private static int endGameKingSafety(Position pos) {
        //evaluate white king safety
        int whiteKingSafety= PieceWeights.endGameWhiteKingTable[pos.pieceSquareList[Type.White | Type.King][0]];


        //evaluate black king safety
        int blackKingSafety= PieceWeights.endGameBlackKingTable[pos.pieceSquareList[Type.Black | Type.King][0]];



        return whiteKingSafety-blackKingSafety;
    }
}
