package engine;

import chessUtilities.PrintColor;
import eval.StaticEval;
import move.Move;
import position.CurrentPosition;
import position.Position;
import position.Type;

import java.util.ArrayList;

public class Evaluator implements Runnable{//always analyzes the current position
    //TODO: when adding transposition tables, if a position found matches the previous position (including left hash), return a score of 0
    //transposition tables would break draws by repetition otherwise
    public int bestMove;
    public int bestEval;
    private final int MAX_DEPTH;
    public int[] principalVariation;

    //node types
    public static final byte TYPE_1 = 1;//table entry gives exact eval
    public static final byte TYPE_2 = 2;//table entry gives lower bound
    public static final byte TYPE_3 = 3;//table entry gives upper bound

    public Evaluator(int depth) {
        this.MAX_DEPTH = depth;
        principalVariation = new int[MAX_DEPTH];
    }

    @Override
    public void run() {
        //make a new position that is a copy of the old one
        Position pos = new Position(CurrentPosition.position.getFen());//probably too slow
        System.arraycopy(CurrentPosition.position.previousZobristKeys, 0, pos.previousZobristKeys, 0, pos.plyNumber);

        if (pos.plyNumber <= OpeningBook.OPENING_BOOK_LENGTH) {
            bestMove = pos.whiteToMove ? checkOpeningDatabase(pos.zobristKey, 0, OpeningBook.whiteOpeningBook.size(), OpeningBook.whiteOpeningBook) :
                    checkOpeningDatabase(pos.zobristKey,0, OpeningBook.blackOpeningBook.size(), OpeningBook.blackOpeningBook);
        }

        if (!pos.moveIsOnMoveList(bestMove)) {//asserts I don't make an illegal move
            bestMove = Type.illegalMove;
        }

        if (bestMove == Type.illegalMove && pos.gameState <= Type.endGame) {//no move was found in the database, the game still continues
            shiftPrincipalVariation();//use the pv from the previous search
            for (int i=1;i<=MAX_DEPTH; i++) {
                findBestMove(pos, i);
                if (Thread.interrupted()){
                    //System.out.println("Search made it to a depth of: "+i);
                    break;
                }
            }
            //printPrincipalVariation();
            //printEvaluation(pos.whiteToMove);
        }
        else if (pos.gameState > Type.endGame) {
            System.out.println("game has ended");
        }
    }

    //this level's position already has legal moves, but not in order
    public void findBestMove(Position pos, int depth){
        int alpha = Integer.MIN_VALUE+1;
        int beta = Integer.MAX_VALUE;

        pos.optimizeMoveOrder(principalVariation[0]);
        searchRoot(pos,depth,depth,alpha,beta);
        bestMove = principalVariation[0];
    }

    //takes input with legal moves, in order
    private void searchRoot(Position pos, int depthLeft, final int SEARCH_MAX_DEPTH, int alpha, int beta) {
        int[] localPV = new int[depthLeft];
        System.arraycopy(principalVariation,0,localPV,0,localPV.length);

        for (int i=pos.indexOfFirstEmptyMove-1; i>=0 ;i--) {
            int moveToMake = pos.legalMoves[i];

            pos.makeMove(moveToMake);
            int eval = -evaluatePosition(pos, depthLeft-1, SEARCH_MAX_DEPTH, 0,-beta, -alpha, localPV);
            pos.unmakeMove(moveToMake);

            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                break;
            }

            if (eval > alpha){
                localPV[0] = moveToMake;
                alpha = eval;
            }
        }

        if (!Thread.interrupted()) {
            System.arraycopy(localPV,0,principalVariation,0,depthLeft);
            TranspositionTable.tryAddingEntry(pos.zobristKey, alpha, localPV[0], (byte)depthLeft, TYPE_1);
            bestEval = alpha;
        }
        else {//the search was ended halfway
            Thread.currentThread().interrupt();
        }
    }

    //takes input of position without legal moves or moves ordered
    public int evaluatePosition(Position pos, int depthLeft, final int SEARCH_MAX_DEPTH, int searchDepth, int alpha, int beta, int[] parentPV) {
        if (depthLeft==0){
            if (!pos.inCheck || searchDepth >= SEARCH_MAX_DEPTH+4) {//arbitrary constant to limit check extensions forever
                return quiescenceEvaluation(pos,alpha,beta);
            }
            depthLeft++;//extend the search for checks
        }

        if (Thread.interrupted()) {
            Thread.currentThread().interrupt();
            return 0;
        }


        boolean nodeIsLikelyType1 = beta - alpha > 1;
        if (!nodeIsLikelyType1 && TranspositionTable.positionIsInTable(pos.zobristKey)){//check for this position on the transposition table to use that eval instead
            TranspositionTable.TableEntry temp = TranspositionTable.getTableEntry(pos.zobristKey);
            switch (temp.nodeType()) {
                case TYPE_1 -> {//only use depth that is equal to or greater than the current depth of the search
                    if (depthLeft <= temp.depth())return temp.eval();
                }
                case TYPE_2 -> {
                    if (depthLeft == temp.depth()){
                        beta = Math.min(beta, temp.eval());//set upper bound
                        if (alpha >= beta)return alpha;
                    }
                }
                case TYPE_3 -> {
                    if (depthLeft == temp.depth()){
                        alpha = Math.max(alpha, temp.eval());//set lower bound
                        if (alpha >= beta)return alpha;
                    }
                }
            }
        }

        pos.calculateLegalMoves();

        if (pos.gameState > Type.endGame) {//game has ended
            int eval;
            if (pos.gameState == Type.gameIsADraw) {
                eval = StaticEval.DRAW;
            }
            else {//always the worst possible position for the player to move in checkmate, so always the worst value
                eval = -StaticEval.CHECKMATE+SEARCH_MAX_DEPTH-depthLeft;
            }//add the distance from the root node so engine prefers faster checkmates

            TranspositionTable.tryAddingEntry(pos.zobristKey, eval, Type.illegalMove, (byte)depthLeft, TYPE_1);
            return eval;
        }

        pos.optimizeMoveOrder(principalVariation[searchDepth]);
        int[] localPV = new int[depthLeft];
        boolean evalExceededAlpha = false;
        boolean isFirstMove = true;

        for (int i=pos.indexOfFirstEmptyMove-1; i>=0;i--) {
            int moveToMake = pos.legalMoves[i];
            int eval;

            pos.makeMove(moveToMake);
            if (isFirstMove){//full window search for expected best move
                eval = -evaluatePosition(pos,depthLeft-1,SEARCH_MAX_DEPTH, searchDepth+1,-beta,-alpha, localPV);
                isFirstMove = false;
            }
            else {//zero window search with expected non-PV nodes, uses a faster and less precise search to find eval
                eval = -evaluatePosition(pos,depthLeft-1,SEARCH_MAX_DEPTH,searchDepth+1,-alpha - 1,-alpha, localPV);//replace -beta with -alpha -1
                if (eval > alpha && eval < beta) {//if the search actually found an increase in alpha
                    pos.indexOfFirstEmptyMove = 0;//resets the move list
                    eval = -evaluatePosition(pos,depthLeft-1,SEARCH_MAX_DEPTH, searchDepth+1,-beta,-alpha, localPV);
                }
            }
            pos.unmakeMove(moveToMake);

            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                return 0;
            }

            if (eval >= beta) {
                TranspositionTable.tryAddingEntry(pos.zobristKey, eval, moveToMake, (byte)depthLeft, TYPE_3);
                return beta;
            }
            if (eval > alpha) {
                alpha = eval;
                localPV[0] = moveToMake;
                evalExceededAlpha = true;
            }
        }

        byte nodeType = evalExceededAlpha? TYPE_1 : TYPE_2;
        TranspositionTable.tryAddingEntry(pos.zobristKey, alpha, localPV[0], (byte)depthLeft, nodeType);
        if (evalExceededAlpha) {
            System.arraycopy(localPV,0,parentPV,1, parentPV.length-1);//add PV to parent node's PV if there is an alpha raise
        }

        return alpha;
    }

    //takes input of position without legal moves or moves ordered
    private int quiescenceEvaluation(Position pos, int alpha, int beta) {
        if (Thread.interrupted()) {
            Thread.currentThread().interrupt();
            return 0;
        }

        int standingPat = StaticEval.evaluate(pos);
        if (standingPat >= beta) return beta;
        alpha = Math.max(alpha, standingPat);

        pos.calculateCapturingMovesOnly();
        pos.optimizeMoveOrder();
        for (int i=pos.indexOfFirstEmptyMove-1; i>=0 ;i--) {//keep recursion going until no more captures
            int moveEvaluating = pos.legalMoves[i];

            pos.makeMove(moveEvaluating);
            int eval = -quiescenceEvaluation(pos, -beta, -alpha);
            pos.unmakeMove(moveEvaluating);

            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                return 0;
            }

            if (eval >= beta)return beta;
            alpha = Math.max(alpha, eval);
        }
        return alpha;
    }

    public int checkOpeningDatabase(long zobristKey, int lowerBound, int upperBound, ArrayList<BookPosition> book) {
        while (lowerBound <= upperBound) {
            int indexToCheck = (upperBound + lowerBound)/2;
            long hashFound = book.get(indexToCheck).getHash();

            if (hashFound == zobristKey) {
                return book.get(indexToCheck).getRandomMove();
            }

            if (zobristKey > hashFound)lowerBound = indexToCheck+1;
            else upperBound = indexToCheck-1;
        }

        return Type.illegalMove;//no move was found
    }

    public void printPrincipalVariation() {
        int listLength = 0;
        for (int i : principalVariation) {
            if (i == Type.illegalMove) break;//pruning made the PV shorter than the depth
            listLength++;
        }

        System.out.print(PrintColor.RED+"Principal Variation: ");
        for (int i=0;i<listLength;i++) {
            System.out.print(Move.getStandardStringFromMove(principalVariation[i])+" ");
        }
        System.out.println(PrintColor.RESET);
    }

    private void shiftPrincipalVariation() {
        /*
        although not guaranteed that every position will have the new best moves in the new principal variation,
        the previously searched moves that were found to be good will probably be part of the new initial PV
         */
        for (int i=0; i<principalVariation.length-2; i++) {
            principalVariation[i] = principalVariation[i+2];
        }
    }

    private void printEvaluation(boolean playingAsWhite) {
        if (Math.abs(bestEval) >= StaticEval.CHECKMATE-MAX_DEPTH) {
            int movesBeforeCheckmate = (StaticEval.CHECKMATE-Math.abs(bestEval))/2;
            String evalString = "Mate in "+movesBeforeCheckmate;
            if (movesBeforeCheckmate != 0)System.out.println(evalString);
            else System.out.println("Checkmate");
        }
        else {
            if (playingAsWhite)System.out.println("Eval: "+bestEval);
            else {
                int printingEval = -bestEval;
                System.out.println("Eval: "+printingEval);
            }
        }
    }
}
