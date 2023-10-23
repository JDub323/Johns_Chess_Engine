package engine;

import gui.FrameHolder;
import gui.Graphical;
import position.CurrentPosition;
import position.Position;
import move.Move;

public class MoveMaker implements Runnable{
    Position pos;
    boolean playAsWhite;
    boolean makeMoves;
    int waitTimeMS;

    int indexOfBestMove;

    public MoveMaker(Position pos, boolean playAsWhite, int waitTimeMS, boolean makeMoves) {
        this.pos=pos;
        this.playAsWhite=playAsWhite;
        this.waitTimeMS=waitTimeMS;
        this.makeMoves=makeMoves;
    }

    public int findBestMove() {
        indexOfBestMove = (int)(Math.random()*pos.indexOfFirstEmptyMove);
        return pos.legalMoves[indexOfBestMove];
    }

    public void makeBestMove(int move) {
        pos.makeMove(move);
    }

    public boolean isPlaying() {
        return makeMoves;
    }

    @Override
    public void run() {
        if (makeMoves & (pos.whiteToMove==playAsWhite)) {
            Graphical.stopAllMoves=true;
            try {
                Thread.sleep(waitTimeMS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            makeBestMove(findBestMove());
            FrameHolder.chessGraphics.updateGraphics();
            Graphical.stopAllMoves=false;
            CurrentPosition.updateMoveMakers();
        }
    }
}
