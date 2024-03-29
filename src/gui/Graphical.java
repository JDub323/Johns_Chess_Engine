package gui;

import java.awt.*;
import javax.swing.JPanel;

import position.CurrentPosition;
import position.Type;
import move.Move;
import tests.MoveTests;

public class Graphical extends JPanel{
    final int PANEL_WIDTH = 448;
    final int PANEL_HEIGHT = 448;
    public static byte pickedUpPiece = Type.EMPTY;
    public static byte selectedSquare = -1;
    public static byte previousSelectedSquare = -1;
    public static byte[] graphicSquareCentricPos = new byte[64];
    public static byte fromSquare=0,toSquare=0;
    private static byte recentMoveFromSquare = -100, recentMoveToSquare = -100;
    public static boolean stopAllMoves;

    Graphical () {
        this.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
    }

    @Override
    public void paintComponent (Graphics g) {
        super.paintComponent(g);
        DrawPieces.drawBoard(g);
        DrawPieces.drawHighlightedSquares(g,recentMoveFromSquare,recentMoveToSquare);
        drawPosition(g,graphicSquareCentricPos);
        drawPickedUpPiece(g, pickedUpPiece);
        drawSquaresFromBitboard(g,MoveTests.getToSquaresFromMoveList(CurrentPosition.position,selectedSquare));
    }

    public void drawPosition(Graphics g, byte[] scp) {
        for (int x=0;x<64;x++) {
            drawPiece(g,scp[x],x);
        }
    }

    public void drawPiece(Graphics g, byte pc, int square) {
        switch (pc) {
            case Type.EMPTY -> {
            }
            case Type.WHITE | Type.PAWN -> DrawPieces.drawPawn(g, square, true);
            case Type.BLACK | Type.PAWN -> DrawPieces.drawPawn(g, square, false);
            case Type.WHITE | Type.KNIGHT -> DrawPieces.drawKnight(g, square, true);
            case Type.BLACK | Type.KNIGHT -> DrawPieces.drawKnight(g, square, false);
            case Type.WHITE | Type.BISHOP -> DrawPieces.drawBishop(g, square, true);
            case Type.BLACK | Type.BISHOP -> DrawPieces.drawBishop(g, square, false);
            case Type.WHITE | Type.ROOK -> DrawPieces.drawRook(g, square, true);
            case Type.BLACK | Type.ROOK -> DrawPieces.drawRook(g, square, false);
            case Type.WHITE | Type.QUEEN -> DrawPieces.drawQueen(g, square, true);
            case Type.BLACK | Type.QUEEN -> DrawPieces.drawQueen(g, square, false);
            case Type.WHITE | Type.KING -> DrawPieces.drawKing(g, square, true);
            case Type.BLACK | Type.KING -> DrawPieces.drawKing(g, square, false);
        }
    }

    public void drawPickedUpPiece(Graphics g, byte pc) {
        switch (pc) {
            case Type.EMPTY -> {
            }
            case Type.WHITE | Type.PAWN -> DrawPieces.drawPawn(g, MyFrame.mousex, MyFrame.mousey, true);
            case Type.BLACK | Type.PAWN -> DrawPieces.drawPawn(g, MyFrame.mousex, MyFrame.mousey, false);
            case Type.WHITE | Type.KNIGHT -> DrawPieces.drawKnight(g, MyFrame.mousex, MyFrame.mousey, true);
            case Type.BLACK | Type.KNIGHT -> DrawPieces.drawKnight(g, MyFrame.mousex, MyFrame.mousey, false);
            case Type.WHITE | Type.BISHOP -> DrawPieces.drawBishop(g, MyFrame.mousex, MyFrame.mousey, true);
            case Type.BLACK | Type.BISHOP -> DrawPieces.drawBishop(g, MyFrame.mousex, MyFrame.mousey, false);
            case Type.WHITE | Type.ROOK -> DrawPieces.drawRook(g, MyFrame.mousex, MyFrame.mousey, true);
            case Type.BLACK | Type.ROOK -> DrawPieces.drawRook(g, MyFrame.mousex, MyFrame.mousey, false);
            case Type.WHITE | Type.QUEEN -> DrawPieces.drawQueen(g, MyFrame.mousex, MyFrame.mousey, true);
            case Type.BLACK | Type.QUEEN -> DrawPieces.drawQueen(g, MyFrame.mousex, MyFrame.mousey, false);
            case Type.WHITE | Type.KING -> DrawPieces.drawKing(g, MyFrame.mousex, MyFrame.mousey, true);
            case Type.BLACK | Type.KING -> DrawPieces.drawKing(g, MyFrame.mousex, MyFrame.mousey, false);
        }
    }

    public static void pickUpPiece(byte square) {
        boolean pieceIsPlayerPiece;
        int pieceGrabbing = graphicSquareCentricPos[square];

        if (CurrentPosition.playerPlaysForWhite && CurrentPosition.playerPlaysForBlack)pieceIsPlayerPiece=true;
        else if (CurrentPosition.playerPlaysForWhite)pieceIsPlayerPiece = pieceGrabbing/8==0;
        else if (CurrentPosition.playerPlaysForBlack)pieceIsPlayerPiece = pieceGrabbing/8==1;
        else pieceIsPlayerPiece=false;

        if (pieceIsPlayerPiece) {
            pickedUpPiece=graphicSquareCentricPos[square];
            graphicSquareCentricPos[square]= Type.EMPTY;
        }
        fromSquare=square;
    }
    public static void putDownPiece(byte square) {
        toSquare=square;

        int temp = Move.makeMoveFromBytes(Type.EMPTY,fromSquare,toSquare,Type.EMPTY);
        boolean moveListContainsFromSquareToSquare=false;
        int indexOfMoveFound=-1;

        for (int i = 0; i<CurrentPosition.position.indexOfFirstEmptyMove; i++) {
            if ((Move.fromSquareToSquareMask & CurrentPosition.position.legalMoves[i]) == temp) {//only take into account from/toSquare
                moveListContainsFromSquareToSquare=true;
                indexOfMoveFound=i;

                if (Move.getMoveTypeFromMove(CurrentPosition.position.legalMoves[i]) == Type.PAWN_PROMOTES_TO_Q) {
                    //find index of piece promoting to from fromSquare, toSquare, moveType
                    int tempMove = Move.makeMoveFromBytes(MyFrame.getPromotionMoveType(),fromSquare,toSquare, Type.EMPTY);

                    for (int j = 0; j<CurrentPosition.position.indexOfFirstEmptyMove; j++) {
                        if (CurrentPosition.position.legalMoves[j] == tempMove) {
                            indexOfMoveFound=j;
                            break;
                        }
                    }
                }
                break;//if not a pawn promotion
            }
        }

        if (moveListContainsFromSquareToSquare && !stopAllMoves) {
            CurrentPosition.makeMove(CurrentPosition.position.legalMoves[indexOfMoveFound]);
            CurrentPosition.position.calculateLegalMoves();
            CurrentPosition.updateMoveMakers();
            System.arraycopy(CurrentPosition.position.squareCentricPos,0,graphicSquareCentricPos,0,64);
            pickedUpPiece= Type.EMPTY;
        }
        else if (pickedUpPiece != Type.EMPTY)returnPiece();
    }
    public static void returnPiece() {
        graphicSquareCentricPos[fromSquare]=pickedUpPiece;
        pickedUpPiece= Type.EMPTY;
    }

    public static void clickPiece() {
            int temp = Move.makeMoveFromBytes(Type.EMPTY,previousSelectedSquare,selectedSquare,Type.EMPTY);
            boolean moveListContainsFromSquareToSquare=false;
            int indexOfMoveFound=-1;

            for (int i = 0; i<CurrentPosition.position.indexOfFirstEmptyMove; i++) {
                if ((Move.fromSquareToSquareMask & CurrentPosition.position.legalMoves[i]) == temp) {//only take into account from/toSquare
                    moveListContainsFromSquareToSquare=true;
                    indexOfMoveFound=i;

                    if (Move.getMoveTypeFromMove(CurrentPosition.position.legalMoves[i]) == Type.PAWN_PROMOTES_TO_Q) {
                        //find index of piece promoting to from fromSquare, toSquare, moveType
                        int tempMove = Move.makeMoveFromBytes(MyFrame.getPromotionMoveType(),previousSelectedSquare,selectedSquare, Type.EMPTY);

                        for (int j = 0; j<CurrentPosition.position.indexOfFirstEmptyMove; j++) {
                            if (CurrentPosition.position.legalMoves[j] == tempMove) {
                                indexOfMoveFound=j;
                                break;
                            }
                        }
                    }
                    break;//if not a pawn promotion
                }
            }

            if (moveListContainsFromSquareToSquare && !stopAllMoves) {
                CurrentPosition.makeMove(CurrentPosition.position.legalMoves[indexOfMoveFound]);
                CurrentPosition.position.calculateLegalMoves();
                CurrentPosition.updateMoveMakers();
                System.arraycopy(CurrentPosition.position.squareCentricPos,0,graphicSquareCentricPos,0,64);
                selectedSquare = -1;
            }
    }

    public static void updateHighlightedSquares(int move) {
        recentMoveFromSquare = Move.getFromSquareFromMove(move);
        recentMoveToSquare = Move.getToSquareFromMove(move);
    }

    public static void drawSquaresFromBitboard(Graphics g, long input) {
        long square=1;
        for (int i=0;i<64;i++) {
            if (((square<<i)|input)==input && !CurrentPosition.botPlaysForColorToMove()) {
                DrawPieces.drawTarget(g,i);
            }
        }
    }
}
