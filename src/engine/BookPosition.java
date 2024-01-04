package engine;

import java.util.ArrayList;
import java.util.Comparator;

public class BookPosition {
    private final long hash;
    private ArrayList<Integer> moves;
    private ArrayList<Short> weights;

    public BookPosition(long hash, ArrayList<Integer> moves, ArrayList<Short> weights) {
        this.hash = hash;
        this.moves = moves;
        this.weights = weights;
    }

    public int getRandomMove() {
        int sum = 0;
        for (Short weight : weights) {
            sum += weight;
        }
        int decision = (int) (Math.random()*sum);

        for (int i=0; i<moves.size(); i++) {
            decision -= weights.get(i);
            if (decision <= 0) return moves.get(i);
        }

        throw new IllegalStateException();
    }

    public long getHash() {
        return hash;
    }

    public void addMove(int move) {
        if (moves.contains(move)) {
            int index = moves.indexOf(move);
            short newWeight = (short) (weights.get(index) + 1);
            weights.set(index, newWeight);
        }
        else {
            moves.add(move);
            weights.add((short)1);
        }
    }

    public int getSumOfWeights() {
        int ret = 0;
        for (Short weight : weights) {
            ret += weight;
        }
        return ret;
    }

    public static Comparator<BookPosition> bookPositionComparator = Comparator.comparingLong(BookPosition::getHash);


    public String toString() {
        String ret;
        ret = hash+" "+weights.toString()+"-"+moves.toString();
        return ret;
    }

}
