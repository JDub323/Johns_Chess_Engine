package position;

import java.security.SecureRandom;

public class Zobrist {



    private static final long[] colorToMove = new long[2];
    private static final long[] enPassantFileRandom = new long[8];
    private static final long[] castlingRightsRandom = new long[4];
    private static final long[][] pieceOnSquare = new long[15][64];

    static {
        SecureRandom random = new SecureRandom();

        colorToMove[0] = random.nextLong();//white
        colorToMove[1] = random.nextLong();//black

        for (int i=0;i<8;i++) {
            enPassantFileRandom[i] = random.nextLong();
        }

        for (int i=0;i<4;i++) {
            castlingRightsRandom[i] = random.nextLong();
        }

        for (int square = 0; square<64; square++) {
            for (int color = Type.White; color <= Type.Black; color += Type.Black) {
                for (int piece = Type.Pawn; piece<= Type.King; piece++) {
                    pieceOnSquare[color | piece][square] = random.nextLong();
                }
            }
        }
    }

    public static long getZobristKeyFromPosition(boolean whiteToMove, long castlingRights, int enPassantTargetFiles, byte[] squareCentricPos) {
        long ret = 0;

        ret ^= whiteToMove ? colorToMove[0] : colorToMove[1];

        if ((castlingRights & Type.whiteCanCS) !=0){
            ret ^= castlingRightsRandom[0];
        }
        if ((castlingRights & Type.whiteCanCL) !=0){
            ret ^= castlingRightsRandom[1];
        }
        if ((castlingRights & Type.blackCanCS) !=0){
            ret ^= castlingRightsRandom[2];
        }
        if ((castlingRights & Type.blackCanCL) !=0){
            ret ^= castlingRightsRandom[3];
        }

        if (enPassantTargetFiles != 0)ret ^= enPassantFileRandom[Long.numberOfTrailingZeros(enPassantTargetFiles)];

        for (int square=0;square<64;square++) {
            byte piece = squareCentricPos[square];
            if (piece!=0)ret ^= pieceOnSquare[piece][square];
        }

        return ret;
    }
}
