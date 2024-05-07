package engine;

import chessUtilities.PositionFens;
import move.GenerateMagicBitBoards;
import move.PieceAttack;
import position.CurrentPosition;


public class MainEngine {

    private static boolean playerPlaysWhite, playerPlaysBlack;
    private static int thinkTimeMS;

    public static void startGame(boolean ppw, boolean ppb, int thinkTime) {
        playerPlaysWhite = ppw;//since this method can only be called once, variables are instantiated here
        playerPlaysBlack = ppb;
        thinkTimeMS = thinkTime;
        PieceAttack.generateMoveArrays();
        GenerateMagicBitBoards.makeBitboardDatabase();
        OpeningBook.initializeBookLists();
        gui.FrameHolder.makeFrame();
        startPosition();
        CurrentPosition.updateMoveMakers();
    }

    public static void startPosition() {
        String startingPosition = PositionFens.startingpos;
        CurrentPosition.InitializePosition(startingPosition,playerPlaysWhite,playerPlaysBlack,thinkTimeMS);
    }
}
