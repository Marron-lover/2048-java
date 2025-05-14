package TwentyFortyEight;
import java.util.*;

import processing.core.PApplet;
import processing.core.PImage;
import processing.event.MouseEvent;

public class App extends PApplet {

    public static final int CELLSIZE = 100;
    public static int GRID_SIZE = 4; // Default grid size
    public static int WIDTH = GRID_SIZE * CELLSIZE;
    public static int HEIGHT = GRID_SIZE * CELLSIZE;
    public static final int FPS = 60; // Frames per second

    private long startTime;
    private boolean gameOver = false;
    private ArrayList<Animation> animations = new ArrayList<>(); // Animations for tile movements
    private boolean animating = false;
    private int[][] pendingBoard; // Store the state of the board before the move
    private Cell[][] board;
    private HashMap<String, PImage> sprites = new HashMap<>(); // Images for tiles
    public static final Random random = new Random();

    @Override
    public void settings() {
        size(WIDTH, HEIGHT); // Set the size of the window based on grid size
    }

    public Cell[][] getBoard() {
        return board;
    }
    
    // Load images
    public PImage getSprite(String name) {
        if (!sprites.containsKey(name)) {
            PImage img = loadImage(name + ".png");
            if (img == null) {
                System.err.println("Failed to load image: " + name + ".png");
            }
            sprites.put(name, img);
            }
            return sprites.get(name);
    }


    @Override
    // Load resources and initialize the game board
    public void setup() {
        frameRate(FPS);

        String[] baseTiles = { "tile", "tile_hover" };
        for (String name : baseTiles) getSprite(name);
        for (int val = 2; val <= 2048; val *= 2) getSprite("tile_" + val);

        // Initialize the board with Cell objects
        board = new Cell[GRID_SIZE][GRID_SIZE];
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                board[y][x] = new Cell(x, y);
            }
        }

        // Generate new numbers (2 or 4)
        generateNewNumber();
        generateNewNumber();
        // Start time counter
        startTime = System.currentTimeMillis();
        gameOver = false;
    }

    // Generate a new number (2 or 4) in a random empty cell
    public void generateNewNumber() {
        ArrayList<Cell> emptyCells = new ArrayList<>();
        // Find all empty cells
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                if (board[y][x].getValue() == 0) {
                    emptyCells.add(board[y][x]);
                }
            }
        }

        // If there are empty cells, randomly select one and set its value to 2 or 4
        if (!emptyCells.isEmpty()) {
            Cell randomCell = emptyCells.get(random.nextInt(emptyCells.size()));
            // Generate 2 or 4 with equal probability: 50% each
            int value = random.nextBoolean() ? 2 : 4;
            randomCell.setValue(value);
        }
    }

    // Main draw loop is called continuously by Processing
    @Override
    public void draw() {
        background(230);

        // Draw the grid
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                board[y][x].draw(this);
            }
        }

        // Draw the animations when animating
        if (animating) {
            // Use an iterator to remove completed animations
            Iterator<Animation> iterator = animations.iterator();
            while (iterator.hasNext()) {
                Animation animation = iterator.next();
                // Animate is done
                if (animation.update()) {
                    iterator.remove();
                // Continue animating
                }
                else{
                    animation.draw(this);
                }
            }
            // If no animations are left, generate a new number and check for game over
            if (animations.isEmpty()) {
                animating = false;
                applyPendingMoves();
                generateNewNumber();
                if (isGameOver()) {
                    gameOver = true;
                }
            }
        }

        // Show the timer on the top right corner when the game is not over
        if (!gameOver) {
            long time = (System.currentTimeMillis() - startTime) / 1000; // Convert milliseconds to seconds
            fill(0);
            textSize(18);
            textAlign(RIGHT, TOP);
            text("Time: " + time + "s", WIDTH - 10, 10);
        }

        // Show game over message
        if (gameOver) {
            fill(0);
            textSize(50);
            textAlign(CENTER, CENTER);
            text("Game Over", WIDTH / 2, HEIGHT / 2);
        }
    }

    // Create a copy of the current board to store pending moves
    // This allows us to animate the moves before applying them to the actual board
    private void initPendingBoard() {
        pendingBoard = new int[GRID_SIZE][GRID_SIZE];
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                pendingBoard[y][x] = board[y][x].getValue();
            }
        }
    }

    // Apply the pending moves to the actual board
    // This is called after all animations are done
    private void applyPendingMoves() {
        if (pendingBoard != null) {
            for (int y = 0; y < GRID_SIZE; y++) {
                for (int x = 0; x < GRID_SIZE; x++) {
                    board[y][x].setValue(pendingBoard[y][x]);
                }
            }
            // Clear the pending board for the next move
            pendingBoard = null;
        }
    }

    // Mouse hover effect
    @Override
    public void mouseMoved() {
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                Cell c = board[y][x];
                boolean hovering =
                    mouseX >= c.getPixelX() &&
                    mouseX < c.getPixelX() + CELLSIZE &&
                    mouseY >= c.getPixelY() &&
                    mouseY < c.getPixelY() + CELLSIZE;
                c.setHovered(hovering);
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                Cell c = board[y][x];
                if (
                    mouseX >= c.getPixelX() &&
                    mouseX < c.getPixelX() + CELLSIZE &&
                    mouseY >= c.getPixelY() &&
                    mouseY < c.getPixelY() + CELLSIZE &&
                    c.getValue() == 0
                ) {
                    int value = random.nextBoolean() ? 2 : 4;
                    c.setValue(value);
                    return;
                }
            }
        }
    }

    @Override
    public void keyPressed() {
        // Check if the game is over and the key pressed is 'r' to restart
        if (animating) return;
        if (gameOver && key != 'r') return;

        boolean moved = false;

        // Handle 4 movements and one restart action 'r'
        switch (keyCode) {
            case UP: moved = moveUp(); break;
            case DOWN: moved = moveDown(); break;
            case LEFT: moved = moveLeft(); break;
            case RIGHT: moved = moveRight(); break;
            default:
                if (key == 'r') {
                    restartGame();
                    return;
                }
        }

        // Start the animation if any move was made
        if (moved) {
            animating = true;
        }
    }

    private void restartGame() {
        // Empty the game board
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                board[y][x].setValue(0);
            }
        }
        animations.clear();
        animating = false;

        // Generate two new numbers to start the game
        generateNewNumber();
        generateNewNumber();
        gameOver = false;
        startTime = System.currentTimeMillis();
        // Continuously call draw until the program is closed
        loop();
    }

    // Move tiles in the specified direction and merge them if they are the same
    private boolean moveUp() { // Iterate from top to bottom
        boolean moved = false;
        animations.clear(); // Clear previous animations

        initPendingBoard();

        // Iterate through each column
        for (int x = 0; x < GRID_SIZE; x++) {
            // Initialize targetY to the top of the column
            int targetY = 0;
            
            int lastValue = 0;

            // Iterate through each row in the column
            for (int y = 0; y < GRID_SIZE; y++) {
                if (pendingBoard[y][x] != 0) {
                    int value = pendingBoard[y][x];
                    // Calculate the starting position of the tile
                    // Use the cell size to determine the pixel position
                    float startX = x * CELLSIZE;
                    float startY = y * CELLSIZE;

                    // Check if the last value is the same as the current value
                    // If so, merge them and double the value
                    if (lastValue != 0 && lastValue == value) {
                        pendingBoard[targetY - 1][x] *= 2;
                        pendingBoard[y][x] = 0; 
                        animations.add(new Animation(startX, startY, startX, (targetY - 1) * CELLSIZE, value));
                        moved = true;
                        lastValue = 0;
                    }
                    // If the targetY is not the same as the current y, move the tile to the targetY
                    else if (targetY != y) {
                        pendingBoard[targetY][x] = value;
                        pendingBoard[y][x] = 0;
                        animations.add(new Animation(startX, startY, startX, targetY * CELLSIZE, value));
                        moved = true;
                        lastValue = value;
                        targetY++; // Move the targetY down
                    }
                    // If the targetY is the same as the current y, just update the lastValue
                    // This means the tile is already in the correct position
                    else {
                        lastValue = value;
                        targetY++;
                    }
                }
            }
        }

        return moved;
    }

    private boolean moveDown() {
        boolean moved = false;
        animations.clear();

        initPendingBoard();
        for (int x = 0; x < GRID_SIZE; x++) {
            int targetY = GRID_SIZE - 1;
            int lastValue = 0;

            for (int y = GRID_SIZE - 1; y >= 0; y--) {
                if (pendingBoard[y][x] != 0) {
                    int value = pendingBoard[y][x];
                    float startX = x * CELLSIZE;
                    float startY = y * CELLSIZE;

                    if (lastValue != 0 && lastValue == value) {
                        pendingBoard[targetY + 1][x] *= 2;
                        pendingBoard[y][x] = 0;
                        animations.add(new Animation(startX, startY, startX, (targetY + 1) * CELLSIZE, value));
                        moved = true;
                        lastValue = 0;
                    }
                    else if (targetY != y) {
                        pendingBoard[targetY][x] = value;
                        pendingBoard[y][x] = 0;
                        animations.add(new Animation(startX, startY, startX, targetY * CELLSIZE, value));
                        moved = true;
                        lastValue = value;
                        targetY--;
                    }
                    else {
                        lastValue = value;
                        targetY--;
                    }
                }
            }
        }
        return moved;
    }

    private boolean moveLeft() {
        boolean moved = false;
        animations.clear();

        initPendingBoard();

        for (int y = 0; y < GRID_SIZE; y++) {
            int targetX = 0;
            int lastValue = 0;

            for (int x = 0; x < GRID_SIZE; x++) {
                if (pendingBoard[y][x] != 0) {
                    int value = pendingBoard[y][x];
                    float startX = x * CELLSIZE;
                    float startY = y * CELLSIZE;

                    if (lastValue != 0 && lastValue == value) {
                        pendingBoard[y][targetX - 1] *= 2;
                        pendingBoard[y][x] = 0;
                        animations.add(new Animation(startX, startY, (targetX - 1) * CELLSIZE, startY, value));
                        moved = true;
                        lastValue = 0;
                    }
                    else if (targetX != x) {
                        pendingBoard[y][targetX] = value;
                        pendingBoard[y][x] = 0;
                        animations.add(new Animation(startX, startY, targetX * CELLSIZE, startY, value));
                        moved = true;
                        lastValue = value;
                        targetX++;
                    }
                    else {
                        lastValue = value;
                        targetX++;
                    }
                }
            }
        }
        return moved;
    }

    private boolean moveRight() {
        boolean moved = false;
        animations.clear();

        initPendingBoard();

        for (int y = 0; y < GRID_SIZE; y++) {
            int targetX = GRID_SIZE - 1;
            int lastValue = 0;

            for (int x = GRID_SIZE - 1; x >= 0; x--) {
                if (pendingBoard[y][x] != 0) {
                    int value = pendingBoard[y][x];
                    float startX = x * CELLSIZE;
                    float startY = y * CELLSIZE;

                    if (lastValue != 0 && lastValue == value) {
                        pendingBoard[y][targetX + 1] *= 2;
                        pendingBoard[y][x] = 0;
                        animations.add(new Animation(startX, startY, (targetX + 1) * CELLSIZE, startY, value));
                        moved = true;
                        lastValue = 0;
                    }
                    else if (targetX != x) {
                        pendingBoard[y][targetX] = value;
                        pendingBoard[y][x] = 0;
                        animations.add(new Animation(startX, startY, targetX * CELLSIZE, startY, value));
                        moved = true;
                        lastValue = value;
                        targetX--;
                    }
                    else {
                        lastValue = value;
                        targetX--;
                    }
                }
            }
        }

        return moved;
    }

    private boolean isGameOver() {
        // Check if there are any empty cells left
        for (int y = 0; y < GRID_SIZE; y ++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                if (board[y][x].getValue() == 0) {
                    return false;
                }
            }
        }

        // Check if there are any adjacent cells with the same value
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x ++) {
                int value = board[y][x].getValue();
                // Check right
                if (x < GRID_SIZE - 1 && board[y][x + 1].getValue() == value) {
                    return false;
                }
                // Check down
                if (y < GRID_SIZE - 1 && board[y + 1][x].getValue() == value) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void main(String[] args) {
        int gridSize = 4;
        // Check if a grid size is provided as an argument
        if (args.length > 0) {
            try {
                gridSize = Integer.parseInt(args[0]);
                if (gridSize < 2) {
                    System.out.println("Grid size must be at least 2. Using default size 4.");
                    gridSize = 4;
                }
            }
            catch (NumberFormatException e) {
                System.out.println("Invalid grid size provided. Using default size 4.");
            }
        }

        // Set the grid size and window 
        App.GRID_SIZE = gridSize;
        App.WIDTH = gridSize * CELLSIZE;
        App.HEIGHT = gridSize * CELLSIZE;

        // Start game
        PApplet.main("TwentyFortyEight.App");
    }
}

