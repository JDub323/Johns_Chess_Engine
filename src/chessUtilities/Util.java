package chessUtilities;

import position.Type;

public class Util {


    public static byte getPieceFromSquareWithBB (int square, long[] pa) {
        long squareBB=toBitboard(square);
        if ((squareBB|pa[1])==pa[1])return Type.WHITE | Type.PAWN;
        else if ((squareBB|pa[2])==pa[2])return Type.WHITE | Type.KNIGHT;
        else if ((squareBB|pa[3])==pa[3])return Type.WHITE | Type.BISHOP;
        else if ((squareBB|pa[4])==pa[4])return Type.WHITE | Type.ROOK;
        else if ((squareBB|pa[5])==pa[5])return Type.WHITE | Type.QUEEN;
        else if ((squareBB|pa[6])==pa[6])return Type.WHITE | Type.KING;
        else if ((squareBB|pa[9])==pa[9])return Type.BLACK | Type.PAWN;
        else if ((squareBB|pa[10])==pa[10])return Type.BLACK | Type.KNIGHT;
        else if ((squareBB|pa[11])==pa[11])return Type.BLACK | Type.BISHOP;
        else if ((squareBB|pa[12])==pa[12])return Type.BLACK | Type.ROOK;
        else if ((squareBB|pa[13])==pa[13])return Type.BLACK | Type.QUEEN;
        else if ((squareBB|pa[14])==pa[14])return Type.BLACK | Type.KING;
        else return Type.EMPTY;
    }

    public static byte getPieceFromString(String str) {
        return switch (str) {
            case "P" -> Type.WHITE | Type.PAWN;
            case "p" -> Type.BLACK | Type.PAWN;
            case "N" -> Type.WHITE | Type.KNIGHT;
            case "n" -> Type.BLACK | Type.KNIGHT;
            case "B" -> Type.WHITE | Type.BISHOP;
            case "b" -> Type.BLACK | Type.BISHOP;
            case "R" -> Type.WHITE | Type.ROOK;
            case "r" -> Type.BLACK | Type.ROOK;
            case "Q" -> Type.WHITE | Type.QUEEN;
            case "q" -> Type.BLACK | Type.QUEEN;
            case "K" -> Type.WHITE | Type.KING;
            case "k" -> Type.BLACK | Type.KING;
            default -> -1;
        };
    }

    public static String getPieceStringFromShort(short piece) {//assume not empty
        switch (piece) {
            case Type.WHITE | Type.PAWN -> {
                return "P";
            }
            case Type.WHITE | Type.KNIGHT -> {
                return "N";
            }
            case Type.WHITE | Type.BISHOP -> {
                return "B";
            }
            case Type.WHITE | Type.ROOK -> {
                return "R";
            }
            case Type.WHITE | Type.QUEEN -> {
                return "Q";
            }
            case Type.WHITE | Type.KING -> {
                return "K";
            }
            case Type.BLACK | Type.PAWN -> {
                return "p";
            }
            case Type.BLACK | Type.KNIGHT -> {
                return "n";
            }
            case Type.BLACK | Type.BISHOP -> {
                return "b";
            }
            case Type.BLACK | Type.ROOK -> {
                return "r";
            }
            case Type.BLACK | Type.QUEEN -> {
                return "q";
            }
            case Type.BLACK | Type.KING -> {
                return "k";
            }
        }
        return "invalidInputError";
    }

    public static int[] cloneArray(int[] input, int indexOfFirstEmptyMove) {
        int[] ret = new int[218];//I only clone the move list, so is always this size
        System.arraycopy(input, 0, ret, 0,indexOfFirstEmptyMove);
        return ret;
    }

    public static long toBitboard(int x) {return 1L<<x;}

    public static int kingDistanceBetween(int a, int b) {
        int fileDistance = Math.abs(a%8 - b%8);
        int rankDistance = Math.abs(a/8 - b/8);
        return Math.max(fileDistance, rankDistance);
    }

    public static int manhattanDistanceFromCenter(int s) {
        int file = s%8;
        int rank = s/8;

        int fileDistance = Math.max(3-file,file-4);
        int rankDistance = Math.max(3-rank,rank-4);

        return fileDistance + rankDistance;
    }

    public static void printArray(byte[] array) {
        for (int j : array) {
            System.out.print(j+", ");
        }
        System.out.println();
    }

}
