package position;

public class Constants {
    public static final long a1=1;
    public static final long b1=a1<<1;
    public static final long c1=b1<<1;
    public static final long d1=c1<<1;
    public static final long e1=d1<<1;
    public static final long f1=e1<<1;
    public static final long g1=f1<<1;
    public static final long h1=g1<<1;

    public static final long a2=h1<<1;
    public static final long b2=a2<<1;
    public static final long c2=b2<<1;
    public static final long d2=c2<<1;
    public static final long e2=d2<<1;
    public static final long f2=e2<<1;
    public static final long g2=f2<<1;
    public static final long h2=g2<<1;

    public static final long a3=h2<<1;
    public static final long b3=a3<<1;
    public static final long c3=b3<<1;
    public static final long d3=c3<<1;
    public static final long e3=d3<<1;
    public static final long f3=e3<<1;
    public static final long g3=f3<<1;
    public static final long h3=g3<<1;

    public static final long a4=h3<<1;
    public static final long b4=a4<<1;
    public static final long c4=b4<<1;
    public static final long d4=c4<<1;
    public static final long e4=d4<<1;
    public static final long f4=e4<<1;
    public static final long g4=f4<<1;
    public static final long h4=g4<<1;

    public static final long a5=h4<<1;
    public static final long b5=a5<<1;
    public static final long c5=b5<<1;
    public static final long d5=c5<<1;
    public static final long e5=d5<<1;
    public static final long f5=e5<<1;
    public static final long g5=f5<<1;
    public static final long h5=g5<<1;

    public static final long a6=h5<<1;
    public static final long b6=a6<<1;
    public static final long c6=b6<<1;
    public static final long d6=c6<<1;
    public static final long e6=d6<<1;
    public static final long f6=e6<<1;
    public static final long g6=f6<<1;
    public static final long h6=g6<<1;

    public static final long a7=h6<<1;
    public static final long b7=a7<<1;
    public static final long c7=b7<<1;
    public static final long d7=c7<<1;
    public static final long e7=d7<<1;
    public static final long f7=e7<<1;
    public static final long g7=f7<<1;
    public static final long h7=g7<<1;

    public static final long a8=h7<<1;
    public static final long b8=a8<<1;
    public static final long c8=b8<<1;
    public static final long d8=c8<<1;
    public static final long e8=d8<<1;
    public static final long f8=e8<<1;
    public static final long g8=f8<<1;
    public static final long h8=g8<<1;

    //files
    public static final long A_FILE=a1|a2|a3|a4|a5|a6|a7|a8;
    public static final long B_FILE=b1|b2|b3|b4|b5|b6|b7|b8;
    public static final long C_FILE=c1|c2|c3|c4|c5|c6|c7|c8;
    public static final long D_FILE=d1|d2|d3|d4|d5|d6|d7|d8;
    public static final long E_FILE=e1|e2|e3|e4|e5|e6|e7|e8;
    public static final long F_FILE=f1|f2|f3|f4|f5|f6|f7|f8;
    public static final long G_FILE=g1|g2|g3|g4|g5|g6|g7|g8;
    public static final long H_FILE=h1|h2|h3|h4|h5|h6|h7|h8;

    public static final long NOT_H_FILE=~H_FILE;
    public static final long NOT_A_FILE=~A_FILE;
    public static final long NOT_GH_FILE=~(H_FILE|G_FILE);
    public static final long NOT_AB_FILE=~(A_FILE|B_FILE);

    //ranks
    public static final long RANK_1=a1|b1|c1|d1|e1|f1|g1|h1;
    public static final long RANK_2=a2|b2|c2|d2|e2|f2|g2|h2;
    public static final long RANK_3=a3|b3|c3|d3|e3|f3|g3|h3;
    public static final long RANK_4=a4|b4|c4|d4|e4|f4|g4|h4;
    public static final long RANK_5=a5|b5|c5|d5|e5|f5|g5|h5;
    public static final long RANK_6=a6|b6|c6|d6|e6|f6|g6|h6;
    public static final long RANK_7=a7|b7|c7|d7|e7|f7|g7|h7;
    public static final long RANK_8=a8|b8|c8|d8|e8|f8|g8|h8;

    //special
    public static final long LIGHT_SQUARES=0x55AA55AA55AA55AAL;
    public static final long DARK_SQUARES= ~LIGHT_SQUARES;
    public static final long BISHOP_TL_EDGE= A_FILE | RANK_8;
    public static final long BISHOP_TR_EDGE= H_FILE | RANK_8;
    public static final long BISHOP_BL_EDGE= A_FILE | RANK_1;
    public static final long BISHOP_BR_EDGE= H_FILE | RANK_1;
    public static final long ALL_EDGES=A_FILE | RANK_8 | H_FILE | RANK_1;
    public static final long NOT_EDGES=~ALL_EDGES;
    public static final long CORNERS= a1 | h1 | a8 | h8;

    public static final long whiteCanCSPieceMask = g1 | f1;
    public static final long whiteCanCSCheckMask = e1 | g1 | f1;
    public static final long whiteCanCLPieceMask= b1 | c1 | d1;
    public static final long whiteCanCLCheckMask= c1 | d1 | e1;

    public static final long blackCanCSPieceMask= g8 | f8;
    public static final long blackCanCSCheckMask= e8 | g8 | f8;
    public static final long blackCanCLPieceMask= b8 | c8 | d8;
    public static final long blackCanCLCheckMask= c8 | d8 | e8;


}
