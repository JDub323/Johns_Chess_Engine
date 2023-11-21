package engine;

import move.GenerateMagicBitBoards;
import move.PieceAttack;
import position.CurrentPosition;


public class MainEngine {

    public static void main(String[] args) {
        PieceAttack.generateMoveArrays();
        GenerateMagicBitBoards.makeBitboardDatabase();
        gui.FrameHolder.makeFrame();
        startPosition();
        //start move search
        CurrentPosition.updateMoveMakers();


        //MoveTests.testAllTestPositions();
    }

    public static void startPosition() {
        String startingPosition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        CurrentPosition.InitializePosition(startingPosition,true,false, 1000);
        //Normal starting position: rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
    }
}
