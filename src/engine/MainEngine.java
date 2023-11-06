package engine;

import move.GenerateMagicBitBoards;
import move.MagicBitboards;
import move.PieceAttack;
import position.CurrentPosition;
import tests.EvaluationTests;
import tests.MoveTests;
import move.Move;
import position.Type;
import tests.StaticEvalTests;


public class MainEngine {

    public static void main(String[] args) {
        PieceAttack.generateMoveArrays();
        GenerateMagicBitBoards.makeBitboardDatabase();
        gui.FrameHolder.makeFrame();
        startPosition();
        //start move search
        //CurrentPosition.updateMoveMakers();
        EvaluationTests.testMidGameTestPositions(4);


        //MoveTests.testAllTestPositions();
        //MoveTests.testInitialPosition(5);
        //Move tempMove = new Move (Type.Empty,"a6","a7",Type.Empty);
        //MoveTests.testTestPositionAndShowNodes("8/8/4k3/8/2p5/8/B2P2K1/8 w - - 0 1",6);
        //CurrentPosition.position.makeMove(new Move(Type.enPassant, (byte) 34, (byte) 43,Type.Empty));
    }

    public static void startPosition() {
        String startingPosition = "r1bq1br1/ppppk1p1/5p2/4pQp1/3P4/4K3/PPP1PPPP/RN3B1R w - - 0 5";
        CurrentPosition.InitializePosition(startingPosition,false,true, 1000);
        //Normal starting position: rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
    }
}
