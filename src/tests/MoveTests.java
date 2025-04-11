package tests;

import chessUtilities.Util;
import move.GenerateMagicBitBoards;
import move.PieceAttack;
import position.Position;
import move.Move;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import static chessUtilities.Util.PROJECT_PATH;

public class MoveTests {

    private static final ArrayList<PerftTestPosition> testPositions = new ArrayList<>();

    public static void main(String[] args) {
        PieceAttack.generateMoveArrays();
        GenerateMagicBitBoards.makeBitboardDatabase();
        initializeMoveTestArray();
        long startingTime = System.nanoTime();

        testPositions.get(1).testPosition();
        long sumOfAllNodes=0;
        for (PerftTestPosition position : testPositions) {
            sumOfAllNodes = position.getAllNodes();
            position.testPosition();
        }

        long duration = (System.nanoTime() -startingTime)/1000000;
        duration -= 184;//to account for the time it takes to make the new positions
        long nodesPerSecond= (sumOfAllNodes/duration) * 1000L;
        System.out.println("\nNodes Per Second: "+nodesPerSecond);
    }

    public static void testAllEtherealTestPositions() {

    }

    private static void initializeMoveTestArray() {
        File f = new File(PROJECT_PATH+"\\src\\tests\\EtherealTestPositions");
        Scanner a;
        try {
            a = new Scanner(f);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        while (a.hasNextLine()) {
            String line = a.nextLine();
            Scanner b = new Scanner(line);
            b.useDelimiter(" ;");

            String fen = b.next();

            ArrayList<Long> targetList = new ArrayList<>();
            while (b.hasNext()) {
                targetList.add(b.nextLong());
            }

            long[] returnList = new long[targetList.size()];

            for (int i=0; i<targetList.size();i++) {
                returnList[i] = targetList.get(i);
            }

            testPositions.add(new PerftTestPosition(fen,(byte)targetList.size(),returnList));
        }


    }





    public static long getToSquaresFromMoveList(Position pos, int tempFromSquare) {
        long ret=0;
        for (int i=0;i<pos.indexOfFirstEmptyMove;i++) {
            if (Move.getFromSquareFromMove(pos.legalMoves[i]) == tempFromSquare){
                ret|= Util.toBitboard(Move.getToSquareFromMove(pos.legalMoves[i]));
            }
        }
        return ret;
    }
    public static boolean pieceListsAreAccurate(Position pos) {
        Position pos2 = new Position(pos.getFen());

        for (int i=1;i<7;i++) {
            for (int color = 0; color<=8; color+=8) {
                if (pos.numPieces[color | i] != pos2.numPieces[color | i])return false;

                int sum1=0,sum2=0;

                for (int j=0;j<pos.numPieces[color | i];j++) {
                    sum1+=pos.pieceSquareList[color | i][j];
                    sum2+=pos2.pieceSquareList[color | i][j];
                }
                if (sum1 != sum2)return false;
            }
        }

        return true;
    }
}
