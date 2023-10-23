package gui;

import java.awt.Color;
import java.awt.Graphics;

public class DrawPieces
{
    private final static Color lightSquare = new Color(207,215,230);
    private final static Color darkSquare = new Color(80,79,125);
    private final static Color lightSquareBorder = new Color(197,205,220);
    private final static Color darkSquareBorder = new Color(70,69,115);

    private final static Color pBlack = new Color(58,63,82);
    private final static Color wpLightGray = new Color(207,207,214);
    private final static Color wpDarkGray = new Color(137,139,151);
    private final static Color bpLightGray = new Color(105,127,150);
    private final static Color bpDarkGray = new Color(79,90,108);
    private final static Color bpWhite = new Color(127,159,184);
    private final static Color white = new Color(255,255,255);





    public static void drawSquare(Graphics g, int file, int rank, boolean isLight) {
        int x = file*56;
        int y = rank*56;
        if (isLight)
        {
            g.setColor(lightSquareBorder);
            g.fillRect(x,y,56,56);
            g.setColor(lightSquare);
            g.fillRect(x+4,y+4,48,48);
        }
        else
        {
            g.setColor(darkSquareBorder);
            g.fillRect(x,y,56,56);
            g.setColor(darkSquare);
            g.fillRect(x+4,y+4,48,48);
        }
    }
    public static void drawBoard(Graphics g) {
        for (int rank=0;rank<8;rank++) {
            for (int file=0;file<8;file++){
                boolean isLight = (file+rank)%2==0;
                drawSquare(g,file,rank,isLight);
            }
        }
    }
    public static void drawTarget(Graphics g, int square) {
        int x=(square%8)*56;
        int y=(7-(square)/8)*56;

        g.setColor(new Color(177,185,200));
        g.fillRect(x+18,y+18,20,20);
        g.setColor(new Color(187,195,210));
        g.fillRect(x+22,y+22,12,12);



    }

    public static void drawKnight(Graphics g, int square, boolean isWhite) {
        int x=(square%8)*56;
        int y=(7-(square)/8)*56;
        y -= 2;

        if (isWhite) {
            knight(g,x,y,white,wpLightGray,wpDarkGray);
        }
        else {
            knight(g,x,y,bpWhite,bpLightGray,bpDarkGray);
        }
    }
    public static void drawPawn(Graphics g, int square, boolean isWhite) {
        int x=(square%8)*56;
        int y=(7-(square)/8)*56;
        y -= 2;

        if (isWhite) {
            pawn(g,x,y,white,wpLightGray,wpDarkGray);
        }
        else {
            pawn(g,x,y,bpWhite,bpLightGray,bpDarkGray);
        }
    }
    public static void drawBishop(Graphics g, int square, boolean isWhite) {
        int x=(square%8)*56;
        int y=(7-(square)/8)*56;
        y -= 2;

        if (isWhite) {
            bishop(g,x,y,white,wpLightGray,wpDarkGray);
        }
        else {
            bishop(g,x,y,bpWhite,bpLightGray,bpDarkGray);
        }
    }
    public static void drawRook(Graphics g, int square, boolean isWhite) {
        int x=(square%8)*56;
        int y=(7-(square)/8)*56;
        y -= 2;

        if (isWhite) {
            rook(g,x,y,white,wpLightGray,wpDarkGray);
        }
        else {
            rook(g,x,y,bpWhite,bpLightGray,bpDarkGray);
        }
    }
    public static void drawKing(Graphics g, int square, boolean isWhite) {
        int x=(square%8)*56;
        int y=(7-(square)/8)*56;
        y -= 2;

        if (isWhite) {
            king(g,x,y,white,wpLightGray,wpDarkGray);
        }
        else {
            king(g,x,y,bpWhite,bpLightGray,bpDarkGray);
        }
    }
    public static void drawQueen(Graphics g, int square, boolean isWhite) {
        int x=(square%8)*56;
        int y=(7-(square)/8)*56;
        y -= 2;

        if (isWhite) {
            queen(g,x,y,white,wpLightGray,wpDarkGray);
        }
        else {
            queen(g,x,y,bpWhite,bpLightGray,bpDarkGray);
        }
    }

    public static void drawKnight(Graphics g, int x, int y, boolean isWhite) {
        x-=29;
        y-=27;

        if (isWhite) {
            knight(g,x,y,white,wpLightGray,wpDarkGray);
        }
        else {
            knight(g,x,y,bpWhite,bpLightGray,bpDarkGray);
        }
    }
    public static void drawPawn(Graphics g, int x, int y, boolean isWhite) {
        x-=29;
        y-=27;

        if (isWhite) {
            pawn(g,x,y,white,wpLightGray,wpDarkGray);
        }
        else {
            pawn(g,x,y,bpWhite,bpLightGray,bpDarkGray);
        }
    }
    public static void drawBishop(Graphics g, int x, int y, boolean isWhite) {
        x-=29;
        y-=27;

        if (isWhite) {
            bishop(g,x,y,white,wpLightGray,wpDarkGray);
        }
        else {
            bishop(g,x,y,bpWhite,bpLightGray,bpDarkGray);
        }
    }
    public static void drawRook(Graphics g, int x, int y, boolean isWhite) {
        x-=29;
        y-=27;

        if (isWhite) {
            rook(g,x,y,white,wpLightGray,wpDarkGray);
        }
        else {
            rook(g,x,y,bpWhite,bpLightGray,bpDarkGray);
        }
    }
    public static void drawKing(Graphics g, int x, int y, boolean isWhite) {
        x-=29;
        y-=27;

        if (isWhite) {
            king(g,x,y,white,wpLightGray,wpDarkGray);
        }
        else {
            king(g,x,y,bpWhite,bpLightGray,bpDarkGray);
        }
    }
    public static void drawQueen(Graphics g, int x, int y, boolean isWhite) {
        x-=29;
        y-=27;

        if (isWhite) {
            queen(g,x,y,white,wpLightGray,wpDarkGray);
        }
        else {
            queen(g,x,y,bpWhite,bpLightGray,bpDarkGray);
        }
    }

    private static void knight(Graphics g, int x, int y, Color white, Color lightGray, Color darkGray) {
        g.setColor(white);
        g.fillRect(x+17,y+15,18,24);
        g.fillRect(x+13,y+23,4,6);
        g.fillRect(x+17,y+43,22,4);

        g.setColor(pBlack);
        g.fillRect(x+13,y+39,4,12);
        g.fillRect(x+13,y+47,30,4);
        g.fillRect(x+39,y+39,4,12);
        g.fillRect(x+35,y+35,4,8);
        g.fillRect(x+33,y+31,4,4);
        g.fillRect(x+37,y+29,4,4);
        g.fillRect(x+35,y+27,4,2);
        g.fillRect(x+37,y+23,4,4);
        g.fillRect(x+35,y+21,4,2);
        g.fillRect(x+37,y+17,4,4);
        g.fillRect(x+35,y+15,4,4);
        g.fillRect(x+33,y+13,4,4);
        g.fillRect(x+31,y+9,6,4);
        g.fillRect(x+23,y+9,6,4);
        g.fillRect(x+27,y+11,4,4);
        g.fillRect(x+21,y+11,4,4);//start of diag
        g.fillRect(x+19,y+13,4,4);
        g.fillRect(x+17,y+15,4,4);
        g.fillRect(x+15,y+17,4,4);
        g.fillRect(x+13,y+19,4,4);
        g.fillRect(x+11,y+21,4,4);
        g.fillRect(x+15,y+25,2,2);//nostril
        g.fillRect(x+9,y+23,4,8);
        g.fillRect(x+11,y+29,4,4);
        g.fillRect(x+13,y+31,6,4);
        g.fillRect(x+19,y+29,2,4);
        g.fillRect(x+21,y+29,2,2);
        g.fillRect(x+15,y+35,4,8);
        g.fillRect(x+19,y+37,2,6);
        g.fillRect(x+25,y+19,2,2);//eye

        g.setColor(darkGray);
        g.fillRect(x+21,y+41,14,2);

        g.setColor(lightGray);
        g.fillRect(x+17,y+43,2,2);//BR pixel
        g.fillRect(x+37,y+43,2,2);//BL pixel
        g.fillRect(x+21,y+37,6,4);
        g.fillRect(x+25,y+35,10,6);

        g.setColor(white);
        g.fillRect(x+27,y+37,4,2);
        g.fillRect(x+31,y+35,2,2);

        g.setColor(lightGray);
        g.fillRect(x+29,y+33,4,2);
        g.fillRect(x+31,y+29,2,4);
        g.fillRect(x+33,y+29,4,2);
        g.fillRect(x+33,y+25,2,4);
        g.fillRect(x+35,y+23,2,4);
        g.fillRect(x+35,y+19,2,2);
        g.fillRect(x+33,y+17,2,2);
        g.fillRect(x+31,y+13,2,4);//right ear
        g.fillRect(x+25,y+13,2,2);
        g.fillRect(x+15,y+29,4,2);//mouth
        g.fillRect(x+23,y+27,6,2);
        g.fillRect(x+29,y+25,2,2);
    }
    private static void pawn(Graphics g, int x, int y, Color white, Color lightGray, Color darkGray) {
        g.setColor(white);
        g.fillRect(x+19,y+13,18,26);
        g.fillRect(x+15,y+39,24,8);

        g.setColor(pBlack);
        g.fillRect(x+13,y+47,30,4);
        g.fillRect(x+39,y+39,4,8);
        g.fillRect(x+35,y+39,4,4);//BRSquare
        g.fillRect(x+37,y+31,4,8);
        g.fillRect(x+35,y+29,4,4);
        g.fillRect(x+33,y+25,4,4);
        g.fillRect(x+35,y+15,4,12);
        g.fillRect(x+33,y+13,4,4);//TR Diagonal
        g.fillRect(x+13,y+39,4,8);
        g.fillRect(x+17,y+39,4,4);//BLSquare
        g.fillRect(x+15,y+31,4,8);
        g.fillRect(x+17,y+29,4,4);
        g.fillRect(x+19,y+25,4,4);
        g.fillRect(x+17,y+15,4,12);
        g.fillRect(x+19,y+13,4,4);//TL Diagonal
        g.fillRect(x+21,y+11,14,4);

        g.setColor(lightGray);
        g.fillRect(x+31,y+15,2,2);
        g.fillRect(x+33,y+17,2,2);
        g.fillRect(x+33,y+21,2,4);
        g.fillRect(x+23,y+15,2,2);
        g.fillRect(x+21,y+17,2,2);
        g.fillRect(x+21,y+21,2,4);
        g.fillRect(x+23,y+23,10,4);
        g.fillRect(x+23,y+29,10,2);
        g.fillRect(x+19,y+35,2,4);
        g.fillRect(x+21,y+37,14,4);
        g.fillRect(x+17,y+43,2,2);//bottompixels
        g.fillRect(x+37,y+43,2,2);
        g.fillRect(x+35,y+35,2,4);

        g.setColor(darkGray);
        g.fillRect(x+23,y+27,10,2);
        g.fillRect(x+21,y+41,14,2);
    }
    private static void bishop(Graphics g, int x, int y, Color white, Color lightGray, Color darkGray) {
        g.setColor(white);
        g.fillRect(x+19,y+13,18,28);
        g.fillRect(x+17,y+23,22,2);
        g.fillRect(x+13,y+39,26,8);

        g.setColor(pBlack);
        g.fillRect(x+13,y+47,30,4);
        g.fillRect(x+13,y+39,4,8);//BL Side
        g.fillRect(x+17,y+37,4,6);
        g.fillRect(x+19,y+33,4,4);//firstBL diag
        g.fillRect(x+17,y+31,4,4);
        g.fillRect(x+15,y+29,4,4);//lastBL diag
        g.fillRect(x+13,y+21,4,10);//leftSide
        g.fillRect(x+15,y+17,4,6);
        g.fillRect(x+17,y+15,4,4);//beginTL diag
        g.fillRect(x+19,y+13,4,4);
        g.fillRect(x+21,y+11,4,4);
        g.fillRect(x+23,y+9,10,4);//top
        g.fillRect(x+39,y+39,4,8);//BR side
        g.fillRect(x+35,y+37,4,6);
        g.fillRect(x+33,y+33,4,4);//firstBR diag
        g.fillRect(x+35,y+31,4,4);
        g.fillRect(x+37,y+29,4,4);
        g.fillRect(x+39,y+21,4,10);//rightSide
        g.fillRect(x+37,y+17,4,6);
        g.fillRect(x+35,y+15,4,4);
        g.fillRect(x+33,y+13,4,4);
        g.fillRect(x+31,y+11,4,4);
        g.fillRect(x+27,y+19,2,10);//vertCross
        g.fillRect(x+23,y+23,10,2);//horizCross

        g.setColor(lightGray);
        g.fillRect(x+17,y+43,2,2);//bottompixels
        g.fillRect(x+37,y+43,2,2);
        g.fillRect(x+21,y+39,2,2);//leftpixel
        g.fillRect(x+33,y+39,2,2);
        g.fillRect(x+23,y+35,2,2);//leftpixel
        g.fillRect(x+31,y+35,2,2);
        g.fillRect(x+25,y+37,6,2);
        g.fillRect(x+17,y+25,2,4);//leftshadow
        g.fillRect(x+19,y+27,2,4);
        g.fillRect(x+21,y+29,4,4);
        g.fillRect(x+25,y+31,6,4);//midshadow
        g.fillRect(x+31,y+29,4,4);
        g.fillRect(x+35,y+27,2,4);
        g.fillRect(x+37,y+25,2,4);//rightshadow
        g.fillRect(x+35,y+19,2,2);
        g.fillRect(x+33,y+17,2,2);
        g.fillRect(x+31,y+15,2,2);
        g.fillRect(x+29,y+13,2,2);
        g.fillRect(x+25,y+13,2,2);//leftshadow
        g.fillRect(x+23,y+15,2,2);
        g.fillRect(x+21,y+17,2,2);
        g.fillRect(x+19,y+19,2,2);

        g.setColor(darkGray);
        g.fillRect(x+23,y+33,2,2);
        g.fillRect(x+25,y+35,6,2);
        g.fillRect(x+31,y+33,2,2);
        g.fillRect(x+21,y+41,14,2);
    }
    private static void rook(Graphics g, int x, int y, Color white, Color lightGray, Color darkGray) {
        g.setColor(white);
        g.fillRect(x+17,y+13,22,34);

        g.setColor(pBlack);
        g.fillRect(x+13,y+47,30,4);
        g.fillRect(x+13,y+39,4,8);//BL Side
        g.fillRect(x+15,y+21,4,22);//leftSide
        g.fillRect(x+13,y+9,4,14);
        g.fillRect(x+17,y+9,4,4);
        g.fillRect(x+21,y+11,4,4);
        g.fillRect(x+23,y+9,10,4);
        g.fillRect(x+39,y+39,4,8);//BR Side
        g.fillRect(x+37,y+21,4,22);
        g.fillRect(x+39,y+9,4,14);
        g.fillRect(x+35,y+9,4,4);
        g.fillRect(x+31,y+11,4,4);

        g.setColor(lightGray);
        g.fillRect(x+17,y+43,2,2);//bottompixels
        g.fillRect(x+37,y+43,2,2);
        g.fillRect(x+19,y+35,2,2);//midpixels
        g.fillRect(x+35,y+35,2,2);
        g.fillRect(x+19,y+37,18,4);//midshadow
        g.fillRect(x+17,y+17,2,2);//toppixels
        g.fillRect(x+37,y+17,2,2);
        g.fillRect(x+25,y+17,6,2);
        g.fillRect(x+17,y+19,22,2);
        g.fillRect(x+21,y+23,14,2);

        g.setColor(darkGray);
        g.fillRect(x+19,y+41,18,2);
        g.fillRect(x+19,y+21,18,2);
    }
    private static void queen(Graphics g, int x, int y, Color white, Color lightGray, Color darkGray) {
        g.setColor(white);
        g.fillRect(x+13,y+19,30,14);
        g.fillRect(x+17,y+37,22,10);

        g.setColor(lightGray);
        g.fillRect(x+13,y+17,30,2);

        g.setColor(pBlack);
        g.fillRect(x+13,y+47,30,4);
        g.fillRect(x+13,y+43,4,4);//BL Side
        g.fillRect(x+13,y+37,4,4);//leftHip
        g.fillRect(x+15,y+41,2,2);
        g.fillRect(x+15,y+35,4,2);
        g.fillRect(x+13,y+31,4,4);
        g.fillRect(x+11,y+25,4,6);
        g.fillRect(x+9,y+17,4,8);
        g.fillRect(x+7,y+11,42,6);//long crown
        g.fillRect(x+9,y+9,6,2);//start top crown
        g.fillRect(x+17,y+9,6,2);
        g.fillRect(x+25,y+9,6,2);
        g.fillRect(x+33,y+9,6,2);
        g.fillRect(x+41,y+9,6,2);//end top crown
        g.fillRect(x+15,y+17,4,2);//botcrown
        g.fillRect(x+17,y+19,2,2);
        g.fillRect(x+21,y+17,6,2);//botcrown2
        g.fillRect(x+23,y+19,2,2);
        g.fillRect(x+29,y+17,6,2);//botcrown3
        g.fillRect(x+31,y+19,2,2);
        g.fillRect(x+37,y+17,4,2);//botcrown4
        g.fillRect(x+37,y+19,2,2);
        g.fillRect(x+43,y+17,4,8);//TRCrownside
        g.fillRect(x+41,y+25,4,6);
        g.fillRect(x+39,y+31,4,4);
        g.fillRect(x+39,y+43,4,4);//startBR
        g.fillRect(x+39,y+41,2,2);
        g.fillRect(x+39,y+37,4,4);//righthip
        g.fillRect(x+37,y+35,4,2);

        g.setColor(white);//jewels
        g.fillRect(x+11,y+13,2,2);
        g.fillRect(x+19,y+13,2,2);
        g.fillRect(x+27,y+13,2,2);
        g.fillRect(x+35,y+13,2,2);
        g.fillRect(x+43,y+13,2,2);

        g.setColor(lightGray);
        g.fillRect(x+17,y+43,2,2);//bottompixels
        g.fillRect(x+37,y+43,2,2);
        g.fillRect(x+19,y+37,18,2);//botshadow
        g.fillRect(x+15,y+19,2,4);//crownshadow1
        g.fillRect(x+17,y+21,2,4);
        g.fillRect(x+21,y+19,2,4);
        g.fillRect(x+23,y+21,2,6);
        g.fillRect(x+25,y+19,2,4);
        g.fillRect(x+29,y+19,2,4);
        g.fillRect(x+31,y+21,2,6);
        g.fillRect(x+33,y+19,2,4);
        g.fillRect(x+37,y+21,2,4);
        g.fillRect(x+39,y+19,2,4);//endCshadow
        g.fillRect(x+37,y+27,2,4);//startBRshad
        g.fillRect(x+39,y+29,2,2);
        g.fillRect(x+35,y+31,4,4);//BRcubeshadow
        g.fillRect(x+29,y+31,4,4);
        g.fillRect(x+23,y+31,4,4);
        g.fillRect(x+17,y+31,4,4);//BLcubeshadow
        g.fillRect(x+15,y+29,4,2);
        g.fillRect(x+17,y+27,2,2);
        g.fillRect(x+23,y+29,2,2);
        g.fillRect(x+31,y+29,2,2);
        g.fillRect(x+17,y+33,18,2);

        g.setColor(darkGray);
        g.fillRect(x+19,y+35,18,2);
        g.fillRect(x+17,y+41,22,2);
    }
    private static void king(Graphics g, int x, int y, Color white, Color lightGray, Color darkGray) {
        g.setColor(white);
        g.fillRect(x+15,y+17,26,30);

        g.setColor(pBlack);
        g.fillRect(x+13,y+47,30,4);
        g.fillRect(x+13,y+43,4,4);//BL Side
        g.fillRect(x+13,y+37,4,4);//leftHip
        g.fillRect(x+15,y+41,2,2);
        g.fillRect(x+15,y+35,4,2);
        g.fillRect(x+13,y+33,4,2);
        g.fillRect(x+11,y+21,4,12);//left side
        g.fillRect(x+13,y+19,4,4);//start diag
        g.fillRect(x+15,y+17,4,4);
        g.fillRect(x+17,y+15,4,4);//end diag
        g.fillRect(x+21,y+13,14,6);
        g.fillRect(x+23,y+11,10,2);
        g.fillRect(x+25,y+9,6,2);//top
        g.fillRect(x+25,y+19,6,2);//bottom
        g.fillRect(x+39,y+43,4,4);//startBR
        g.fillRect(x+39,y+41,2,2);
        g.fillRect(x+39,y+37,4,4);//righthip
        g.fillRect(x+37,y+35,4,2);
        g.fillRect(x+39,y+33,4,2);
        g.fillRect(x+41,y+21,4,12);//rightside
        g.fillRect(x+39,y+19,4,4);//startTRdiag
        g.fillRect(x+37,y+17,4,4);
        g.fillRect(x+35,y+15,4,4);//endTRdiag

        g.setColor(white);
        g.fillRect(x+27,y+13,2,6);
        g.fillRect(x+25,y+15,6,2);

        g.setColor(lightGray);
        g.fillRect(x+17,y+43,2,2);//bottompixels
        g.fillRect(x+37,y+43,2,2);
        g.fillRect(x+19,y+37,18,2);//botshadow
        g.fillRect(x+15,y+27,2,6);
        g.fillRect(x+17,y+29,2,6);
        g.fillRect(x+19,y+31,18,4);
        g.fillRect(x+39,y+27,2,6);
        g.fillRect(x+37,y+29,2,6);
        g.fillRect(x+25,y+29,6,2);
        g.fillRect(x+17,y+21,2,2);//lowleftpix
        g.fillRect(x+19,y+19,2,2);
        g.fillRect(x+35,y+19,2,2);
        g.fillRect(x+37,y+21,2,2);
        g.fillRect(x+25,y+21,6,2);
        g.fillRect(x+27,y+23,2,2);//botmidpix

        g.setColor(darkGray);
        g.fillRect(x+19,y+35,18,2);
        g.fillRect(x+17,y+41,22,2);
    }
}
