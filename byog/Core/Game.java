package byog.Core;
import byog.TileEngine.TERenderer;
import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;
import java.awt.Color;
import java.awt.Font;
//import java.io.File;
import java.io.IOException;
//import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Calendar;
import java.time.LocalDateTime;

public class Game {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;
    private int[] spriteLoc = new int[2];
    private int[][] filled;
    private int[][] locCoord = new int[30][2];
    private TETile[][] world;
    private static long SEED;
    private static Random RANDOM = new Random(SEED);
    private boolean gameOver = true;
    private int cXloc = 500;
    private int cYloc = 500;
    private String spriteImg = "C:\\Users\\TTQT\\cs61b\\sp18-brc\\proj2\\byog\\Core\\boyfront.png";
    //IMAGE SOURCE: http://untamed.wild-refuge.net/images/rpgxp/goodomens/brian.png
    private int hour;
    private int mCapStart = 0;
    private String date;
    private String name = "Guest";
    private String minute;
    private String amPm;
    private String mCap = "";
    private String description;
    private String  saveFile = "C:\\Users\\TTQT\\cs61b\\sp18-brc\\proj2\\byog\\Core\\gameState.txt";
    private Calendar currentTime;
    private boolean lastColon = false;

    public void generateWorld(long seed) {
        SEED = seed;
        this.RANDOM = new Random(SEED);
        drawFrame();
        ter.initialize(WIDTH, HEIGHT + 2);
        world = new TETile[WIDTH][HEIGHT];
        filled = new int[WIDTH][HEIGHT];
        gameOver = false;
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                world[x][y] = Tileset.NOTHING;
                filled[x][y] = 0;
            }
        }
        int rooms = RANDOM.nextInt(18) + 10;
        for (int n = 0; n < rooms; n++) {
            locCoord[n] = chooseRoomType();
        }
        cleanOutliers();
        for (int i = 0; i < 30 - 1 && locCoord[i + 1] != null; i++) {
            connector(locCoord[i], locCoord[i + 1]);
        }
        cleanEdges();
        generateWalls();
        ter.renderFrame(world);
    }

    private void playOne() {
        createSpriteLoc();
        while (!gameOver) {
            updateHUD();
        }
    }

    private void playTwo(String moves, int last) {
        createSpriteLoc();
        for (int i = last + 1; i < moves.length(); i++) {
            moveSprite(moves.charAt(i));
        }
        while (!gameOver) {
            updateHUD();
        }
    }

    private void connector(int[] a, int[] b) {
        if (a[1] < b[1]) {
            for (int w = a[1]; w < b[1]; w++) {
                world[a[0]][w] = Tileset.FLOOR;
                filled[a[0]][w] = 1;
            }
        } else {
            for (int w = a[1]; w > b[1]; w--) {
                world[a[0]][w] = Tileset.FLOOR;
                filled[a[0]][w] = 1;
            }
        }
        if (a[0] < b[0]) {
            for (int w = a[0]; w < b[0]; w++) {
                world[w][b[1]] = Tileset.FLOOR;
                filled[w][b[1]] = 1;
            }
        } else {
            for (int w = a[0]; w > b[0]; w--) {
                world[w][b[1]] = Tileset.FLOOR;
                filled[w][b[1]] = 1;
            }
        }
    }
    private int[] chooseRoomType() {
        int choice = RANDOM.nextInt(2);
        switch (choice) {
            case 0:
                return generateRoom();
            case 1:
                return generateRectRoom();
            default:
                return generateRoom();
        }
    }
    private int[] generateRoomLoc() {
        int[] loc = new int[2];
        loc[0] = RANDOM.nextInt(WIDTH - 2) + 1;
        loc[1] = RANDOM.nextInt(HEIGHT - 2) + 1;
        if (filled[loc[0]][loc[1]] == 0) {     // no overlap with existing rooms
            return loc;
        } else {
            return generateRoomLoc();
        }
    }
    private int[] generateRoom() {
        int[] loc = generateRoomLoc();
        for (int x = loc[0]; x < (loc[0] + RANDOM.nextInt(6) + 3) % (WIDTH - 1); x++) {
            if ((filled[x + 1][loc[1]] % 2) == 1) {
                break;
            }
            for (int y = loc[1]; y < (loc[1] + RANDOM.nextInt(6) + 3) % (HEIGHT - 1); y++) {
                if (filled[x][y + 1] == 0) {
                    world[x][y] = Tileset.FLOOR;
                    filled[x][y] = 1;
                } else {
                    break;
                }
            }
        }
        return loc;
    }
    private int[] generateRectRoom() {
        int[] loc = generateRoomLoc();
        int maxY = (loc[1] + RANDOM.nextInt(6) + 3) % (HEIGHT - 1);
        for (int x = loc[0]; x < (loc[0] + RANDOM.nextInt(6) + 3) % (WIDTH - 1); x++) {
            for (int y = loc[1]; y < maxY; y++) {
                world[x][y] = Tileset.FLOOR;
                filled[x][y] = 1;
            }
        }
        return loc;
    }
    private void cleanOutliers() {
        for (int x = 1; x < WIDTH - 1; x++) {
            for (int y = 1; y < HEIGHT - 1; y++) {
                if (filled[x][y] == 1 && filled[x - 1][y] == 0 && filled[x + 1][y] == 0
                        && filled[x][y - 1] == 0 && filled[x][y + 1] == 0) {
                    world[x][y] = Tileset.NOTHING;
                    filled[x][y] = 0;
                }
            }
        }
    }
    private void cleanEdges() {
        for (int x = 0; x < WIDTH; x = x + WIDTH - 1) {
            for (int y = 0; y < HEIGHT; y++) {
                if (filled[x][y] == 1) {
                    world[x][y] = Tileset.NOTHING;
                    filled[x][y] = 0;
                }
            }
        }
        for (int y = 0; y < HEIGHT; y = y + HEIGHT - 1) {
            for (int x = 0; x < WIDTH; x++) {
                if (filled[x][y] == 1) {
                    world[x][y] = Tileset.NOTHING;
                    filled[x][y] = 0;
                }
            }
        }
    }
    private void generateWalls() {
        for (int x = 1; x < WIDTH - 1; x++) {
            for (int y = 1; y < HEIGHT - 1; y++) {
                if (filled[x][y] == 1) {
                    for (int i = -1; i <= 1; i++) {
                        for (int j = -1; j <= 1; j++) {
                            if (filled[x + i][y + j] == 0) {
                                world[x + i][y + j] = Tileset.WALL;
                                filled[x + i][y + j] = 2;
                            }
                        }
                    }
                }
            }
        }
    }
    private void drawFrame() {
        StdDraw.clear();
        StdDraw.clear(Color.BLACK);
        Font font = new Font("Monaco", Font.BOLD, 5);
        StdDraw.setFont(font);
        StdDraw.enableDoubleBuffering();
        StdDraw.show();
    }
    private void updateHUD() {
        int newXloc = (int) (StdDraw.mouseX() / 1);
        int newYloc = (int) (StdDraw.mouseY() / 1);
        StdDraw.clear(Color.BLACK);
        ter.renderFrame(world);
        StdDraw.setPenColor(Color.white);
        int tileType = 0;
        if (newXloc >= 0 && newXloc < 80 && newYloc >= 0 && newYloc < 30) {
            tileType = filled[newXloc][newYloc];
        }
        description = "EMPTY. 'The void fills you with determination. "
                + "It also looks back.' - Some Old Guy";
        if (tileType == 1) {
            description = "SNOW. Cold, just like my heart. It catches me when I fall.";
        }
        if (tileType == 2) {
            description = "WALL. I didn't pay for it. They didn't pay for it either.";
        }
        if (tileType == 3) {
            description = "GRASS. It's always greener on the other side.";
        }
        StdDraw.textLeft(2, HEIGHT + 1, description);
        cXloc = newXloc;
        cYloc = newYloc;
        StdDraw.line(0, HEIGHT + 0.2, 80, HEIGHT + 0.2);
        StdDraw.text(2 * WIDTH / 3, HEIGHT + 1, "Player name: " + name);
        checkTime();
        StdDraw.textRight(76, HEIGHT + 1, date + "    " + hour + ":" + minute + " " + amPm);
        changeSpriteLocation();
        StdDraw.show();
        StdDraw.pause(100);
    }
    private void checkTime() {
        //@ https://stackoverflow.com/questions/20766090/how-to-read-time-continuously,
        // https://stackoverflow.com/questions/907170/java-getminutes-and-gethours
        currentTime = Calendar.getInstance();
        hour = LocalDateTime.now().getHour();
        minute = String.valueOf(LocalDateTime.now().getMinute());
        date = String.valueOf((currentTime.get(Calendar.MONTH) + 1) % 12) + "/"
                + String.valueOf(currentTime.get(Calendar.DATE)) + "/"
                + String.valueOf(currentTime.get(Calendar.YEAR));
        amPm = "PM";
        if (LocalDateTime.now().getMinute() < 10) {
            minute = "0" + String.valueOf(LocalDateTime.now().getMinute());
        }
        if (currentTime.get(Calendar.AM_PM) == 0) {
            amPm = "AM";
        }
    }
    private void createSpriteLoc() {
        spriteLoc[0] = RANDOM.nextInt(WIDTH - 2) + 5;
        spriteLoc[1] = RANDOM.nextInt(HEIGHT - 2) + 5;
        while (spriteLoc[0] < 0 || spriteLoc[0] >= WIDTH
                || spriteLoc[1] < 0 || spriteLoc[1] >= HEIGHT
                || filled[spriteLoc[0]][spriteLoc[1]] != 1) {
            spriteLoc[0] = RANDOM.nextInt(WIDTH - 2) + 5;
            spriteLoc[1] = RANDOM.nextInt(HEIGHT - 2) + 5;
        }
    }
    private void detectPlayerInput() {
        char key = StdDraw.nextKeyTyped();
        mCap += String.valueOf(key);
    }
    private void changeSpriteLocation() {
        while (StdDraw.hasNextKeyTyped()) {
            detectPlayerInput();
        }
        while (mCapStart != mCap.length()) {
            char holder = mCap.charAt(mCapStart);
            moveSprite(holder);
            mCapStart++;
        }
        StdDraw.picture(spriteLoc[0] + 0.5, spriteLoc[1] + 1.2, spriteImg);
    }

    private void moveSprite(char holder) {
        world[spriteLoc[0]][spriteLoc[1]] = Tileset.GRASS;
        filled[spriteLoc[0]][spriteLoc[1]] = 3;
        if (holder == 'w' || holder == 'W') {
            if ((filled[spriteLoc[0]][spriteLoc[1] + 1] % 2) == 1) {
                spriteLoc[1] = spriteLoc[1] + 1;
                spriteImg = "C:\\Users\\TTQT\\cs61b\\sp18-brc\\proj2\\byog\\Core\\boyback.png";
            }
        } else if (holder == 'a' || holder == 'A') {
            if ((filled[spriteLoc[0] - 1][spriteLoc[1]] % 2) == 1) {
                spriteLoc[0] = spriteLoc[0] - 1;
                spriteImg = "C:\\Users\\TTQT\\cs61b\\sp18-brc\\proj2\\byog\\Core\\boyleft.png";
            }
        } else if (holder == 's' || holder == 'S') {
            if ((filled[spriteLoc[0]][spriteLoc[1] - 1] % 2) == 1) {
                spriteLoc[1] = spriteLoc[1] - 1;
                spriteImg = "C:\\Users\\TTQT\\cs61b\\sp18-brc\\proj2\\byog\\Core\\boyfront.png";
            }
        } else if (holder == 'd' || holder == 'D') {
            if ((filled[spriteLoc[0] + 1][spriteLoc[1]] % 2) == 1) {
                spriteLoc[0] = spriteLoc[0] + 1;
                spriteImg = "C:\\Users\\TTQT\\cs61b\\sp18-brc\\proj2\\byog\\Core\\boyright.png";
            }
        } else if (holder == ':') {
            lastColon = true;
        } else if ((holder == 'q' || holder == 'Q') && lastColon) {
            saveGame();
            gameOver = true;
            System.exit(0);
        }
        if (holder != ':') {
            lastColon = false;
        }
    }

    private void saveGame() {
        try {
            String text = "N" + Long.toString(SEED) + "S" + name + "*" + mCap;
            Files.write(Paths.get(saveFile), text.getBytes());
        } catch (IOException e) {
            return;
        }
    }
    private void loadGame() {
        try {
            String gameState = new String(Files.readAllBytes(Paths.get(saveFile)));
            String getSeed = "";
            String getName = "";
            String getMovements = "";
            String looper;
            boolean newGame = false;
            boolean receiveSeed = false;
            boolean receiveName = false;
            for (int i = 0; i < gameState.length(); i++) {
                looper = Character.toString(gameState.charAt(i));
                if (!newGame && !receiveSeed
                        && !receiveName && looper.equals("N")) {
                    newGame = true;
                } else if (newGame && !receiveSeed
                        && !receiveName && looper.equals("S")) {
                    receiveSeed = true;
                } else if (newGame && receiveSeed
                        && !receiveName && looper.equals("*")) {
                    receiveName = true;
                } else if (newGame && !receiveSeed
                        && !receiveName) {
                    getSeed += looper;

                } else if (newGame && receiveSeed
                        && !receiveName) {
                    getName += looper;
                } else {
                    getMovements += looper;
                }
            }
            SEED = Long.parseLong(getSeed);
            name = getName;
            mCap = getMovements;
            String temp = "";
            for (int i = 0; i < mCap.length(); i++) {
                if (mCap.charAt(i) != ':') {
                    temp += mCap.charAt(i);
                }
            }
            mCap = temp;
            mCapStart = 0;
            generateWorld(SEED);
            playOne();
        } catch (Exception e) {
            System.exit(0);
        }
    }
    /**
     * Method used for playing 3a fresh game. The game should start from the main menu.
     */
    public void playWithKeyboard() {
        StdDraw.setCanvasSize();
        StdDraw.clear(Color.BLACK);
        StdDraw.setXscale(0, 512);
        StdDraw.setYscale(0, 512);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(256, 350, "ICE MELTS");
        Font subtitle = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(subtitle);
        StdDraw.text(256, 310, "A Game by Julie Tran & Kevin Lin");
        StdDraw.text(256, 230, "New Game (N)");
        StdDraw.text(256, 210, "Load Game (L)");
        StdDraw.text(256, 190, "Set Username (U)");
        StdDraw.text(256, 170, "Quit (Q)");
        StdDraw.show();
        String keystrokes = "";
        char key = '?';
        while (key != 'n' && key != 'N') {
            if (StdDraw.hasNextKeyTyped()) {
                key = StdDraw.nextKeyTyped();
                if (key == 'q' || key == 'Q') {
                    System.exit(0);
                }
                if (key == 'l' || key == 'L') {
                    loadGame();
                    playOne();
                    return;
                }
                if (key == 'u' || key == 'U') {
                    StdDraw.text(256, 90, "Enter a username and press '*' when finished.");
                    StdDraw.show();
                    name = "";
                    int x = 45;
                    while (key != '*') {
                        if (StdDraw.hasNextKeyTyped()) {
                            key = StdDraw.nextKeyTyped();
                            if (key != '*') {
                                name += String.valueOf(key);
                                StdDraw.text(x, 70, String.valueOf(key));
                                x = x + 14;
                                StdDraw.show();
                            }
                        }
                    }
                }
            }
        }
        StdDraw.text(256, 50, "Enter a seed and press 'S' when finished.");
        StdDraw.show();
        int x = 65;
        while (key != 's' && key != 'S') {
            if (StdDraw.hasNextKeyTyped()) {
                key = StdDraw.nextKeyTyped();
                if (key != 's' && key != 'S') {
                    keystrokes += String.valueOf(key);
                    StdDraw.text(x, 30, String.valueOf(key));
                    x = x + 14;
                    StdDraw.show();
                }
            }
        }
        generateWorld(Long.parseLong(keystrokes));
        playOne();
    }
    /**
     * Method used for autograding and testing the game code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The game should
     * behave exactly as if the user typed these characters into the game after playing
     * playWithKeyboard. If the string ends in ":q", the same world should be returned as if the
     * string did not end with q. For example "n123sss" and "n123sss:q" should return the same
     * world. However, the behavior is slightly different. After playing with "n123sss:q", the game
     * should save, and thus if we then called playWithInputString with the string "l", we'd expect
     * to get the exact same world back again, since this corresponds to loading the saved game.
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] playWithInputString(String input) {
        // Fill out this method to run the game using the input passed in,
        // and return a 2D tile representation of the world that would have been
        // drawn if the same inputs had been given to playWithKeyboard().
        String seed = "";
        int last = 0;
        if (input.charAt(0) == 'n' || input.charAt(0) == 'N') {
            for (int i = 1; i < input.length(); i++) {
                if (input.charAt(i) == 's' || input.charAt(i) == 'S') {
                    last = i;
                    break;
                }
                seed += input.charAt(i);
            }
            generateWorld(Long.parseLong(seed));
            //playTwo(input, last);
        } else if (input.charAt(0) == 'l' || input.charAt(0) == 'L') {
            loadGame();
            //playTwo(input, last);
        }

        createSpriteLoc();
        for (int i = last + 1; i < input.length(); i++) {
            if (!gameOver) {
                moveSprite(input.charAt(i));
            }
        }

        TETile[][] finalWorldFrame = world;
        return finalWorldFrame;
    }
}
