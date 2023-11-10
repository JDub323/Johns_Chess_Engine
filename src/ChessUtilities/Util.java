package ChessUtilities;

import move.Move;
import position.Type;

public class Util {


    public static byte getPieceFromSquareWithBB (int square, long[] pa) {
        long squareBB=toBitboard(square);
        if ((squareBB|pa[1])==pa[1])return Type.White| Type.Pawn;
        else if ((squareBB|pa[2])==pa[2])return Type.White| Type.Knight;
        else if ((squareBB|pa[3])==pa[3])return Type.White| Type.Bishop;
        else if ((squareBB|pa[4])==pa[4])return Type.White| Type.Rook;
        else if ((squareBB|pa[5])==pa[5])return Type.White| Type.Queen;
        else if ((squareBB|pa[6])==pa[6])return Type.White| Type.King;
        else if ((squareBB|pa[9])==pa[9])return Type.Black| Type.Pawn;
        else if ((squareBB|pa[10])==pa[10])return Type.Black| Type.Knight;
        else if ((squareBB|pa[11])==pa[11])return Type.Black| Type.Bishop;
        else if ((squareBB|pa[12])==pa[12])return Type.Black| Type.Rook;
        else if ((squareBB|pa[13])==pa[13])return Type.Black| Type.Queen;
        else if ((squareBB|pa[14])==pa[14])return Type.Black| Type.King;
        else return Type.Empty;
    }

    public static byte getPieceFromString(String str) {
        return switch (str) {
            case "P" -> Type.White | Type.Pawn;
            case "p" -> Type.Black | Type.Pawn;
            case "N" -> Type.White | Type.Knight;
            case "n" -> Type.Black | Type.Knight;
            case "B" -> Type.White | Type.Bishop;
            case "b" -> Type.Black | Type.Bishop;
            case "R" -> Type.White | Type.Rook;
            case "r" -> Type.Black | Type.Rook;
            case "Q" -> Type.White | Type.Queen;
            case "q" -> Type.Black | Type.Queen;
            case "K" -> Type.White | Type.King;
            case "k" -> Type.Black | Type.King;
            default -> -1;
        };
    }

    public static String getPieceStringFromShort(short piece) {//assume not empty
        switch (piece) {
            case Type.White | Type.Pawn -> {
                return "P";
            }
            case Type.White | Type.Knight -> {
                return "N";
            }
            case Type.White | Type.Bishop -> {
                return "B";
            }
            case Type.White | Type.Rook -> {
                return "R";
            }
            case Type.White | Type.Queen -> {
                return "Q";
            }
            case Type.White | Type.King -> {
                return "K";
            }
            case Type.Black | Type.Pawn -> {
                return "p";
            }
            case Type.Black | Type.Knight -> {
                return "n";
            }
            case Type.Black | Type.Bishop -> {
                return "b";
            }
            case Type.Black | Type.Rook -> {
                return "r";
            }
            case Type.Black | Type.Queen -> {
                return "q";
            }
            case Type.Black | Type.King -> {
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

    public static void printMoveArray(int[] moveArray) {
        for (int j : moveArray) {
            System.out.print(Move.getStringFromMove(j) + " | ");
        }
        System.out.println();
    }

}
