package move;

public class Move {
    //private static final int moveTypeMask  = 0xFF000000;  no need to mask since the bits are deleted on right shift
    private static final int fromSquareMask  = 0x00FF0000;
    private static final int toSquareMask    = 0x0000FF00;
    private static final int capturedPieceMask=0x000000FF;
    public static final int fromSquareToSquareMask = 0x00FFFF00;

    public static void printMoveInStandardNotation(int move) {
        System.out.print(getStringFromMove(move));
    }
    public static String getStringFromMove(int move) {
        byte fromSquare = getFromSquareFromMove(move);
        byte toSquare = getToSquareFromMove(move);
        byte capturedPiece = getCapturedPieceFromMove(move);
        byte moveType = getMoveTypeFromMove(move);
        return giveSquareAsStringFromByte(fromSquare) + giveSquareAsStringFromByte(toSquare)+" c"+capturedPiece+" t"+moveType;
    }
    public static String giveSquareAsStringFromByte(byte square) {
        String rank =""+(1+(square/8));
        String file = switch (square%8) {
            case 0-> "a";
            case 1-> "b";
            case 2-> "c";
            case 3-> "d";
            case 4-> "e";
            case 5-> "f";
            case 6-> "g";
            case 7-> "h";
            default -> "error";
        };

        return file+rank;
    }
    private static byte giveSquareAsByteFromString(String s) {
        String file = s.substring(0,1);
        String rank = s.substring(1,2);

        int fileNum = switch (file) {
            case "a" -> fileNum = 0;
            case "b" -> fileNum = 1;
            case "c" -> fileNum = 2;
            case "d" -> fileNum = 3;
            case "e" -> fileNum = 4;
            case "f" -> fileNum = 5;
            case "g" -> fileNum = 6;
            default  -> fileNum = 7;
        };
        int rankNum = Integer.parseInt(rank)-1;

        return (byte)(fileNum+rankNum*8);
    }


    public static byte getMoveTypeFromMove(int move) {
        return (byte)(move>>>24);
    }
    public static byte getFromSquareFromMove(int move) {
        return (byte)((move & fromSquareMask)>>>16);
    }
    public static byte getToSquareFromMove(int move) {
        return (byte)((move & toSquareMask)>>>8);
    }
    public static byte getCapturedPieceFromMove(int move) {
        return (byte)(move & capturedPieceMask);
    }
    public static int makeMoveFromBytes(byte moveType, byte fromSquare, byte toSquare, byte capturedPiece) {
        return moveType<<24 | fromSquare<<16 | toSquare<<8 | capturedPiece;
    }
    public static int makeMoveFromString(String fromSquareToSquareStr, byte moveType, byte capturedPiece) {
        String fromSquareStr = fromSquareToSquareStr.substring(0,2);
        String toSquareStr = fromSquareToSquareStr.substring(2,4);

        byte fromSquare = giveSquareAsByteFromString(fromSquareStr);
        byte toSquare = giveSquareAsByteFromString(toSquareStr);

        return moveType<<24 | fromSquare<<16 | toSquare<<8 | capturedPiece;
    }
}
