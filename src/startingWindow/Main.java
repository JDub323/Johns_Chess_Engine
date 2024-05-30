package startingWindow;

import engine.MainEngine;

public class Main {

    private static boolean playerPlaysWhite = true, playerPlaysBlack = false;
    private static int engineWaitTimeIndex = 0;
    private static StartingFrame frame;

    private static final int[] engineWaitTimes = {500,750,1000,1250,1500,2000,4000,8000};

    public static void main(String[] args) {
        frame = new StartingFrame();
    }

    public static void flipPlayerPlaysWhite() {
        playerPlaysWhite = !playerPlaysWhite;
    }
    public static void flipPlayerPlaysBlack() {
        playerPlaysBlack = !playerPlaysBlack;
    }
    public static void incrementEngineWaitTime() {
        engineWaitTimeIndex++;
        if (engineWaitTimeIndex >= engineWaitTimes.length) engineWaitTimeIndex = 0;
    }
    public static void startGame() {
        MainEngine.startGame(playerPlaysWhite, playerPlaysBlack, getEngineWaitTime());
        frame.closePanel();
    }

    public static boolean isPlayerWhite() {
        return playerPlaysWhite;
    }

    public static boolean isPlayerBlack() {
        return playerPlaysBlack;
    }

    public static int getEngineWaitTime() {
        return engineWaitTimes[engineWaitTimeIndex];
    }
}
