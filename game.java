import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;
import java.util.ArrayList;

public class game extends JPanel implements ActionListener, KeyListener {
    class Block {
        int x, y, width, height;
        Image image;
        int startX, startY;
        char direction = 'U';
        int velocityX = 0, velocityY = 0;
        int stepsSinceLastTurn = 0;
        final int turnCooldown = 10;

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        void updateDirection(char direction) {
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;
            for (Block wall : walls) {
                if (collision(this, wall)) {
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                }
            }
        }

        void updateVelocity() {
            switch (this.direction) {
                case 'U': velocityX = 0; velocityY = -tileSize / 4; break;
                case 'D': velocityX = 0; velocityY = tileSize / 4; break;
                case 'L': velocityX = -tileSize / 4; velocityY = 0; break;
                case 'R': velocityX = tileSize / 4; velocityY = 0; break;
            }
        }

        void reset() {
            this.x = this.startX;
            this.y = this.startY;
            velocityX = 0;
            velocityY = 0;
            direction = 'U';
            stepsSinceLastTurn = 0;
        }
    }

    private int rowCount = 21, columnCount = 19, tileSize = 32;
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    private Image wallImage, blueGhostImage, orangeGhostImage, pinkGhostImage, redGhostImage;
    private Image pacmanUpImage, pacmanDownImage, pacmanLeftImage, pacmanRightImage;

    private String[][] tileMaps = {
        {   // Level 1
         "XXXXXXXXXXXXXXXXXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X                 X",
        "X XX X XXXXX X XX X",
        "X    X       X    X",
        "XXXX XXXX XXXX XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXrXX X XXXX",
        "X       bpo       X",
        "XXXX X XXXXX X XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXXXX X XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "XX X X XXXXX X X XX",
        "X    X   X   X    X",
        "X XXXXXX X XXXXXX X",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX"
    },
        {   // Level 2
            "XXXXXXXXXXXXXXXXXXX",
            "X   o     X     p X",
            "X XX XXbX X XrX XX",
            "X    X        X   X",
            "X XXXX XXXXX XX XXX",
            "X    X    P    X  X",
            "XX XX XXXXXX XX XX",
            "XOOX X   X   X XOOX",
            "XX X XX XXX XX X XX",
            "X                 X",
            "XX X XXXXXXXX X XXX",
            "XOOX     X     XOOX",
            "XX XX XXXXXXX XX XX",
            "X       X         X",
            "X XX XXX X XXX XX X",
            "X X     X X     X X",
            "XX X XXXXX X XXX XX",
            "X    X   X   X    X",
            "X XXXXXX X XXXXXX X",
            "X                 X",
            "XXXXXXXXXXXXXXXXXXX"
        },
        {   // Level 3
            "XXXXXXXXXXXXXXXXXXX",
            "X    r     b      X",
            "X XXX XXXXXXXX XX X",
            "X                 X",
            "XX XXXX XXX XXXX XX",
            "X    o     P    p X",
            "XX XXXX XXX XXXX XX",
            "X                 X",
            "X XXXXX XX XX XXXXX",
            "X                 X",
            "X XXXXX XX XX XXXXX",
            "X                 X",
            "XX XXXX XXX XXXX XX",
            "X    X        X   X",
            "X XX XXX XX XXX XX",
            "X X             X X",
            "XX XXXX XXX XXXX XX",
            "X                 X",
            "X XXXXX XXXXXXX XXX",
            "X                 X",
            "XXXXXXXXXXXXXXXXXXX"
        }
    };

    private int currentLevel = 0;

    HashSet<Block> walls = new HashSet<>();
    HashSet<Block> foods = new HashSet<>();
    HashSet<Block> ghosts = new HashSet<>();
    Block pacman;

    Timer gameLoop;
    char[] directions = {'U', 'D', 'L', 'R'};
    Random random = new Random();
    int score = 0, lives = 3;
    boolean gameOver = false;

    game() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(new Color(10, 10, 20)); // Dark background
        addKeyListener(this);
        setFocusable(true);

        wallImage = new ImageIcon(getClass().getResource("wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("redGhost.png")).getImage();
        pacmanUpImage = new ImageIcon(getClass().getResource("pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("pacmanRight.png")).getImage();

        loadMap();
        for (Block ghost : ghosts) {
            ghost.updateDirection(directions[random.nextInt(4)]);
        }
        gameLoop = new Timer(50, this);
        gameLoop.start();
    }

    public void loadMap() {
        walls.clear();
        foods.clear();
        ghosts.clear();
        pacman = null;

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                char ch = tileMaps[currentLevel][r].charAt(c);
                int x = c * tileSize;
                int y = r * tileSize;

                switch (ch) {
                    case 'X': walls.add(new Block(wallImage, x, y, tileSize, tileSize)); break;
                    case 'b': ghosts.add(new Block(blueGhostImage, x, y, tileSize, tileSize)); break;
                    case 'o': ghosts.add(new Block(orangeGhostImage, x, y, tileSize, tileSize)); break;
                    case 'p': ghosts.add(new Block(pinkGhostImage, x, y, tileSize, tileSize)); break;
                    case 'r': ghosts.add(new Block(redGhostImage, x, y, tileSize, tileSize)); break;
                    case 'P': pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize); break;
                    case ' ': foods.add(new Block(null, x + 14, y + 14, 4, 4)); break;
                }
            }
        }
        if (pacman != null) {
            pacman.velocityX = 0;
            pacman.velocityY = 0;
            pacman.direction = 'U';
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.setColor(new Color(30, 10, 50, 80));
        g.fillRect(0, 0, boardWidth, boardHeight);

        for (Block wall : walls)
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);

        g.setColor(new Color(0, 255, 200));
        for (Block food : foods)
            g.fillOval(food.x, food.y, food.width, food.height);

        for (Block ghost : ghosts)
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);

        if (pacman != null)
            g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.setColor(new Color(255, 255, 120));
        g.drawString("❤️ x " + lives + "    ⭐ Score: " + score, tileSize, tileSize - 6);
        g.setColor(new Color(0, 255, 100));
        g.drawString("Level: " + (currentLevel + 1), tileSize * 13, tileSize - 6);

        if (gameOver) {
            g.setFont(new Font("SansSerif", Font.BOLD, 28));
            g.setColor(Color.RED);
            g.drawString("💀 Game Over! Final Score: " + score, tileSize, tileSize * 10);
        }
    }

    public void move() {
        if (pacman == null) return;

        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        for (Block ghost : ghosts) {
            if (collision(ghost, pacman)) {
                lives--;
                if (lives == 0) {
                    gameOver = true;
                    return;
                }
                resetPositions();
            }

            ghost.stepsSinceLastTurn++;

            boolean atTileCenter = isCenteredOnTile(ghost);
            boolean shouldTurn = (ghost.stepsSinceLastTurn >= ghost.turnCooldown);

            if (atTileCenter && shouldTurn) {
                if (random.nextBoolean()) {
                    ghost.stepsSinceLastTurn = 0;
                } else {
                    char[] possibleDirs = getValidDirections(ghost);
                    if (possibleDirs.length > 0) {
                        char backward = getOppositeDirection(ghost.direction);
                        ArrayList<Character> filtered = new ArrayList<>();
                        for (char d : possibleDirs) {
                            if (d != backward) filtered.add(d);
                        }
                        if (filtered.isEmpty()) filtered = new ArrayList<>();
                        for (char d : possibleDirs) {
                            if (!filtered.contains(d)) filtered.add(d);
                        }
                        char newDir = filtered.get(random.nextInt(filtered.size()));
                        ghost.updateDirection(newDir);
                        ghost.stepsSinceLastTurn = 0;
                    }
                }
            }

            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;

            boolean hitWall = false;
            for (Block wall : walls) {
                if (collision(ghost, wall) || ghost.x < 0 || ghost.x + ghost.width > boardWidth ||
                    ghost.y < 0 || ghost.y + ghost.height > boardHeight) {
                    hitWall = true;
                    break;
                }
            }
            if (hitWall) {
                ghost.x -= ghost.velocityX;
                ghost.y -= ghost.velocityY;
                char[] possibleDirs = getValidDirections(ghost);
                if (possibleDirs.length > 0) {
                    ghost.updateDirection(possibleDirs[random.nextInt(possibleDirs.length)]);
                    ghost.stepsSinceLastTurn = 0;
                }
            }
        }

        Block eaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                eaten = food;
                score += 10;
                break;
            }
        }
        if (eaten != null)
            foods.remove(eaten);

        if (foods.isEmpty()) {
            currentLevel++;
            if (currentLevel >= tileMaps.length) {
                gameOver = true;
                return;
            }
            loadMap();
            resetPositions();
        }
    }

    public boolean collision(Block a, Block b) {
        return a.x < b.x + b.width && a.x + a.width > b.x &&
               a.y < b.y + b.height && a.y + a.height > b.y;
    }

    public void resetPositions() {
        if (pacman != null) {
            pacman.reset();
            pacman.velocityX = 0;
            pacman.velocityY = 0;
        }
        for (Block ghost : ghosts) {
            ghost.reset();
            ghost.updateDirection(directions[random.nextInt(4)]);
        }
    }

    boolean isCenteredOnTile(Block ghost) {
        return (ghost.x % tileSize == 0) && (ghost.y % tileSize == 0);
    }

    char[] getValidDirections(Block ghost) {
        ArrayList<Character> validDirs = new ArrayList<>();

        char[] allDirs = {'U', 'D', 'L', 'R'};
        for (char dir : allDirs) {
            ghost.direction = dir;
            ghost.updateVelocity();

            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;

            boolean blocked = false;
            for (Block wall : walls) {
                if (collision(ghost, wall)) {
                    blocked = true;
                    break;
                }
            }
            ghost.x -= ghost.velocityX;
            ghost.y -= ghost.velocityY;

            if (!blocked)
                validDirs.add(dir);
        }

        char[] result = new char[validDirs.size()];
        for (int i = 0; i < validDirs.size(); i++)
            result[i] = validDirs.get(i);

        return result;
    }

    char getOppositeDirection(char dir) {
        switch (dir) {
            case 'U': return 'D';
            case 'D': return 'U';
            case 'L': return 'R';
            case 'R': return 'L';
        }
        return 'U'; // default fallback
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver)
            gameLoop.stop();
    }

    @Override
    public void keyTyped(KeyEvent e) { }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver) {
            currentLevel = 0;
            loadMap();
            resetPositions();
            lives = 3;
            score = 0;
            gameOver = false;
            gameLoop.start();
        }

        int code = e.getKeyCode();
        if (pacman != null) {
            if (code == KeyEvent.VK_UP) pacman.updateDirection('U');
            else if (code == KeyEvent.VK_DOWN) pacman.updateDirection('D');
            else if (code == KeyEvent.VK_LEFT) pacman.updateDirection('L');
            else if (code == KeyEvent.VK_RIGHT) pacman.updateDirection('R');

            switch (pacman.direction) {
                case 'U': pacman.image = pacmanUpImage; break;
                case 'D': pacman.image = pacmanDownImage; break;
                case 'L': pacman.image = pacmanLeftImage; break;
                case 'R': pacman.image = pacmanRightImage; break;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) { }
}
