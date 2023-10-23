package move;

import position.Constants;
import position.Type;

public class PieceAttack {

    private static final long[] whitePawnAttacksDB = new long[56];
    private static final long[] blackPawnAttacksDB = new long[56];
    private static final long[] knightAttacksDB = new long[64];
    private static final long[] kingAttacksDB = new long[64];
    public static final long[][] maskOfLineInDirection = new long[8][64];


    public static long lookUpWhitePawnAttacks(int square) {
        return whitePawnAttacksDB[square];
    }
    public static long lookUpBlackPawnAttacks(int square) {
        return blackPawnAttacksDB[square];
    }
    public static long lookUpKnightAttacks(int square) {
        return knightAttacksDB[square];
    }
    public static long lookUpKingAttacks(int square) {
        return kingAttacksDB[square];
    }
    public static long lookUpBishopAttacks(int square, long blockers) {
        blockers &= GenerateMagicBitBoards.getMaskForBishop(square);
        int index = (int) ((blockers * MagicBitboards.bishopMagics[square]) >>> MagicBitboards.bishopBitshiftBy[square]);
        return MagicBitboards.bishopAttacks[square][index];
    }
    public static long lookUpRookAttacks(int square, long blockers) {
        blockers &= GenerateMagicBitBoards.getMaskForRook(square);
        int index = (int) ((blockers * MagicBitboards.rookMagics[square]) >>> MagicBitboards.rookBitshiftBy[square]);
        return MagicBitboards.rookAttacks[square][index];
    }
    public static long lookUpQueenAttacks(int square, long blockers) {
        return lookUpBishopAttacks(square,blockers) | lookUpRookAttacks(square,blockers);
    }

    public static void generateAllMasksOfLines() {
        for (int square = 0; square<64; square++) {
            for (int direction = 0; direction<8;direction++) {
                maskOfLineInDirection[direction][square] = makeMaskOfLineInDirection(square,direction);
            }
        }
    }

    private static long makeMaskOfLineInDirection(int square, int direction) {
        long ret=0;
        long squareBB = 1L<<square;

        switch (direction) {
            case Type.pinRight-> {
                long edge = Constants.H_FILE;
                while ((squareBB & edge) ==0) {
                    squareBB<<=1;
                    ret+=squareBB;
                }
            }
            case Type.pinUpRight-> {
                long edge = Constants.BISHOP_TR_EDGE;
                while ((squareBB & edge) ==0) {
                    squareBB<<=9;
                    ret+=squareBB;
                }
            }
            case Type.pinUp-> {
                long edge = Constants.RANK_8;
                while ((squareBB & edge) ==0) {
                    squareBB<<=8;
                    ret+=squareBB;
                }
            }
            case Type.pinUpLeft-> {
                long edge = Constants.BISHOP_TL_EDGE;
                while ((squareBB & edge) ==0) {
                    squareBB<<=7;
                    ret+=squareBB;
                }
            }
            case Type.pinLeft-> {
                long edge = Constants.A_FILE;
                while ((squareBB & edge) ==0) {
                    squareBB>>>=1;
                    ret+=squareBB;
                }
            }
            case Type.pinDownLeft-> {
                long edge = Constants.BISHOP_BL_EDGE;
                while ((squareBB & edge) ==0) {
                    squareBB>>>=9;
                    ret+=squareBB;
                }
            }
            case Type.pinDown-> {
                long edge = Constants.RANK_1;
                while ((squareBB & edge) ==0) {
                    squareBB>>>=8;
                    ret+=squareBB;
                }
            }
            case Type.pinDownRight-> {
                long edge = Constants.BISHOP_BR_EDGE;
                while ((squareBB & edge) ==0) {
                    squareBB>>>=7;
                    ret+=squareBB;
                }
            }
        }
        return ret;
    }

    public static long generateRookAttackBitboard(int square, long occupiedSquareBitboard) {
        long tickerSquareBB;
        long currentSquare;
        long tempRA=0;
        currentSquare=1L<<square;
        tickerSquareBB=currentSquare;

        while ((tickerSquareBB | Constants.H_FILE) !=Constants.H_FILE) {
            tickerSquareBB=tickerSquareBB<<1;
            tempRA|=tickerSquareBB;
            if ((tickerSquareBB & occupiedSquareBitboard) !=0)break;
        }

        tickerSquareBB=currentSquare;
        while ((tickerSquareBB | Constants.RANK_8) !=Constants.RANK_8) {
            tickerSquareBB=tickerSquareBB<<8;
            tempRA|=tickerSquareBB;
            if ((tickerSquareBB & occupiedSquareBitboard) !=0)break;
        }

        tickerSquareBB=currentSquare;
        while ((tickerSquareBB | Constants.A_FILE) !=Constants.A_FILE) {
            tickerSquareBB=tickerSquareBB>>>1;
            tempRA|=tickerSquareBB;
            if ((tickerSquareBB & occupiedSquareBitboard) !=0)break;
        }

        tickerSquareBB=currentSquare;
        while ((tickerSquareBB | Constants.RANK_1) !=Constants.RANK_1) {
            tickerSquareBB=tickerSquareBB>>>8;
            tempRA|=tickerSquareBB;
            if ((tickerSquareBB & occupiedSquareBitboard) !=0)break;
        }

        return tempRA;
    }
    public static long generateBishopAttackBitboard(int square, long occupiedSquareBitboard) {
        long tickerSquareBB;
        long currentSquare;
        long tempBA=0;
        currentSquare=1L<<square;
        tickerSquareBB=currentSquare;

        while ((tickerSquareBB | Constants.BISHOP_TR_EDGE) !=Constants.BISHOP_TR_EDGE) {
            tickerSquareBB=tickerSquareBB<<9;
            tempBA|=tickerSquareBB;
            if ((tickerSquareBB & occupiedSquareBitboard) !=0)break;
        }
        tickerSquareBB=currentSquare;
        while ((tickerSquareBB | Constants.BISHOP_TL_EDGE) !=Constants.BISHOP_TL_EDGE) {
            tickerSquareBB=tickerSquareBB<<7;
            tempBA|=tickerSquareBB;
            if ((tickerSquareBB & occupiedSquareBitboard) !=0)break;
        }
        tickerSquareBB=currentSquare;
        while ((tickerSquareBB | Constants.BISHOP_BR_EDGE) !=Constants.BISHOP_BR_EDGE) {
            tickerSquareBB=tickerSquareBB>>>7;
            tempBA|=tickerSquareBB;
            if ((tickerSquareBB & occupiedSquareBitboard) !=0)break;
        }
        tickerSquareBB=currentSquare;
        while ((tickerSquareBB | Constants.BISHOP_BL_EDGE) !=Constants.BISHOP_BL_EDGE) {
            tickerSquareBB=tickerSquareBB>>>9;
            tempBA|=tickerSquareBB;
            if ((tickerSquareBB & occupiedSquareBitboard) !=0)break;
        }

        return tempBA;
    }
    public static long generateWhitePawnAttacks(long pawns) {
        return pawns << 7 & Constants.NOT_H_FILE | pawns << 9 & Constants.NOT_A_FILE;
    }
    public static long generateBlackPawnAttacks(long pawns) {
        return pawns >>> 7 & Constants.NOT_A_FILE | pawns >>> 9 & Constants.NOT_H_FILE;
    }
    public static long generateKnightAttacks(long knights) {
        return (knights<<6|knights>>>10)&Constants.NOT_GH_FILE|(knights<<10|knights>>>6)&Constants.NOT_AB_FILE|(knights<<17|knights>>>15)&Constants.NOT_A_FILE|(knights<<15|knights>>>17)&Constants.NOT_H_FILE;
    }
    public static long generateKingAttacks(long king) {
        return (king<<1|king<<9|king>>>7)&Constants.NOT_A_FILE|(king<<7|king>>>1|king>>>9)&Constants.NOT_H_FILE|king<<8|king>>>8;
    }

    private static void generateMaskOfRelevantSquaresForBishop() {
        long bitBoardPos;
        long currentSquare;
        long tempBA;
        for (int i=0;i<64;i++) {
            bitBoardPos=1;
            bitBoardPos= bitBoardPos<<i;
            currentSquare=bitBoardPos;
            tempBA=0;
            while ((bitBoardPos | Constants.BISHOP_TR_EDGE) !=Constants.BISHOP_TR_EDGE) {
                bitBoardPos=bitBoardPos<<9;
                tempBA|=bitBoardPos;
            }
            bitBoardPos=currentSquare;
            while ((bitBoardPos | Constants.BISHOP_TL_EDGE) !=Constants.BISHOP_TL_EDGE) {
                bitBoardPos=bitBoardPos<<7;
                tempBA|=bitBoardPos;
            }
            bitBoardPos=currentSquare;
            while ((bitBoardPos | Constants.BISHOP_BR_EDGE) !=Constants.BISHOP_BR_EDGE) {
                bitBoardPos=bitBoardPos>>>7;
                tempBA|=bitBoardPos;
            }
            bitBoardPos=currentSquare;
            while ((bitBoardPos | Constants.BISHOP_BL_EDGE) !=Constants.BISHOP_BL_EDGE) {
                bitBoardPos=bitBoardPos>>>9;
                tempBA|=bitBoardPos;
            }
            GenerateMagicBitBoards.maskForBishop[i] = tempBA & Constants.NOT_EDGES;
        }
    }
    private static void generateMaskOfRelevantSquaresForRook() {//generate after rook target squares are generated
        long bitBoardPos;
        long currentSquare;
        long tempRA;
        for (int i=0;i<64;i++) {
            bitBoardPos = 1L<<i;
            currentSquare = bitBoardPos;
            tempRA = 0;
            while ((bitBoardPos | Constants.H_FILE) != Constants.H_FILE) {
                bitBoardPos = bitBoardPos << 1;
                if ((bitBoardPos | Constants.H_FILE) != Constants.H_FILE) tempRA |= bitBoardPos;
            }
            bitBoardPos = currentSquare;
            while ((bitBoardPos | Constants.RANK_8) != Constants.RANK_8) {
                bitBoardPos = bitBoardPos << 8;
                if ((bitBoardPos | Constants.RANK_8) != Constants.RANK_8) tempRA |= bitBoardPos;
            }
            bitBoardPos = currentSquare;
            while ((bitBoardPos | Constants.A_FILE) != Constants.A_FILE) {
                bitBoardPos = bitBoardPos >>> 1;
                if ((bitBoardPos | Constants.A_FILE) != Constants.A_FILE) tempRA |= bitBoardPos;
            }
            bitBoardPos = currentSquare;
            while ((bitBoardPos | Constants.RANK_1) != Constants.RANK_1) {
                bitBoardPos = bitBoardPos >>> 8;
                if ((bitBoardPos | Constants.RANK_1) != Constants.RANK_1) tempRA |= bitBoardPos;
            }
            GenerateMagicBitBoards.maskForRook[i] = tempRA;
        }
    }
    private static void generateWhitePawnDB() {
        for (int square=0;square<56;square++) {
            whitePawnAttacksDB[square] = generateWhitePawnAttacks(1L<<square);
        }
    }
    private static void generateBlackPawnDB() {
        for (int square=0;square<56;square++) {
            blackPawnAttacksDB[square] = generateBlackPawnAttacks(1L<<square);
        }
    }
    private static void generateKnightDB() {
        for (int square=0;square<64;square++) {
            knightAttacksDB[square] = generateKnightAttacks(1L<<square);
        }
    }
    private static void generateKingDB() {
        for (int square=0;square<64;square++) {
            kingAttacksDB[square] = generateKingAttacks(1L<<square);
        }
    }

    public static void generateMoveArrays() {
        generateWhitePawnDB();
        generateBlackPawnDB();
        generateKnightDB();
        generateKingDB();
        generateMaskOfRelevantSquaresForBishop();
        generateMaskOfRelevantSquaresForRook();
        generateAllMasksOfLines();
    }
}
