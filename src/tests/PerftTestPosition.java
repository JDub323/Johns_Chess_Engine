package tests;

import move.Move;
import position.Position;
import position.Type;
import position.Zobrist;

public class PerftTestPosition {
    private final String fen;
    private final byte MAX_DEPTH;
    private final long[] targetNodes;

    public PerftTestPosition(String fen, byte MAX_DEPTH, long[] targetNodes) {
        this.fen = fen;
        this.MAX_DEPTH = MAX_DEPTH;
        this.targetNodes = targetNodes;
    }

    public String getFen() {
        return fen;
    }

    public void testPosition() {
        for (int i=1; i<=MAX_DEPTH; i++) {
            testPosition(i);
        }
    }
    private void testPosition(int depth) {
        Position temp = new Position(fen);

        long startingTime = System.nanoTime();
        int nodesFound = testNumPositions(temp, depth);
        long duration = (System.nanoTime()-startingTime)/1000000;

        long correctZobristKey = Zobrist.getZobristKeyFromPosition(temp.whiteToMove,temp.castlingRights,temp.enPassantTargetFiles,temp.squareCentricPos);
        boolean testPassed = nodesFound==targetNodes[depth-1] && correctZobristKey == temp.zobristKey;

        if(testPassed)System.out.println("Nodes: "+targetNodes[depth-1]+" = "+nodesFound+" PASS  "+duration+" ms");
        else System.out.println("fen: "+fen+" Nodes: "+targetNodes[depth-1]+" = "+nodesFound+" FAIL");
    }

    public static int testNumPositions(Position pos, int depth) {
        if (depth==0)return 1;

        int numPositions=0;

        for (int i=0;i<pos.indexOfFirstEmptyMove;i++) {
            int moveToMake = pos.legalMoves[i];

            assert pos.squareCentricPos[Move.getFromSquareFromMove(moveToMake)] != Type.EMPTY :
                    "Position: "+pos.getFen()+" Move: "+Move.getFullStringFromMove(moveToMake)+" "+pos.allPositionRepresentationsAgree();

            pos.makeMove(moveToMake);
            pos.calculateLegalMoves();
            numPositions += testNumPositions(pos,depth-1);
            pos.unmakeMove(moveToMake);

        }
        return numPositions;
    }

    public long getMaxNodes() {
        return targetNodes[MAX_DEPTH-1];
    }

    public long getAllNodes() {
        long ret = 0;
        for (long n: targetNodes) {
            ret += n;
        }
        return ret;
    }
}
