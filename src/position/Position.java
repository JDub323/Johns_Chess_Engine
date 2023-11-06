package position;

import move.Move;
import move.PieceAttack;
import java.util.Stack;


public class Position {//TODO: get rid of unnecessary methods and put them in another class

    //Position representation variables
    public long[] PieceArray = new long[15];
    public byte[] squareCentricPos = new byte[64];
    public long castlingRights;
    public int hundredHalfmoveTimer;
    public int enPassantTargetFiles;
    public boolean whiteToMove;
    public byte[][] pieceSquareList = new byte[15][];
    {
        pieceSquareList[Type.White | Type.Pawn] = new byte[8];
        pieceSquareList[Type.White | Type.Knight] = new byte[10];//max 10 knights with 8 pawn underpromotions
        pieceSquareList[Type.White | Type.Bishop] = new byte[10];//same idea for the rest of the pieces
        pieceSquareList[Type.White | Type.Rook] = new byte[10];
        pieceSquareList[Type.White | Type.Queen] = new byte[9];
        pieceSquareList[Type.White | Type.King] = new byte[1];
        pieceSquareList[Type.Black | Type.Pawn] = new byte[8];
        pieceSquareList[Type.Black | Type.Knight] = new byte[10];
        pieceSquareList[Type.Black | Type.Bishop] = new byte[10];
        pieceSquareList[Type.Black | Type.Rook] = new byte[10];
        pieceSquareList[Type.Black | Type.Queen] = new byte[9];
        pieceSquareList[Type.Black | Type.King] = new byte[1];
    }
    public byte[] numPieces = new byte[15];
    public byte[][] colorIndexBoard = new byte[2][64];//0 for white, 1 for black
    public byte gameState;

    //additional useful variables in the position
    public Stack<Integer> PreviousMadeMoves = new Stack<>();
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
                PieceArray[getPieceFromString(temp)]+=toBitboard(tickerSquare);
                tickerSquare++;
            }
        }

        for (int x=0;x<64;x++) {
            byte pieceOnX = getPieceFromSquareWithBB(x,PieceArray);
            squareCentricPos[x]=pieceOnX;

            if (pieceOnX != Type.Empty) {
                pieceSquareList[pieceOnX][numPieces[pieceOnX]]= (byte) x;
                colorIndexBoard[pieceOnX/8][x] = numPieces[pieceOnX];
                numPieces[pieceOnX]++;
            }
        }

        whiteToMove = fen.substring(indexOfFirstSpace+1,indexOfFirstSpace+2).equals("w");

        int endOfFen = fen.length();
        String fenWithoutPosition = fen.substring(indexOfFirstSpace,endOfFen);

        if (fenWithoutPosition.contains("K"))castlingRights |= Type.whiteCanCS;
        if (fenWithoutPosition.contains("Q"))castlingRights |= Type.whiteCanCL;
        if (fenWithoutPosition.contains("k"))castlingRights |= Type.blackCanCS;
        if (fenWithoutPosition.contains("q"))castlingRights |= Type.blackCanCL;

        String epTargetSquare = fen.substring(fen.length()-6,fen.length()-5);
        if (!epTargetSquare.equals(" "))enPassantTargetFiles= switch (epTargetSquare) {
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
            default -> {
                yield -1;
            }
        };

        String moveClocks = fen.substring(endOfFen-3,endOfFen);
        hundredHalfmoveTimer= Integer.parseInt(moveClocks.substring(0,1));

        calculatePreCalculatedData();
        calculateLegalMoves();
    }
    public Position(long[] PieceArray, byte[] squareCentricPos, boolean whiteToMove) {
        System.arraycopy(squareCentricPos, 0, this.squareCentricPos, 0, 64);
        System.arraycopy(PieceArray, 0, this.PieceArray, 0, 15);
        this.whiteToMove=whiteToMove;
    }

    //TODO: fix a probable bug in the way castling works with piece lists
    //I sometimes get huge error messages when I castling is legal or when I move a King
    //Find out if the bug only exists in quiescence eval
    public void makeMove(int move) {
        quietlyMakeMove(move);
        calculatePreCalculatedData();
    }
    private void quietlyMakeMove(int move) {//TODO: make only one switch case, not two
        PreviousMadeMoves.push(move);
        PreviousCastlingRights.push(castlingRights);
        PreviousEnPassantTargetFiles.push(enPassantTargetFiles);
        PreviousHalfMoveTimers.push(hundredHalfmoveTimer);
        PreviousMovelists.push(cloneMoveArray(legalMoves, indexOfFirstEmptyMove));
        PreviousIndexOfFirstEmptyMove.push(indexOfFirstEmptyMove);

        byte fromSquare = Move.getFromSquareFromMove(move);
        byte toSquare = Move.getToSquareFromMove(move);
        byte moveType = Move.getMoveTypeFromMove(move);
        byte movingPiece = squareCentricPos[fromSquare];
        byte colorIndex = (byte)(movingPiece/8);
        byte index = colorIndexBoard[colorIndex][fromSquare];

        switch (moveType) {
            case Type.normalMove -> {
                colorIndexBoard[colorIndex][toSquare] = index;
                pieceSquareList[movingPiece][index] = toSquare;
            }
            case Type.enPassant -> {
                colorIndexBoard[colorIndex][toSquare] = index;
                pieceSquareList[movingPiece][index] = toSquare;

                byte squareOfCapturedPawn = (byte) (whiteToMove ? toSquare-8 : toSquare+8);
                colorIndex ^= 1;
                byte capturedPiece = (byte) (colorIndex*8 | Type.Pawn);

                numPieces[capturedPiece]--;
                index = colorIndexBoard[colorIndex][squareOfCapturedPawn];
                byte square = pieceSquareList[capturedPiece][numPieces[capturedPiece]];
                pieceSquareList[capturedPiece][index] = square;
                colorIndexBoard[colorIndex][square] = index;
            }
            case Type.doublePawnMove -> {
                colorIndexBoard[colorIndex][toSquare] = index;
                pieceSquareList[movingPiece][index] = toSquare;
            }
            case Type.castles -> {
                colorIndexBoard[colorIndex][toSquare] = index;
                pieceSquareList[movingPiece][index] = toSquare;

                switch (toSquare) {
                    case 6-> {//white castles short
                        index = colorIndexBoard[colorIndex][7];
                        movingPiece = (byte) (colorIndex*8 | Type.Rook);
                        colorIndexBoard[colorIndex][5] = index;
                        pieceSquareList[movingPiece][index] = 5;
                    }
                    case 2-> {//white castles long
                        index = colorIndexBoard[colorIndex][0];
                        movingPiece = (byte) (colorIndex*8 | Type.Rook);
                        colorIndexBoard[colorIndex][3] = index;
                        pieceSquareList[movingPiece][index] = 3;
                    }
                    case 62-> {//black castles short
                        index = colorIndexBoard[colorIndex][63];
                        movingPiece = (byte) (colorIndex*8 | Type.Rook);
                        colorIndexBoard[colorIndex][61] = index;
                        pieceSquareList[movingPiece][index] = 61;
                    }
                    case 58-> {//black castles long
                        index = colorIndexBoard[colorIndex][56];
                        movingPiece = (byte) (colorIndex*8 | Type.Rook);
                        colorIndexBoard[colorIndex][59] = index;
                        pieceSquareList[movingPiece][index] = 59;
                    }
                }
            }
            case Type.pawnPromotesToQ -> {
                byte colorPromotingTo = (byte) (colorIndex*8);
                byte piecePromotingTo = (byte) (colorPromotingTo | Type.Queen);

                numPieces[movingPiece]--;
                index = colorIndexBoard[colorIndex][fromSquare];
                byte square = pieceSquareList[movingPiece][numPieces[movingPiece]];
                pieceSquareList[movingPiece][index] = square;
                colorIndexBoard[colorIndex][square] = index;

                pieceSquareList[piecePromotingTo][numPieces[piecePromotingTo]] = toSquare;
                colorIndexBoard[colorIndex][toSquare] = numPieces[piecePromotingTo];
                numPieces[piecePromotingTo]++;
            }
            case Type.pawnPromotesToN -> {
                byte colorPromotingTo = (byte) (colorIndex*8);
                byte piecePromotingTo = (byte) (colorPromotingTo | Type.Knight);

                numPieces[movingPiece]--;
                index = colorIndexBoard[colorIndex][fromSquare];
                byte square = pieceSquareList[movingPiece][numPieces[movingPiece]];
                pieceSquareList[movingPiece][index] = square;
                colorIndexBoard[colorIndex][square] = index;

                pieceSquareList[piecePromotingTo][numPieces[piecePromotingTo]] = toSquare;
                colorIndexBoard[colorIndex][toSquare] = numPieces[piecePromotingTo];
                numPieces[piecePromotingTo]++;
            }
            case Type.pawnPromotesToB -> {
                byte colorPromotingTo = (byte) (colorIndex*8);
                byte piecePromotingTo = (byte) (colorPromotingTo | Type.Bishop);

                numPieces[movingPiece]--;
                index = colorIndexBoard[colorIndex][fromSquare];
                byte square = pieceSquareList[movingPiece][numPieces[movingPiece]];
                pieceSquareList[movingPiece][index] = square;
                colorIndexBoard[colorIndex][square] = index;

                pieceSquareList[piecePromotingTo][numPieces[piecePromotingTo]] = toSquare;
                colorIndexBoard[colorIndex][toSquare] = numPieces[piecePromotingTo];
                numPieces[piecePromotingTo]++;
            }
            case Type.pawnPromotesToR -> {
                byte colorPromotingTo = (byte) (colorIndex*8);
                byte piecePromotingTo = (byte) (colorPromotingTo | Type.Rook);

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
        byte capturedPiece = squareCentricPos[toSquare];
        if (capturedPiece != Type.Empty) {//update piece list for captured piece
            numPieces[capturedPiece]--;
            colorIndex ^= 1;
            index = colorIndexBoard[colorIndex][toSquare];
            byte lastSquare = pieceSquareList[capturedPiece][numPieces[capturedPiece]];
            pieceSquareList[capturedPiece][index] = lastSquare;
            colorIndexBoard[colorIndex][lastSquare] = index;
        }

        switch (moveType) {
            case Type.normalMove -> {
                long fromSquareBB = toBitboard(fromSquare);
                long toSquareBB = toBitboard(toSquare);

                squareCentricPos[fromSquare]= Type.Empty;//updates redundant squareCentricPosition
                squareCentricPos[toSquare]=movingPiece;//updates redundant squareCentricPosition

                PieceArray[movingPiece]^=fromSquareBB|toSquareBB;//moves piece on bb
                PieceArray[capturedPiece]^=toSquareBB;//clears captured piece from bb

                enPassantTargetFiles=0;
                whiteToMove= !whiteToMove;
                hundredHalfmoveTimer++;
                if (capturedPiece!=0||movingPiece%8== Type.Pawn)hundredHalfmoveTimer=0;//resets Half-move timer to 0 if capture/pawn push

                //Can AND the king bitshifted each direction with castlingRights to check if the king moved, if it does, will lose the right to castle
                castlingRights &= PieceArray[Type.White | Type.King]<<2 | PieceArray[Type.White | Type.King]>>>2 | PieceArray[Type.Black | Type.King]<<2 | PieceArray[Type.Black | Type.King]>>>2;

                //Can AND the rooks bitshifted to where the king will be to check if the rook still exists on that square, if it doesn't, will lose the right to castle
                castlingRights &= PieceArray[Type.White | Type.Rook]<<2 | (PieceArray[Type.White | Type.Rook] & Constants.CORNERS)>>>1 | PieceArray[Type.Black | Type.Rook]<<2 | (PieceArray[Type.Black | Type.Rook] & Constants.CORNERS)>>>1;
            }
            case Type.enPassant -> {
                quietlyEnPassant(fromSquare, toSquare);
            }
            case Type.doublePawnMove -> {
                long fromSquareBB = toBitboard(fromSquare);
                long toSquareBB = toBitboard(toSquare);

                squareCentricPos[fromSquare]= Type.Empty;//updates redundant squareCentricPosition
                squareCentricPos[toSquare]=movingPiece;//updates redundant squareCentricPosition

                PieceArray[movingPiece]^=fromSquareBB|toSquareBB;//moves piece on bb

                hundredHalfmoveTimer=0;
                whiteToMove= !whiteToMove;
                enPassantTargetFiles = 1<<toSquare%8;//creates enPassantTargetFile
            }
            case Type.castles -> {
                switch (toSquare) {
                    case 6 -> {//white castles short
                        PieceArray[Type.White| Type.King]^= Constants.g1 | Constants.e1;
                        PieceArray[Type.White| Type.Rook]^= Constants.h1 | Constants.f1;
                        squareCentricPos[6]= Type.White | Type.King;
                        squareCentricPos[5]= Type.White | Type.Rook;
                        squareCentricPos[4]= Type.Empty;
                        squareCentricPos[7]= Type.Empty;

                        castlingRights&= Type.notWhiteCanCS & Type.notWhiteCanCL;
                    }
                    case 62 -> {//black castles short
                        PieceArray[Type.Black| Type.King]^= Constants.g8 | Constants.e8;
                        PieceArray[Type.Black| Type.Rook]^= Constants.h8 | Constants.f8;
                        squareCentricPos[62]= Type.Black | Type.King;
                        squareCentricPos[61]= Type.Black | Type.Rook;
                        squareCentricPos[60]= Type.Empty;
                        squareCentricPos[63]= Type.Empty;

                        castlingRights&= Type.notBlackCanCS & Type.notBlackCanCL;
                    }
                    case 2 -> {//white castles long
                        PieceArray[Type.White| Type.King]^= Constants.c1 | Constants.e1;
                        PieceArray[Type.White| Type.Rook]^= Constants.d1 | Constants.a1;
                        squareCentricPos[2]= Type.White | Type.King;
                        squareCentricPos[3]= Type.White | Type.Rook;
                        squareCentricPos[0]= Type.Empty;
                        squareCentricPos[4]= Type.Empty;

                        castlingRights&= Type.notWhiteCanCS & Type.notWhiteCanCL;
                    }
                    case 58 -> {//black castles long
                        PieceArray[Type.Black| Type.King]^= Constants.c8 | Constants.e8;
                        PieceArray[Type.Black| Type.Rook]^= Constants.d8 | Constants.a8;
                        squareCentricPos[58]= Type.Black | Type.King;
                        squareCentricPos[59]= Type.Black | Type.Rook;
                        squareCentricPos[56]= Type.Empty;
                        squareCentricPos[60]= Type.Empty;

                        castlingRights&= Type.notBlackCanCS & Type.notBlackCanCL;
                    }
                }

                hundredHalfmoveTimer++;
                enPassantTargetFiles=0;
                whiteToMove= !whiteToMove;
            }
            case Type.pawnPromotesToQ -> {
                short colorPromoting= Type.White;
                if (toSquare/8==0) {
                    colorPromoting= Type.Black;
                }

                long fromSquareBB = toBitboard(fromSquare);
                long toSquareBB = toBitboard(toSquare);

                squareCentricPos[fromSquare]= Type.Empty;//updates redundant squareCentricPosition
                squareCentricPos[toSquare]= (byte)(colorPromoting | Type.Queen);//updates redundant squareCentricPosition

                PieceArray[colorPromoting | Type.Pawn]^=fromSquareBB;
                PieceArray[colorPromoting | Type.Queen]^=toSquareBB;
                PieceArray[capturedPiece] ^=toSquareBB;

                hundredHalfmoveTimer=0;
                whiteToMove= !whiteToMove;
                enPassantTargetFiles=0;
            }
            case Type.pawnPromotesToN -> {
                short colorPromoting= Type.White;
                if (toSquare/8==0) {
                    colorPromoting= Type.Black;
                }

                long fromSquareBB = toBitboard(fromSquare);
                long toSquareBB = toBitboard(toSquare);

                squareCentricPos[fromSquare]= Type.Empty;//updates redundant squareCentricPosition
                squareCentricPos[toSquare]= (byte)(colorPromoting | Type.Knight);//updates redundant squareCentricPosition

                PieceArray[colorPromoting | Type.Pawn]^=fromSquareBB;
                PieceArray[colorPromoting | Type.Knight]^=toSquareBB;
                PieceArray[capturedPiece] ^=toSquareBB;

                hundredHalfmoveTimer=0;
                whiteToMove= !whiteToMove;
                enPassantTargetFiles=0;
            }
            case Type.pawnPromotesToB -> {
                short colorPromoting= Type.White;
                if (toSquare/8==0) {
                    colorPromoting= Type.Black;
                }

                long fromSquareBB = toBitboard(fromSquare);
                long toSquareBB = toBitboard(toSquare);

                squareCentricPos[fromSquare]= Type.Empty;//updates redundant squareCentricPosition
                squareCentricPos[toSquare]= (byte)(colorPromoting | Type.Bishop);//updates redundant squareCentricPosition

                PieceArray[colorPromoting | Type.Pawn]^=fromSquareBB;
                PieceArray[colorPromoting | Type.Bishop]^=toSquareBB;
                PieceArray[capturedPiece] ^=toSquareBB;

                hundredHalfmoveTimer=0;
                whiteToMove= !whiteToMove;
                enPassantTargetFiles=0;
            }
            case Type.pawnPromotesToR -> {
                short colorPromoting= Type.White;
                if (toSquare/8==0) {
                    colorPromoting= Type.Black;
                }

                long fromSquareBB = toBitboard(fromSquare);
                long toSquareBB = toBitboard(toSquare);

                squareCentricPos[fromSquare]= Type.Empty;//updates redundant squareCentricPosition
                squareCentricPos[toSquare]= (byte)(colorPromoting | Type.Rook);//updates redundant squareCentricPosition

                PieceArray[colorPromoting | Type.Pawn]^=fromSquareBB;
                PieceArray[colorPromoting | Type.Rook]^=toSquareBB;
                PieceArray[capturedPiece] ^=toSquareBB;

                hundredHalfmoveTimer=0;
                whiteToMove= !whiteToMove;
                enPassantTargetFiles=0;
            }
        }


        indexOfFirstEmptyMove=0;
    }
    public void unmakeMove(int move) {
        byte fromSquare = Move.getFromSquareFromMove(move);
        byte toSquare = Move.getToSquareFromMove(move);
        byte moveType = Move.getMoveTypeFromMove(move);
        byte movingPiece = squareCentricPos[toSquare];
        byte colorIndex = (byte)(movingPiece/8);
        byte index = colorIndexBoard[colorIndex][toSquare];


        switch (moveType) {
            case Type.normalMove -> {
                colorIndexBoard[colorIndex][fromSquare] = index;
                pieceSquareList[movingPiece][index] = fromSquare;
            }
            case Type.enPassant -> {
                colorIndexBoard[colorIndex][fromSquare] = index;
                pieceSquareList[movingPiece][index] = fromSquare;

                byte squareOfCapturedPawn = (byte) (!whiteToMove ? toSquare-8 : toSquare+8);//try switching these if doesn't work on first try
                colorIndex ^= 1;
                byte capturedPiece = (byte) (colorIndex*8 | Type.Pawn);

                pieceSquareList[capturedPiece][numPieces[capturedPiece]] = squareOfCapturedPawn;
                colorIndexBoard[colorIndex][squareOfCapturedPawn] = numPieces[capturedPiece];
                numPieces[capturedPiece]++;
            }
            case Type.doublePawnMove -> {
                colorIndexBoard[colorIndex][fromSquare] = index;
                pieceSquareList[movingPiece][index] = fromSquare;
            }
            case Type.castles -> {
                colorIndexBoard[colorIndex][fromSquare] = index;
                pieceSquareList[movingPiece][index] = fromSquare;

                switch (toSquare) {
                    case 6-> {//white castles short
                        index = colorIndexBoard[colorIndex][5];
                        movingPiece = (byte) (colorIndex*8 | Type.Rook);
                        colorIndexBoard[colorIndex][7] = index;
                        pieceSquareList[movingPiece][index] = 7;
                    }
                    case 2-> {//white castles long
                        index = colorIndexBoard[colorIndex][3];
                        movingPiece = (byte) (colorIndex*8 | Type.Rook);
                        colorIndexBoard[colorIndex][0] = index;
                        pieceSquareList[movingPiece][index] = 0;
                    }
                    case 62-> {//black castles short
                        index = colorIndexBoard[colorIndex][61];
                        movingPiece = (byte) (colorIndex*8 | Type.Rook);
                        colorIndexBoard[colorIndex][63] = index;
                        pieceSquareList[movingPiece][index] = 63;
                    }
                    case 58-> {//black castles long
                        index = colorIndexBoard[colorIndex][59];
                        movingPiece = (byte) (colorIndex*8 | Type.Rook);
                        colorIndexBoard[colorIndex][56] = index;
                        pieceSquareList[movingPiece][index] = 56;
                    }
                }
            }
            case Type.pawnPromotesToQ -> {
                byte colorPromoting = (byte) (colorIndex*8);
                byte piecePromotedTo = squareCentricPos[toSquare];
                byte piecePromotedFrom = (byte) (colorPromoting | Type.Pawn);

                numPieces[piecePromotedTo]--;
                index = colorIndexBoard[colorIndex][toSquare];
                byte square = pieceSquareList[piecePromotedTo][numPieces[piecePromotedTo]];
                pieceSquareList[piecePromotedTo][index] = square;
                colorIndexBoard[colorIndex][square] = index;

                pieceSquareList[piecePromotedFrom][numPieces[piecePromotedFrom]] = fromSquare;
                colorIndexBoard[colorIndex][fromSquare] = numPieces[piecePromotedFrom];
                numPieces[piecePromotedFrom]++;
            }
            case Type.pawnPromotesToN -> {
                byte colorPromoting = (byte) (colorIndex*8);
                byte piecePromotedTo = squareCentricPos[toSquare];
                byte piecePromotedFrom = (byte) (colorPromoting | Type.Pawn);

                numPieces[piecePromotedTo]--;
                index = colorIndexBoard[colorIndex][toSquare];
                byte square = pieceSquareList[piecePromotedTo][numPieces[piecePromotedTo]];
                pieceSquareList[piecePromotedTo][index] = square;
                colorIndexBoard[colorIndex][square] = index;

                pieceSquareList[piecePromotedFrom][numPieces[piecePromotedFrom]] = fromSquare;
                colorIndexBoard[colorIndex][fromSquare] = numPieces[piecePromotedFrom];
                numPieces[piecePromotedFrom]++;
            }
            case Type.pawnPromotesToB -> {
                byte colorPromoting = (byte) (colorIndex*8);
                byte piecePromotedTo = squareCentricPos[toSquare];
                byte piecePromotedFrom = (byte) (colorPromoting | Type.Pawn);

                numPieces[piecePromotedTo]--;
                index = colorIndexBoard[colorIndex][toSquare];
                byte square = pieceSquareList[piecePromotedTo][numPieces[piecePromotedTo]];
                pieceSquareList[piecePromotedTo][index] = square;
                colorIndexBoard[colorIndex][square] = index;

                pieceSquareList[piecePromotedFrom][numPieces[piecePromotedFrom]] = fromSquare;
                colorIndexBoard[colorIndex][fromSquare] = numPieces[piecePromotedFrom];
                numPieces[piecePromotedFrom]++;
            }
            case Type.pawnPromotesToR -> {
                byte colorPromoting = (byte) (colorIndex*8);
                byte piecePromotedTo = squareCentricPos[toSquare];
                byte piecePromotedFrom = (byte) (colorPromoting | Type.Pawn);

                numPieces[piecePromotedTo]--;
                index = colorIndexBoard[colorIndex][toSquare];
                byte square = pieceSquareList[piecePromotedTo][numPieces[piecePromotedTo]];
                pieceSquareList[piecePromotedTo][index] = square;
                colorIndexBoard[colorIndex][square] = index;

                pieceSquareList[piecePromotedFrom][numPieces[piecePromotedFrom]] = fromSquare;
                colorIndexBoard[colorIndex][fromSquare] = numPieces[piecePromotedFrom];
                numPieces[piecePromotedFrom]++;
            }
        }
        byte capturedPiece = Move.getCapturedPieceFromMove(move);

        if (capturedPiece !=0) {
            colorIndex ^=1;
            pieceSquareList[capturedPiece][numPieces[capturedPiece]] = toSquare;
            colorIndexBoard[colorIndex][toSquare] = numPieces[capturedPiece];
            numPieces[capturedPiece]++;
        }

        switch (moveType) {
            case Type.normalMove-> {
                long fromSquareBB = toBitboard(fromSquare);
                long toSquareBB = toBitboard(toSquare);

                squareCentricPos[toSquare]= capturedPiece;//updates redundant squareCentricPosition
                squareCentricPos[fromSquare]=movingPiece;//updates redundant squareCentricPosition

                PieceArray[movingPiece]^=fromSquareBB|toSquareBB;
                PieceArray[capturedPiece]^=toSquareBB;
            }
            case Type.doublePawnMove -> {
                long fromSquareBB = toBitboard(fromSquare);
                long toSquareBB = toBitboard(toSquare);

                squareCentricPos[toSquare]= Type.Empty;
                squareCentricPos[fromSquare]=movingPiece;

                PieceArray[movingPiece]^=fromSquareBB|toSquareBB;
            }
            case Type.castles -> {
                long toSquareBB=toBitboard(toSquare);

                if (toSquareBB==Constants.g1) {//white castles short
                    PieceArray[Type.White| Type.King]^= Constants.g1 | Constants.e1;
                    PieceArray[Type.White| Type.Rook]^= Constants.h1 | Constants.f1;
                    squareCentricPos[6]= Type.Empty;
                    squareCentricPos[5]= Type.Empty;
                    squareCentricPos[4]= Type.White | Type.King;
                    squareCentricPos[7]= Type.White | Type.Rook;
                }
                else if (toSquareBB==Constants.g8) {//black castles short
                    PieceArray[Type.Black| Type.King]^= Constants.g8 | Constants.e8;
                    PieceArray[Type.Black| Type.Rook]^= Constants.h8 | Constants.f8;
                    squareCentricPos[62]= Type.Empty;
                    squareCentricPos[61]= Type.Empty;
                    squareCentricPos[60]= Type.Black | Type.King;
                    squareCentricPos[63]= Type.Black | Type.Rook;
                }
                else if (toSquareBB==Constants.c1) {//white castles long
                    PieceArray[Type.White| Type.King]^= Constants.c1 | Constants.e1;
                    PieceArray[Type.White| Type.Rook]^= Constants.d1 | Constants.a1;
                    squareCentricPos[2]= Type.Empty;
                    squareCentricPos[3]= Type.Empty;
                    squareCentricPos[0]= Type.White | Type.Rook;
                    squareCentricPos[4]= Type.White | Type.King;
                }
                else if (toSquareBB==Constants.c8) {//black castles long
                    PieceArray[Type.Black| Type.King]^= Constants.c8 | Constants.e8;
                    PieceArray[Type.Black| Type.Rook]^= Constants.d8 | Constants.a8;
                    squareCentricPos[58]= Type.Empty;
                    squareCentricPos[59]= Type.Empty;
                    squareCentricPos[56]= Type.Black | Type.Rook;
                    squareCentricPos[60]= Type.Black | Type.King;
                }
            }
            case Type.enPassant -> {
                long fromSquareBB = toBitboard(fromSquare);
                long toSquareBB = toBitboard(toSquare);

                int enPassantCapturedSquare=toSquare-8;
                short colorNotMoving= Type.Black;
                if (whiteToMove){//inverse of not white to move
                    enPassantCapturedSquare=toSquare+8;
                    colorNotMoving= Type.White;
                }

                squareCentricPos[toSquare]= Type.Empty;//updates redundant squareCentricPosition
                squareCentricPos[fromSquare]=movingPiece;//updates redundant squareCentricPosition
                squareCentricPos[enPassantCapturedSquare]= (byte)(colorNotMoving | Type.Pawn);

                PieceArray[movingPiece]^=fromSquareBB|toSquareBB;
                PieceArray[colorNotMoving | Type.Pawn]^=toBitboard(enPassantCapturedSquare);
            }
            case Type.pawnPromotesToQ -> {
                short colorPromoting= toSquare/8==7 ? Type.White : Type.Black;

                long fromSquareBB = toBitboard(fromSquare);
                long toSquareBB = toBitboard(toSquare);

                squareCentricPos[toSquare]= capturedPiece;//updates redundant squareCentricPosition
                squareCentricPos[fromSquare]= (byte)(colorPromoting | Type.Pawn);//updates redundant squareCentricPosition

                PieceArray[colorPromoting | Type.Pawn]^=fromSquareBB;
                PieceArray[colorPromoting | Type.Queen]^=toSquareBB;
                PieceArray[capturedPiece] ^=toSquareBB;
            }
            case Type.pawnPromotesToN -> {
                short colorPromoting= toSquare/8==7 ? Type.White : Type.Black;

                long fromSquareBB = toBitboard(fromSquare);
                long toSquareBB = toBitboard(toSquare);

                squareCentricPos[toSquare]= capturedPiece;//updates redundant squareCentricPosition
                squareCentricPos[fromSquare]= (byte)(colorPromoting | Type.Pawn);//updates redundant squareCentricPosition

                PieceArray[colorPromoting | Type.Pawn]^=fromSquareBB;
                PieceArray[colorPromoting | Type.Knight]^=toSquareBB;
                PieceArray[capturedPiece] ^=toSquareBB;
            }
            case Type.pawnPromotesToB -> {
                short colorPromoting= toSquare/8==7 ? Type.White : Type.Black;

                long fromSquareBB = toBitboard(fromSquare);
                long toSquareBB = toBitboard(toSquare);

                squareCentricPos[toSquare]= capturedPiece;//updates redundant squareCentricPosition
                squareCentricPos[fromSquare]= (byte)(colorPromoting | Type.Pawn);//updates redundant squareCentricPosition

                PieceArray[colorPromoting | Type.Pawn]^=fromSquareBB;
                PieceArray[colorPromoting | Type.Bishop]^=toSquareBB;
                PieceArray[capturedPiece] ^=toSquareBB;
            }
            case Type.pawnPromotesToR -> {
                short colorPromoting= toSquare/8==7 ? Type.White : Type.Black;

                long fromSquareBB = toBitboard(fromSquare);
                long toSquareBB = toBitboard(toSquare);

                squareCentricPos[toSquare]= capturedPiece;//updates redundant squareCentricPosition
                squareCentricPos[fromSquare]= (byte)(colorPromoting | Type.Pawn);//updates redundant squareCentricPosition

                PieceArray[colorPromoting | Type.Pawn]^=fromSquareBB;
                PieceArray[colorPromoting | Type.Rook]^=toSquareBB;
                PieceArray[capturedPiece] ^=toSquareBB;
            }
        }

        whiteToMove= !whiteToMove;
        gameState = calculateGameState();
        enPassantTargetFiles= PreviousEnPassantTargetFiles.pop();
        castlingRights= PreviousCastlingRights.pop();
        hundredHalfmoveTimer= PreviousHalfMoveTimers.pop();
        legalMoves= PreviousMovelists.pop();
        indexOfFirstEmptyMove= PreviousIndexOfFirstEmptyMove.pop();
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
        gameState = calculateGameState();
    }

    public void calculatePieceLocations() {
        whitePieces = PieceArray[Type.White| Type.Pawn]|PieceArray[Type.White| Type.Knight]|PieceArray[Type.White| Type.Bishop]|PieceArray[Type.White| Type.Rook]|PieceArray[Type.White| Type.Queen]|PieceArray[Type.White| Type.King];
        blackPieces = PieceArray[Type.Black| Type.Pawn]|PieceArray[Type.Black| Type.Knight]|PieceArray[Type.Black| Type.Bishop]|PieceArray[Type.Black| Type.Rook]|PieceArray[Type.Black| Type.Queen]|PieceArray[Type.Black| Type.King];
        allPieces = whitePieces | blackPieces;
        emptySquares = ~allPieces;
    }
    public void calculateSquareAttacks() {
        long tempPieceBB= PieceArray[Type.White | Type.Pawn];

        for (int i=0;i<64;i++) {
            squareAttacksArray[i]=0;
        }
        for (int i=1;i<7;i++) {
            whiteAttacksArray[i]=0;
            blackAttacksArray[i]=0;
        }

        long whiteBlockers = allPieces ^ PieceArray[Type.Black | Type.King];
        long blackBlockers = allPieces ^ PieceArray[Type.White | Type.King];

        for (int i=0;i<numPieces[Type.White | Type.Pawn];i++) {
            int pieceSquare = pieceSquareList[Type.White | Type.Pawn][i];
            long pieceAttackBB = PieceAttack.lookUpWhitePawnAttacks(pieceSquare);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            whiteAttacksArray[Type.Pawn] |= pieceAttackBB;
        }

        for (int i=0;i<numPieces[Type.White | Type.Knight];i++) {
            int pieceSquare = pieceSquareList[Type.White | Type.Knight][i];
            long pieceAttackBB = PieceAttack.lookUpKnightAttacks(pieceSquare);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            whiteAttacksArray[Type.Knight] |= pieceAttackBB;
        }

        for (int i=0;i<numPieces[Type.White | Type.Bishop];i++) {
            int pieceSquare = pieceSquareList[Type.White | Type.Bishop][i];
            long pieceAttackBB = PieceAttack.lookUpBishopAttacks(pieceSquare, whiteBlockers);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            whiteAttacksArray[Type.Bishop] |= pieceAttackBB;
        }

        for (int i=0;i<numPieces[Type.White | Type.Rook];i++) {
            int pieceSquare = pieceSquareList[Type.White | Type.Rook][i];
            long pieceAttackBB = PieceAttack.lookUpRookAttacks(pieceSquare, whiteBlockers);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            whiteAttacksArray[Type.Rook] |= pieceAttackBB;
        }

        for (int i=0;i<numPieces[Type.White | Type.Queen];i++) {
            int pieceSquare = pieceSquareList[Type.White | Type.Queen][i];
            long pieceAttackBB = PieceAttack.lookUpQueenAttacks(pieceSquare, whiteBlockers);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            whiteAttacksArray[Type.Queen] |= pieceAttackBB;
        }

        {
            int pieceSquare = pieceSquareList[Type.White | Type.King][0];
            long pieceAttackBB = PieceAttack.lookUpKingAttacks(pieceSquare);
            squareAttacksArray[pieceSquare] = pieceAttackBB;
            whiteAttacksArray[Type.King] |= pieceAttackBB;
        }


        for (int i=0;i<numPieces[Type.Black | Type.Pawn];i++) {
            int pieceSquare = pieceSquareList[Type.Black | Type.Pawn][i];
            long pieceAttackBB = PieceAttack.lookUpBlackPawnAttacks(pieceSquare);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            blackAttacksArray[Type.Pawn] |= pieceAttackBB;
        }

        for (int i=0;i<numPieces[Type.Black | Type.Knight];i++) {
            int pieceSquare = pieceSquareList[Type.Black | Type.Knight][i];
            long pieceAttackBB = PieceAttack.lookUpKnightAttacks(pieceSquare);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            blackAttacksArray[Type.Knight] |= pieceAttackBB;
        }

        for (int i=0;i<numPieces[Type.Black | Type.Bishop];i++) {
            int pieceSquare = pieceSquareList[Type.Black | Type.Bishop][i];
            long pieceAttackBB = PieceAttack.lookUpBishopAttacks(pieceSquare, blackBlockers);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            blackAttacksArray[Type.Bishop] |= pieceAttackBB;
        }

        for (int i=0;i<numPieces[Type.Black | Type.Rook];i++) {
            int pieceSquare = pieceSquareList[Type.Black | Type.Rook][i];
            long pieceAttackBB = PieceAttack.lookUpRookAttacks(pieceSquare, blackBlockers);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            blackAttacksArray[Type.Rook] |= pieceAttackBB;
        }

        for (int i=0;i<numPieces[Type.Black | Type.Queen];i++) {
            int pieceSquare = pieceSquareList[Type.Black | Type.Queen][i];
            long pieceAttackBB = PieceAttack.lookUpQueenAttacks(pieceSquare, blackBlockers);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            blackAttacksArray[Type.Queen] |= pieceAttackBB;
        }

        {
            int pieceSquare = pieceSquareList[Type.Black | Type.King][0];
            long pieceAttackBB = PieceAttack.lookUpKingAttacks(pieceSquare);
            squareAttacksArray[pieceSquare] = pieceAttackBB;
            blackAttacksArray[Type.King] |= pieceAttackBB;
        }
    }
    public void calculateInCheck() {
        numChecks=0;
        whiteAttacks = whiteAttacksArray[Type.Pawn] | whiteAttacksArray[Type.Knight] | whiteAttacksArray[Type.Bishop] | whiteAttacksArray[Type.Rook] | whiteAttacksArray[Type.Queen] | whiteAttacksArray[Type.King];
        blackAttacks = blackAttacksArray[Type.Pawn] | blackAttacksArray[Type.Knight] | blackAttacksArray[Type.Bishop] | blackAttacksArray[Type.Rook] | blackAttacksArray[Type.Queen] | blackAttacksArray[Type.King];
        if (whiteToMove) {
            if ((blackAttacksArray[Type.Pawn] & PieceArray[Type.White | Type.King]) == PieceArray[Type.White | Type.King])numChecks++;
            if ((blackAttacksArray[Type.Knight] & PieceArray[Type.White | Type.King]) == PieceArray[Type.White | Type.King])numChecks++;
            if ((blackAttacksArray[Type.Bishop] & PieceArray[Type.White | Type.King]) == PieceArray[Type.White | Type.King])numChecks++;
            if ((blackAttacksArray[Type.Rook] & PieceArray[Type.White | Type.King]) == PieceArray[Type.White | Type.King])numChecks++;
            if ((blackAttacksArray[Type.Queen] & PieceArray[Type.White | Type.King]) == PieceArray[Type.White | Type.King])numChecks++;
            if ((blackAttacksArray[Type.King] & PieceArray[Type.White | Type.King]) == PieceArray[Type.White | Type.King])numChecks++;

            inCheck= numChecks>0;
        }
        else {
            if ((whiteAttacksArray[Type.Pawn] & PieceArray[Type.Black | Type.King]) == PieceArray[Type.Black | Type.King])numChecks++;
            if ((whiteAttacksArray[Type.Knight] & PieceArray[Type.Black | Type.King]) == PieceArray[Type.Black | Type.King])numChecks++;
            if ((whiteAttacksArray[Type.Bishop] & PieceArray[Type.Black | Type.King]) == PieceArray[Type.Black | Type.King])numChecks++;
            if ((whiteAttacksArray[Type.Rook] & PieceArray[Type.Black | Type.King]) == PieceArray[Type.Black | Type.King])numChecks++;
            if ((whiteAttacksArray[Type.Queen] & PieceArray[Type.Black | Type.King]) == PieceArray[Type.Black | Type.King])numChecks++;
            if ((whiteAttacksArray[Type.King] & PieceArray[Type.Black | Type.King]) == PieceArray[Type.Black | Type.King])numChecks++;

            inCheck= numChecks>0;
        }
    }
    public void calculateCheckResolveRay() {
        if (inCheck){
            if (whiteToMove) {
                if (numChecks>1)checkResolveRay=0;//if double check must move king
                else checkResolveRay=findCheckResolveRay(PieceArray[Type.White | Type.King]);
            }
            else {//black to move
                if (numChecks>1)checkResolveRay=0;//if double check must move king
                else checkResolveRay=findCheckResolveRay(PieceArray[Type.Black | Type.King]);
            }
        }
        else checkResolveRay=~0;
    }
    public void calculatePinRay() {
        pinnedPieces=0;
        if (whiteToMove) {
            long kingLocationBB = PieceArray[Type.King];
            int kingLocation = Long.numberOfTrailingZeros(kingLocationBB);
            assert (kingLocation < 64);

            long rookPinners = PieceArray[Type.Black | Type.Queen] | PieceArray[Type.Black | Type.Rook];
            long bishopPinners = PieceArray[Type.Black | Type.Queen] | PieceArray[Type.Black | Type.Bishop];

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
            long kingLocationBB = PieceArray[Type.Black | Type.King];
            int kingLocation = Long.numberOfTrailingZeros(kingLocationBB);
            long rookPinners = PieceArray[Type.White | Type.Queen] | PieceArray[Type.White | Type.Rook];
            long bishopPinners = PieceArray[Type.White | Type.Queen] | PieceArray[Type.White | Type.Bishop];

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
            colorToFindMovesFor=Type.White;
            notFriendlyPieces=~whitePieces;
        }
        else {
            colorToFindMovesFor=Type.Black;
            notFriendlyPieces=~blackPieces;
        }

        for (int i=0;i<numPieces[colorToFindMovesFor | Type.Pawn];i++) {
            generatePawnMoves(pieceSquareList[colorToFindMovesFor | Type.Pawn][i]);
        }

        for (int i=0;i<numPieces[colorToFindMovesFor | Type.Knight];i++) {
            generateKnightMoves(pieceSquareList[colorToFindMovesFor | Type.Knight][i], notFriendlyPieces);
        }

        for (int i=0;i<numPieces[colorToFindMovesFor | Type.Bishop];i++) {
            generateBishopMoves(pieceSquareList[colorToFindMovesFor | Type.Bishop][i], notFriendlyPieces);
        }

        for (int i=0;i<numPieces[colorToFindMovesFor | Type.Rook];i++) {
            generateRookMoves(pieceSquareList[colorToFindMovesFor | Type.Rook][i], notFriendlyPieces);
        }

        for (int i=0;i<numPieces[colorToFindMovesFor | Type.Queen];i++) {
            generateQueenMoves(pieceSquareList[colorToFindMovesFor | Type.Queen][i], notFriendlyPieces);
        }

        for (int i=0;i<numPieces[colorToFindMovesFor | Type.King];i++) {
            generateKingMoves(pieceSquareList[colorToFindMovesFor | Type.King][i]);
        }
    }
    public void findLegalCapturingMoves() {
        int colorToFindMovesFor;
        long notFriendlyPieces;
        long enemyPieces;

        if (whiteToMove) {
            colorToFindMovesFor=Type.White;
            notFriendlyPieces=~whitePieces;
            enemyPieces = blackPieces;
        }
        else {
            colorToFindMovesFor=Type.Black;
            notFriendlyPieces=~blackPieces;
            enemyPieces = whitePieces;
        }

        for (int i=0;i<numPieces[colorToFindMovesFor | Type.Pawn];i++) {
            squareAttacksArray[pieceSquareList[colorToFindMovesFor | Type.Pawn][i]] &= enemyPieces;
            generatePawnCapturesOnly(pieceSquareList[colorToFindMovesFor | Type.Pawn][i]);
        }

        for (int i=0;i<numPieces[colorToFindMovesFor | Type.Knight];i++) {
            squareAttacksArray[pieceSquareList[colorToFindMovesFor | Type.Knight][i]] &= enemyPieces;
            generateKnightMoves(pieceSquareList[colorToFindMovesFor | Type.Knight][i], notFriendlyPieces);
        }

        for (int i=0;i<numPieces[colorToFindMovesFor | Type.Bishop];i++) {
            squareAttacksArray[pieceSquareList[colorToFindMovesFor | Type.Bishop][i]] &= enemyPieces;
            generateBishopMoves(pieceSquareList[colorToFindMovesFor | Type.Bishop][i], notFriendlyPieces);
        }

        for (int i=0;i<numPieces[colorToFindMovesFor | Type.Rook];i++) {
            squareAttacksArray[pieceSquareList[colorToFindMovesFor | Type.Rook][i]] &= enemyPieces;
            generateRookMoves(pieceSquareList[colorToFindMovesFor | Type.Rook][i], notFriendlyPieces);
        }

        for (int i=0;i<numPieces[colorToFindMovesFor | Type.Queen];i++) {
            squareAttacksArray[pieceSquareList[colorToFindMovesFor | Type.Queen][i]] &= enemyPieces;
            generateQueenMoves(pieceSquareList[colorToFindMovesFor | Type.Queen][i], notFriendlyPieces);
        }

        for (int i=0;i<numPieces[colorToFindMovesFor | Type.King];i++) {
            squareAttacksArray[pieceSquareList[colorToFindMovesFor | Type.King][i]] &= enemyPieces;
            generateKingCapturesOnly(pieceSquareList[colorToFindMovesFor | Type.King][i]);
        }
    }
    public byte calculateGameState() {//TODO: add 50 move draw, draw by repitition, draw by insuffecient material
        if (indexOfFirstEmptyMove==0) {
            if (inCheck) {
                if (whiteToMove)return Type.whiteIsCheckmated;
                return Type.blackIsCheckmated;
            }
            return Type.gameIsADraw;
        }

        //only check white's number of pieces for the switch to the endgame, can assume piece count is similiar for black
        int pieceCount = numPieces[Type.Knight] + numPieces[Type.Bishop] + numPieces[Type.Rook] + numPieces[Type.Queen];
        //if there are 3 or fewer pieces for each color, then it is the endGame
        if (pieceCount > 3)return Type.midGame;
        return Type.endGame;
    }
    public void optimizeMoveOrder() {
        for (int i=0;i<indexOfFirstEmptyMove;i++) {
            legalMovePriorities[i] = getMovePriority(legalMoves[i]);
        }
        sortMoves();
    }

    private int getMovePriority(int legalMove) {//priority of 1 is an estimated 100 centipawn gain, - 100 cp loss, etc
        int movePriority = 0;

        int fromSquare = Move.getFromSquareFromMove(legalMove);
        int movingPiece = squareCentricPos[fromSquare]%8;//take mod 8 to make pieces Colorless
        int capturedPiece = Move.getCapturedPieceFromMove(legalMove)%8;
        long enemyPawnAttacks = whiteToMove ? blackAttacksArray[Type.Pawn] : whiteAttacksArray[Type.Pawn];

        if (capturedPiece != 0)movePriority = 2 * capturedPiece - movingPiece;//prioritize capturing low value with high value

        long toSquareBB = 1L<<Move.getToSquareFromMove(legalMove);
        if ((toSquareBB | enemyPawnAttacks) == enemyPawnAttacks)movePriority -= movingPiece;//devalue moving to enemy pawn attacking squares

        byte moveType = Move.getMoveTypeFromMove(legalMove);
        if (moveType == Type.pawnPromotesToQ) movePriority+=9;//prioritize pawn promoting to queen
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

    public long findCheckResolveRay(long kingLocation) {//TODO: MAKE FASTER
        long tickerSquareBB;
        long tempRAY=0;
        long occupiedSquareBitboard= allPieces;
        long relevantAttackers;
        short friendlyColor;
        short enemyColor;
        if (whiteToMove) {
            friendlyColor = Type.White;
            enemyColor = Type.Black;
            long pawnCheck = PieceAttack.generateWhitePawnAttacks(kingLocation) & PieceArray[enemyColor | Type.Pawn];
            if (pawnCheck !=0)return pawnCheck;
        }
        else {
            friendlyColor = Type.Black;
            enemyColor = Type.White;
            long pawnCheck = PieceAttack.generateBlackPawnAttacks(kingLocation) & PieceArray[enemyColor | Type.Pawn];
            if (pawnCheck !=0)return pawnCheck;
        }

        long knightCheck = PieceAttack.generateKnightAttacks(kingLocation) & PieceArray[enemyColor | Type.Knight];
        if (knightCheck !=0)return knightCheck;

        relevantAttackers= PieceArray[enemyColor | Type.Rook] | PieceArray[enemyColor | Type.Queen];
        tickerSquareBB=kingLocation;
        while ((tickerSquareBB | Constants.H_FILE) !=Constants.H_FILE) {
            tickerSquareBB=tickerSquareBB<<1;
            tempRAY|=tickerSquareBB;
            if ((tickerSquareBB & relevantAttackers) !=0)return tempRAY;
            if ((tickerSquareBB & occupiedSquareBitboard) !=0)break;
        }

        tempRAY=0;
        tickerSquareBB=kingLocation;
        while ((tickerSquareBB | Constants.RANK_8) !=Constants.RANK_8) {
            tickerSquareBB=tickerSquareBB<<8;
            tempRAY|=tickerSquareBB;
            if ((tickerSquareBB & relevantAttackers) !=0)return tempRAY;
            if ((tickerSquareBB & occupiedSquareBitboard) !=0)break;
        }

        tempRAY=0;
        tickerSquareBB=kingLocation;
        while ((tickerSquareBB | Constants.A_FILE) !=Constants.A_FILE) {
            tickerSquareBB=tickerSquareBB>>>1;
            tempRAY|=tickerSquareBB;
            if ((tickerSquareBB & relevantAttackers) !=0)return tempRAY;
            if ((tickerSquareBB & occupiedSquareBitboard) !=0)break;
        }

        tempRAY=0;
        tickerSquareBB=kingLocation;
        while ((tickerSquareBB | Constants.RANK_1) !=Constants.RANK_1) {
            tickerSquareBB=tickerSquareBB>>>8;
            tempRAY|=tickerSquareBB;
            if ((tickerSquareBB & relevantAttackers) !=0)return tempRAY;
            if ((tickerSquareBB & occupiedSquareBitboard) !=0)break;
        }

        relevantAttackers= PieceArray[enemyColor | Type.Bishop] | PieceArray[enemyColor | Type.Queen];
        tempRAY=0;
        tickerSquareBB=kingLocation;
        while ((tickerSquareBB | Constants.BISHOP_TR_EDGE) !=Constants.BISHOP_TR_EDGE) {
            tickerSquareBB=tickerSquareBB<<9;
            tempRAY|=tickerSquareBB;
            if ((tickerSquareBB & relevantAttackers) !=0)return tempRAY;
            if ((tickerSquareBB & occupiedSquareBitboard) !=0)break;
        }

        tempRAY=0;
        tickerSquareBB=kingLocation;
        while ((tickerSquareBB | Constants.BISHOP_TL_EDGE) !=Constants.BISHOP_TL_EDGE) {
            tickerSquareBB=tickerSquareBB<<7;
            tempRAY|=tickerSquareBB;
            if ((tickerSquareBB & relevantAttackers) !=0)return tempRAY;
            if ((tickerSquareBB & occupiedSquareBitboard) !=0)break;
        }

        tempRAY=0;
        tickerSquareBB=kingLocation;
        while ((tickerSquareBB | Constants.BISHOP_BR_EDGE) !=Constants.BISHOP_BR_EDGE) {
            tickerSquareBB=tickerSquareBB>>>7;
            tempRAY|=tickerSquareBB;
            if ((tickerSquareBB & relevantAttackers) !=0)return tempRAY;
            if ((tickerSquareBB & occupiedSquareBitboard) !=0)break;
        }

        tempRAY=0;
        tickerSquareBB=kingLocation;
        while ((tickerSquareBB | Constants.BISHOP_BL_EDGE) !=Constants.BISHOP_BL_EDGE) {
            tickerSquareBB=tickerSquareBB>>>9;
            tempRAY|=tickerSquareBB;
            if ((tickerSquareBB & relevantAttackers) !=0)return tempRAY;
            if ((tickerSquareBB & occupiedSquareBitboard) !=0)break;
        }

        tempRAY=0;
        return tempRAY;
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

    public static byte getPieceFromSquareWithBB (int square, long[] pa) {
        long squareBB=toBitboard(square);
        if ((squareBB|pa[1])==pa[1])return Type.White| Type.Pawn;
        else if ((squareBB|pa[2])==pa[2])return Type.White| Type.Knight;
        else if ((squareBB|pa[3])==pa[3])return Type.White| Type.Bishop;
        else if ((squareBB|pa[4])==pa[4])return Type.White| Type.Rook;
        else if ((squareBB|pa[5])==pa[5])return Type.White| Type.Queen;
        else if ((squareBB|pa[6])==pa[6])return Type.White| Type.King;
        else if ((squareBB|pa[9])==pa[9])return Type.Black| Type.Pawn;
        else if ((squareBB|pa[10])==pa[10])return Type.Black| Type.Knight;
        else if ((squareBB|pa[11])==pa[11])return Type.Black| Type.Bishop;
        else if ((squareBB|pa[12])==pa[12])return Type.Black| Type.Rook;
        else if ((squareBB|pa[13])==pa[13])return Type.Black| Type.Queen;
        else if ((squareBB|pa[14])==pa[14])return Type.Black| Type.King;
        else return Type.Empty;
    }

    private static short getPieceFromString(String str) {
        if (str.equals("P")) return Type.White | Type.Pawn;
        else if (str.equals("p")) return Type.Black | Type.Pawn;
        else if (str.equals("N")) return Type.White | Type.Knight;
        else if (str.equals("n")) return Type.Black | Type.Knight;
        else if (str.equals("B")) return Type.White | Type.Bishop;
        else if (str.equals("b")) return Type.Black | Type.Bishop;
        else if (str.equals("R")) return Type.White | Type.Rook;
        else if (str.equals("r")) return Type.Black | Type.Rook;
        else if (str.equals("Q")) return Type.White | Type.Queen;
        else if (str.equals("q")) return Type.Black | Type.Queen;
        else if (str.equals("K")) return Type.White | Type.King;
        else if (str.equals("k")) return Type.Black | Type.King;
        return -1;
    }

    public void generatePawnMoves(byte fromSquare) {//TODO: refactor this to make more optimal
        long[] possibleMoves = new long[8];
        if (whiteToMove) {
            long fromSquareBB= toBitboard(fromSquare);
            long attackingSquares= PieceAttack.lookUpWhitePawnAttacks(fromSquare);
            if (fromSquare/8==6){//on the seventh rank
                long tempToSquareBB= fromSquareBB<<8;

                tempToSquareBB &= emptySquares;
                tempToSquareBB |= attackingSquares & blackPieces;

                if ((pinnedPieces & fromSquareBB) == fromSquareBB && onPinRay(fromSquareBB)) tempToSquareBB &=pinRay[indexOfPin];//if on the pin ray
                tempToSquareBB &= checkResolveRay;

                possibleMoves[Type.pawnPromotesToQ]=tempToSquareBB;
                possibleMoves[Type.pawnPromotesToN]=tempToSquareBB;
                possibleMoves[Type.pawnPromotesToB]=tempToSquareBB;
                possibleMoves[Type.pawnPromotesToR]=tempToSquareBB;
            }
            else {//not on the seventh rank, white to move
                possibleMoves[Type.normalMove]|= fromSquareBB << 8 & emptySquares;
                possibleMoves[Type.doublePawnMove]|=possibleMoves[Type.normalMove]<<8 & emptySquares & Constants.RANK_4;
                possibleMoves[Type.normalMove]|= attackingSquares & blackPieces;

                if ((pinnedPieces & fromSquareBB) == fromSquareBB && onPinRay(fromSquareBB)) {
                    possibleMoves[Type.normalMove] &=pinRay[indexOfPin];
                    possibleMoves[Type.doublePawnMove] &=pinRay[indexOfPin];
                }

                possibleMoves[Type.normalMove] &= checkResolveRay;
                possibleMoves[Type.doublePawnMove] &= checkResolveRay;


                possibleMoves[Type.enPassant]= attackingSquares & (long)enPassantTargetFiles<<40;//en passant
                boolean enPassantCouldBePossible=possibleMoves[Type.enPassant] !=0;
                //check to see if enPassant could be possible before doing slow pin check
                if (enPassantCouldBePossible && enPassantIsPinned(fromSquareBB,possibleMoves[Type.enPassant])) possibleMoves[Type.enPassant] = 0;
            }
        }

        else {
            long fromSquareBB= toBitboard(fromSquare);
            long attackingSquares= PieceAttack.lookUpBlackPawnAttacks(fromSquare);
            if (fromSquare/8==1){//on the second rank, black to move
                long tempToSquareBB;
                tempToSquareBB= fromSquareBB>>>8;

                tempToSquareBB &= emptySquares;
                tempToSquareBB |= attackingSquares & whitePieces;

                if ((pinnedPieces & fromSquareBB) == fromSquareBB && onPinRay(fromSquareBB)) tempToSquareBB &=pinRay[indexOfPin];//if on the pin ray
                tempToSquareBB &= checkResolveRay;

                possibleMoves[Type.pawnPromotesToQ]=tempToSquareBB;
                possibleMoves[Type.pawnPromotesToN]=tempToSquareBB;
                possibleMoves[Type.pawnPromotesToB]=tempToSquareBB;
                possibleMoves[Type.pawnPromotesToR]=tempToSquareBB;
            }
            else {//not on the second rank, black to move
                possibleMoves[Type.normalMove]|= fromSquareBB >>> 8 & emptySquares;
                possibleMoves[Type.doublePawnMove]|=possibleMoves[Type.normalMove]>>>8& emptySquares & Constants.RANK_5;
                possibleMoves[Type.normalMove]|= attackingSquares & whitePieces;

                if ((pinnedPieces & fromSquareBB) == fromSquareBB && onPinRay(fromSquareBB)) {
                    possibleMoves[Type.normalMove] &=pinRay[indexOfPin];
                    possibleMoves[Type.doublePawnMove] &=pinRay[indexOfPin];
                }
                possibleMoves[Type.normalMove] &= checkResolveRay;
                possibleMoves[Type.doublePawnMove] &= checkResolveRay;

                possibleMoves[Type.enPassant]= attackingSquares & (long)enPassantTargetFiles<<16;//en passant
                boolean enPassantCouldBePossible=possibleMoves[Type.enPassant] !=0;
                //check to see if enPassant could be possible before doing slow pin check
                if (enPassantCouldBePossible && enPassantIsPinned(fromSquareBB,possibleMoves[Type.enPassant])) possibleMoves[Type.enPassant] = 0;
            }
        }

        addMovesToMoveList(Type.normalMove,fromSquare,possibleMoves[Type.normalMove]);
        addMovesToMoveList(Type.enPassant,fromSquare,possibleMoves[Type.enPassant]);
        addMovesToMoveList(Type.doublePawnMove,fromSquare,possibleMoves[Type.doublePawnMove]);
        addMovesToMoveList(Type.pawnPromotesToQ,fromSquare,possibleMoves[Type.pawnPromotesToQ]);
        addMovesToMoveList(Type.pawnPromotesToN,fromSquare,possibleMoves[Type.pawnPromotesToN]);
        addMovesToMoveList(Type.pawnPromotesToB,fromSquare,possibleMoves[Type.pawnPromotesToB]);
        addMovesToMoveList(Type.pawnPromotesToR,fromSquare,possibleMoves[Type.pawnPromotesToR]);
    }
    private void generatePawnCapturesOnly(byte fromSquare) {//TODO: refactor this to make more optimal
        long possibleMoves[] = new long[8];
        if (whiteToMove) {
            long fromSquareBB= toBitboard(fromSquare);
            long attackingSquares= PieceAttack.lookUpWhitePawnAttacks(fromSquare);

            if (fromSquare/8==6){//on the seventh rank
                long tempToSquareBB = attackingSquares & blackPieces;

                if ((pinnedPieces & fromSquareBB) == fromSquareBB && onPinRay(fromSquareBB)) tempToSquareBB &= pinRay[indexOfPin];//if on the pin ray
                tempToSquareBB &= checkResolveRay;

                possibleMoves[Type.pawnPromotesToQ]=tempToSquareBB;
                possibleMoves[Type.pawnPromotesToN]=tempToSquareBB;
                possibleMoves[Type.pawnPromotesToB]=tempToSquareBB;
                possibleMoves[Type.pawnPromotesToR]=tempToSquareBB;
            }
            else {//not on the seventh rank, white to move
                possibleMoves[Type.normalMove]|= attackingSquares & blackPieces;

                if ((pinnedPieces & fromSquareBB) == fromSquareBB && onPinRay(fromSquareBB)) {
                    possibleMoves[Type.normalMove] &=pinRay[indexOfPin];
                }

                possibleMoves[Type.normalMove] &= checkResolveRay;
                possibleMoves[Type.doublePawnMove] &= checkResolveRay;

                possibleMoves[Type.enPassant]= attackingSquares & (long)enPassantTargetFiles<<40;//en passant
                boolean enPassantCouldBePossible=possibleMoves[Type.enPassant] !=0;
                //check to see if enPassant could be possible before doing slow pin check
                if (enPassantCouldBePossible && enPassantIsPinned(fromSquareBB,possibleMoves[Type.enPassant])) possibleMoves[Type.enPassant] = 0;
            }
        }

        else {
            long fromSquareBB= toBitboard(fromSquare);
            long attackingSquares= PieceAttack.lookUpBlackPawnAttacks(fromSquare);

            if (fromSquare/8==1){//on the second rank, black to move
                long tempToSquareBB = attackingSquares & whitePieces;

                if ((pinnedPieces & fromSquareBB) == fromSquareBB && onPinRay(fromSquareBB)) tempToSquareBB &= pinRay[indexOfPin];//if on the pin ray
                tempToSquareBB &= checkResolveRay;

                possibleMoves[Type.pawnPromotesToQ]=tempToSquareBB;
                possibleMoves[Type.pawnPromotesToN]=tempToSquareBB;
                possibleMoves[Type.pawnPromotesToB]=tempToSquareBB;
                possibleMoves[Type.pawnPromotesToR]=tempToSquareBB;
            }
            else {//not on the second rank, black to move
                possibleMoves[Type.normalMove]|= attackingSquares & whitePieces;

                if ((pinnedPieces & fromSquareBB) == fromSquareBB && onPinRay(fromSquareBB)) {
                    possibleMoves[Type.normalMove] &=pinRay[indexOfPin];
                }

                possibleMoves[Type.enPassant]= attackingSquares & (long)enPassantTargetFiles<<16;//en passant
                boolean enPassantCouldBePossible=possibleMoves[Type.enPassant] !=0;
                //check to see if enPassant could be possible before doing slow pin check
                if (enPassantCouldBePossible && enPassantIsPinned(fromSquareBB,possibleMoves[Type.enPassant])) possibleMoves[Type.enPassant] = 0;
            }
        }
    }
    public void generateKnightMoves(byte fromSquare, long notFriendlyPiecesBB) {
        long possibleMoves = squareAttacksArray[fromSquare];

        possibleMoves&= notFriendlyPiecesBB & checkResolveRay;

        long squareBB = 1L<<fromSquare;
        if ((pinnedPieces & squareBB) == squareBB && onPinRay(squareBB))possibleMoves=0;
        addMovesToMoveList(Type.normalMove,fromSquare,possibleMoves);
    }
    public void generateBishopMoves(byte fromSquare, long notFriendlyPiecesBB) {
        long possibleMoves = squareAttacksArray[fromSquare];

        possibleMoves&= notFriendlyPiecesBB & checkResolveRay;

        long squareBB = 1L<<fromSquare;
        if ((pinnedPieces & squareBB) == squareBB && onPinRay(squareBB))possibleMoves &= pinRay[indexOfPin];
        addMovesToMoveList(Type.normalMove,fromSquare,possibleMoves);
    }
    public void generateRookMoves(byte fromSquare, long notFriendlyPiecesBB) {
        long possibleMoves = squareAttacksArray[fromSquare];

        possibleMoves&= notFriendlyPiecesBB & checkResolveRay;

        long squareBB = 1L<<fromSquare;
        if ((pinnedPieces & squareBB) == squareBB && onPinRay(squareBB))possibleMoves &=pinRay[indexOfPin];
        addMovesToMoveList(Type.normalMove,fromSquare,possibleMoves);
    }
    public void generateQueenMoves(byte fromSquare, long notFriendlyPiecesBB) {
        long possibleMoves = squareAttacksArray[fromSquare];
        possibleMoves&= notFriendlyPiecesBB & checkResolveRay;

        long squareBB = toBitboard(fromSquare);
        if ((pinnedPieces & squareBB) == squareBB && onPinRay(squareBB))possibleMoves &=pinRay[indexOfPin];
        addMovesToMoveList(Type.normalMove,fromSquare,possibleMoves);
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

        addMovesToMoveList(Type.normalMove,fromSquare,possibleMoves);
        addMovesToMoveList(Type.castles,fromSquare,possibleCastles);
    }
    public void generateKingCapturesOnly(byte fromSquare) {
        long possibleMoves = squareAttacksArray[fromSquare];

        if (whiteToMove) {
            possibleMoves&= ~(whitePieces | blackAttacks);//can't move into check
        }
        else {
            possibleMoves&= ~(blackPieces | whiteAttacks);//can't move into check
        }

        addMovesToMoveList(Type.normalMove, fromSquare, possibleMoves);
    }

    public boolean onPinRay(long squareBB) {
        for (indexOfPin=0;indexOfPin<8;indexOfPin++) {
            if ((pinRay[indexOfPin] & squareBB) == squareBB) return true;
        }
        return false;
    }//returns true if the square is on a pinRay and sets indexOfPin to which pin it is on

    private boolean enPassantIsPinned(long fromSquareBB, long toSquareBB) {
        long[] tempPA = PieceArray;
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
    private void quietlyEnPassant(byte fromSquare, byte toSquare) {//TODO: refactor and delete this
        long fromSquareBB = toBitboard(fromSquare);
        long toSquareBB = toBitboard(toSquare);
        byte movingPiece = squareCentricPos[fromSquare];

        int enPassantCapturedSquare= toSquare-8;
        short colorNotMoving= Type.Black;
        if (!whiteToMove){
            enPassantCapturedSquare= toSquare+8;
            colorNotMoving= Type.White;
        }

        squareCentricPos[fromSquare]= Type.Empty;//updates redundant squareCentricPosition
        squareCentricPos[toSquare]=movingPiece;//updates redundant squareCentricPosition
        squareCentricPos[enPassantCapturedSquare]= Type.Empty;

        PieceArray[movingPiece]^=fromSquareBB|toSquareBB;
        PieceArray[colorNotMoving | Type.Pawn]^=toBitboard(enPassantCapturedSquare);

        hundredHalfmoveTimer=0;
        whiteToMove= !whiteToMove;
        enPassantTargetFiles =0;
    }
    public void calculateSquareAttacksFromBitboards() {
        long tempPieceBB= PieceArray[Type.White | Type.Pawn];

        for (int i=0;i<64;i++) {
            squareAttacksArray[i]=0;
        }
        for (int i=0;i<6;i++) {
            whiteAttacksArray[i+1]=0;
            blackAttacksArray[i+1]=0;
        }

        long whiteBlockers = allPieces ^ PieceArray[Type.Black | Type.King];
        long blackBlockers = allPieces ^ PieceArray[Type.White | Type.King];


        while (tempPieceBB !=0) {
            int pieceSquare = Long.numberOfTrailingZeros(tempPieceBB);
            long pieceAttackBB = PieceAttack.lookUpWhitePawnAttacks(pieceSquare);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            whiteAttacksArray[Type.Pawn] |= pieceAttackBB;
            tempPieceBB ^= 1L<<pieceSquare;
        }

        tempPieceBB= PieceArray[Type.White | Type.Knight];
        while (tempPieceBB !=0) {
            int pieceSquare = Long.numberOfTrailingZeros(tempPieceBB);
            long pieceSquareBB = 1L<<pieceSquare;
            long pieceAttackBB = PieceAttack.lookUpKnightAttacks(pieceSquare);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            whiteAttacksArray[Type.Knight] |= pieceAttackBB;
            tempPieceBB ^= pieceSquareBB;
        }

        tempPieceBB= PieceArray[Type.White | Type.Bishop];
        while (tempPieceBB !=0) {
            int pieceSquare = Long.numberOfTrailingZeros(tempPieceBB);
            long pieceSquareBB = 1L<<pieceSquare;
            long pieceAttackBB = PieceAttack.lookUpBishopAttacks(pieceSquare,whiteBlockers);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            whiteAttacksArray[Type.Bishop] |= pieceAttackBB;
            tempPieceBB ^= 1L<<pieceSquare;
        }

        tempPieceBB=PieceArray[Type.White | Type.Rook];
        while (tempPieceBB !=0) {
            int pieceSquare = Long.numberOfTrailingZeros(tempPieceBB);
            long pieceAttackBB = PieceAttack.lookUpRookAttacks(pieceSquare,whiteBlockers);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            whiteAttacksArray[Type.Rook] |= pieceAttackBB;
            tempPieceBB ^= 1L<<pieceSquare;
        }

        tempPieceBB=PieceArray[Type.White | Type.Queen];
        while (tempPieceBB !=0) {
            int pieceSquare = Long.numberOfTrailingZeros(tempPieceBB);
            long pieceAttackBB = PieceAttack.lookUpQueenAttacks(pieceSquare,whiteBlockers);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            whiteAttacksArray[Type.Queen] |= pieceAttackBB;
            tempPieceBB ^= 1L<<pieceSquare;
        }

        tempPieceBB=PieceArray[Type.White | Type.King];
        int tempPS = Long.numberOfTrailingZeros(tempPieceBB);
        long tempPABB = PieceAttack.lookUpKingAttacks(tempPS);
        squareAttacksArray[tempPS]= tempPABB;
        whiteAttacksArray[Type.King] |= tempPABB;


        tempPieceBB= PieceArray[Type.Black | Type.Pawn];
        while (tempPieceBB !=0) {
            int pieceSquare = Long.numberOfTrailingZeros(tempPieceBB);
            long pieceAttackBB = PieceAttack.lookUpBlackPawnAttacks(pieceSquare);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            blackAttacksArray[Type.Pawn] |= pieceAttackBB;
            tempPieceBB ^= 1L<<pieceSquare;
        }

        tempPieceBB= PieceArray[Type.Black | Type.Knight];
        while (tempPieceBB !=0) {
            int pieceSquare = Long.numberOfTrailingZeros(tempPieceBB);
            long pieceSquareBB = 1L<<pieceSquare;
            long pieceAttackBB = PieceAttack.lookUpKnightAttacks(pieceSquare);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            blackAttacksArray[Type.Knight] |= pieceAttackBB;
            tempPieceBB ^= pieceSquareBB;
        }

        tempPieceBB= PieceArray[Type.Black | Type.Bishop];
        while (tempPieceBB !=0) {
            int pieceSquare = Long.numberOfTrailingZeros(tempPieceBB);
            long pieceAttackBB = PieceAttack.lookUpBishopAttacks(pieceSquare,blackBlockers);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            blackAttacksArray[Type.Bishop] |= pieceAttackBB;
            tempPieceBB ^= 1L<<pieceSquare;
        }

        tempPieceBB=PieceArray[Type.Black | Type.Rook];
        while (tempPieceBB !=0) {
            int pieceSquare = Long.numberOfTrailingZeros(tempPieceBB);
            long pieceAttackBB = PieceAttack.lookUpRookAttacks(pieceSquare,blackBlockers);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            blackAttacksArray[Type.Rook] |= pieceAttackBB;
            tempPieceBB ^= 1L<<pieceSquare;
        }

        tempPieceBB=PieceArray[Type.Black | Type.Queen];
        while (tempPieceBB !=0) {
            int pieceSquare = Long.numberOfTrailingZeros(tempPieceBB);
            long pieceAttackBB = PieceAttack.lookUpQueenAttacks(pieceSquare,blackBlockers);
            squareAttacksArray[pieceSquare]= pieceAttackBB;
            blackAttacksArray[Type.Queen] |= pieceAttackBB;
            tempPieceBB ^= 1L<<pieceSquare;
        }

        tempPieceBB=PieceArray[Type.Black | Type.King];
        tempPS = Long.numberOfTrailingZeros(tempPieceBB);
        tempPABB = PieceAttack.lookUpKingAttacks(tempPS);
        squareAttacksArray[tempPS]= tempPABB;
        blackAttacksArray[Type.King] |= tempPABB;

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
                    fen+=getPieceStringFromShort(squareCentricPos[tickerSquare]);
                }
                tickerSquare++;
            }

            if (squareCentricPos[tickerSquare] == 0)emptySquareCounter++;
            else {
                if (emptySquareCounter!=0){
                    fen+=emptySquareCounter;
                    emptySquareCounter=0;
                }
                fen+=getPieceStringFromShort(squareCentricPos[tickerSquare]);
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

        fen+=" "+hundredHalfmoveTimer+" ";
        fen+=0;//not keeping track of the move timer
        return fen;
    }
    private String getPieceStringFromShort(short piece) {//assume not empty
        switch (piece) {
            case Type.White | Type.Pawn -> {
                return "P";
            }
            case Type.White | Type.Knight -> {
                return "N";
            }
            case Type.White | Type.Bishop -> {
                return "B";
            }
            case Type.White | Type.Rook -> {
                return "R";
            }
            case Type.White | Type.Queen -> {
                return "Q";
            }
            case Type.White | Type.King -> {
                return "K";
            }
            case Type.Black | Type.Pawn -> {
                return "p";
            }
            case Type.Black | Type.Knight -> {
                return "n";
            }
            case Type.Black | Type.Bishop -> {
                return "b";
            }
            case Type.Black | Type.Rook -> {
                return "r";
            }
            case Type.Black | Type.Queen -> {
                return "q";
            }
            case Type.Black | Type.King -> {
                return "k";
            }
        }
        return "invalidInputError";
    }

    private static int[] cloneMoveArray(int[] input, int indexOfFirstEmptyMove) {
        int[] ret = new int[218];
        System.arraycopy(input, 0, ret, 0,indexOfFirstEmptyMove);
        return ret;
    }
    private static long toBitboard (int square){
        return 1L<<square;
    }


}
