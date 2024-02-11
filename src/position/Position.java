package position;

import eval.StaticEval;
import move.Move;
import move.PieceAttack;
import java.util.Stack;
import chessUtilities.Util;


public class Position {

    //Position representation variables
    public long[] pieceArray = new long[15];
    public byte[] squareCentricPos = new byte[64];
    public long castlingRights;
    public int hundredHalfmoveTimer;
    public int enPassantTargetFiles;
    public boolean whiteToMove;
    public byte[][] pieceSquareList = new byte[15][];
    {
        pieceSquareList[Type.WHITE | Type.PAWN] = new byte[8];
        pieceSquareList[Type.WHITE | Type.KNIGHT] = new byte[10];//max 10 knights with 8 pawn underpromotions
        pieceSquareList[Type.WHITE | Type.BISHOP] = new byte[10];//same idea for the rest of the pieces
        pieceSquareList[Type.WHITE | Type.ROOK] = new byte[10];
        pieceSquareList[Type.WHITE | Type.QUEEN] = new byte[9];
        pieceSquareList[Type.WHITE | Type.KING] = new byte[1];
        pieceSquareList[Type.BLACK | Type.PAWN] = new byte[8];
        pieceSquareList[Type.BLACK | Type.KNIGHT] = new byte[10];
        pieceSquareList[Type.BLACK | Type.BISHOP] = new byte[10];
        pieceSquareList[Type.BLACK | Type.ROOK] = new byte[10];
        pieceSquareList[Type.BLACK | Type.QUEEN] = new byte[9];
        pieceSquareList[Type.BLACK | Type.KING] = new byte[1];
    }
    public byte[] numPieces = new byte[15];
    public byte[][] colorIndexBoard = new byte[2][64];//0 for white, 1 for black
    public byte gameState;
    public int plyNumber;
    public long zobristKey;

    //additional useful variables in the position
    public Stack<Integer> PreviousEnPassantTargetFiles = new Stack<>();
    public Stack<Integer> PreviousHalfMoveTimers = new Stack<>();
    public Stack<Long> PreviousCastlingRights = new Stack<>();
    public Stack<int[]> PreviousMovelists = new Stack<int[]>();
    public Stack<Integer> PreviousIndexOfFirstEmptyMove = new Stack<>();

    public long whiteAttacksArray[] = new long[7];
    public long whiteAttacks;
    public long blackAttacksArray[] = new long[7];
    public long blackAttacks;
    public long whitePieces;
    public long blackPieces;
    public long allPieces;
    public long emptySquares;
    public boolean inCheck;
    public int numChecks;
    public long checkResolveRay;
    public long[] pinRay = new long[8];

    public long[] squareAttacksArray = new long[64];

    public int indexOfFirstEmptyMove;

    public int[] legalMoves = new int[218];
    public int[] legalMovePriorities = new int[218];

    public int indexOfPin;
    public long pinnedPieces;

    public long[] previousZobristKeys = new long[500];//theoretically, a game can go on up to 8,800 moves, but not realistic, so arbitrarily 500

    public Position(String fen) {
        int indexOfFirstSpace= fen.indexOf(' ');
        String temp = "";
        int tickerSquare=56;//start at top left square
        boolean tempIsNumber=false;

        for (int i=0;i<indexOfFirstSpace;i++) {
            temp=fen.substring(i,i+1);

            try {
                Integer.parseInt(temp);
                tempIsNumber=true;
            }
            catch (Exception NumberFormatException) {
                tempIsNumber=false;
            }

            if (temp.equals("/")){
                tickerSquare-=16;
            }
            else if (tempIsNumber) {
                tickerSquare+=Integer.parseInt(temp);
            }
            else {
                pieceArray[Util.getPieceFromString(temp)]+=Util.toBitboard(tickerSquare);
                tickerSquare++;
            }
        }

        for (int x=0;x<64;x++) {
            byte pieceOnX = Util.getPieceFromSquareWithBB(x, pieceArray);
            squareCentricPos[x]=pieceOnX;

            if (pieceOnX != Type.EMPTY) {
                pieceSquareList[pieceOnX][numPieces[pieceOnX]]= (byte) x;
                colorIndexBoard[pieceOnX/8][x] = numPieces[pieceOnX];
                numPieces[pieceOnX]++;
            }
        }

        whiteToMove = fen.substring(indexOfFirstSpace+1,indexOfFirstSpace+2).equals("w");

        int endOfFen = fen.length();
        String fenWithoutPosition = fen.substring(indexOfFirstSpace,endOfFen);

        if (fenWithoutPosition.contains("K"))castlingRights |= Type.WHITE_CAN_CS;
        if (fenWithoutPosition.contains("Q"))castlingRights |= Type.WHITE_CAN_CL;
        if (fenWithoutPosition.contains("k"))castlingRights |= Type.BLACK_CAN_CS;
        if (fenWithoutPosition.contains("q"))castlingRights |= Type.BLACK_CAN_CL;

        int indexOfLastSpace = fen.lastIndexOf(" ");
        int indexOfSecondToLastSpace = fen.substring(0,indexOfLastSpace).lastIndexOf(" ");
        int indexOfMiddleSpace = fen.substring(0,indexOfSecondToLastSpace).lastIndexOf(" ");

        String epTargetSquare = fen.substring(indexOfMiddleSpace+1, indexOfMiddleSpace+2);
        enPassantTargetFiles= switch (epTargetSquare) {
            case "a"-> {
                yield 1<<0;
            }
            case "b"-> {
                yield 1<<1;
            }
            case "c"-> {
                yield 1<<2;
            }
            case "d"-> {
                yield 1<<3;
            }
            case "e"-> {
                yield 1<<4;
            }
            case "f"-> {
                yield 1<<5;
            }
            case "g"-> {
                yield 1<<6;
            }
            case "h"-> {
                yield 1<<7;
            }
            case "-"-> {
                yield 0;
            }
            default -> {//should never be called
                yield -1;
            }
        };

        hundredHalfmoveTimer= Integer.parseInt(fen.substring(indexOfSecondToLastSpace+1,indexOfLastSpace));
        plyNumber = Integer.parseInt(fen.substring(indexOfLastSpace+1,endOfFen));
        zobristKey = Zobrist.getZobristKeyFromPosition(whiteToMove,castlingRights,enPassantTargetFiles,squareCentricPos);

        calculatePreCalculatedData();
        calculateLegalMoves();

        previousZobristKeys[plyNumber] = zobristKey;
    }
    public Position(long[] PieceArray, byte[] squareCentricPos, boolean whiteToMove) {
        System.arraycopy(squareCentricPos, 0, this.squareCentricPos, 0, 64);
        System.arraycopy(PieceArray, 0, this.pieceArray, 0, 15);
        this.whiteToMove=whiteToMove;
    }

    public void makeMove(int move) {
        quietlyMakeMove(move);
        calculatePreCalculatedData();
    }
    private void quietlyMakeMove(int move) {
        PreviousCastlingRights.push(castlingRights);
        PreviousEnPassantTargetFiles.push(enPassantTargetFiles);
        PreviousHalfMoveTimers.push(hundredHalfmoveTimer);
        PreviousMovelists.push(Util.cloneArray(legalMoves, indexOfFirstEmptyMove));
        PreviousIndexOfFirstEmptyMove.push(indexOfFirstEmptyMove);
        previousZobristKeys[plyNumber] = zobristKey;

        byte fromSquare = Move.getFromSquareFromMove(move);
        byte toSquare = Move.getToSquareFromMove(move);
        byte moveType = Move.getMoveTypeFromMove(move);
        byte movingPiece = squareCentricPos[fromSquare];
        byte colorIndex = (byte)(movingPiece/8);
        byte index = colorIndexBoard[colorIndex][fromSquare];
        byte capturedPiece = squareCentricPos[toSquare];

        zobristKey ^= Zobrist.getKeyForMovingColor();
        zobristKey ^= Zobrist.getKeyFromPieceAndSquare(movingPiece, fromSquare);
        if (capturedPiece != 0)zobristKey ^= Zobrist.getKeyFromPieceAndSquare(capturedPiece, toSquare);
        zobristKey ^= Zobrist.getKeyFromCastlingRights(castlingRights);
        zobristKey ^= Zobrist.getKeyFromEPFile(enPassantTargetFiles);

        switch (moveType) {
            case Type.NORMAL_MOVE -> {
                colorIndexBoard[colorIndex][toSquare] = index;
                pieceSquareList[movingPiece][index] = toSquare;
            }
            case Type.EN_PASSANT -> {
                colorIndexBoard[colorIndex][toSquare] = index;
                pieceSquareList[movingPiece][index] = toSquare;

                byte squareOfCapturedPawn = (byte) (whiteToMove ? toSquare-8 : toSquare+8);
                colorIndex ^= 1;
                capturedPiece = (byte) (colorIndex*8 | Type.PAWN);

                numPieces[capturedPiece]--;
                index = colorIndexBoard[colorIndex][squareOfCapturedPawn];
                byte square = pieceSquareList[capturedPiece][numPieces[capturedPiece]];
                pieceSquareList[capturedPiece][index] = square;
                colorIndexBoard[colorIndex][square] = index;

                capturedPiece = squareCentricPos[toSquare];//reset the captured piece
            }
            case Type.DOUBLE_PAWN_MOVE -> {
                colorIndexBoard[colorIndex][toSquare] = index;
                pieceSquareList[movingPiece][index] = toSquare;
            }
            case Type.CASTLES -> {
                colorIndexBoard[colorIndex][toSquare] = index;
                pieceSquareList[movingPiece][index] = toSquare;

                switch (toSquare) {
                    case 6-> {//white castles short
                        index = colorIndexBoard[colorIndex][7];
                        movingPiece = (byte) (colorIndex*8 | Type.ROOK);
                        colorIndexBoard[colorIndex][5] = index;
                        pieceSquareList[movingPiece][index] = 5;
                    }
                    case 2-> {//white castles long
                        index = colorIndexBoard[colorIndex][0];
                        movingPiece = (byte) (colorIndex*8 | Type.ROOK);
                        colorIndexBoard[colorIndex][3] = index;
                        pieceSquareList[movingPiece][index] = 3;
                    }
                    case 62-> {//black castles short
                        index = colorIndexBoard[colorIndex][63];
                        movingPiece = (byte) (colorIndex*8 | Type.ROOK);
                        colorIndexBoard[colorIndex][61] = index;
                        pieceSquareList[movingPiece][index] = 61;
                    }
                    case 58-> {//black castles long
                        index = colorIndexBoard[colorIndex][56];
                        movingPiece = (byte) (colorIndex*8 | Type.ROOK);
                        colorIndexBoard[colorIndex][59] = index;
                        pieceSquareList[movingPiece][index] = 59;
                    }
                }
            }
            case Type.PAWN_PROMOTES_TO_Q -> {
                byte colorPromotingTo = (byte) (colorIndex*8);
                byte piecePromotingTo = (byte) (colorPromotingTo | Type.QUEEN);

                numPieces[movingPiece]--;
                index = colorIndexBoard[colorIndex][fromSquare];
                byte square = pieceSquareList[movingPiece][numPieces[movingPiece]];
                pieceSquareList[movingPiece][index] = square;
                colorIndexBoard[colorIndex][square] = index;

                pieceSquareList[piecePromotingTo][numPieces[piecePromotingTo]] = toSquare;
                colorIndexBoard[colorIndex][toSquare] = numPieces[piecePromotingTo];
                numPieces[piecePromotingTo]++;
            }
            case Type.PAWN_PROMOTES_TO_N -> {
                byte colorPromotingTo = (byte) (colorIndex*8);
                byte piecePromotingTo = (byte) (colorPromotingTo | Type.KNIGHT);

                numPieces[movingPiece]--;
                index = colorIndexBoard[colorIndex][fromSquare];
                byte square = pieceSquareList[movingPiece][numPieces[movingPiece]];
                pieceSquareList[movingPiece][index] = square;
                colorIndexBoard[colorIndex][square] = index;

                pieceSquareList[piecePromotingTo][numPieces[piecePromotingTo]] = toSquare;
                colorIndexBoard[colorIndex][toSquare] = numPieces[piecePromotingTo];
                numPieces[piecePromotingTo]++;
            }
            case Type.PAWN_PROMOTES_TO_B -> {
                byte colorPromotingTo = (byte) (colorIndex*8);
                byte piecePromotingTo = (byte) (colorPromotingTo | Type.BISHOP);

                numPieces[movingPiece]--;
                index = colorIndexBoard[colorIndex][fromSquare];
                byte square = pieceSquareList[movingPiece][numPieces[movingPiece]];
                pieceSquareList[movingPiece][index] = square;
                colorIndexBoard[colorIndex][square] = index;

                pieceSquareList[piecePromotingTo][numPieces[piecePromotingTo]] = toSquare;
                colorIndexBoard[colorIndex][toSquare] = numPieces[piecePromotingTo];
                numPieces[piecePromotingTo]++;
            }
            case Type.PAWN_PROMOTES_TO_R -> {
                byte colorPromotingTo = (byte) (colorIndex*8);
                byte piecePromotingTo = (byte) (colorPromotingTo | Type.ROOK);

                numPieces[movingPiece]--;
                index = colorIndexBoard[colorIndex][fromSquare];
                byte square = pieceSquareList[movingPiece][numPieces[movingPiece]];
                pieceSquareList[movingPiece][index] = square;
                colorIndexBoard[colorIndex][square] = index;

                pieceSquareList[piecePromotingTo][numPieces[piecePromotingTo]] = toSquare;
                colorIndexBoard[colorIndex][toSquare] = numPieces[piecePromotingTo];
                numPieces[piecePromotingTo]++;
            }
        }
        if (capturedPiece != Type.EMPTY) {//update piece list for captured piece
            numPieces[capturedPiece]--;
            colorIndex ^= 1;
            index = colorIndexBoard[colorIndex][toSquare];
            byte lastSquare = pieceSquareList[capturedPiece][numPieces[capturedPiece]];
            pieceSquareList[capturedPiece][index] = lastSquare;
            colorIndexBoard[colorIndex][lastSquare] = index;
        }

        switch (moveType) {
            case Type.NORMAL_MOVE -> {
                long fromSquareBB = Util.toBitboard(fromSquare);
                long toSquareBB = Util.toBitboard(toSquare);

                squareCentricPos[fromSquare]= Type.EMPTY;//updates redundant squareCentricPosition
                squareCentricPos[toSquare]=movingPiece;//updates redundant squareCentricPosition

                pieceArray[movingPiece]^=fromSquareBB|toSquareBB;//moves piece on bb
                pieceArray[capturedPiece]^=toSquareBB;//clears captured piece from bb

                enPassantTargetFiles=0;
                whiteToMove= !whiteToMove;
                hundredHalfmoveTimer++;
                if (capturedPiece!=0||movingPiece%8== Type.PAWN)hundredHalfmoveTimer=0;//resets Half-move timer to 0 if capture/pawn push

                //Can AND the king bitshifted each direction with castlingRights to check if the king moved, if it does, will lose the right to castle
                castlingRights &= pieceArray[Type.WHITE | Type.KING]<<2 | pieceArray[Type.WHITE | Type.KING]>>>2 | pieceArray[Type.BLACK | Type.KING]<<2 | pieceArray[Type.BLACK | Type.KING]>>>2;

                //Can AND the rooks bitshifted to where the king will be to check if the rook still exists on that square, if it doesn't, will lose the right to castle
                castlingRights &= pieceArray[Type.WHITE | Type.ROOK]<<2 | (pieceArray[Type.WHITE | Type.ROOK] & Constants.CORNERS)>>>1 | pieceArray[Type.BLACK | Type.ROOK]<<2 | (pieceArray[Type.BLACK | Type.ROOK] & Constants.CORNERS)>>>1;

                zobristKey ^= Zobrist.getKeyFromPieceAndSquare(movingPiece, toSquare);
            }
            case Type.EN_PASSANT -> {
                quietlyEnPassant(fromSquare, toSquare);

                zobristKey ^= Zobrist.getKeyFromPieceAndSquare(movingPiece, toSquare);
                byte capturedSquare = (byte) (whiteToMove ? toSquare+8 : toSquare-8);//called on after white to move is switched
                byte capturedColor = (byte) (colorIndex*8);

                zobristKey ^= Zobrist.getKeyFromPieceAndSquare(capturedColor | Type.PAWN, capturedSquare);
            }
            case Type.DOUBLE_PAWN_MOVE -> {
                long fromSquareBB = Util.toBitboard(fromSquare);
                long toSquareBB = Util.toBitboard(toSquare);

                squareCentricPos[fromSquare]= Type.EMPTY;//updates redundant squareCentricPosition
                squareCentricPos[toSquare]=movingPiece;//updates redundant squareCentricPosition

                pieceArray[movingPiece]^=fromSquareBB|toSquareBB;//moves piece on bb

                hundredHalfmoveTimer=0;
                whiteToMove= !whiteToMove;
                enPassantTargetFiles = 1<<toSquare%8;//creates enPassantTargetFile

                zobristKey ^= Zobrist.getKeyFromPieceAndSquare(movingPiece, toSquare);
            }
            case Type.CASTLES -> {
                switch (toSquare) {
                    case 6 -> {//white castles short
                        pieceArray[Type.WHITE | Type.KING]^= Constants.g1 | Constants.e1;
                        pieceArray[Type.WHITE | Type.ROOK]^= Constants.h1 | Constants.f1;
                        squareCentricPos[6]= Type.WHITE | Type.KING;
                        squareCentricPos[5]= Type.WHITE | Type.ROOK;
                        squareCentricPos[4]= Type.EMPTY;
                        squareCentricPos[7]= Type.EMPTY;

                        castlingRights&= Type.NOT_WHITE_CAN_CS & Type.NOT_WHITE_CAN_CL;

                        zobristKey ^= Zobrist.getKeyFromPieceAndSquare(movingPiece, toSquare);
                        zobristKey ^= Zobrist.getKeyFromPieceAndSquare(Type.ROOK, 7);
                        zobristKey ^= Zobrist.getKeyFromPieceAndSquare(Type.ROOK, 5);
                    }
                    case 62 -> {//black castles short
                        pieceArray[Type.BLACK | Type.KING]^= Constants.g8 | Constants.e8;
                        pieceArray[Type.BLACK | Type.ROOK]^= Constants.h8 | Constants.f8;
                        squareCentricPos[62]= Type.BLACK | Type.KING;
                        squareCentricPos[61]= Type.BLACK | Type.ROOK;
                        squareCentricPos[60]= Type.EMPTY;
                        squareCentricPos[63]= Type.EMPTY;

                        castlingRights&= Type.NOT_BLACK_CAN_CS & Type.NOT_BLACK_CAN_CL;

                        zobristKey ^= Zobrist.getKeyFromPieceAndSquare(movingPiece, toSquare);
                        zobristKey ^= Zobrist.getKeyFromPieceAndSquare(Type.BLACK | Type.ROOK, 63);
                        zobristKey ^= Zobrist.getKeyFromPieceAndSquare(Type.BLACK | Type.ROOK, 61);
                    }
                    case 2 -> {//white castles long
                        pieceArray[Type.WHITE | Type.KING]^= Constants.c1 | Constants.e1;
                        pieceArray[Type.WHITE | Type.ROOK]^= Constants.d1 | Constants.a1;
                        squareCentricPos[2]= Type.WHITE | Type.KING;
                        squareCentricPos[3]= Type.WHITE | Type.ROOK;
                        squareCentricPos[0]= Type.EMPTY;
                        squareCentricPos[4]= Type.EMPTY;

                        castlingRights&= Type.NOT_WHITE_CAN_CS & Type.NOT_WHITE_CAN_CL;

                        zobristKey ^= Zobrist.getKeyFromPieceAndSquare(movingPiece, toSquare);
                        zobristKey ^= Zobrist.getKeyFromPieceAndSquare(Type.ROOK, 0);
                        zobristKey ^= Zobrist.getKeyFromPieceAndSquare(Type.ROOK, 3);
                    }
                    case 58 -> {//black castles long
                        pieceArray[Type.BLACK | Type.KING]^= Constants.c8 | Constants.e8;
                        pieceArray[Type.BLACK | Type.ROOK]^= Constants.d8 | Constants.a8;
                        squareCentricPos[58]= Type.BLACK | Type.KING;
                        squareCentricPos[59]= Type.BLACK | Type.ROOK;
                        squareCentricPos[56]= Type.EMPTY;
                        squareCentricPos[60]= Type.EMPTY;

                        castlingRights&= Type.NOT_BLACK_CAN_CS & Type.NOT_BLACK_CAN_CL;

                        zobristKey ^= Zobrist.getKeyFromPieceAndSquare(movingPiece, toSquare);
                        zobristKey ^= Zobrist.getKeyFromPieceAndSquare(Type.BLACK | Type.ROOK, 56);
                        zobristKey ^= Zobrist.getKeyFromPieceAndSquare(Type.BLACK | Type.ROOK, 59);
                    }
                }

                hundredHalfmoveTimer++;
                enPassantTargetFiles=0;
                whiteToMove= !whiteToMove;
            }
            case Type.PAWN_PROMOTES_TO_Q -> {
                short colorPromoting= Type.WHITE;
                if (toSquare/8==0) {
                    colorPromoting= Type.BLACK;
                }

                long fromSquareBB = Util.toBitboard(fromSquare);
                long toSquareBB = Util.toBitboard(toSquare);

                squareCentricPos[fromSquare]= Type.EMPTY;//updates redundant squareCentricPosition
                squareCentricPos[toSquare]= (byte)(colorPromoting | Type.QUEEN);//updates redundant squareCentricPosition

                pieceArray[colorPromoting | Type.PAWN]^=fromSquareBB;
                pieceArray[colorPromoting | Type.QUEEN]^=toSquareBB;
                pieceArray[capturedPiece] ^=toSquareBB;

                zobristKey ^= Zobrist.getKeyFromPieceAndSquare((byte)(colorPromoting | Type.QUEEN), toSquare);

                hundredHalfmoveTimer=0;
                whiteToMove= !whiteToMove;
                enPassantTargetFiles=0;
            }
            case Type.PAWN_PROMOTES_TO_N -> {
                short colorPromoting= Type.WHITE;
                if (toSquare/8==0) {
                    colorPromoting= Type.BLACK;
                }

                long fromSquareBB = Util.toBitboard(fromSquare);
                long toSquareBB = Util.toBitboard(toSquare);

                squareCentricPos[fromSquare]= Type.EMPTY;//updates redundant squareCentricPosition
                squareCentricPos[toSquare]= (byte)(colorPromoting | Type.KNIGHT);//updates redundant squareCentricPosition

                pieceArray[colorPromoting | Type.PAWN]^=fromSquareBB;
                pieceArray[colorPromoting | Type.KNIGHT]^=toSquareBB;
                pieceArray[capturedPiece] ^=toSquareBB;

                zobristKey ^= Zobrist.getKeyFromPieceAndSquare((byte)(colorPromoting | Type.KNIGHT), toSquare);

                hundredHalfmoveTimer=0;
                whiteToMove= !whiteToMove;
                enPassantTargetFiles=0;
            }
            case Type.PAWN_PROMOTES_TO_B -> {
                short colorPromoting= Type.WHITE;
                if (toSquare/8==0) {
                    colorPromoting= Type.BLACK;
                }

                long fromSquareBB = Util.toBitboard(fromSquare);
                long toSquareBB = Util.toBitboard(toSquare);

                squareCentricPos[fromSquare]= Type.EMPTY;//updates redundant squareCentricPosition
                squareCentricPos[toSquare]= (byte)(colorPromoting | Type.BISHOP);//updates redundant squareCentricPosition

                pieceArray[colorPromoting | Type.PAWN]^=fromSquareBB;
                pieceArray[colorPromoting | Type.BISHOP]^=toSquareBB;
                pieceArray[capturedPiece] ^=toSquareBB;

                zobristKey ^= Zobrist.getKeyFromPieceAndSquare((byte)(colorPromoting | Type.BISHOP), toSquare);

                hundredHalfmoveTimer=0;
                whiteToMove= !whiteToMove;
                enPassantTargetFiles=0;
            }
            case Type.PAWN_PROMOTES_TO_R -> {
                short colorPromoting= Type.WHITE;
                if (toSquare/8==0) {
                    colorPromoting= Type.BLACK;
                }

                long fromSquareBB = Util.toBitboard(fromSquare);
                long toSquareBB = Util.toBitboard(toSquare);

                squareCentricPos[fromSquare]= Type.EMPTY;//updates redundant squareCentricPosition
                squareCentricPos[toSquare]= (byte)(colorPromoting | Type.ROOK);//updates redundant squareCentricPosition

                pieceArray[colorPromoting | Type.PAWN]^=fromSquareBB;
                pieceArray[colorPromoting | Type.ROOK]^=toSquareBB;
                pieceArray[capturedPiece] ^=toSquareBB;

                zobristKey ^= Zobrist.getKeyFromPieceAndSquare((byte)(colorPromoting | Type.ROOK), toSquare);

                hundredHalfmoveTimer=0;
                whiteToMove= !whiteToMove;
                enPassantTargetFiles=0;
            }
        }

        zobristKey ^= Zobrist.getKeyFromCastlingRights(castlingRights);
        zobristKey ^= Zobrist.getKeyFromEPFile(enPassantTargetFiles);

        indexOfFirstEmptyMove=0;
        plyNumber++;
    }
    public void unmakeMove(int move) {
        byte fromSquare = Move.getFromSquareFromMove(move);
        byte toSquare = Move.getToSquareFromMove(move);
        byte moveType = Move.getMoveTypeFromMove(move);
        byte movingPiece = squareCentricPos[toSquare];
        byte colorIndex = (byte)(movingPiece/8);
        byte index = colorIndexBoard[colorIndex][toSquare];
        byte capturedPiece = Move.getCapturedPieceFromMove(move);

        zobristKey ^= Zobrist.getKeyForMovingColor();
        zobristKey ^= Zobrist.getKeyFromPieceAndSquare(movingPiece, fromSquare);
        if (capturedPiece != 0)zobristKey ^= Zobrist.getKeyFromPieceAndSquare(capturedPiece, toSquare);
        zobristKey ^= Zobrist.getKeyFromCastlingRights(castlingRights);
        zobristKey ^= Zobrist.getKeyFromEPFile(enPassantTargetFiles);

        switch (moveType) {
            case Type.NORMAL_MOVE -> {
                colorIndexBoard[colorIndex][fromSquare] = index;
                pieceSquareList[movingPiece][index] = fromSquare;
                zobristKey ^= Zobrist.getKeyFromPieceAndSquare(movingPiece, toSquare);
            }
            case Type.EN_PASSANT -> {
                colorIndexBoard[colorIndex][fromSquare] = index;
                pieceSquareList[movingPiece][index] = fromSquare;

                byte squareOfCapturedPawn = (byte) (!whiteToMove ? toSquare-8 : toSquare+8);//try switching these if doesn't work on first try
                colorIndex ^= 1;
                capturedPiece = (byte) (colorIndex*8 | Type.PAWN);

                pieceSquareList[capturedPiece][numPieces[capturedPiece]] = squareOfCapturedPawn;
                colorIndexBoard[colorIndex][squareOfCapturedPawn] = numPieces[capturedPiece];
                numPieces[capturedPiece]++;

                zobristKey ^= Zobrist.getKeyFromPieceAndSquare(movingPiece, toSquare);
                zobristKey ^= Zobrist.getKeyFromPieceAndSquare(capturedPiece,squareOfCapturedPawn);

                capturedPiece = Move.getCapturedPieceFromMove(move);
            }
            case Type.DOUBLE_PAWN_MOVE -> {
                colorIndexBoard[colorIndex][fromSquare] = index;
                pieceSquareList[movingPiece][index] = fromSquare;
                zobristKey ^= Zobrist.getKeyFromPieceAndSquare(movingPiece, toSquare);
            }
            case Type.CASTLES -> {
                colorIndexBoard[colorIndex][fromSquare] = index;
                pieceSquareList[movingPiece][index] = fromSquare;

                switch (toSquare) {
                    case 6-> {//white castles short
                        index = colorIndexBoard[colorIndex][5];
                        movingPiece = (byte) (colorIndex*8 | Type.ROOK);
                        colorIndexBoard[colorIndex][7] = index;
                        pieceSquareList[movingPiece][index] = 7;

                        zobristKey ^= Zobrist.getKeyFromPieceAndSquare(movingPiece, toSquare);
                        zobristKey ^= Zobrist.getKeyFromPieceAndSquare(Type.ROOK, 7);
                        zobristKey ^= Zobrist.getKeyFromPieceAndSquare(Type.ROOK, 5);
                    }
                    case 2-> {//white castles long
                        index = colorIndexBoard[colorIndex][3];
                        movingPiece = (byte) (colorIndex*8 | Type.ROOK);
                        colorIndexBoard[colorIndex][0] = index;
                        pieceSquareList[movingPiece][index] = 0;

                        zobristKey ^= Zobrist.getKeyFromPieceAndSquare(movingPiece, toSquare);
                        zobristKey ^= Zobrist.getKeyFromPieceAndSquare(Type.ROOK, 0);
                        zobristKey ^= Zobrist.getKeyFromPieceAndSquare(Type.ROOK, 3);
                    }
                    case 62-> {//black castles short
                        index = colorIndexBoard[colorIndex][61];
                        movingPiece = (byte) (colorIndex*8 | Type.ROOK);
                        colorIndexBoard[colorIndex][63] = index;
                        pieceSquareList[movingPiece][index] = 63;

                        zobristKey ^= Zobrist.getKeyFromPieceAndSquare(movingPiece, toSquare);
                        zobristKey ^= Zobrist.getKeyFromPieceAndSquare(Type.BLACK | Type.ROOK, 63);
                        zobristKey ^= Zobrist.getKeyFromPieceAndSquare(Type.BLACK | Type.ROOK, 61);
                    }
                    case 58-> {//black castles long
                        index = colorIndexBoard[colorIndex][59];
                        movingPiece = (byte) (colorIndex*8 | Type.ROOK);
                        colorIndexBoard[colorIndex][56] = index;
                        pieceSquareList[movingPiece][index] = 56;

                        zobristKey ^= Zobrist.getKeyFromPieceAndSquare(movingPiece, toSquare);
                        zobristKey ^= Zobrist.getKeyFromPieceAndSquare(Type.BLACK | Type.ROOK, 56);
                        zobristKey ^= Zobrist.getKeyFromPieceAndSquare(Type.BLACK | Type.ROOK, 59);
                    }
                }
            }
            case Type.PAWN_PROMOTES_TO_Q -> {
                byte colorPromoting = (byte) (colorIndex*8);
                byte piecePromotedTo = squareCentricPos[toSquare];
                byte piecePromotedFrom = (byte) (colorPromoting | Type.PAWN);

                numPieces[piecePromotedTo]--;
                index = colorIndexBoard[colorIndex][toSquare];
                byte square = pieceSquareList[piecePromotedTo][numPieces[piecePromotedTo]];
                pieceSquareList[piecePromotedTo][index] = square;
                colorIndexBoard[colorIndex][square] = index;

                pieceSquareList[piecePromotedFrom][numPieces[piecePromotedFrom]] = fromSquare;
                colorIndexBoard[colorIndex][fromSquare] = numPieces[piecePromotedFrom];
                numPieces[piecePromotedFrom]++;

                zobristKey ^= Zobrist.getKeyFromPieceAndSquare(movingPiece, fromSquare);//removes the mistaken zobrist key edit, moving piece was incorrect
                zobristKey ^= Zobrist.getKeyFromPieceAndSquare(colorPromoting | Type.PAWN, fromSquare);
                zobristKey ^= Zobrist.getKeyFromPieceAndSquare(colorPromoting | Type.QUEEN, toSquare);
            }
            case Type.PAWN_PROMOTES_TO_N -> {
                byte colorPromoting = (byte) (colorIndex*8);
                byte piecePromotedTo = squareCentricPos[toSquare];
                byte piecePromotedFrom = (byte) (colorPromoting | Type.PAWN);

                numPieces[piecePromotedTo]--;
                index = colorIndexBoard[colorIndex][toSquare];
                byte square = pieceSquareList[piecePromotedTo][numPieces[piecePromotedTo]];
                pieceSquareList[piecePromotedTo][index] = square;
                colorIndexBoard[colorIndex][square] = index;

                pieceSquareList[piecePromotedFrom][numPieces[piecePromotedFrom]] = fromSquare;
                colorIndexBoard[colorIndex][fromSquare] = numPieces[piecePromotedFrom];
                numPieces[piecePromotedFrom]++;

                zobristKey ^= Zobrist.getKeyFromPieceAndSquare(movingPiece, fromSquare);//removes the mistaken zobrist key edit, moving piece was incorrect
                zobristKey ^= Zobrist.getKeyFromPieceAndSquare(colorPromoting | Type.PAWN, fromSquare);
                zobristKey ^= Zobrist.getKeyFromPieceAndSquare(colorPromoting | Type.KNIGHT, toSquare);
            }
            case Type.PAWN_PROMOTES_TO_B -> {
                byte colorPromoting = (byte) (colorIndex*8);
                byte piecePromotedTo = squareCentricPos[toSquare];
                byte piecePromotedFrom = (byte) (colorPromoting | Type.PAWN);

                numPieces[piecePromotedTo]--;
                index = colorIndexBoard[colorIndex][toSquare];
                byte square = pieceSquareList[piecePromotedTo][numPieces[piecePromotedTo]];
                pieceSquareList[piecePromotedTo][index] = square;
                colorIndexBoard[colorIndex][square] = index;

                pieceSquareList[piecePromotedFrom][numPieces[piecePromotedFrom]] = fromSquare;
                colorIndexBoard[colorIndex][fromSquare] = numPieces[piecePromotedFrom];
                numPieces[piecePromotedFrom]++;

                zobristKey ^= Zobrist.getKeyFromPieceAndSquare(movingPiece, fromSquare);//removes the mistaken zobrist key edit, moving piece was incorrect
                zobristKey ^= Zobrist.getKeyFromPieceAndSquare(colorPromoting | Type.PAWN, fromSquare);
                zobristKey ^= Zobrist.getKeyFromPieceAndSquare(colorPromoting | Type.BISHOP, toSquare);
            }
            case Type.PAWN_PROMOTES_TO_R -> {
                byte colorPromoting = (byte) (colorIndex*8);
                byte piecePromotedTo = squareCentricPos[toSquare];
                byte piecePromotedFrom = (byte) (colorPromoting | Type.PAWN);

                numPieces[piecePromotedTo]--;
                index = colorIndexBoard[colorIndex][toSquare];
                byte square = pieceSquareList[piecePromotedTo][numPieces[piecePromotedTo]];
                pieceSquareList[piecePromotedTo][index] = square;
                colorIndexBoard[colorIndex][square] = index;

                pieceSquareList[piecePromotedFrom][numPieces[piecePromotedFrom]] = fromSquare;
                colorIndexBoard[colorIndex][fromSquare] = numPieces[piecePromotedFrom];
                numPieces[piecePromotedFrom]++;

                zobristKey ^= Zobrist.getKeyFromPieceAndSquare(movingPiece, fromSquare);//removes the mistaken zobrist key edit, moving piece was incorrect
                zobristKey ^= Zobrist.getKeyFromPieceAndSquare(colorPromoting | Type.PAWN, fromSquare);
                zobristKey ^= Zobrist.getKeyFromPieceAndSquare(colorPromoting | Type.ROOK, toSquare);
            }
        }

        if (capturedPiece !=0) {
            colorIndex ^=1;
            pieceSquareList[capturedPiece][numPieces[capturedPiece]] = toSquare;
            colorIndexBoard[colorIndex][toSquare] = numPieces[capturedPiece];
            numPieces[capturedPiece]++;
        }

        switch (moveType) {
            case Type.NORMAL_MOVE -> {
                long fromSquareBB = Util.toBitboard(fromSquare);
                long toSquareBB = Util.toBitboard(toSquare);

                squareCentricPos[toSquare]= capturedPiece;//updates redundant squareCentricPosition
                squareCentricPos[fromSquare]=movingPiece;//updates redundant squareCentricPosition

                pieceArray[movingPiece]^=fromSquareBB|toSquareBB;
                pieceArray[capturedPiece]^=toSquareBB;
            }
            case Type.DOUBLE_PAWN_MOVE -> {
                long fromSquareBB = Util.toBitboard(fromSquare);
                long toSquareBB = Util.toBitboard(toSquare);

                squareCentricPos[toSquare]= Type.EMPTY;
                squareCentricPos[fromSquare]=movingPiece;

                pieceArray[movingPiece]^=fromSquareBB|toSquareBB;
            }
            case Type.CASTLES -> {
                long toSquareBB=Util.toBitboard(toSquare);

                if (toSquareBB==Constants.g1) {//white castles short
                    pieceArray[Type.WHITE | Type.KING]^= Constants.g1 | Constants.e1;
                    pieceArray[Type.WHITE | Type.ROOK]^= Constants.h1 | Constants.f1;
                    squareCentricPos[6]= Type.EMPTY;
                    squareCentricPos[5]= Type.EMPTY;
                    squareCentricPos[4]= Type.WHITE | Type.KING;
                    squareCentricPos[7]= Type.WHITE | Type.ROOK;
                }
                else if (toSquareBB==Constants.g8) {//black castles short
                    pieceArray[Type.BLACK | Type.KING]^= Constants.g8 | Constants.e8;
                    pieceArray[Type.BLACK | Type.ROOK]^= Constants.h8 | Constants.f8;
                    squareCentricPos[62]= Type.EMPTY;
                    squareCentricPos[61]= Type.EMPTY;
                    squareCentricPos[60]= Type.BLACK | Type.KING;
                    squareCentricPos[63]= Type.BLACK | Type.ROOK;
                }
                else if (toSquareBB==Constants.c1) {//white castles long
                    pieceArray[Type.WHITE | Type.KING]^= Constants.c1 | Constants.e1;
                    pieceArray[Type.WHITE | Type.ROOK]^= Constants.d1 | Constants.a1;
                    squareCentricPos[2]= Type.EMPTY;
                    squareCentricPos[3]= Type.EMPTY;
                    squareCentricPos[0]= Type.WHITE | Type.ROOK;
                    squareCentricPos[4]= Type.WHITE | Type.KING;
                }
                else if (toSquareBB==Constants.c8) {//black castles long
                    pieceArray[Type.BLACK | Type.KING]^= Constants.c8 | Constants.e8;
                    pieceArray[Type.BLACK | Type.ROOK]^= Constants.d8 | Constants.a8;
                    squareCentricPos[58]= Type.EMPTY;
                    squareCentricPos[59]= Type.EMPTY;
                    squareCentricPos[56]= Type.BLACK | Type.ROOK;
                    squareCentricPos[60]= Type.BLACK | Type.KING;
                }
            }
            case Type.EN_PASSANT -> {
                long fromSquareBB = Util.toBitboard(fromSquare);
                long toSquareBB = Util.toBitboard(toSquare);

                int enPassantCapturedSquare=toSquare-8;
                short colorNotMoving= Type.BLACK;
                if (whiteToMove){//inverse of not white to move
                    enPassantCapturedSquare=toSquare+8;
                    colorNotMoving= Type.WHITE;
                }

                squareCentricPos[toSquare]= Type.EMPTY;//updates redundant squareCentricPosition
                squareCentricPos[fromSquare]=movingPiece;//updates redundant squareCentricPosition
                squareCentricPos[enPassantCapturedSquare]= (byte)(colorNotMoving | Type.PAWN);

                pieceArray[movingPiece]^=fromSquareBB|toSquareBB;
                pieceArray[colorNotMoving | Type.PAWN]^=Util.toBitboard(enPassantCapturedSquare);
            }
            case Type.PAWN_PROMOTES_TO_Q -> {
                short colorPromoting= toSquare/8==7 ? Type.WHITE : Type.BLACK;

                long fromSquareBB = Util.toBitboard(fromSquare);
                long toSquareBB = Util.toBitboard(toSquare);

                squareCentricPos[toSquare]= capturedPiece;//updates redundant squareCentricPosition
                squareCentricPos[fromSquare]= (byte)(colorPromoting | Type.PAWN);//updates redundant squareCentricPosition

                pieceArray[colorPromoting | Type.PAWN]^=fromSquareBB;
                pieceArray[colorPromoting | Type.QUEEN]^=toSquareBB;
                pieceArray[capturedPiece] ^=toSquareBB;
            }
            case Type.PAWN_PROMOTES_TO_N -> {
                short colorPromoting= toSquare/8==7 ? Type.WHITE : Type.BLACK;

                long fromSquareBB = Util.toBitboard(fromSquare);
                long toSquareBB = Util.toBitboard(toSquare);

                squareCentricPos[toSquare]= capturedPiece;//updates redundant squareCentricPosition
                squareCentricPos[fromSquare]= (byte)(colorPromoting | Type.PAWN);//updates redundant squareCentricPosition

                pieceArray[colorPromoting | Type.PAWN]^=fromSquareBB;
                pieceArray[colorPromoting | Type.KNIGHT]^=toSquareBB;
                pieceArray[capturedPiece] ^=toSquareBB;
            }
            case Type.PAWN_PROMOTES_TO_B -> {
                short colorPromoting= toSquare/8==7 ? Type.WHITE : Type.BLACK;

                long fromSquareBB = Util.toBitboard(fromSquare);
                long toSquareBB = Util.toBitboard(toSquare);

                squareCentricPos[toSquare]= capturedPiece;//updates redundant squareCentricPosition
                squareCentricPos[fromSquare]= (byte)(colorPromoting | Type.PAWN);//updates redundant squareCentricPosition

                pieceArray[colorPromoting | Type.PAWN]^=fromSquareBB;
                pieceArray[colorPromoting | Type.BISHOP]^=toSquareBB;
                pieceArray[capturedPiece] ^=toSquareBB;
            }
            case Type.PAWN_PROMOTES_TO_R -> {
                short colorPromoting= toSquare/8==7 ? Type.WHITE : Type.BLACK;

                long fromSquareBB = Util.toBitboard(fromSquare);
                long toSquareBB = Util.toBitboard(toSquare);

                squareCentricPos[toSquare]= capturedPiece;//updates redundant squareCentricPosition
                squareCentricPos[fromSquare]= (byte)(colorPromoting | Type.PAWN);//updates redundant squareCentricPosition

                pieceArray[colorPromoting | Type.PAWN]^=fromSquareBB;
                pieceArray[colorPromoting | Type.ROOK]^=toSquareBB;
                pieceArray[capturedPiece] ^=toSquareBB;
            }
        }

        whiteToMove= !whiteToMove;
        plyNumber--;
        enPassantTargetFiles= PreviousEnPassantTargetFiles.pop();
        castlingRights= PreviousCastlingRights.pop();
        hundredHalfmoveTimer= PreviousHalfMoveTimers.pop();
        legalMoves= PreviousMovelists.pop();
        indexOfFirstEmptyMove= PreviousIndexOfFirstEmptyMove.pop();

        zobristKey ^= Zobrist.getKeyFromCastlingRights(castlingRights);
        zobristKey ^= Zobrist.getKeyFromEPFile(enPassantTargetFiles);

        calculatePreCalculatedData();
    }
    public void calculatePreCalculatedData() {//finds piece locations, piece attacks, pins, and checks
        calculatePieceLocations();
        calculateSquareAttacks();
        calculateInCheck();
        calculateCheckResolveRay();
        calculatePinRay();
    }
    public void calculateLegalMoves() {//ALSO FINDS THE GAME STATE
        findLegalMoves();
        gameState = calculateGameState();
    }
    public void calculateCapturingMovesOnly() {//ALSO FINDS THE GAME STATE
        findLegalCapturingMoves();
        gameState = calculateGameStateWhenOnlyCaptures();//is possible that the game state is incorrect, could be mate or stalemate
    }

    public void calculatePieceLocations() {
        whitePieces = pieceArray[Type.WHITE | Type.PAWN]| pieceArray[Type.WHITE | Type.KNIGHT]| pieceArray[Type.WHITE | Type.BISHOP]| pieceArray[Type.WHITE | Type.ROOK]| pieceArray[Type.WHITE | Type.QUEEN]| pieceArray[Type.WHITE | Type.KING];
        blackPieces = pieceArray[Type.BLACK | Type.PAWN]| pieceArray[Type.BLACK | Type.KNIGHT]| pieceArray[Type.BLACK | Type.BISHOP]| pieceArray[Type.BLACK | Type.ROOK]| pieceArray[Type.BLACK | Type.QUEEN]| pieceArray[Type.BLACK | Type.KING];
        allPieces = whitePieces | blackPieces;
        emptySquares = ~allPieces;
    }
    public void calculateSquareAttacks() {
        long tempPieceBB= pieceArray[Type.WHITE | Type.PAWN];

        for (int i=0;i<64;i++) {
            squareAttacksArray[i]=0;
        }
        for (int i=1;i<7;i++) {
            whiteAttacksArray[i]=0;
            blackAttacksArray[i]=0;
        }

        long whiteBlockers = allPieces ^ pieceArray[Type.BLACK | Type.KING];
        long blackBlockers = allPieces ^ pieceArray[Type.WHITE | Type.KING];

        for (int i = 0; i<numPieces[Type.WHITE | Type.PAWN]; i++) {
            int pieceSquare = pieceSquareList[Type.WHITE | Type.PAWN][i];
            long pieceAttackBB = PieceAttack.lookUpWhitePawnAttacks(pieceSquare);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            whiteAttacksArray[Type.PAWN] |= pieceAttackBB;
        }

        for (int i = 0; i<numPieces[Type.WHITE | Type.KNIGHT]; i++) {
            int pieceSquare = pieceSquareList[Type.WHITE | Type.KNIGHT][i];
            long pieceAttackBB = PieceAttack.lookUpKnightAttacks(pieceSquare);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            whiteAttacksArray[Type.KNIGHT] |= pieceAttackBB;
        }

        for (int i = 0; i<numPieces[Type.WHITE | Type.BISHOP]; i++) {
            int pieceSquare = pieceSquareList[Type.WHITE | Type.BISHOP][i];
            long pieceAttackBB = PieceAttack.lookUpBishopAttacks(pieceSquare, whiteBlockers);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            whiteAttacksArray[Type.BISHOP] |= pieceAttackBB;
        }

        for (int i = 0; i<numPieces[Type.WHITE | Type.ROOK]; i++) {
            int pieceSquare = pieceSquareList[Type.WHITE | Type.ROOK][i];
            long pieceAttackBB = PieceAttack.lookUpRookAttacks(pieceSquare, whiteBlockers);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            whiteAttacksArray[Type.ROOK] |= pieceAttackBB;
        }

        for (int i = 0; i<numPieces[Type.WHITE | Type.QUEEN]; i++) {
            int pieceSquare = pieceSquareList[Type.WHITE | Type.QUEEN][i];
            long pieceAttackBB = PieceAttack.lookUpQueenAttacks(pieceSquare, whiteBlockers);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            whiteAttacksArray[Type.QUEEN] |= pieceAttackBB;
        }

        {
            int pieceSquare = pieceSquareList[Type.WHITE | Type.KING][0];
            long pieceAttackBB = PieceAttack.lookUpKingAttacks(pieceSquare);
            squareAttacksArray[pieceSquare] = pieceAttackBB;
            whiteAttacksArray[Type.KING] |= pieceAttackBB;
        }


        for (int i = 0; i<numPieces[Type.BLACK | Type.PAWN]; i++) {
            int pieceSquare = pieceSquareList[Type.BLACK | Type.PAWN][i];
            long pieceAttackBB = PieceAttack.lookUpBlackPawnAttacks(pieceSquare);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            blackAttacksArray[Type.PAWN] |= pieceAttackBB;
        }

        for (int i = 0; i<numPieces[Type.BLACK | Type.KNIGHT]; i++) {
            int pieceSquare = pieceSquareList[Type.BLACK | Type.KNIGHT][i];
            long pieceAttackBB = PieceAttack.lookUpKnightAttacks(pieceSquare);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            blackAttacksArray[Type.KNIGHT] |= pieceAttackBB;
        }

        for (int i = 0; i<numPieces[Type.BLACK | Type.BISHOP]; i++) {
            int pieceSquare = pieceSquareList[Type.BLACK | Type.BISHOP][i];
            long pieceAttackBB = PieceAttack.lookUpBishopAttacks(pieceSquare, blackBlockers);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            blackAttacksArray[Type.BISHOP] |= pieceAttackBB;
        }

        for (int i = 0; i<numPieces[Type.BLACK | Type.ROOK]; i++) {
            int pieceSquare = pieceSquareList[Type.BLACK | Type.ROOK][i];
            long pieceAttackBB = PieceAttack.lookUpRookAttacks(pieceSquare, blackBlockers);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            blackAttacksArray[Type.ROOK] |= pieceAttackBB;
        }

        for (int i = 0; i<numPieces[Type.BLACK | Type.QUEEN]; i++) {
            int pieceSquare = pieceSquareList[Type.BLACK | Type.QUEEN][i];
            long pieceAttackBB = PieceAttack.lookUpQueenAttacks(pieceSquare, blackBlockers);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            blackAttacksArray[Type.QUEEN] |= pieceAttackBB;
        }

        {
            int pieceSquare = pieceSquareList[Type.BLACK | Type.KING][0];
            long pieceAttackBB = PieceAttack.lookUpKingAttacks(pieceSquare);
            squareAttacksArray[pieceSquare] = pieceAttackBB;
            blackAttacksArray[Type.KING] |= pieceAttackBB;
        }
    }
    public void calculateInCheck() {
        numChecks=0;
        whiteAttacks = whiteAttacksArray[Type.PAWN] | whiteAttacksArray[Type.KNIGHT] | whiteAttacksArray[Type.BISHOP] | whiteAttacksArray[Type.ROOK] | whiteAttacksArray[Type.QUEEN] | whiteAttacksArray[Type.KING];
        blackAttacks = blackAttacksArray[Type.PAWN] | blackAttacksArray[Type.KNIGHT] | blackAttacksArray[Type.BISHOP] | blackAttacksArray[Type.ROOK] | blackAttacksArray[Type.QUEEN] | blackAttacksArray[Type.KING];
        if (whiteToMove) {
            if ((blackAttacksArray[Type.PAWN] & pieceArray[Type.WHITE | Type.KING]) == pieceArray[Type.WHITE | Type.KING])numChecks++;
            if ((blackAttacksArray[Type.KNIGHT] & pieceArray[Type.WHITE | Type.KING]) == pieceArray[Type.WHITE | Type.KING])numChecks++;
            if ((blackAttacksArray[Type.BISHOP] & pieceArray[Type.WHITE | Type.KING]) == pieceArray[Type.WHITE | Type.KING])numChecks++;
            if ((blackAttacksArray[Type.ROOK] & pieceArray[Type.WHITE | Type.KING]) == pieceArray[Type.WHITE | Type.KING])numChecks++;
            if ((blackAttacksArray[Type.QUEEN] & pieceArray[Type.WHITE | Type.KING]) == pieceArray[Type.WHITE | Type.KING])numChecks++;
            if ((blackAttacksArray[Type.KING] & pieceArray[Type.WHITE | Type.KING]) == pieceArray[Type.WHITE | Type.KING])numChecks++;

            inCheck= numChecks>0;
        }
        else {
            if ((whiteAttacksArray[Type.PAWN] & pieceArray[Type.BLACK | Type.KING]) == pieceArray[Type.BLACK | Type.KING])numChecks++;
            if ((whiteAttacksArray[Type.KNIGHT] & pieceArray[Type.BLACK | Type.KING]) == pieceArray[Type.BLACK | Type.KING])numChecks++;
            if ((whiteAttacksArray[Type.BISHOP] & pieceArray[Type.BLACK | Type.KING]) == pieceArray[Type.BLACK | Type.KING])numChecks++;
            if ((whiteAttacksArray[Type.ROOK] & pieceArray[Type.BLACK | Type.KING]) == pieceArray[Type.BLACK | Type.KING])numChecks++;
            if ((whiteAttacksArray[Type.QUEEN] & pieceArray[Type.BLACK | Type.KING]) == pieceArray[Type.BLACK | Type.KING])numChecks++;
            if ((whiteAttacksArray[Type.KING] & pieceArray[Type.BLACK | Type.KING]) == pieceArray[Type.BLACK | Type.KING])numChecks++;

            inCheck= numChecks>0;
        }
    }
    public void calculateCheckResolveRay() {
        if (inCheck){
            if (whiteToMove) {
                if (numChecks>1)checkResolveRay=0;//if double check must move king
                else checkResolveRay=findCheckResolveRay(pieceArray[Type.WHITE | Type.KING]);
            }
            else {//black to move
                if (numChecks>1)checkResolveRay=0;//if double check must move king
                else checkResolveRay=findCheckResolveRay(pieceArray[Type.BLACK | Type.KING]);
            }
        }
        else checkResolveRay=~0;
    }
    public void calculatePinRay() {
        pinnedPieces=0;
        if (whiteToMove) {
            long kingLocationBB = pieceArray[Type.KING];
            int kingLocation = Long.numberOfTrailingZeros(kingLocationBB);

            long rookPinners = pieceArray[Type.BLACK | Type.QUEEN] | pieceArray[Type.BLACK | Type.ROOK];
            long bishopPinners = pieceArray[Type.BLACK | Type.QUEEN] | pieceArray[Type.BLACK | Type.BISHOP];

            long possibleRookPinners = PieceAttack.lookUpRookAttacks(kingLocation, rookPinners) & rookPinners;//bitboard of rooks/queens inline with king
            long possibleBishopPinners = PieceAttack.lookUpBishopAttacks(kingLocation, bishopPinners) & bishopPinners;//bitboard of bishops/queens inline with king

            long possibleBishopPinnedPieces = PieceAttack.lookUpBishopAttacks(kingLocation, allPieces) & whitePieces;
            long possibleRookPinnedPieces = PieceAttack.lookUpRookAttacks(kingLocation, allPieces) & whitePieces;//bitboard of white pieces king sees



            //is not pinned if (possiblePinner's directionMaskTowardKing & pinner | possiblePinnedPiece) ^ (possiblePinner's directionMaskTowardKing & allPieces !=0)
            for (int direction = 0; direction < 8; direction += 2) {//rook
                long kingRay = PieceAttack.maskOfLineInDirection[direction][kingLocation];
                long pinnerBB = kingRay & possibleRookPinners;

                if (pinnerBB !=0) {
                    long possiblePinRay = kingRay ^ PieceAttack.maskOfLineInDirection[direction][Long.numberOfTrailingZeros(pinnerBB)];
                    long criticalPiecesOnPinRay = possiblePinRay & (pinnerBB | possibleRookPinnedPieces);

                    if (Long.bitCount(criticalPiecesOnPinRay) == 2 && ((possiblePinRay & allPieces ^ criticalPiecesOnPinRay) ==0)) {
                        //there is a pinner and pinned on the pin ray and no more pieces blocking the pin
                        pinRay[direction] = possiblePinRay;
                        pinnedPieces |= possibleRookPinnedPieces & possiblePinRay;
                    }
                    else pinRay[direction]=0;
                }
                else pinRay[direction] = 0;
            }

            for (int direction = 1; direction < 8; direction += 2) {//bishop
                long kingRay = PieceAttack.maskOfLineInDirection[direction][kingLocation];
                long pinnerBB = kingRay & possibleBishopPinners;

                if (pinnerBB !=0) {
                    long possiblePinRay = kingRay ^ PieceAttack.maskOfLineInDirection[direction][Long.numberOfTrailingZeros(pinnerBB)];
                    long criticalPiecesOnPinRay = possiblePinRay & (pinnerBB | possibleBishopPinnedPieces);

                    if (Long.bitCount(criticalPiecesOnPinRay) == 2 && ((possiblePinRay & allPieces ^ criticalPiecesOnPinRay) ==0)) {
                        //there is a pinner and pinned on the pin ray and no more pieces blocking the pin
                        pinRay[direction] = possiblePinRay;
                        pinnedPieces |= possibleBishopPinnedPieces & possiblePinRay;
                    }
                    else pinRay[direction] = 0;
                }
                else pinRay[direction] = 0;
            }
        }
        else {//black to move
            long kingLocationBB = pieceArray[Type.BLACK | Type.KING];
            int kingLocation = Long.numberOfTrailingZeros(kingLocationBB);
            long rookPinners = pieceArray[Type.WHITE | Type.QUEEN] | pieceArray[Type.WHITE | Type.ROOK];
            long bishopPinners = pieceArray[Type.WHITE | Type.QUEEN] | pieceArray[Type.WHITE | Type.BISHOP];

            long possibleRookPinners = PieceAttack.lookUpRookAttacks(kingLocation, rookPinners) & rookPinners;//bitboard of rooks/queens inline with king
            long possibleBishopPinners = PieceAttack.lookUpBishopAttacks(kingLocation, bishopPinners) & bishopPinners;//bitboard of bishops/queens inline with king

            long possibleBishopPinnedPieces = PieceAttack.lookUpBishopAttacks(kingLocation, allPieces) & blackPieces;
            long possibleRookPinnedPieces = PieceAttack.lookUpRookAttacks(kingLocation, allPieces) & blackPieces;//bitboard of white pieces king sees



            //is not pinned if (possiblePinner's directionMaskTowardKing & pinner | possiblePinnedPiece) ^ (possiblePinner's directionMaskTowardKing & allPieces !=0)
            for (int direction = 0; direction < 8; direction += 2) {//rook
                long kingRay = PieceAttack.maskOfLineInDirection[direction][kingLocation];
                long pinnerBB = kingRay & possibleRookPinners;

                if (pinnerBB !=0) {
                    long possiblePinRay = kingRay ^ PieceAttack.maskOfLineInDirection[direction][Long.numberOfTrailingZeros(pinnerBB)];
                    long criticalPiecesOnPinRay = possiblePinRay & (pinnerBB | possibleRookPinnedPieces);

                    if (Long.bitCount(criticalPiecesOnPinRay) == 2 && ((possiblePinRay & allPieces ^ criticalPiecesOnPinRay) ==0)) {
                        //there is a pinner and pinned on the pin ray and no more pieces blocking the pin
                        pinRay[direction] = possiblePinRay;
                        pinnedPieces |= possibleRookPinnedPieces & possiblePinRay;
                    }
                    else pinRay[direction]=0;
                }
                else pinRay[direction] = 0;
            }

            for (int direction = 1; direction < 8; direction += 2) {//bishop
                long kingRay = PieceAttack.maskOfLineInDirection[direction][kingLocation];
                long pinnerBB = kingRay & possibleBishopPinners;

                if (pinnerBB !=0) {
                    long possiblePinRay = kingRay ^ PieceAttack.maskOfLineInDirection[direction][Long.numberOfTrailingZeros(pinnerBB)];
                    long criticalPiecesOnPinRay = possiblePinRay & (pinnerBB | possibleBishopPinnedPieces);

                    if (Long.bitCount(criticalPiecesOnPinRay) == 2 && ((possiblePinRay & allPieces ^ criticalPiecesOnPinRay) ==0)) {
                        //there is a pinner and pinned on the pin ray and no more pieces blocking the pin
                        pinRay[direction] = possiblePinRay;
                        pinnedPieces |= possibleBishopPinnedPieces & possiblePinRay;
                    }
                    else pinRay[direction] = 0;
                }
                else pinRay[direction] = 0;
            }
        }
    }
    public void findLegalMoves() {
        int colorToFindMovesFor;
        long notFriendlyPieces;

        if (whiteToMove) {
            colorToFindMovesFor=Type.WHITE;
            notFriendlyPieces=~whitePieces;
        }
        else {
            colorToFindMovesFor=Type.BLACK;
            notFriendlyPieces=~blackPieces;
        }

        for (int i = 0; i<numPieces[colorToFindMovesFor | Type.PAWN]; i++) {
            generatePawnMoves(pieceSquareList[colorToFindMovesFor | Type.PAWN][i]);
        }

        for (int i = 0; i<numPieces[colorToFindMovesFor | Type.KNIGHT]; i++) {
            generateKnightMoves(pieceSquareList[colorToFindMovesFor | Type.KNIGHT][i], notFriendlyPieces);
        }

        for (int i = 0; i<numPieces[colorToFindMovesFor | Type.BISHOP]; i++) {
            generateBishopMoves(pieceSquareList[colorToFindMovesFor | Type.BISHOP][i], notFriendlyPieces);
        }

        for (int i = 0; i<numPieces[colorToFindMovesFor | Type.ROOK]; i++) {
            generateRookMoves(pieceSquareList[colorToFindMovesFor | Type.ROOK][i], notFriendlyPieces);
        }

        for (int i = 0; i<numPieces[colorToFindMovesFor | Type.QUEEN]; i++) {
            generateQueenMoves(pieceSquareList[colorToFindMovesFor | Type.QUEEN][i], notFriendlyPieces);
        }

        for (int i = 0; i<numPieces[colorToFindMovesFor | Type.KING]; i++) {
            generateKingMoves(pieceSquareList[colorToFindMovesFor | Type.KING][i]);
        }
    }
    public void findLegalCapturingMoves() {
        int colorToFindMovesFor;
        long notFriendlyPieces;
        long enemyPieces;

        if (whiteToMove) {
            colorToFindMovesFor=Type.WHITE;
            notFriendlyPieces=~whitePieces;
            enemyPieces = blackPieces;
        }
        else {
            colorToFindMovesFor=Type.BLACK;
            notFriendlyPieces=~blackPieces;
            enemyPieces = whitePieces;
        }

        for (int i = 0; i<numPieces[colorToFindMovesFor | Type.PAWN]; i++) {
            squareAttacksArray[pieceSquareList[colorToFindMovesFor | Type.PAWN][i]] &= enemyPieces;
            generatePawnCapturesOnly(pieceSquareList[colorToFindMovesFor | Type.PAWN][i]);
        }

        for (int i = 0; i<numPieces[colorToFindMovesFor | Type.KNIGHT]; i++) {
            squareAttacksArray[pieceSquareList[colorToFindMovesFor | Type.KNIGHT][i]] &= enemyPieces;
            generateKnightMoves(pieceSquareList[colorToFindMovesFor | Type.KNIGHT][i], notFriendlyPieces);
        }

        for (int i = 0; i<numPieces[colorToFindMovesFor | Type.BISHOP]; i++) {
            squareAttacksArray[pieceSquareList[colorToFindMovesFor | Type.BISHOP][i]] &= enemyPieces;
            generateBishopMoves(pieceSquareList[colorToFindMovesFor | Type.BISHOP][i], notFriendlyPieces);
        }

        for (int i = 0; i<numPieces[colorToFindMovesFor | Type.ROOK]; i++) {
            squareAttacksArray[pieceSquareList[colorToFindMovesFor | Type.ROOK][i]] &= enemyPieces;
            generateRookMoves(pieceSquareList[colorToFindMovesFor | Type.ROOK][i], notFriendlyPieces);
        }

        for (int i = 0; i<numPieces[colorToFindMovesFor | Type.QUEEN]; i++) {
            squareAttacksArray[pieceSquareList[colorToFindMovesFor | Type.QUEEN][i]] &= enemyPieces;
            generateQueenMoves(pieceSquareList[colorToFindMovesFor | Type.QUEEN][i], notFriendlyPieces);
        }

        for (int i = 0; i<numPieces[colorToFindMovesFor | Type.KING]; i++) {
            squareAttacksArray[pieceSquareList[colorToFindMovesFor | Type.KING][i]] &= enemyPieces;
            generateKingCapturesOnly(pieceSquareList[colorToFindMovesFor | Type.KING][i]);
        }
    }
    public byte calculateGameState() {
        if (indexOfFirstEmptyMove==0) {
            if (inCheck) {
                if (whiteToMove){
                    return Type.WHITE_IS_CHECKMATED;
                }
                return Type.BLACK_IS_CHECKMATED;
            }
            return Type.GAME_IS_A_DRAW;//stalemate
        }

        if (hundredHalfmoveTimer>=100) {
            return Type.GAME_IS_A_DRAW;//fifty-move rule draw
        }

        if (hundredHalfmoveTimer > 10) {//no possibility of a draw before 10 reversible plies are made
            int numRepititions=0;
            for (int i = plyNumber; i > plyNumber-hundredHalfmoveTimer; i--) {
                if (previousZobristKeys[i] == zobristKey)numRepititions++;
            }
            if (numRepititions >= 2){
                return Type.GAME_IS_A_DRAW;//the third repetition being the current position
            }
        }

        int whitePieceCount = numPieces[Type.KNIGHT] + numPieces[Type.BISHOP] + numPieces[Type.ROOK] + numPieces[Type.QUEEN];
        int blackPieceCount = numPieces[Type.BLACK | Type.KNIGHT] + numPieces[Type.BLACK | Type.BISHOP] + numPieces[Type.BLACK | Type.ROOK] + numPieces[Type.BLACK | Type.QUEEN];
        //if there are 3 or fewer pieces for each color, then it is the endGame
        if (whitePieceCount >= 3 && blackPieceCount >= 3)return Type.MID_GAME;

        //check for draw by insufficient material after checking pieceCount>3, since it can only occur with fewer than 3 pieces on the board
        int numHeavyPiecesAndPawns = numPieces[Type.ROOK] + numPieces[Type.QUEEN] + numPieces[Type.PAWN] + numPieces[Type.BLACK | Type.ROOK] + numPieces[Type.BLACK | Type.QUEEN] + numPieces[Type.BLACK | Type.PAWN];

        if (StaticEval.gameIsDrawnByInsufficientMaterial(numHeavyPiecesAndPawns, numPieces[Type.KNIGHT], numPieces[Type.BISHOP], numPieces[Type.BLACK | Type.KNIGHT], numPieces[Type.BLACK | Type.BISHOP])){
            return Type.GAME_IS_A_DRAW;
        }
        return Type.END_GAME;
    }
    private byte calculateGameStateWhenOnlyCaptures() {
        if (hundredHalfmoveTimer>=100) {
            return Type.GAME_IS_A_DRAW;//fifty-move rule draw
        }

        if (hundredHalfmoveTimer > 10) {//no possibility of a draw before 10 reversible plies are made
            int numRepititions=0;
            for (int i = plyNumber; i > plyNumber-hundredHalfmoveTimer; i--) {
                if (previousZobristKeys[i] == zobristKey)numRepititions++;
            }
            if (numRepititions >= 2){
                return Type.GAME_IS_A_DRAW;//the third repetition being the current position
            }
        }

        int whitePieceCount = numPieces[Type.KNIGHT] + numPieces[Type.BISHOP] + numPieces[Type.ROOK] + numPieces[Type.QUEEN];
        int blackPieceCount = numPieces[Type.BLACK | Type.KNIGHT] + numPieces[Type.BLACK | Type.BISHOP] + numPieces[Type.BLACK | Type.ROOK] + numPieces[Type.BLACK | Type.QUEEN];
        //if there are 3 or fewer pieces for each color, then it is the endGame
        if (whitePieceCount >= 3 && blackPieceCount >= 3)return Type.MID_GAME;

        //check for draw by insufficient material after checking pieceCount>3, since it can only occur with fewer than 3 pieces on the board
        int numHeavyPiecesAndPawns = numPieces[Type.ROOK] + numPieces[Type.QUEEN] + numPieces[Type.PAWN] + numPieces[Type.BLACK | Type.ROOK] + numPieces[Type.BLACK | Type.QUEEN] + numPieces[Type.BLACK | Type.PAWN];

        if (StaticEval.gameIsDrawnByInsufficientMaterial( numHeavyPiecesAndPawns, numPieces[Type.KNIGHT], numPieces[Type.BISHOP], numPieces[Type.BLACK | Type.KNIGHT], numPieces[Type.BLACK | Type.BISHOP])){
            return Type.GAME_IS_A_DRAW;
        }
        return Type.END_GAME;
    }
    public void optimizeMoveOrder() {
        for (int i=0;i<indexOfFirstEmptyMove;i++) {
            legalMovePriorities[i] = getMovePriority(legalMoves[i]);
        }
        sortMoves();
    }
    public void optimizeMoveOrder(int principalVariationBestMove) {
        if (principalVariationBestMove != Type.ILLEGAL_MOVE) {//slight speedup by making sure the move isn't empty before checking it dozens of times
            for (int i=0;i<indexOfFirstEmptyMove;i++) {
                legalMovePriorities[i] = getMovePriority(legalMoves[i], principalVariationBestMove);
            }
        }
        else {
            for (int i=0;i<indexOfFirstEmptyMove;i++) {
                legalMovePriorities[i] = getMovePriority(legalMoves[i]);
            }
        }
        sortMoves();
    }
    private int getMovePriority(int legalMove) {//priority of 1 is an estimated 100 centipawn gain, -1 is 100 cp loss, etc
        int movePriority = 0;

        int fromSquare = Move.getFromSquareFromMove(legalMove);
        int movingPiece = squareCentricPos[fromSquare]%8;//take mod 8 to make pieces Colorless
        int capturedPiece = Move.getCapturedPieceFromMove(legalMove)%8;
        long enemyPawnAttacks = whiteToMove ? blackAttacksArray[Type.PAWN] : whiteAttacksArray[Type.PAWN];

        if (capturedPiece != 0)movePriority = 2 * capturedPiece - movingPiece;//prioritize capturing low value with high value

        long toSquareBB = 1L<<Move.getToSquareFromMove(legalMove);
        if ((toSquareBB | enemyPawnAttacks) == enemyPawnAttacks)movePriority -= movingPiece;//devalue moving to enemy pawn attacking squares

        byte moveType = Move.getMoveTypeFromMove(legalMove);
        if (moveType == Type.PAWN_PROMOTES_TO_Q) movePriority+=9;//prioritize pawn promoting to queen
        else if (moveType > 4) movePriority++;//other pawn promotion that isn't a queen


        return movePriority;
    }
    private int getMovePriority(int legalMove, int principalVariationBestMove) {
        int movePriority = 0;

        if (legalMove == principalVariationBestMove)return 1000;//arbitrary large number to make it the highest priority

        int fromSquare = Move.getFromSquareFromMove(legalMove);
        int movingPiece = squareCentricPos[fromSquare]%8;//take mod 8 to make pieces Colorless
        int capturedPiece = Move.getCapturedPieceFromMove(legalMove)%8;
        long enemyPawnAttacks = whiteToMove ? blackAttacksArray[Type.PAWN] : whiteAttacksArray[Type.PAWN];

        if (capturedPiece != 0)movePriority = 2 * capturedPiece - movingPiece;//prioritize capturing low value with high value

        long toSquareBB = 1L<<Move.getToSquareFromMove(legalMove);
        if ((toSquareBB | enemyPawnAttacks) == enemyPawnAttacks)movePriority -= movingPiece;//devalue moving to enemy pawn attacking squares

        byte moveType = Move.getMoveTypeFromMove(legalMove);
        if (moveType == Type.PAWN_PROMOTES_TO_Q) movePriority+=9;//prioritize pawn promoting to queen
        else if (moveType > 4) movePriority++;//other pawn promotion that isn't a queen


        return movePriority;
    }
    private void sortMoves() {
        //could be causing my bugs, not sure
        //using a custom sorting algorithm because 1: I only want to sort one part of the whole array and
        //2: I want to swap the positions of elements in both legalMovePriorities and legalMoves at the same time

        for (int i= indexOfFirstEmptyMove/2 - 1; i>=0; i--) {
            heapify(legalMovePriorities, indexOfFirstEmptyMove, i);
        }

        for (int i= indexOfFirstEmptyMove-1; i >0; i--) {
            swapMoves(i,0);

            heapify(legalMovePriorities, i, 0);
        }
    }
    private void heapify(int arr[], int n, int i) {
        int largest = i;
        int l = 2 * i + 1;
        int r = 2 * i + 2;

        if (l < n && arr[l] > arr[largest])
            largest = l;

        if (r < n && arr[r] > arr[largest])
            largest = r;

        if (largest != i) {
            swapMoves(i,largest);

            heapify(arr, n, largest);
        }
    }
    private void swapMoves(int fromIndex, int toIndex) {
        legalMoves[fromIndex] = legalMoves[fromIndex] ^ legalMoves[toIndex];
        legalMoves[toIndex] = legalMoves[fromIndex] ^ legalMoves[toIndex];
        legalMoves[fromIndex] = legalMoves[fromIndex] ^ legalMoves[toIndex];

        legalMovePriorities[fromIndex] = legalMovePriorities[fromIndex] ^ legalMovePriorities[toIndex];
        legalMovePriorities[toIndex] = legalMovePriorities[fromIndex] ^ legalMovePriorities[toIndex];
        legalMovePriorities[fromIndex] = legalMovePriorities[fromIndex] ^ legalMovePriorities[toIndex];
    }



    public void addMovesToMoveList(byte moveType, byte fromSquare, long toSquareBB) {
        while (toSquareBB != 0) {
            byte tempToSquare= (byte)Long.numberOfTrailingZeros(toSquareBB);
            int newMove = Move.makeMoveFromBytes(moveType,fromSquare,tempToSquare,squareCentricPos[tempToSquare]);
            legalMoves[indexOfFirstEmptyMove] = newMove;
            indexOfFirstEmptyMove++;
            toSquareBB &= toSquareBB-1;
        }
    }
    public boolean moveIsOnMoveList(int target) {
        for (int i = 0; i<indexOfFirstEmptyMove; i++) {
            if (legalMoves[i] == target) return true;
        }
        return false;
    }

    public void generatePawnMoves(byte fromSquare) {
        long[] possibleMoves = new long[8];
        if (whiteToMove) {
            long fromSquareBB= Util.toBitboard(fromSquare);
            long attackingSquares= PieceAttack.lookUpWhitePawnAttacks(fromSquare);
            if (fromSquare/8==6){//on the seventh rank
                long tempToSquareBB= fromSquareBB<<8;

                tempToSquareBB &= emptySquares;
                tempToSquareBB |= attackingSquares & blackPieces;

                if ((pinnedPieces & fromSquareBB) == fromSquareBB && onPinRay(fromSquareBB)) tempToSquareBB &=pinRay[indexOfPin];//if on the pin ray
                tempToSquareBB &= checkResolveRay;

                possibleMoves[Type.PAWN_PROMOTES_TO_Q]=tempToSquareBB;
                possibleMoves[Type.PAWN_PROMOTES_TO_N]=tempToSquareBB;
                possibleMoves[Type.PAWN_PROMOTES_TO_B]=tempToSquareBB;
                possibleMoves[Type.PAWN_PROMOTES_TO_R]=tempToSquareBB;
            }
            else {//not on the seventh rank, white to move
                possibleMoves[Type.NORMAL_MOVE]|= fromSquareBB << 8 & emptySquares;
                possibleMoves[Type.DOUBLE_PAWN_MOVE]|=possibleMoves[Type.NORMAL_MOVE]<<8 & emptySquares & Constants.RANK_4;
                possibleMoves[Type.NORMAL_MOVE]|= attackingSquares & blackPieces;

                if ((pinnedPieces & fromSquareBB) == fromSquareBB && onPinRay(fromSquareBB)) {
                    possibleMoves[Type.NORMAL_MOVE] &=pinRay[indexOfPin];
                    possibleMoves[Type.DOUBLE_PAWN_MOVE] &=pinRay[indexOfPin];
                }

                possibleMoves[Type.NORMAL_MOVE] &= checkResolveRay;
                possibleMoves[Type.DOUBLE_PAWN_MOVE] &= checkResolveRay;


                possibleMoves[Type.EN_PASSANT]= attackingSquares & (long)enPassantTargetFiles<<40;//en passant
                boolean enPassantCouldBePossible=possibleMoves[Type.EN_PASSANT] !=0;
                //check to see if enPassant could be possible before doing slow pin check
                if (enPassantCouldBePossible && enPassantIsPinned(fromSquareBB,possibleMoves[Type.EN_PASSANT])) possibleMoves[Type.EN_PASSANT] = 0;
            }
        }

        else {
            long fromSquareBB= Util.toBitboard(fromSquare);
            long attackingSquares= PieceAttack.lookUpBlackPawnAttacks(fromSquare);
            if (fromSquare/8==1){//on the second rank, black to move
                long tempToSquareBB;
                tempToSquareBB= fromSquareBB>>>8;

                tempToSquareBB &= emptySquares;
                tempToSquareBB |= attackingSquares & whitePieces;

                if ((pinnedPieces & fromSquareBB) == fromSquareBB && onPinRay(fromSquareBB)) tempToSquareBB &=pinRay[indexOfPin];//if on the pin ray
                tempToSquareBB &= checkResolveRay;

                possibleMoves[Type.PAWN_PROMOTES_TO_Q]=tempToSquareBB;
                possibleMoves[Type.PAWN_PROMOTES_TO_N]=tempToSquareBB;
                possibleMoves[Type.PAWN_PROMOTES_TO_B]=tempToSquareBB;
                possibleMoves[Type.PAWN_PROMOTES_TO_R]=tempToSquareBB;
            }
            else {//not on the second rank, black to move
                possibleMoves[Type.NORMAL_MOVE]|= fromSquareBB >>> 8 & emptySquares;
                possibleMoves[Type.DOUBLE_PAWN_MOVE]|=possibleMoves[Type.NORMAL_MOVE]>>>8& emptySquares & Constants.RANK_5;
                possibleMoves[Type.NORMAL_MOVE]|= attackingSquares & whitePieces;

                if ((pinnedPieces & fromSquareBB) == fromSquareBB && onPinRay(fromSquareBB)) {
                    possibleMoves[Type.NORMAL_MOVE] &=pinRay[indexOfPin];
                    possibleMoves[Type.DOUBLE_PAWN_MOVE] &=pinRay[indexOfPin];
                }
                possibleMoves[Type.NORMAL_MOVE] &= checkResolveRay;
                possibleMoves[Type.DOUBLE_PAWN_MOVE] &= checkResolveRay;

                possibleMoves[Type.EN_PASSANT]= attackingSquares & (long)enPassantTargetFiles<<16;//en passant
                boolean enPassantCouldBePossible=possibleMoves[Type.EN_PASSANT] !=0;
                //check to see if enPassant could be possible before doing slow pin check
                if (enPassantCouldBePossible && enPassantIsPinned(fromSquareBB,possibleMoves[Type.EN_PASSANT])) possibleMoves[Type.EN_PASSANT] = 0;
            }
        }

        addMovesToMoveList(Type.NORMAL_MOVE,fromSquare,possibleMoves[Type.NORMAL_MOVE]);
        addMovesToMoveList(Type.EN_PASSANT,fromSquare,possibleMoves[Type.EN_PASSANT]);
        addMovesToMoveList(Type.DOUBLE_PAWN_MOVE,fromSquare,possibleMoves[Type.DOUBLE_PAWN_MOVE]);
        addMovesToMoveList(Type.PAWN_PROMOTES_TO_Q,fromSquare,possibleMoves[Type.PAWN_PROMOTES_TO_Q]);
        addMovesToMoveList(Type.PAWN_PROMOTES_TO_N,fromSquare,possibleMoves[Type.PAWN_PROMOTES_TO_N]);
        addMovesToMoveList(Type.PAWN_PROMOTES_TO_B,fromSquare,possibleMoves[Type.PAWN_PROMOTES_TO_B]);
        addMovesToMoveList(Type.PAWN_PROMOTES_TO_R,fromSquare,possibleMoves[Type.PAWN_PROMOTES_TO_R]);
    }
    private void generatePawnCapturesOnly(byte fromSquare) {
        long possibleMoves[] = new long[8];
        if (whiteToMove) {
            long fromSquareBB= Util.toBitboard(fromSquare);
            long attackingSquares= PieceAttack.lookUpWhitePawnAttacks(fromSquare);

            if (fromSquare/8==6){//on the seventh rank
                long tempToSquareBB = attackingSquares & blackPieces;

                if ((pinnedPieces & fromSquareBB) == fromSquareBB && onPinRay(fromSquareBB)) tempToSquareBB &= pinRay[indexOfPin];//if on the pin ray
                tempToSquareBB &= checkResolveRay;

                possibleMoves[Type.PAWN_PROMOTES_TO_Q]=tempToSquareBB;
                possibleMoves[Type.PAWN_PROMOTES_TO_N]=tempToSquareBB;
                possibleMoves[Type.PAWN_PROMOTES_TO_B]=tempToSquareBB;
                possibleMoves[Type.PAWN_PROMOTES_TO_R]=tempToSquareBB;
            }
            else {//not on the seventh rank, white to move
                possibleMoves[Type.NORMAL_MOVE]|= attackingSquares & blackPieces;

                if ((pinnedPieces & fromSquareBB) == fromSquareBB && onPinRay(fromSquareBB)) {
                    possibleMoves[Type.NORMAL_MOVE] &=pinRay[indexOfPin];
                }

                possibleMoves[Type.NORMAL_MOVE] &= checkResolveRay;
                possibleMoves[Type.DOUBLE_PAWN_MOVE] &= checkResolveRay;

                possibleMoves[Type.EN_PASSANT]= attackingSquares & (long)enPassantTargetFiles<<40;//en passant
                boolean enPassantCouldBePossible=possibleMoves[Type.EN_PASSANT] !=0;
                //check to see if enPassant could be possible before doing slow pin check
                if (enPassantCouldBePossible && enPassantIsPinned(fromSquareBB,possibleMoves[Type.EN_PASSANT])) possibleMoves[Type.EN_PASSANT] = 0;
            }
        }

        else {
            long fromSquareBB= Util.toBitboard(fromSquare);
            long attackingSquares= PieceAttack.lookUpBlackPawnAttacks(fromSquare);

            if (fromSquare/8==1){//on the second rank, black to move
                long tempToSquareBB = attackingSquares & whitePieces;

                if ((pinnedPieces & fromSquareBB) == fromSquareBB && onPinRay(fromSquareBB)) tempToSquareBB &= pinRay[indexOfPin];//if on the pin ray
                tempToSquareBB &= checkResolveRay;

                possibleMoves[Type.PAWN_PROMOTES_TO_Q]=tempToSquareBB;
                possibleMoves[Type.PAWN_PROMOTES_TO_N]=tempToSquareBB;
                possibleMoves[Type.PAWN_PROMOTES_TO_B]=tempToSquareBB;
                possibleMoves[Type.PAWN_PROMOTES_TO_R]=tempToSquareBB;
            }
            else {//not on the second rank, black to move
                possibleMoves[Type.NORMAL_MOVE]|= attackingSquares & whitePieces;

                if ((pinnedPieces & fromSquareBB) == fromSquareBB && onPinRay(fromSquareBB)) {
                    possibleMoves[Type.NORMAL_MOVE] &=pinRay[indexOfPin];
                }

                possibleMoves[Type.EN_PASSANT]= attackingSquares & (long)enPassantTargetFiles<<16;//en passant
                boolean enPassantCouldBePossible=possibleMoves[Type.EN_PASSANT] !=0;
                //check to see if enPassant could be possible before doing slow pin check
                if (enPassantCouldBePossible && enPassantIsPinned(fromSquareBB,possibleMoves[Type.EN_PASSANT])) possibleMoves[Type.EN_PASSANT] = 0;
            }
        }
    }
    public void generateKnightMoves(byte fromSquare, long notFriendlyPiecesBB) {
        long possibleMoves = squareAttacksArray[fromSquare];

        possibleMoves&= notFriendlyPiecesBB & checkResolveRay;

        long squareBB = 1L<<fromSquare;
        if ((pinnedPieces & squareBB) == squareBB && onPinRay(squareBB))possibleMoves=0;
        addMovesToMoveList(Type.NORMAL_MOVE,fromSquare,possibleMoves);
    }
    public void generateBishopMoves(byte fromSquare, long notFriendlyPiecesBB) {
        long possibleMoves = squareAttacksArray[fromSquare];

        possibleMoves&= notFriendlyPiecesBB & checkResolveRay;

        long squareBB = 1L<<fromSquare;
        if ((pinnedPieces & squareBB) == squareBB && onPinRay(squareBB))possibleMoves &= pinRay[indexOfPin];
        addMovesToMoveList(Type.NORMAL_MOVE,fromSquare,possibleMoves);
    }
    public void generateRookMoves(byte fromSquare, long notFriendlyPiecesBB) {
        long possibleMoves = squareAttacksArray[fromSquare];

        possibleMoves&= notFriendlyPiecesBB & checkResolveRay;

        long squareBB = 1L<<fromSquare;
        if ((pinnedPieces & squareBB) == squareBB && onPinRay(squareBB))possibleMoves &=pinRay[indexOfPin];
        addMovesToMoveList(Type.NORMAL_MOVE,fromSquare,possibleMoves);
    }
    public void generateQueenMoves(byte fromSquare, long notFriendlyPiecesBB) {
        long possibleMoves = squareAttacksArray[fromSquare];
        possibleMoves&= notFriendlyPiecesBB & checkResolveRay;

        long squareBB = Util.toBitboard(fromSquare);
        if ((pinnedPieces & squareBB) == squareBB && onPinRay(squareBB))possibleMoves &=pinRay[indexOfPin];
        addMovesToMoveList(Type.NORMAL_MOVE,fromSquare,possibleMoves);
    }
    public void generateKingMoves(byte fromSquare) {
        long possibleMoves=PieceAttack.lookUpKingAttacks(fromSquare);
        long possibleCastles=0;

        if (whiteToMove) {
            possibleMoves&= ~(whitePieces | blackAttacks);//can't move into check


            if ((Constants.whiteCanCSPieceMask & allPieces | Constants.whiteCanCSCheckMask & blackAttacks) ==0){
                possibleCastles+= Constants.g1 & castlingRights;
            }

            if ((Constants.whiteCanCLPieceMask & allPieces | Constants.whiteCanCLCheckMask & blackAttacks) == 0){
                possibleCastles+= Constants.c1 & castlingRights;
            }

        }
        else {//black to move
            possibleMoves&= ~(blackPieces | whiteAttacks);//can't move into check

            if ((Constants.blackCanCSPieceMask & allPieces | Constants.blackCanCSCheckMask & whiteAttacks) == 0){
                possibleCastles+=Constants.g8 & castlingRights;
            }

            if ((Constants.blackCanCLPieceMask & allPieces | Constants.blackCanCLCheckMask & whiteAttacks) == 0){
                possibleCastles+=Constants.c8 & castlingRights;
            }

        }

        addMovesToMoveList(Type.NORMAL_MOVE,fromSquare,possibleMoves);
        addMovesToMoveList(Type.CASTLES,fromSquare,possibleCastles);
    }
    public void generateKingCapturesOnly(byte fromSquare) {
        long possibleMoves = squareAttacksArray[fromSquare];

        if (whiteToMove) {
            possibleMoves&= ~(whitePieces | blackAttacks);//can't move into check
        }
        else {
            possibleMoves&= ~(blackPieces | whiteAttacks);//can't move into check
        }

        addMovesToMoveList(Type.NORMAL_MOVE, fromSquare, possibleMoves);
    }

    public boolean onPinRay(long squareBB) {
        for (indexOfPin=0;indexOfPin<8;indexOfPin++) {
            if ((pinRay[indexOfPin] & squareBB) == squareBB) return true;
        }
        return false;
    }//returns true if the square is on a pinRay and sets indexOfPin to which pin it is on
    public long findCheckResolveRay(long kingLocation) {
        short friendlyColor;
        short enemyColor;
        if (whiteToMove) {
            friendlyColor = Type.WHITE;
            enemyColor = Type.BLACK;
            long pawnCheck = PieceAttack.generateWhitePawnAttacks(kingLocation) & pieceArray[enemyColor | Type.PAWN];
            if (pawnCheck !=0)return pawnCheck;
        }
        else {
            friendlyColor = Type.BLACK;
            enemyColor = Type.WHITE;
            long pawnCheck = PieceAttack.generateBlackPawnAttacks(kingLocation) & pieceArray[enemyColor | Type.PAWN];
            if (pawnCheck !=0)return pawnCheck;
        }

        long knightCheck = PieceAttack.generateKnightAttacks(kingLocation) & pieceArray[enemyColor | Type.KNIGHT];
        if (knightCheck !=0)return knightCheck;

        long relevantAttackers= pieceArray[enemyColor | Type.ROOK] | pieceArray[enemyColor | Type.QUEEN];
        byte kingSquare = pieceSquareList[friendlyColor | Type.KING][0];

        if (numPieces[enemyColor | Type.QUEEN] < 2) {//my numChecks variable is correct
            for (int i=0; i<8; i+=2) {
                long ray = PieceAttack.lookUpRookAttacks(kingSquare,allPieces) & PieceAttack.maskOfLineInDirection[i][kingSquare];
                if ((ray & relevantAttackers) != 0) {
                    return ray;
                }
            }
            relevantAttackers= pieceArray[enemyColor | Type.BISHOP] | pieceArray[enemyColor | Type.QUEEN];
            for (int i=1; i<8; i+=2) {
                long ray = PieceAttack.lookUpBishopAttacks(kingSquare,allPieces) & PieceAttack.maskOfLineInDirection[i][kingSquare];
                if ((ray & relevantAttackers) != 0) {
                    return ray;
                }
            }
        }
        else {//numChecks could be wrong
            long tempRay=0;
            byte numChecks=0;
            for (int i=0; i<7; i+=2) {
                long ray = PieceAttack.lookUpRookAttacks(kingSquare,allPieces) & PieceAttack.maskOfLineInDirection[i][kingSquare];
                if ((ray & relevantAttackers) != 0) {
                    tempRay = ray;
                    numChecks++;
                }
            }
            relevantAttackers= pieceArray[enemyColor | Type.BISHOP] | pieceArray[enemyColor | Type.QUEEN];
            for (int i=1; i<7; i+=2) {
                long ray = PieceAttack.lookUpBishopAttacks(kingSquare,allPieces) & PieceAttack.maskOfLineInDirection[i][kingSquare];
                if ((ray & relevantAttackers) != 0) {
                    tempRay = ray;
                    numChecks++;
                }
            }

            return numChecks == 1 ? tempRay : 0;
        }

        return 0;
    }
    private boolean enPassantIsPinned(long fromSquareBB, long toSquareBB) {
        long[] tempPA = pieceArray;
        byte[] tempSCP = squareCentricPos;
        boolean tempWTM = whiteToMove;
        Position newPosition= new Position(tempPA,tempSCP,tempWTM);

        byte toSquare = (byte)Long.numberOfTrailingZeros(toSquareBB);
        byte fromSquare = (byte)Long.numberOfTrailingZeros(fromSquareBB);

        newPosition.quietlyEnPassant(fromSquare,toSquare);
        newPosition.whiteToMove = !newPosition.whiteToMove;
        newPosition.calculatePieceLocations();
        newPosition.calculateSquareAttacksFromBitboards();
        newPosition.calculateInCheck();
        return newPosition.inCheck;
    }
    private void quietlyEnPassant(byte fromSquare, byte toSquare) {
        long fromSquareBB = Util.toBitboard(fromSquare);
        long toSquareBB = Util.toBitboard(toSquare);
        byte movingPiece = squareCentricPos[fromSquare];

        int enPassantCapturedSquare= toSquare-8;
        short colorNotMoving= Type.BLACK;
        if (!whiteToMove){
            enPassantCapturedSquare= toSquare+8;
            colorNotMoving= Type.WHITE;
        }

        squareCentricPos[fromSquare]= Type.EMPTY;//updates redundant squareCentricPosition
        squareCentricPos[toSquare]=movingPiece;//updates redundant squareCentricPosition
        squareCentricPos[enPassantCapturedSquare]= Type.EMPTY;

        pieceArray[movingPiece]^=fromSquareBB|toSquareBB;
        pieceArray[colorNotMoving | Type.PAWN]^= Util.toBitboard(enPassantCapturedSquare);

        hundredHalfmoveTimer=0;
        whiteToMove= !whiteToMove;
        enPassantTargetFiles =0;
    }
    public void calculateSquareAttacksFromBitboards() {
        long tempPieceBB= pieceArray[Type.WHITE | Type.PAWN];

        for (int i=0;i<64;i++) {
            squareAttacksArray[i]=0;
        }
        for (int i=0;i<6;i++) {
            whiteAttacksArray[i+1]=0;
            blackAttacksArray[i+1]=0;
        }

        long whiteBlockers = allPieces ^ pieceArray[Type.BLACK | Type.KING];
        long blackBlockers = allPieces ^ pieceArray[Type.WHITE | Type.KING];


        while (tempPieceBB !=0) {
            int pieceSquare = Long.numberOfTrailingZeros(tempPieceBB);
            long pieceAttackBB = PieceAttack.lookUpWhitePawnAttacks(pieceSquare);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            whiteAttacksArray[Type.PAWN] |= pieceAttackBB;
            tempPieceBB ^= 1L<<pieceSquare;
        }

        tempPieceBB= pieceArray[Type.WHITE | Type.KNIGHT];
        while (tempPieceBB !=0) {
            int pieceSquare = Long.numberOfTrailingZeros(tempPieceBB);
            long pieceSquareBB = 1L<<pieceSquare;
            long pieceAttackBB = PieceAttack.lookUpKnightAttacks(pieceSquare);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            whiteAttacksArray[Type.KNIGHT] |= pieceAttackBB;
            tempPieceBB ^= pieceSquareBB;
        }

        tempPieceBB= pieceArray[Type.WHITE | Type.BISHOP];
        while (tempPieceBB !=0) {
            int pieceSquare = Long.numberOfTrailingZeros(tempPieceBB);
            long pieceSquareBB = 1L<<pieceSquare;
            long pieceAttackBB = PieceAttack.lookUpBishopAttacks(pieceSquare,whiteBlockers);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            whiteAttacksArray[Type.BISHOP] |= pieceAttackBB;
            tempPieceBB ^= 1L<<pieceSquare;
        }

        tempPieceBB= pieceArray[Type.WHITE | Type.ROOK];
        while (tempPieceBB !=0) {
            int pieceSquare = Long.numberOfTrailingZeros(tempPieceBB);
            long pieceAttackBB = PieceAttack.lookUpRookAttacks(pieceSquare,whiteBlockers);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            whiteAttacksArray[Type.ROOK] |= pieceAttackBB;
            tempPieceBB ^= 1L<<pieceSquare;
        }

        tempPieceBB= pieceArray[Type.WHITE | Type.QUEEN];
        while (tempPieceBB !=0) {
            int pieceSquare = Long.numberOfTrailingZeros(tempPieceBB);
            long pieceAttackBB = PieceAttack.lookUpQueenAttacks(pieceSquare,whiteBlockers);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            whiteAttacksArray[Type.QUEEN] |= pieceAttackBB;
            tempPieceBB ^= 1L<<pieceSquare;
        }

        tempPieceBB= pieceArray[Type.WHITE | Type.KING];
        int tempPS = Long.numberOfTrailingZeros(tempPieceBB);
        long tempPABB = PieceAttack.lookUpKingAttacks(tempPS);
        squareAttacksArray[tempPS]= tempPABB;
        whiteAttacksArray[Type.KING] |= tempPABB;


        tempPieceBB= pieceArray[Type.BLACK | Type.PAWN];
        while (tempPieceBB !=0) {
            int pieceSquare = Long.numberOfTrailingZeros(tempPieceBB);
            long pieceAttackBB = PieceAttack.lookUpBlackPawnAttacks(pieceSquare);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            blackAttacksArray[Type.PAWN] |= pieceAttackBB;
            tempPieceBB ^= 1L<<pieceSquare;
        }

        tempPieceBB= pieceArray[Type.BLACK | Type.KNIGHT];
        while (tempPieceBB !=0) {
            int pieceSquare = Long.numberOfTrailingZeros(tempPieceBB);
            long pieceSquareBB = 1L<<pieceSquare;
            long pieceAttackBB = PieceAttack.lookUpKnightAttacks(pieceSquare);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            blackAttacksArray[Type.KNIGHT] |= pieceAttackBB;
            tempPieceBB ^= pieceSquareBB;
        }

        tempPieceBB= pieceArray[Type.BLACK | Type.BISHOP];
        while (tempPieceBB !=0) {
            int pieceSquare = Long.numberOfTrailingZeros(tempPieceBB);
            long pieceAttackBB = PieceAttack.lookUpBishopAttacks(pieceSquare,blackBlockers);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            blackAttacksArray[Type.BISHOP] |= pieceAttackBB;
            tempPieceBB ^= 1L<<pieceSquare;
        }

        tempPieceBB= pieceArray[Type.BLACK | Type.ROOK];
        while (tempPieceBB !=0) {
            int pieceSquare = Long.numberOfTrailingZeros(tempPieceBB);
            long pieceAttackBB = PieceAttack.lookUpRookAttacks(pieceSquare,blackBlockers);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            blackAttacksArray[Type.ROOK] |= pieceAttackBB;
            tempPieceBB ^= 1L<<pieceSquare;
        }

        tempPieceBB= pieceArray[Type.BLACK | Type.QUEEN];
        while (tempPieceBB !=0) {
            int pieceSquare = Long.numberOfTrailingZeros(tempPieceBB);
            long pieceAttackBB = PieceAttack.lookUpQueenAttacks(pieceSquare,blackBlockers);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            blackAttacksArray[Type.QUEEN] |= pieceAttackBB;
            tempPieceBB ^= 1L<<pieceSquare;
        }

        tempPieceBB= pieceArray[Type.BLACK | Type.KING];
        tempPS = Long.numberOfTrailingZeros(tempPieceBB);
        tempPABB = PieceAttack.lookUpKingAttacks(tempPS);
        squareAttacksArray[tempPS]= tempPABB;
        blackAttacksArray[Type.KING] |= tempPABB;

    }

    public boolean allPositionRepresentationsAgree() {
        //turn each position representation into a scp, then check for equality on each
        byte[] scpFromPieceArray = new byte[64];
        byte[] scpFromLists = new byte[64];

        for (int color = 0; color < 9; color+= 8) {
            for (int pieceType = 1; pieceType <= 6; pieceType++) {
                for (int square = 0; square < 64; square++) {
                    if ((pieceArray[color | pieceType] & Util.toBitboard(square)) != 0){
                        scpFromPieceArray[square] = (byte) (color | pieceType);
                    }
                }

                for (int i = 0; i< numPieces[color | pieceType]; i++) {
                    int square = pieceSquareList[color | pieceType][i];
                    scpFromLists[square] = (byte) (color | pieceType);
                }
            }
        }

        for (int i=0; i<64; i++) {
            if (squareCentricPos[i] != scpFromPieceArray[i]){
                Util.printArray(squareCentricPos);
                Util.printArray(scpFromPieceArray);
                Util.printArray(scpFromLists);
                System.out.println("Piece Array disagrees");
                return false;
            }
            if (squareCentricPos[i] != scpFromLists[i]){
                Util.printArray(squareCentricPos);
                Util.printArray(scpFromPieceArray);
                Util.printArray(scpFromLists);
                System.out.println("Piece List disagrees");
                return false;
            }
        }
        return true;
    }

    public void printFen() {
        System.out.print(getFen());
    }
    public String getFen() {
        String fen="";
        int tickerSquare;//start on top left square
        int emptySquareCounter=0;

        for (int rank=7;rank>=0;rank--) {
            tickerSquare=rank*8;

            while (tickerSquare%8 !=7) {//while not on the h file
                if (squareCentricPos[tickerSquare] == 0)emptySquareCounter++;
                else {
                    if (emptySquareCounter!=0) {
                        fen+=emptySquareCounter;
                        emptySquareCounter=0;
                    }
                    fen += Util.getPieceStringFromShort(squareCentricPos[tickerSquare]);
                }
                tickerSquare++;
            }

            if (squareCentricPos[tickerSquare] == 0)emptySquareCounter++;
            else {
                if (emptySquareCounter!=0){
                    fen+=emptySquareCounter;
                    emptySquareCounter=0;
                }
                fen += Util.getPieceStringFromShort(squareCentricPos[tickerSquare]);
            }

            if (emptySquareCounter!=0){
                fen+=emptySquareCounter;
                emptySquareCounter=0;
            }

            fen+="/";
        }
        fen = fen.substring(0,fen.length()-1);//take off last "/"

        if (whiteToMove)fen+=" w ";
        else fen+=" b ";

        boolean anySideCanCastle = castlingRights!= 0;
        if (!anySideCanCastle)fen+="-";
        else {
            if ((castlingRights & Constants.g1) == Constants.g1)fen+="K";
            if ((castlingRights & Constants.c1) == Constants.c1)fen+="Q";
            if ((castlingRights & Constants.g8) == Constants.g8)fen+="k";
            if ((castlingRights & Constants.c8) == Constants.c8)fen+="q";
        }

        fen+=" ";

        if (enPassantTargetFiles==0)fen+="-";
        else {
            int fileNumber= Long.numberOfTrailingZeros(enPassantTargetFiles);
            if (whiteToMove) {
                byte enPassantTargetSquare = (byte)(fileNumber + 40);
                fen+=Move.giveSquareAsStringFromByte(enPassantTargetSquare);
            }
            else {
                byte enPassantTargetSquare = (byte)(fileNumber + 16);
                fen+=Move.giveSquareAsStringFromByte(enPassantTargetSquare);
            }
        }

        fen+=" "+hundredHalfmoveTimer+" "+plyNumber;
        return fen;
    }
}