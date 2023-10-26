package engine;

import gui.FrameHolder;
import gui.Graphical;
import position.CurrentPosition;
import position.Position;

public class MoveMaker implements Runnable{
    Position pos;
    boolean playAsWhite;
    boolean makeMoves;
    int waitTimeMS;

    Evaluator evaluator;
    Thread bestMoveFinder;

    public MoveMaker(Position pos, boolean playAsWhite, int waitTimeMS, boolean makeMoves) {
        this.pos=pos;
        this.playAsWhite=playAsWhite;
        this.waitTimeMS=waitTimeMS;
        this.makeMoves=makeMoves;
    }

    private void startMoveSearch() {
        evaluator = new Evaluator();
        bestMoveFinder = new Thread(evaluator);
        bestMoveFinder.start();
    }

    private void makeBestMove(int move) {
        pos.makeMove(move);
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
            makeBestMove(evaluator.bestMove);
            FrameHolder.chessGraphics.updateGraphics();
            Graphical.stopAllMoves=false;
            CurrentPosition.updateMoveMakers();
        }
    }
}
