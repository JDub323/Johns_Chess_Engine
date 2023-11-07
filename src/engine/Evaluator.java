package engine;

import eval.StaticEval;
import move.Move;
import position.CurrentPosition;
import position.Position;
import position.Type;

public class Evaluator implements Runnable{//always analyzes the current position

    public int bestMove;
    public int bestEval;

    @Override
    public void run() {
        //make a new position that is a copy of the old one
        Position pos= new Position(CurrentPosition.position.getFen());//probably too slow, def a way to make faster

        //in the future, would like to make time constant and variable depth
        //right now this is the other way around
        //TODO: implement iterative deepening
        findBestMove(pos,3);
    }

    //this level's position already has legal moves, but not in order
    public void findBestMove(Position pos, int depth){
        //pos.optimizeMoveOrder();
        int alpha = Integer.MIN_VALUE+1;
        int beta = Integer.MAX_VALUE-1;

        bestMove = searchRoot(pos,depth,alpha,beta);
    }

    //takes input with legal moves, in order
    private int searchRoot(Position pos, int depth, int alpha, int beta) {
        //root node, returns a move instead of an evaluation, prints the evaluation
        bestEval = Integer.MIN_VALUE;//careful about overflow

        int floatingBestMove = Type.illegalMove;

        if (pos.gameState > Type.endGame) {//game is over, so return an empty move
            return floatingBestMove;
        }

        for (int i=pos.indexOfFirstEmptyMove-1; i>=0 ;i--) {
            int moveToMake = pos.legalMoves[i];

            pos.makeMove(moveToMake);
            int eval = -evaluatePosition(pos, depth, -beta, -alpha);
            pos.unmakeMove(moveToMake);

            if (eval > bestEval){
                floatingBestMove = moveToMake;
                bestEval = eval;
            }
            alpha = Math.max(alpha,eval);
        }
        return floatingBestMove;
    }

    //takes input of position without legal moves or moves ordered
    public int evaluatePosition(Position pos, int depth, int alpha, int beta) {
        if (depth==0) {
            return quiescenceEvaluation(pos,alpha,beta);
        }

        pos.calculateLegalMoves();

        if (pos.gameState > Type.endGame) {//game has ended
            if (pos.gameState == Type.gameIsADraw)return 0;
            return Integer.MIN_VALUE+1;//always the worst possible position for the player to move in checkmate, so always the worst value
        }//be careful about integer overflow errors with this. add one to be safe

        //pos.optimizeMoveOrder();
        for (int i=pos.indexOfFirstEmptyMove-1; i>=0 ;i--) {
            int moveToMake = pos.legalMoves[i];

            pos.makeMove(moveToMake);
            int eval = -evaluatePosition(pos,depth-1,-beta,-alpha);
            pos.unmakeMove(moveToMake);

            if (eval >= beta) {
                return beta;
            }
            alpha = Math.max(alpha, eval);
        }
        return alpha;
    }

    //takes input of position without legal moves or moves ordered
    private int quiescenceEvaluation(Position pos, int alpha, int beta) {
        int standingPat = StaticEval.evaluate(pos);
        if (standingPat >= beta) return beta;
        alpha = Math.max(alpha, standingPat);

        pos.calculateCapturingMovesOnly();
        //pos.optimizeMoveOrder();
        for (int i=pos.indexOfFirstEmptyMove-1; i>=0 ;i--) {//keep recursion going until no more captures
            int moveEvaluating = pos.legalMoves[i];

            pos.makeMove(moveEvaluating);
            int eval = -quiescenceEvaluation(pos, -beta, -alpha);
            pos.unmakeMove(moveEvaluating);

            if (eval >= beta)return beta;
            alpha = Math.max(alpha, eval);
        }
        return alpha;
    }
}
