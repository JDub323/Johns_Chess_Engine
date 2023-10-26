package tests;

import position.Position;
import position.CurrentPosition;
import move.Move;

public class MoveTests {

    public static void testAllTestPositions() {
        long startingTime = System.nanoTime();

        testTestPosition("r6r/1b2k1bq/8/8/7B/8/8/R3K2R b KQ - 3 2", 1, 8, 1);
        long duration = (System.nanoTime()-startingTime)/1000000;
        System.out.println(duration + "ms");

        testTestPosition("8/8/8/2k5/2pP4/8/B7/4K3 b - d3 0 3", 1, 8,2);
        duration = (System.nanoTime()-startingTime)/1000000;
        System.out.println(duration + "ms");

        testTestPosition("r1bqkbnr/pppppppp/n7/8/8/P7/1PPPPPPP/RNBQKBNR w KQkq - 2 2", 1, 19,3);
        duration = (System.nanoTime()-startingTime)/1000000;
        System.out.println(duration + "ms");

        testTestPosition("r3k2r/p1pp1pb1/bn2Qnp1/2qPN3/1p2P3/2N5/PPPBBPPP/R3K2R b KQkq - 3 2", 1, 5,4);
        duration = (System.nanoTime()-startingTime)/1000000;
        System.out.println(duration + "ms");

        testTestPosition("2kr3r/p1ppqpb1/bn2Qnp1/3PN3/1p2P3/2N5/PPPBBPPP/R3K2R b KQ - 3 2", 1, 44,5);
        duration = (System.nanoTime()-startingTime)/1000000;
        System.out.println(duration + "ms");

        testTestPosition("rnb2k1r/pp1Pbppp/2p5/q7/2B5/8/PPPQNnPP/RNB1K2R w KQ - 3 9", 1, 39,6);
        duration = (System.nanoTime()-startingTime)/1000000;
        System.out.println(duration + "ms");

        testTestPosition("2r5/3pk3/8/2P5/8/2K5/8/8 w - - 5 4", 1, 9,7);
        duration = (System.nanoTime()-startingTime)/1000000;
        System.out.println(duration + "ms");

        testTestPosition("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 3, 62379,8);
        duration = (System.nanoTime()-startingTime)/1000000;
        System.out.println(duration + "ms");

        testTestPosition("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 0", 3, 89890,9);
        duration = (System.nanoTime()-startingTime)/1000000;
        System.out.println(duration + "ms");

        testTestPosition("3k4/3p4/8/K1P4r/8/8/8/8 b - - 0 1", 6, 1134888,10);
        duration = (System.nanoTime()-startingTime)/1000000;
        System.out.println(duration + "ms");

        testTestPosition("8/8/4k3/8/2p5/8/B2P2K1/8 w - - 0 1", 6, 1015133,11);
        duration = (System.nanoTime()-startingTime)/1000000;
        System.out.println(duration + "ms");

        testTestPosition("8/8/1k6/2b5/2pP4/8/5K2/8 b - d3 0 1", 6, 1440467,12);
        duration = (System.nanoTime()-startingTime)/1000000;
        System.out.println(duration + "ms");

        testTestPosition("5k2/8/8/8/8/8/8/4K2R w K - 0 1", 6, 661072,13);
        duration = (System.nanoTime()-startingTime)/1000000;
        System.out.println(duration + "ms");

        testTestPosition("3k4/8/8/8/8/8/8/R3K3 w Q - 0 1", 6, 803711,14);
        duration = (System.nanoTime()-startingTime)/1000000;
        System.out.println(duration + "ms");

        testTestPosition("r3k2r/1b4bq/8/8/8/8/7B/R3K2R w KQkq - 0 1", 4, 1274206,15);
        duration = (System.nanoTime()-startingTime)/1000000;
        System.out.println(duration + "ms");

        testTestPosition("r3k2r/8/3Q4/8/8/5q2/8/R3K2R b KQkq - 0 1", 4, 1720476,16);
        duration = (System.nanoTime()-startingTime)/1000000;
        System.out.println(duration + "ms");

        testTestPosition("2K2r2/4P3/8/8/8/8/8/3k4 w - - 0 1", 6, 3821001,17);
        duration = (System.nanoTime()-startingTime)/1000000;
        System.out.println(duration + "ms");

        testTestPosition("8/8/1P2K3/8/2n5/1q6/8/5k2 b - - 0 1", 5, 1004658,18);
        duration = (System.nanoTime()-startingTime)/1000000;
        System.out.println(duration + "ms");

        testTestPosition("4k3/1P6/8/8/8/8/K7/8 w - - 0 1", 6, 217342,19);
        duration = (System.nanoTime()-startingTime)/1000000;
        System.out.println(duration + "ms");

        testTestPosition("8/P1k5/K7/8/8/8/8/8 w - - 0 1", 6, 92683,20);
        duration = (System.nanoTime()-startingTime)/1000000;
        System.out.println(duration + "ms");

        testTestPosition("K1k5/8/P7/8/8/8/8/8 w - - 0 1", 6, 2217,21);
        duration = (System.nanoTime()-startingTime)/1000000;
        System.out.println(duration + "ms");

        testTestPosition("8/k1P5/8/1K6/8/8/8/8 w - - 0 1", 7, 567584,22);
        duration = (System.nanoTime()-startingTime)/1000000;
        System.out.println(duration + "ms");

        testTestPosition("8/8/2k5/5q2/5n2/8/5K2/8 b - - 0 1", 4, 23527,23);
        duration = (System.nanoTime()-startingTime)/1000000;
        System.out.println(duration + "ms");

        int totalNodes = 13931366;
        long nodesPerSecond= totalNodes* 1000L /(duration);
        System.out.println("\nNodes Per Second: "+nodesPerSecond);
    }
    public static void testInitialPosition(int depth) {
        Position temp = new Position("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        long startingTime = System.nanoTime();
        long duration;

        for (int i=0;i<=depth;i++) {
            testNumPositions(temp,i);
            int nodesFound = MoveTests.testNumPositions(temp, i);
            boolean testPassed = nodesFound==targetNodes[i];

            if(testPassed)System.out.print("Depth: "+i+" Nodes: "+targetNodes[i]+" = "+nodesFound+" PASS  ");
            else System.out.print("Depth: "+i+" Nodes: "+targetNodes[i]+" = "+nodesFound+" FAIL  ");

            duration = (System.nanoTime()-startingTime)/1000000;
            System.out.println(duration + "ms");
        }

        long totalNodes = sumOfNodes(depth);
        duration = (System.nanoTime()-startingTime)/1000000;
        long nodesPerSecond= totalNodes* 1000L /(duration);
        System.out.println("\nNodes Per Second: "+nodesPerSecond);
    }

    private static final long[] targetNodes = {1,20,400,8902,197281,4865609,119060324,3195901860L,84998978956L,2439530234167L,69352859712417L};



    public static long getToSquaresFromMoveList(Position pos, int tempFromSquare) {
        long ret=0;
        for (int i=0;i<pos.indexOfFirstEmptyMove;i++) {
            if (Move.getFromSquareFromMove(pos.legalMoves[i]) == tempFromSquare){
                ret|=toBitboard(Move.getToSquareFromMove(pos.legalMoves[i]));
            }
        }
        return ret;
    }

    public static int testNumPositions(Position pos, int depth) {
        if (depth==0)return 1;

        int numPositions=0;

        for (int i=0;i<pos.indexOfFirstEmptyMove;i++) {

            pos.makeMove(pos.legalMoves[i]);
            numPositions += testNumPositions(pos,depth-1);
            pos.unmakeMove( pos.PreviousMadeMoves.pop() );
        }
        return numPositions;
    }

    public static void testMovesAndShowNodes(Position pos, int depth) {
        int workingTotal=0;
        for (int i = 0; i<pos.indexOfFirstEmptyMove; i++) {

            int testingMove = pos.legalMoves[i];
            Move.printMoveInStandardNotation(testingMove);
            pos.makeMove(testingMove);
            int nodes = MoveTests.testNumPositions(pos, depth-1);
            System.out.println("----"+nodes);
            workingTotal+=nodes;
            pos.unmakeMove( pos.PreviousMadeMoves.pop() );
        }
        System.out.println("Total nodes searched: "+workingTotal);
    }
    public static void testTestPositionAndShowNodes(String fen, int depth) {
        Position pos = new Position (fen);
        testMovesAndShowNodes(pos,depth);
        CurrentPosition.position = pos;
    }
    public static void testTestPositionAndShowNodes(String fen, int depth, int moveToMake) {
        Position pos = new Position (fen);
        pos.makeMove(moveToMake);
        testMovesAndShowNodes(pos,depth);
        System.out.print("New Fen: ");
        pos.printFen();
        CurrentPosition.position = pos;
    }

    public static void testMoves(Position pos, int ply) {
        for (int i=0;i<=ply;i++) {
            System.out.println("Depth "+i+":"+ MoveTests.testNumPositions(pos, i));
        }
    }

    public static void testTestPosition(String fen, int depth, int targetNodes, int moveTestNumber) {
        Position temp = new Position(fen);
        int nodesFound = MoveTests.testNumPositions(temp, depth);
        boolean testPassed = nodesFound==targetNodes;

        if(testPassed)System.out.print("MoveTest "+moveTestNumber+" Nodes: "+targetNodes+" = "+nodesFound+" PASS  ");
        else System.out.print("fen: "+fen+" Nodes: "+targetNodes+" = "+nodesFound+" FAIL  ");
    }




    private static long sumOfNodes(int maxNodeIndex) {
        if (maxNodeIndex==0) return targetNodes[0];
        return targetNodes[maxNodeIndex] + sumOfNodes(maxNodeIndex-1);
    }
    private static long toBitboard(int square) {
        return 1L<<square;
    }
}