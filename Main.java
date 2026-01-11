import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        int rowCount = 21, columnCount = 19, tileSize = 32;
        int boardWidth = columnCount * tileSize;
        int boardHeight = rowCount * tileSize;

        JFrame frame = new JFrame("PacMan "); 
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);//center
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//close if press x

        game pacmanGame = new game();
        frame.add(pacmanGame);
        frame.pack();
        pacmanGame.requestFocus();
        frame.setVisible(true);
    }
}