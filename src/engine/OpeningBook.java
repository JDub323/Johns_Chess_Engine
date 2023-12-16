package engine;

import move.Move;
import position.Position;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class OpeningBook {

    public static final int OPENING_BOOK_LENGTH = 14;

    public static void initializeBookLists() {
        Scanner fileScanner;
        try {
            fileScanner = new Scanner(new File("C:\\Users\\jwhal\\Johns_Chess_Engine\\src\\engine\\White's_Move_Opening_Database"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        writeToArrayList(fileScanner, whiteOpeningBook);

        try {
            fileScanner = new Scanner(new File("C:\\Users\\jwhal\\Johns_Chess_Engine\\src\\engine\\Black's_Move_Opening_Database"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        writeToArrayList(fileScanner, blackOpeningBook);
    }
    private static void writeToArrayList(Scanner fileScanner, ArrayList<BookPosition> blackOpeningBook) {
        while (fileScanner.hasNext()) {
            long hash = Long.parseLong(fileScanner.next());
            String listThings = fileScanner.nextLine();
            ArrayList<Short> weights = new ArrayList<>();
            ArrayList<Integer> moves = new ArrayList<>();

            int indexBetweenLists = listThings.indexOf("-");
            String weightsListString = listThings.substring(0,indexBetweenLists);
            String movesListString = listThings.substring(indexBetweenLists+1);

            Scanner listScanner = new Scanner(weightsListString);
            listScanner.useDelimiter("[\\s\\[\\],]+");

            while (listScanner.hasNext()) {
                short weight = Short.parseShort(listScanner.next());
                weights.add(weight);
            }

            listScanner = new Scanner(movesListString);
            listScanner.useDelimiter("[\\s\\[\\],]+");

            while (listScanner.hasNext()) {
                int move = Integer.parseInt(listScanner.next());
                moves.add(move);
            }

            blackOpeningBook.add(new BookPosition(hash, moves, weights));
        }
    }

    public static ArrayList<BookPosition> whiteOpeningBook = new ArrayList<>();
    public static ArrayList<BookPosition> blackOpeningBook = new ArrayList<>();
    public static ArrayList<Long> whiteOpeningBookKeys = new ArrayList<>();
    public static ArrayList<Long> blackOpeningBookKeys = new ArrayList<>();


    public static void makeBlackAndWhiteOpeningBooks() throws FileNotFoundException {
        File whiteWinGames = new File("C:\\Users\\jwhal\\Johns_Chess_Engine\\src\\engine\\clean-lichess_white_wins_2500.pgn");
        File blackWinGames = new File("C:\\Users\\jwhal\\Johns_Chess_Engine\\src\\engine\\clean-lichess_black_wins_2500.pgn");
        makeAllBookPositions(whiteWinGames, whiteOpeningBook, whiteOpeningBookKeys, true);
        makeAllBookPositions(blackWinGames, blackOpeningBook, blackOpeningBookKeys, false);
    }
    private static void makeAllBookPositions(File f, ArrayList<BookPosition> book, ArrayList<Long> keys, boolean addingWhiteMoves) throws FileNotFoundException {
        Position pos;
        Scanner s = new Scanner(f);
        int gameNum = 0;
        while (s.hasNext()) {//for each position in the file
            pos = new Position("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
            addGameToBook(s, OPENING_BOOK_LENGTH, pos, book, keys, gameNum, addingWhiteMoves);
            gameNum++;
        }
    }

    public static void addGameToBook(Scanner s, int maxPlyAdding, Position pos, ArrayList<BookPosition> book, ArrayList<Long> keys,
                                     int gameNum, boolean addingWhiteMoves) {
        bringScannerToNextGame(s);
        if (!s.hasNext())return;
        for (int i=0; i<maxPlyAdding; i++) {
            String fancyMove = s.next();
            if (fancyMove.contains(".")) {//the "move" is actually the move number in pgn; skip
                i--;
                continue;
            }
            if (fancyMove.equals("1-0") || fancyMove.equals("0-1")) {//the game has ended, giving those symbols instead of a move
                break;
            }
            pos.findLegalMoves();

            int moveToAdd = Move.getMoveFromFancyMove(fancyMove, pos);
            assert moveToAdd != -1 : "No move found for move: "+fancyMove+" in game "+gameNum;

            if (keys.contains(pos.zobristKey)){
                int index = keys.indexOf(pos.zobristKey);
                book.get(index).addMove(moveToAdd);
            }
            else if (pos.whiteToMove == addingWhiteMoves) {
                ArrayList<Integer> integers = new ArrayList<>();
                integers.add(moveToAdd);
                ArrayList<Short> shorts = new ArrayList<>();
                shorts.add((short) 1);
                book.add(new BookPosition(pos.zobristKey, integers, shorts));
                keys.add(pos.zobristKey);
            }
            pos.makeMove(moveToAdd);
        }
    }

    private static void bringScannerToNextGame(Scanner s) {
        while (s.hasNext()) {
            String str = s.nextLine();
            if (str.length() <= 2)continue;//avoid StringOutOfBoundsException when the string is too short

            if (str.startsWith("[BlackRatingDiff")){//the last line before the moves begin
                s.nextLine();//skips the empty line in between
                break;//end the scanner in the correct position
            }
        }
    }

    public static void addAllBookPositionsToDatabases() throws IOException {

        //pruneBook(whiteOpeningBook);
        //pruneBook(blackOpeningBook);

        whiteOpeningBook.sort(BookPosition.bookPositionComparator);
        blackOpeningBook.sort(BookPosition.bookPositionComparator);

        try (FileWriter writer = new FileWriter("White's_Move_Opening_Database")) {
            for (BookPosition bookPosition : whiteOpeningBook) {
                writer.append(String.valueOf(bookPosition)).append("\n");
            }
        }

        try (FileWriter writer = new FileWriter("Black's_Move_Opening_Database")) {
            for (BookPosition bookPosition : blackOpeningBook) {
                writer.append(String.valueOf(bookPosition)).append("\n");
            }
        }

        System.out.println("Done!");
    }

    private static void pruneBook(ArrayList<BookPosition> book) {//not called in my final book
        int minOccurrences = 2;

        for (int i=0; i<book.size(); i++) {
            if (book.get(i).getSumOfWeights() < minOccurrences) {
                book.remove(i);
                i--;
            }
        }
    }


}
