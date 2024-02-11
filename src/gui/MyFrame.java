package gui;

import javax.swing.*;
import java.awt.event.*;

import chessUtilities.Util;
import engine.TranspositionTable;
import position.CurrentPosition;
import engine.MainEngine;


public class MyFrame extends JFrame implements MouseListener, MouseMotionListener, KeyListener {

    Graphical panel;
    JLabel label;
    public static int mousex=0, mousey=0;
    static byte promotionMoveType = position.Type.PAWN_PROMOTES_TO_Q;
    boolean mouseIsDown=false;


    public MyFrame() {
        panel = new Graphical();

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(panel);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.addKeyListener(this);

        label = new JLabel();
        label.setBounds(0,0,448,448);
        label.addMouseListener(this);
        label.addMouseMotionListener(this);

        this.add(label);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(e.getButton()==1) {
            mousex=e.getX();
            mousey=e.getY();
            Graphical.pickUpPiece((byte)getSquareFromXY(mousex,mousey));
            Graphical.previousSelectedSquare = Graphical.selectedSquare;
            Graphical.selectedSquare = (byte)getSquareFromXY(mousex,mousey);
            repaint();
        }
        mouseIsDown=true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(e.getButton()==1){
            mousex=e.getX();
            mousey=e.getY();
            Graphical.putDownPiece((byte)getSquareFromXY(mousex,mousey));//check if legal move first
            //if illegal move, make and execute return piece method
            repaint();
        }
        mouseIsDown=false;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mousex=e.getX();
        mousey=e.getY();
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (e.getKeyChar() == 'R' && !mouseIsDown) {
            MainEngine.startPosition();
            repaint();
        }
        else if (e.getKeyChar() == 'P') {
            for (int x=0;x<64;x++) {
                CurrentPosition.position.squareCentricPos[x]= Util.getPieceFromSquareWithBB(x,CurrentPosition.position.pieceArray);
            }
            System.arraycopy(CurrentPosition.position.squareCentricPos, 0, Graphical.graphicSquareCentricPos, 0, 64);
            repaint();
        }
        else if (e.getKeyChar() == 'u') {
            try {
                CurrentPosition.unmakeMove();
                System.arraycopy(CurrentPosition.position.squareCentricPos, 0, Graphical.graphicSquareCentricPos, 0, 64);
                CurrentPosition.updateMoveMakers();
                repaint();
            }
            catch (Exception EmptyStackException){
                System.out.println("ERROR: cannot unmake no moves");
            }
        }
        else if (e.getKeyChar() == 'p'){
            CurrentPosition.position.printFen();
        }
        else if (e.getKeyChar() == 't'){
            TranspositionTable.printNumEntries();
        }
        else if (e.getKeyChar() == 'z'){
            System.out.println("Current Zobrist Key: "+Long.toHexString(CurrentPosition.position.zobristKey));
        }
        else if (e.getKeyChar() == 'q') promotionMoveType = position.Type.PAWN_PROMOTES_TO_Q;
        else if (e.getKeyChar() == 'n') promotionMoveType = position.Type.PAWN_PROMOTES_TO_N;
        else if (e.getKeyChar() == 'b') promotionMoveType = position.Type.PAWN_PROMOTES_TO_B;
        else if (e.getKeyChar() == 'r') promotionMoveType = position.Type.PAWN_PROMOTES_TO_R;
    }
    @Override
    public void mouseClicked(MouseEvent e) {
        Graphical.clickPiece();
    }

    public int getSquareFromXY(int x, int y) {
        return 8*(7-y/56)+x/56;
    }

    public static byte getPromotionMoveType() {
        return promotionMoveType;
    }

    public void updateGraphics() {
        System.arraycopy(CurrentPosition.position.squareCentricPos,0,Graphical.graphicSquareCentricPos,0,64);
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void keyPressed(KeyEvent e) {}
    @Override
    public void keyReleased(KeyEvent e) {}
}
