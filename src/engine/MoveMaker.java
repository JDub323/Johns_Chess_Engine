package engine;

import gui.FrameHolder;
import gui.Graphical;
import position.CurrentPosition;
import position.Position;
import position.Type;

public class MoveMaker implements Runnable{
    Position pos;
    boolean playAsWhite;
    boolean makeMoves;
    int waitTimeMS;

    Evaluator evaluator = new Evaluator(20);//arbitrarily large depth number
    Thread bestMoveFinder;

    public MoveMaker(Position pos, boolean playAsWhite, int waitTimeMS, boolean makeMoves) {
        this.pos=pos;
        this.playAsWhite=playAsWhite;
        this.waitTimeMS=waitTimeMS;
        this.makeMoves=makeMoves;
    }

    private void startMoveSearch() {
        bestMoveFinder = new Thread(evaluator);
        bestMoveFinder.start();
    }

    private void makeBestMove(int move) {
        if (move != Type.illegalMove) {
            CurrentPosition.makeMove(move);
            pos.calculateLegalMoves();
        }
    }

    public boolean isPlaying() {
        return makeMoves;
    }

    @Override
    public void run() {
        if (makeMoves & (pos.whiteToMove==playAsWhite)) {
            Graphical.stopAllMoves=true;
            startMoveSearch();
            try {
                Thread.sleep(waitTimeMS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            bestMoveFinder.interrupt();
            makeBestMove(evaluator.bestMove);
            FrameHolder.chessGraphics.updateGraphics();
            Graphical.stopAllMoves=false;
            if (evaluator.bestMove != Type.illegalMove)CurrentPosition.updateMoveMakers();//don't try to make a move when the game is over
        }
    }
}
