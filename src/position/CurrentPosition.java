package position;

import engine.MoveMaker;
import gui.Graphical;

public class CurrentPosition {
    public static Position position;
    public static MoveMaker whiteMoveMaker, blackMoveMaker;
    public static Thread whiteMoveMakerThread, blackMoveMakerThread;
    public static boolean playerPlaysForWhite, playerPlaysForBlack;

    public static void InitializePosition(String fen,boolean playerPlaysWhite, boolean playerPlaysBlack, int engineWaitTimeMS) {
        position = new Position(fen);

        System.arraycopy(position.squareCentricPos, 0, Graphical.graphicSquareCentricPos, 0, 64);

        playerPlaysForWhite=playerPlaysWhite;
        playerPlaysForBlack=playerPlaysBlack;
        whiteMoveMaker = new MoveMaker(CurrentPosition.position,true,engineWaitTimeMS, !playerPlaysWhite);
        blackMoveMaker = new MoveMaker(CurrentPosition.position,false,engineWaitTimeMS, !playerPlaysBlack);
    }

    public static void updateMoveMakers(){
        whiteMoveMakerThread = new Thread(whiteMoveMaker);
        whiteMoveMakerThread.start();
        blackMoveMakerThread = new Thread(blackMoveMaker);
        blackMoveMakerThread.start();
    }
    public static boolean botPlaysForColorToMove() {
        if (position.whiteToMove)return whiteMoveMaker.isPlaying();
        else return blackMoveMaker.isPlaying();
    }
}
