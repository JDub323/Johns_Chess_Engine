package startingWindow;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;

import static chessUtilities.Util.PROJECT_PATH;

public class StartingFrame extends JFrame implements ActionListener {

    private final JButton playerPlaysWhiteButton;
    private final JButton playerPlaysBlackButton;
    private final JButton engineDifficultyButton;
    private final JButton startButton;
    private Image chessBotLogo = null;
    private Image humanLogo = null;
    private Image greenCheck = null;
    private Image redX = null;
    private final ImageObserver observer;

    public StartingFrame() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(null);
        this.setSize(500,500);
        this.setVisible(true);
        this.setLocationRelativeTo(null);

        playerPlaysWhiteButton = new JButton();
        playerPlaysBlackButton = new JButton();
        engineDifficultyButton = new JButton();
        startButton = new JButton("Start Game!");
        playerPlaysWhiteButton.setBounds(40,40,40,40);
        playerPlaysBlackButton.setBounds(40,100,40,40);
        engineDifficultyButton.setBounds(40,160,40,40);
        startButton.setBounds(195,400,100,40);
        playerPlaysWhiteButton.addActionListener(this);
        playerPlaysBlackButton.addActionListener(this);
        engineDifficultyButton.addActionListener(this);
        startButton.addActionListener(this);

        this.add(playerPlaysWhiteButton);
        this.add(playerPlaysBlackButton);
        this.add(engineDifficultyButton);
        this.add(startButton);

        try {
            chessBotLogo = ImageIO.read(new File
                    (PROJECT_PATH + "/src/startingWindow/Chess_AI_Logo.png"));
            humanLogo = ImageIO.read(new File
                    (PROJECT_PATH + "/src/startingWindow/HumanChessLogo.png"));
            greenCheck = ImageIO.read(new File
                    (PROJECT_PATH + "/src/startingWindow/green-check-mark-png.png"));
            redX = ImageIO.read(new File
                    (PROJECT_PATH + "/src/startingWindow/red-X-png.png"));
        } catch (IOException ignored) {

        }
        observer = (img, infoFlags, x, y, width, height) -> false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == playerPlaysWhiteButton) {
            Main.flipPlayerPlaysWhite();
            repaint();
        }
        else if (e.getSource() == playerPlaysBlackButton) {
            Main.flipPlayerPlaysBlack();
            repaint();
        }
        else if (e.getSource() == engineDifficultyButton) {
            Main.incrementEngineWaitTime();
            repaint();
        }
        else if (e.getSource() == startButton) {
            Main.startGame();
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        g.setColor(Color.BLACK);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
        g.drawString("Play As White?",100,95);
        g.drawString("Play As Black?",100,155);
        g.drawString("Increase Engine Thinking Time", 100, 215);
        g.drawString("White:",60,300);
        g.drawString("Black:",350,300);

        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 40));
        g.drawString("VS",230,370);


        int CHESS_AI_LOGO_WIDTH = 150;
        int CHESS_AI_LOGO_HEIGHT = 220;
        int HUMAN_LOGO_WIDTH = 150;
        int HUMAN_LOGO_HEIGHT = 170;

        //gives the constructor enough time to load images
        if (redX == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if (Main.isPlayerWhite()) {
            g.drawImage(humanLogo,20,330, HUMAN_LOGO_WIDTH, HUMAN_LOGO_HEIGHT,observer);
            g.drawImage(greenCheck,280,60,50,50,observer);
        }
        else {
            g.drawImage(chessBotLogo,20,330, CHESS_AI_LOGO_WIDTH, CHESS_AI_LOGO_HEIGHT, observer);
            g.drawImage(redX,280,60,50,50,observer);
        }
        if (Main.isPlayerBlack()) {
            g.drawImage(humanLogo,330,330, HUMAN_LOGO_WIDTH, HUMAN_LOGO_HEIGHT,observer);
            g.drawImage(greenCheck,280,120,50,50,observer);
        }
        else {
            g.drawImage(chessBotLogo,320,330, CHESS_AI_LOGO_WIDTH, CHESS_AI_LOGO_HEIGHT, observer);
            g.drawImage(redX,280,120,50,50,observer);
        }


        g.setColor(Color.GRAY);
        g.fillRect(50,250,Main.getEngineWaitTime()/10,20);
    }

    public void closePanel() {
        this.dispose();
    }
}
