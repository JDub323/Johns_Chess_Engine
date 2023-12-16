package move;

import position.Position;
import position.Type;

import static position.Type.Queen;
import static position.Type.normalMove;

public class Move {
    //private static final int moveTypeMask  = 0xFF000000;  no need to mask since the bits are deleted on right shift
    private static final int fromSquareMask  = 0x00FF0000;
    private static final int toSquareMask    = 0x0000FF00;
    private static final int capturedPieceMask=0x000000FF;
    public static final int fromSquareToSquareMask = 0x00FFFF00;

    public static void printMoveInStandardNotation(int move) {
        System.out.print(getStandardStringFromMove(move));
    }
    public static String getStandardStringFromMove(int move) {
        byte fromSquare = getFromSquareFromMove(move);
        byte toSquare = getToSquareFromMove(move);
        byte moveType = getMoveTypeFromMove(move);

        if (moveType < Type.pawnPromotesToQ){//no pawn promotion
            return giveSquareAsStringFromByte(fromSquare) + giveSquareAsStringFromByte(toSquare);
        }

        String moveTypeString = switch (moveType) {
            case Type.pawnPromotesToQ-> "q";
            case Type.pawnPromotesToN-> "n";
            case Type.pawnPromotesToB-> "b";
            case Type.pawnPromotesToR-> "r";
            default -> "error";
        };

        return giveSquareAsStringFromByte(fromSquare) + giveSquareAsStringFromByte(toSquare) + moveTypeString;//specify which pawn promotion
    }
    public static String getFullStringFromMove(int move) {
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

    public static boolean squaresOfMovesAreEqual(int move1, int move2) {
        if (getFromSquareFromMove(move1) == getFromSquareFromMove(move2)) {
            return getToSquareFromMove(move1) == getToSquareFromMove(move2);
        }
        return false;
    }

    public static int getMoveFromFancyMove(String fancyMove, Position pos) {//pos must have had generated moves already
        String letter1 = fancyMove.substring(0,1);

        if (letter1.equals("O")) {//is castles
            boolean isShortCastles = fancyMove.length() == 3;//COULD BE WRONG, CHECK THIS WHEN DEBUGGING
            int fromSquare, toSquare;
            if (pos.whiteToMove) {
                fromSquare = 4;
                toSquare = isShortCastles ? 6 : 2;
            }
            else {
                fromSquare = 60;
                toSquare = isShortCastles ? 62 : 58;
            }

            return Move.makeMoveFromBytes(Type.castles,(byte)fromSquare, (byte)toSquare, (byte)0);
        }
        else if (letter1.equals(letter1.toUpperCase())){//means it is a capital letter, so it isn't a pawn move
            String empty = "";
            fancyMove = fancyMove.replaceAll("x",empty).replaceAll("\\+",empty).replaceAll("#",empty);

            String toSquareString = fancyMove.substring(fancyMove.length()-2);
            byte toSquare = giveSquareAsByteFromString(toSquareString);
            int colorMoving = pos.whiteToMove ? 0 : 8;

            byte pieceMoving = Type.Empty;

            switch (letter1) {
                case "N" -> pieceMoving = Type.Knight;
                case "B" -> pieceMoving = Type.Bishop;
                case "R" -> pieceMoving = Type.Rook;
                case "Q" -> pieceMoving = Type.Queen;
                case "K" -> pieceMoving = Type.King;
            }

            boolean isFileSpecification = false;
            boolean isRankSpecification = false;
            int specifyingNum = 0;
            if (fancyMove.length()==4) {//there is a file rank specification
                try {
                    specifyingNum = Integer.parseInt(fancyMove.substring(1,2))-1;
                    isRankSpecification = true;
                }catch (NumberFormatException e) {
                    specifyingNum = switch (fancyMove.substring(1,2)) {
                        case "a" -> 0;
                        case "b" -> 1;
                        case "c" -> 2;
                        case "d" -> 3;
                        case "e" -> 4;
                        case "f" -> 5;
                        case "g" -> 6;
                        case "h" -> 7;
                        default -> throw new IllegalStateException("Unexpected value: " + fancyMove.charAt(1));//TODO: make this not get called
                    };
                    isFileSpecification = true;
                }
            }
            for (int i=0; i<pos.numPieces[colorMoving | pieceMoving]; i++) {//does not work with more than two pieces per type, but I'm lazy

                byte fromSquare = pos.pieceSquareList[colorMoving | pieceMoving][i];
                int possibleMove = Move.makeMoveFromBytes(Type.normalMove,fromSquare,toSquare,pos.squareCentricPos[toSquare]);

                if (isFileSpecification && fromSquare%8 != specifyingNum)continue;
                if (isRankSpecification && fromSquare/8 != specifyingNum)continue;

                if (pos.moveIsOnMoveList(possibleMove)){
                    return possibleMove;
                }
            }
        }
        else {//is a pawn move
            if (fancyMove.contains("x")){//capture
                int directionAdjustment = pos.whiteToMove ? -1 : 1;
                byte toSquare = giveSquareAsByteFromString(fancyMove.substring(2,4));
                int fromRank = toSquare/8+1+directionAdjustment;
                byte fromSquare = giveSquareAsByteFromString(fancyMove.substring(0,1)+fromRank);

                //first and last parameters don't matter since I only check the squares
                int possibleMove = makeMoveFromBytes(normalMove,fromSquare,toSquare,Type.Empty);
                for (int i=0; i<pos.indexOfFirstEmptyMove; i++) {
                    if (squaresOfMovesAreEqual(pos.legalMoves[i],possibleMove))return pos.legalMoves[i];
                }
            }
            else {//not a capture
                int toSquare = giveSquareAsByteFromString(fancyMove.substring(0,2));

                for (int i=0; i<pos.indexOfFirstEmptyMove; i++) {
                    if (getToSquareFromMove(pos.legalMoves[i]) == toSquare)return pos.legalMoves[i];
                }
            }
        }

        //throw new IllegalStateException("Move not found: "+fancyMove);
        return -1;
    }
}
