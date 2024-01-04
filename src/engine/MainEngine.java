package engine;

import chessUtilities.PositionFens;
import move.GenerateMagicBitBoards;
import move.PieceAttack;
import position.CurrentPosition;


public class MainEngine {

    public static void main(String[] args) {
        PieceAttack.generateMoveArrays();
        GenerateMagicBitBoards.makeBitboardDatabase();
        OpeningBook.initializeBookLists();
        gui.FrameHolder.makeFrame();
        startPosition();
        CurrentPosition.updateMoveMakers();
    }

    public static void startPosition() {
        String startingPosition = PositionFens.startingpos;
        CurrentPosition.InitializePosition(startingPosition,true,false, 1000);
    }
}
