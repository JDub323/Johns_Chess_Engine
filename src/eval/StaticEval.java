package eval;

import chessUtilities.Util;
import move.PieceAttack;
import position.Constants;
import position.Position;
import position.Type;

public class StaticEval {
    static final int midGamePawnValue = 100;
    static final int midGameKnightValue = 330;
    static final int midGameBishopValue = 350;
    static final int midGameRookValue = 500;
    static final int midGameQueenValue = 900;

    static final int endGamePawnValue = 110;
    static final int endGameKnightValue = 300;
    static final int endGameBishopValue = 350;
    static final int endGameRookValue = 550;
    static final int endGameQueenValue = 1050;

    //multiplied by the number of squares seen by each respective piece
    private static final int MANHATTAN_DISTANCE_MULTIPLIER = 10;
    private static final int BISHOP_MOBILITY_MULTIPLIER = 4;
    private static final int ROOK_MOBILITY_MULTIPLIER = 2;
    private static final int QUEEN_MOBILITY_MULTIPLIER = 1;
    private static final int KING_MOBILITY_MULTIPLIER = -3;

    //added each time the event occurs
    private static final int PASSED_PAWN_BONUS = 80;
    private static final int ISOLATED_PAWN_BONUS = -30;
    private static final int DOUBLED_PAWN_BONUS = -30;
    private static final int DEFENDED_PAWN_BONUS = 10;
    private static final int OUTPOST_BONUS = 80;//knights in enemy territory defended by a pawn
    private static final int DEFENDED_MINOR_PIECE_BONUS = 30;//don't like loose bishops/knights
    //no real benefit for having rooks or queens defended by pawns
    private static final int CONNECTED_ROOK_BONUS = 35;//this is always doubled since they connect to each other


    public static final int CHECKMATE = 2000000000;
    public static final int DRAW = 0;

    private static final boolean[][][][] gameIsDrawnByInsufficientMaterial = new boolean[4][4][4][4];//first is white knight, then white bishop, then black knight, then black bishop
    static {
        gameIsDrawnByInsufficientMaterial[0][0][0][0] = true;//no pieces
        gameIsDrawnByInsufficientMaterial[1][0][0][0] = true;//only 1 piece total
        gameIsDrawnByInsufficientMaterial[0][1][0][0] = true;
        gameIsDrawnByInsufficientMaterial[0][0][1][0] = true;
        gameIsDrawnByInsufficientMaterial[0][0][0][1] = true;
        gameIsDrawnByInsufficientMaterial[1][0][1][0] = true;//2 pieces, opposite color
        gameIsDrawnByInsufficientMaterial[1][0][0][1] = true;
        gameIsDrawnByInsufficientMaterial[0][1][1][0] = true;
        gameIsDrawnByInsufficientMaterial[0][1][0][1] = true;
        gameIsDrawnByInsufficientMaterial[2][0][0][0] = true;//2 pieces, same color only draw with knights
        gameIsDrawnByInsufficientMaterial[0][0][2][0] = true;

        //all other games are not drawn
        //can have up to 3 pieces for each piece type as an input, since the method is only called if there are 3 or fewer total pieces for each color
    }

    public static boolean gameIsDrawnByInsufficientMaterial(int numHeavyPiecesAndPawns, int numWhiteKnights, int numWhiteBishops, int numBlackKnights, int numBlackBishops) {
        if (numHeavyPiecesAndPawns > 0)return false;//game never drawn by insufficient material if there are pawns, queens, or rooks on the board
        return gameIsDrawnByInsufficientMaterial[numWhiteKnights][numWhiteBishops][numBlackKnights][numBlackBishops];
    }

    public static int evaluate(Position pos) {//don't check for checkmate in static eval, just hope previous positions found it
        if (pos.gameState == Type.MID_GAME)return midGameEvaluationOf(pos);
        else return endGameEvaluationOf(pos);
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
        int whitePawn = Type.WHITE | Type.PAWN;
        int blackPawn = Type.BLACK | Type.PAWN;

        int ret = pos.numPieces[whitePawn] - pos.numPieces[blackPawn];
        ret *= midGamePawnValue;

        int wpLocationFactor=0;
        int bpLocationFactor=0;
        for (int i=0;i<pos.numPieces[whitePawn];i++) {
            int square = pos.pieceSquareList[whitePawn][i];
            wpLocationFactor += PieceWeights.midGameWhitePawnTable[square];
            wpLocationFactor += getPawnBonuses(pos, square, Type.WHITE);
        }
        for (int i=0;i<pos.numPieces[blackPawn];i++) {
            int square = pos.pieceSquareList[blackPawn][i];
            bpLocationFactor += PieceWeights.midGameBlackPawnTable[square];
            bpLocationFactor += getPawnBonuses(pos, square, Type.BLACK);
        }
        ret += wpLocationFactor - bpLocationFactor;
        return ret;
    }

    private static int getPawnBonuses(Position pos, int square, byte color) {
        int ret =0;
        long fileMask = Constants.ALL_FILES[square%8];
        long adjFileMask = Constants.ADJACENT_FILES[square%8];
        byte enemyColor = (byte) (color^8);


        if (((fileMask | adjFileMask) & pos.pieceArray[enemyColor | Type.PAWN]) != 0) {//the pawn is a passed pawn
            ret += PASSED_PAWN_BONUS;
        }
        if ((adjFileMask & pos.pieceArray[color | Type.PAWN]) == 0) {//the pawn is an isolated pawn
            ret += ISOLATED_PAWN_BONUS;
        }
        if (Long.bitCount(fileMask & pos.pieceArray[color | Type.PAWN]) > 1) {//there is more than one pawn in the same file: doubled pawns
            ret += DOUBLED_PAWN_BONUS;
        }
        long squareBB = Util.toBitboard(square);
        if (color == Type.WHITE) {
            if ((pos.whiteAttacksArray[Type.PAWN] & squareBB) != 0) {
                ret += DEFENDED_PAWN_BONUS;
            }
        }
        else {
            if ((pos.blackAttacksArray[Type.PAWN] & squareBB) != 0) {
                ret += DEFENDED_PAWN_BONUS;
            }
        }

        return ret;
    }

    private static int midGameKnightWeight(Position pos) {
        int whiteKnight = Type.WHITE | Type.KNIGHT;
        int blackKnight = Type.BLACK | Type.KNIGHT;

        int ret = pos.numPieces[whiteKnight] - pos.numPieces[blackKnight];
        ret *= midGameKnightValue;

        int wpLocationFactor=0;
        int bpLocationFactor=0;
        for (int i=0;i<pos.numPieces[whiteKnight];i++) {
            int square = pos.pieceSquareList[whiteKnight][i];
            wpLocationFactor += PieceWeights.whiteKnightTable[square];
            if ((pos.whiteAttacksArray[Type.PAWN] & Util.toBitboard(square)
                    & Constants.ENEMY_SQUARES[0]) != 0) {
                wpLocationFactor += OUTPOST_BONUS;
            }
            if ((pos.whiteAttacks & Util.toBitboard(square)) != 0) {
                wpLocationFactor += DEFENDED_MINOR_PIECE_BONUS;
            }
        }
        for (int i=0;i<pos.numPieces[blackKnight];i++) {
            int square = pos.pieceSquareList[blackKnight][i];
            bpLocationFactor += PieceWeights.blackKnightTable[square];
            if ((pos.blackAttacks & Util.toBitboard(square)) != 0) {
                bpLocationFactor += DEFENDED_MINOR_PIECE_BONUS;
            }
        }
        ret += wpLocationFactor - bpLocationFactor;
        return ret;
    }
    private static int midGameBishopWeight(Position pos) {
        int whiteBishop = Type.WHITE | Type.BISHOP;
        int blackBishop = Type.BLACK | Type.BISHOP;

        int ret = pos.numPieces[whiteBishop] - pos.numPieces[blackBishop];
        ret *= midGameBishopValue;

        int wpLocationFactor=0;
        int bpLocationFactor=0;
        for (int i=0;i<pos.numPieces[whiteBishop];i++) {
            int square = pos.pieceSquareList[whiteBishop][i];
            wpLocationFactor += PieceWeights.whiteBishopTable[square];
            wpLocationFactor += Long.bitCount(PieceAttack.lookUpBishopAttacks(square, pos.allPieces))*BISHOP_MOBILITY_MULTIPLIER;
            if ((pos.blackAttacks & Util.toBitboard(square)) != 0) {
                wpLocationFactor += DEFENDED_MINOR_PIECE_BONUS;
            }
        }
        for (int i=0;i<pos.numPieces[whiteBishop];i++) {
            int square = pos.pieceSquareList[whiteBishop][i];
            bpLocationFactor += PieceWeights.blackBishopTable[square];
            bpLocationFactor += Long.bitCount(PieceAttack.lookUpBishopAttacks(square, pos.allPieces))*BISHOP_MOBILITY_MULTIPLIER;
            if ((pos.whiteAttacksArray[Type.PAWN] & Util.toBitboard(square)
                    & Constants.ENEMY_SQUARES[1]) != 0) {
                bpLocationFactor += OUTPOST_BONUS;
            }
            if ((pos.whiteAttacks & Util.toBitboard(square)) != 0) {
                bpLocationFactor += DEFENDED_MINOR_PIECE_BONUS;
            }
        }
        ret += wpLocationFactor - bpLocationFactor;
        return ret;
    }
    private static int midGameRookWeight(Position pos) {
        int whiteRook = Type.WHITE | Type.ROOK;
        int blackRook = Type.BLACK | Type.ROOK;

        int ret = pos.numPieces[whiteRook] - pos.numPieces[blackRook];
        ret *= midGameRookValue;

        int wpLocationFactor=0;
        int bpLocationFactor=0;
        for (int i=0;i<pos.numPieces[whiteRook];i++) {
            int square = pos.pieceSquareList[whiteRook][i];
            wpLocationFactor += PieceWeights.whiteRookTable[square];
            wpLocationFactor += Long.bitCount(PieceAttack.lookUpRookAttacks(square, pos.allPieces))*ROOK_MOBILITY_MULTIPLIER;
            if ((Util.toBitboard(square) & pos.whiteAttacksArray[Type.ROOK]) != 0) {//rooks are connected
                wpLocationFactor += CONNECTED_ROOK_BONUS;
            }
        }
        for (int i=0;i<pos.numPieces[blackRook];i++) {
            int square = pos.pieceSquareList[blackRook][i];
            bpLocationFactor += PieceWeights.blackRookTable[square];
            bpLocationFactor += Long.bitCount(PieceAttack.lookUpRookAttacks(square, pos.allPieces))*ROOK_MOBILITY_MULTIPLIER;
            if ((Util.toBitboard(square) & pos.blackAttacksArray[Type.ROOK]) != 0) {//rooks are connected
                bpLocationFactor += CONNECTED_ROOK_BONUS;
            }
        }
        ret += wpLocationFactor - bpLocationFactor;
        return ret;
    }
    private static int midGameQueenWeight(Position pos) {
        int whiteQueen = Type.WHITE | Type.QUEEN;
        int blackQueen = Type.BLACK | Type.QUEEN;

        int ret = pos.numPieces[whiteQueen] - pos.numPieces[blackQueen];
        ret *= midGameQueenValue;

        int wpLocationFactor=0;
        int bpLocationFactor=0;
        for (int i=0;i<pos.numPieces[whiteQueen];i++) {
            int square = pos.pieceSquareList[whiteQueen][i];
            wpLocationFactor += PieceWeights.whiteQueenTable[square];
            wpLocationFactor += Long.bitCount(PieceAttack.lookUpQueenAttacks(square, pos.allPieces))*QUEEN_MOBILITY_MULTIPLIER;
        }
        for (int i=0;i<pos.numPieces[blackQueen];i++) {
            int square = pos.pieceSquareList[blackQueen][i];
            bpLocationFactor += PieceWeights.blackQueenTable[square];
            bpLocationFactor += Long.bitCount(PieceAttack.lookUpQueenAttacks(square, pos.allPieces))*QUEEN_MOBILITY_MULTIPLIER;
        }
        ret += wpLocationFactor - bpLocationFactor;
        return ret;
    }
    private static int midGameKingSafety(Position pos) {
        //evaluate white king safety
        int whiteKingPos = pos.pieceSquareList[Type.WHITE | Type.KING][0];
        int whiteKingSafety= PieceWeights.midGameWhiteKingTable[whiteKingPos];

        whiteKingSafety += Long.bitCount(PieceAttack.lookUpQueenAttacks(whiteKingPos,pos.allPieces))*KING_MOBILITY_MULTIPLIER;

        //evaluate black king safety
        int blackKingPos = pos.pieceSquareList[Type.BLACK | Type.KING][0];
        int blackKingSafety= PieceWeights.midGameBlackKingTable[blackKingPos];

        blackKingSafety += Long.bitCount(PieceAttack.lookUpQueenAttacks(blackKingPos,pos.allPieces))*KING_MOBILITY_MULTIPLIER;


        return whiteKingSafety-blackKingSafety;
    }

    private static int endGamePawnWeight(Position pos) {
        int whitePawn = Type.WHITE | Type.PAWN;
        int blackPawn = Type.BLACK | Type.PAWN;

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
        int whiteKnight = Type.WHITE | Type.KNIGHT;
        int blackKnight = Type.BLACK | Type.KNIGHT;

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
        int whiteBishop = Type.WHITE | Type.BISHOP;
        int blackBishop = Type.BLACK | Type.BISHOP;

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
        int whiteRook = Type.WHITE | Type.ROOK;
        int blackRook = Type.BLACK | Type.ROOK;

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
        int whiteQueen = Type.WHITE | Type.QUEEN;
        int blackQueen = Type.BLACK | Type.QUEEN;

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
        int eval = 0;

        //evaluate white king safety
        eval += PieceWeights.endGameWhiteKingTable[pos.pieceSquareList[Type.WHITE | Type.KING][0]];

        //evaluate black king safety
        eval -= PieceWeights.endGameBlackKingTable[pos.pieceSquareList[Type.BLACK | Type.KING][0]];

        //favor positions where the enemy king is farther from the center
        eval += MANHATTAN_DISTANCE_MULTIPLIER*(pos.whiteToMove ? Util.manhattanDistanceFromCenter(pos.pieceSquareList[Type.BLACK | Type.KING][0]) :
                Util.manhattanDistanceFromCenter(pos.pieceSquareList[Type.WHITE | Type.KING][0]));

        //make the king move toward the opponent king
        eval += 100-Util.kingDistanceBetween(pos.pieceSquareList[Type.WHITE | Type.KING][0],
                pos.pieceSquareList[Type.WHITE | Type.KING][0]) * MANHATTAN_DISTANCE_MULTIPLIER;

        return eval;
    }
}
