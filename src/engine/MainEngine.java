package engine;

import chessUtilities.PositionFens;
import move.GenerateMagicBitBoards;
import move.Move;
import move.PieceAttack;
import position.CurrentPosition;
import position.Type;
import tests.MoveTests;


public class MainEngine {

    public static void main(String[] args) {
        PieceAttack.generateMoveArrays();
        GenerateMagicBitBoards.makeBitboardDatabase();
        OpeningBook.initializeBookLists();
        gui.FrameHolder.makeFrame();
        startPosition();
        //start move search
        CurrentPosition.updateMoveMakers();


        //MoveTests.testAllTestPositions();
    }

    public static void startPosition() {
        String startingPosition = PositionFens.startingpos;
        CurrentPosition.InitializePosition(startingPosition,true,false, 1000);
    }
}
