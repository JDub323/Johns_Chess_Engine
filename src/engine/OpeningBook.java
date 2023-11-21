package engine;

public class OpeningBook {

    public record BookPosition(long hash, int move1, int move2, int move3, int move4, int move5) {}

    public static BookPosition[] openingBook = {
            new BookPosition(1,0,0,0,0,0),
    };





}
