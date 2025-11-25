package FPT; // The folder containing all the other files in the project

import javax.swing.*;

// Import statements
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.io.*;
import java.net.URI;

public class Scrabble extends JFrame {
    private static final int BOARD_SIZE = 15; // 15 x 15 board
    private static final int RACK_SIZE = 7; // Number of tiles each player gets
    private static final int TILE_SIZE = 120; // 1.5x larger

    private JPanel greenPanel, boardPanel, playerPanel; // Background panel, board and buttons
    private JLabel[][] boardLabels = new JLabel[BOARD_SIZE][BOARD_SIZE];
    private RoundedButton[] playerRack = new RoundedButton[RACK_SIZE];
    private JLabel scoreLabel1, scoreLabel2, scoreLabel3, scoreLabel4, turnLabel, roundLabel, allScoresLabel, letterBagLabel;
    private RoundedButton checkButton, submitButton, resetButton, optionsButton; // Buttons on the player panel

    private ArrayList<Character> letterBag = new ArrayList<>(); // Array containing all the letters
    private List<RoundedButton> usedRackTiles = new ArrayList<>(); // Array containing the tiles already used
    private Set<String> dictionary = new HashSet<>(); // Dictionary (words.txt) to check if the word is valid
    private Map<Character, Integer> letterValues = new HashMap<>(); // How many points each letter gets you
    private Set<Point> tempValidSpots = new HashSet<>(); // Temporarily stores the available squares on the board
    private Set<Point> occupied = new HashSet<>(); // Stores the squares that already have tiles on them
    private List<Point> currentTilesPlaced = new ArrayList<>();
    private Map<Integer, List<Character>> playerRacks = new HashMap<>();

    private boolean[][] validSpots = new boolean[BOARD_SIZE][BOARD_SIZE]; // Keeps track of which squares are available
    private boolean firstMove = true;
    private char selectedTile = ' ';
    private int currentPlayer = 1; // Starts with player 1
    private int[] scores = new int[4]; // Keeps track of all the players' scores
    private Point firstMoveCenter = new Point(7, 7);
    private int numPlayers = 2; // Default number of players
    private int round = 1; // Starting round
    private Random random = new Random();

    // Types of bonuses available
    private enum Bonus {
        NONE,
        DOUBLE_LETTER,
        TRIPLE_LETTER,
        DOUBLE_WORD,
        TRIPLE_WORD
    }

    private Bonus[][] bonusGrid = new Bonus[15][15];

    // Receives the number of players from the menu
    public Scrabble(int playerCount) {
        this.numPlayers = playerCount;
        commonInit();
    }

    // Default starting config
    public Scrabble() {
        this(2);
    }

    // All of the initializations to begin a new game
    private void commonInit() {
        letterBag.clear();
        initializeLetterBag();
        setupLetterValues();
        loadDictionary();
        initializeValidSpots();
        setupUI();
        initializePlayerRacks();
        loadRackForCurrentPlayer();
        initializeBonusGrid();
    }

    // Adds the right quanitity of each letter to the "bag"
    private void initializeLetterBag() {
        char[] letters = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','*'};
        int[] counts = {9,2,2,4,12,2,3,2,9,1,1,4,2,6,8,2,1,6,4,6,4,2,2,1,2,1,2}; // According to the official rules of scrabble

        for (int i = 0; i < letters.length; i++) { // for loop to add the right quantity efficiently
            for (int j = 0; j < counts[i]; j++) {
                letterBag.add(letters[i]);
            }
        }
        Collections.shuffle(letterBag); // Shuffles the letters in the bag
    }

    // Assigns the correct number of points to each letter (according to the official scrabble rules)
    private void setupLetterValues() {
        letterValues.put('A', 1);
        letterValues.put('B', 3);
        letterValues.put('C', 3);
        letterValues.put('D', 2);
        letterValues.put('E', 1);
        letterValues.put('F', 4);
        letterValues.put('G', 2);
        letterValues.put('H', 4);
        letterValues.put('I', 1);
        letterValues.put('J', 8);
        letterValues.put('K', 5);
        letterValues.put('L', 1);
        letterValues.put('M', 3);
        letterValues.put('N', 1);
        letterValues.put('O', 1);
        letterValues.put('P', 3);
        letterValues.put('Q', 10);
        letterValues.put('R', 1);
        letterValues.put('S', 1);
        letterValues.put('T', 1);
        letterValues.put('U', 1);
        letterValues.put('V', 4);
        letterValues.put('W', 4);
        letterValues.put('X', 8);
        letterValues.put('Y', 4);
        letterValues.put('Z', 10);
        letterValues.put('*', 0);
    }

    // Fills the dictionary array using the words.txt file
    private void loadDictionary() {
        try (BufferedReader br = new BufferedReader(new FileReader("FPT/words.txt"))) { // uses BufferedReader (just like in class)
            String line;
            while ((line = br.readLine()) != null) {
                dictionary.add(line.trim().toUpperCase()); // trims and turns to uppercase for easier detection
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Only the middle square is available when the game begins
    private void initializeValidSpots() {
        validSpots[7][7] = true;
    }

    // Handles the UI with all the buttons and labels
    private void setupUI() {
        setTitle("Scrabble");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1600, 927); // Accomodates a top bar (27 px) along with a 900 x 900 board
        setResizable(false);
        setLocationRelativeTo(null);
        
        JLayeredPane mainPanel = new JLayeredPane();
        mainPanel.setLayout(null); // Allows for easy customization of the panel layout

        greenPanel = new JPanel() { // Solid dark green background (scrabble vibes)
            Image bg = new ImageIcon("background.png").getImage(); // Literally just a solid color image
            @Override
            protected void paintComponent(Graphics g) { // Paint component used for easy resizing
                super.paintComponent(g);
                g.drawImage(bg, 0, 0, 1600, 900, this);
            }
        };
        greenPanel.setBounds(0, 0, 1600, 900);

        boardPanel = new JPanel() { // Actual board to be used for the game
            Image bg = new ImageIcon("board.png").getImage(); // Image of a board I found online (watermark removed - took forever)
            @Override
            protected void paintComponent(Graphics g) { // Paint component used for easy resizing
                super.paintComponent(g);
                g.drawImage(bg, 0, 0, 900, 900, this);
            }
        };
        boardPanel.setBounds(0, 0, 900, 900);
        boardPanel.setLayout(new GridLayout(15, 15)); // Divides board up into squares
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                JLabel label = new JLabel("", SwingConstants.CENTER);
                label.setOpaque(false);
                label.addMouseListener(new BoardClickListener(row, col));
                boardLabels[row][col] = label;
                boardPanel.add(label);
            }
        }
        mainPanel.add(boardPanel);
        
        playerPanel = new JPanel(); // Panel containing all the buttons and labels
        playerPanel.setBounds(952, 300, 600, 600);
        playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.Y_AXIS));
        playerPanel.setOpaque(false);
        
        scoreLabel1 = new JLabel("Player 1 Score: 0"); // Score label for Player 1
        scoreLabel1.setFont(new Font("Segoe Script", Font.BOLD, 24));
        scoreLabel1.setForeground(Color.WHITE);
        scoreLabel1.setBounds(917, 120, 250, 60);

        scoreLabel2 = new JLabel("Player 2 Score: 0"); // Score label for Player 2
        scoreLabel2.setFont(new Font("Segoe Script", Font.BOLD, 24));
        scoreLabel2.setForeground(Color.WHITE);
        scoreLabel2.setBounds(917, 150, 250, 60);

        scoreLabel3 = new JLabel("Player 3 Score: 0"); // Score label for Player 3
        scoreLabel3.setFont(new Font("Segoe Script", Font.BOLD, 24));
        scoreLabel3.setForeground(Color.WHITE);
        scoreLabel3.setBounds(1365, 120, 250, 60);

        scoreLabel4 = new JLabel("Player 4 Score: 0"); // Score label for Player 4
        scoreLabel4.setFont(new Font("Segoe Script", Font.BOLD, 24));
        scoreLabel4.setForeground(Color.WHITE);
        scoreLabel4.setBounds(1365, 150, 250, 60);

        mainPanel.add(scoreLabel1); // Adds label for Player 1
        mainPanel.add(scoreLabel2); // Adds label for Player 2

        if (numPlayers > 2) {
            mainPanel.add(scoreLabel3); // Only adds Player 3 label if there is a player 3
            if (numPlayers > 3) {
                mainPanel.add(scoreLabel4);  // Only adds Player 4 label if there is a player 4
            }
        }
        
        JPanel rackPanel = new JPanel(); // Panel for all the tiles in the rack
        rackPanel.setOpaque(false);
        for (int i = 0; i < RACK_SIZE; i++) {
            RoundedButton tile = new RoundedButton(""); // Each tile gets its own button
            tile.setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));
            int index = i;
            tile.addActionListener(e -> {
                if (!tile.getText().isEmpty()) {
                    selectedTile = tile.getText().charAt(0);
                    highlightValidSpots(); // Highlights which spots the player can place tiles on
                }
            });
            playerRack[i] = tile;
            rackPanel.add(tile);
        }
        playerPanel.add(rackPanel);
        
        checkButton = createControlButton("Check Word", false, e -> handleCheck()); // Button to check if the word is valid (using the dictionary)
        checkButton.setBounds(1160, 600, 170, 60);
        submitButton = createControlButton("Submit", false, e -> handleSubmit()); // Button to submit the word and get points for it, as well as proceed to the next player's turn
        submitButton.setBounds(1340, 600, 130, 60);
        resetButton = createControlButton("Reset", false, e -> resetPlacedTiles()); // Button to put all tiles back on the rack and start the word again
        resetButton.setBounds(1035, 600, 115, 60);
        
        final JPopupMenu optionsMenu = new JPopupMenu();
        JMenuItem helpItem = new JMenuItem("Help"); // Opens up a scrabble help page online
        JMenuItem restartItem = new JMenuItem("Restart"); // Restarts the game
        JMenuItem menuItem = new JMenuItem("Menu"); // Goes back to the menu
        JMenuItem quitItem = new JMenuItem("Quit"); // Quits the game
        optionsMenu.add(helpItem);
        optionsMenu.add(restartItem);
        optionsMenu.add(menuItem);
        optionsMenu.add(quitItem);
        
        optionsButton = createControlButton("Options", true, e -> { // Creates an 'options' dropdown menu
            optionsMenu.show(optionsButton, 0, optionsButton.getHeight());
        });
        optionsButton.setBounds(1445, 825, 140, 60);


        helpItem.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URI("https://www.scrabblepages.com/scrabble/rules/")); // Link to the scrabble help page
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Could not open help page."); // Error Message
            }
        });

        restartItem.addActionListener(e -> {
            new Scrabble(numPlayers).setVisible(true); // Starts a brand new game
            dispose(); // Closes the current instance
        });

        menuItem.addActionListener(e -> {
            Menu x = new Menu(); // Goes back to the menu
            x.setVisible(true);
            dispose(); // Closes the current instance
        });

        quitItem.addActionListener(e -> System.exit(0)); // Closes the program
        
        mainPanel.add(checkButton);
        mainPanel.add(submitButton);
        mainPanel.add(resetButton);
        mainPanel.add(optionsButton);
        
        turnLabel = new JLabel("Player 1's Turn", SwingConstants.CENTER); // Indicates whose turn it is
        turnLabel.setBounds(860, 8, 300, 40);
        turnLabel.setFont(new Font("Segoe Script", Font.BOLD, 24));
        turnLabel.setForeground(Color.WHITE);
        mainPanel.add(turnLabel);
        
        roundLabel = new JLabel("Round 1", SwingConstants.CENTER); // Indicates what round it is
        roundLabel.setBounds(1380, 8, 300, 40);
        roundLabel.setFont(new Font("Segoe Script", Font.BOLD, 24));
        roundLabel.setForeground(Color.WHITE);
        mainPanel.add(roundLabel);

        letterBagLabel = new JLabel("Letter Bag: " + (letterBag.size() - (7 * numPlayers)), SwingConstants.CENTER); // Indicates how many letters are left in the bag
        letterBagLabel.setBounds(860, 852, 300, 40);
        letterBagLabel.setFont(new Font("Segoe Script", Font.BOLD, 24));
        letterBagLabel.setForeground(Color.WHITE);
        mainPanel.add(letterBagLabel);
        
        mainPanel.add(playerPanel);
        setContentPane(mainPanel);
        mainPanel.add(greenPanel); // Order of these additions make sure the green is behind the board and panel
    }

    // Method to assign bonus values to certain squares (according to the official rules)
    private void initializeBonusGrid() {
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                bonusGrid[i][j] = Bonus.NONE; // Default
            }
        }
        bonusGrid[0][0] = Bonus.TRIPLE_WORD;
        bonusGrid[0][3] = Bonus.DOUBLE_LETTER;
        bonusGrid[0][7] = Bonus.TRIPLE_WORD;
        bonusGrid[0][11] = Bonus.DOUBLE_LETTER;
        bonusGrid[0][14] = Bonus.TRIPLE_WORD;

        bonusGrid[1][1] = Bonus.DOUBLE_WORD;
        bonusGrid[1][5] = Bonus.TRIPLE_LETTER;
        bonusGrid[1][9] = Bonus.TRIPLE_LETTER;
        bonusGrid[1][13] = Bonus.DOUBLE_WORD;

        bonusGrid[2][2] = Bonus.DOUBLE_WORD;
        bonusGrid[2][6] = Bonus.DOUBLE_LETTER;
        bonusGrid[2][8] = Bonus.DOUBLE_LETTER;
        bonusGrid[2][12] = Bonus.DOUBLE_WORD;

        bonusGrid[3][0] = Bonus.DOUBLE_LETTER;
        bonusGrid[3][3] = Bonus.DOUBLE_WORD;
        bonusGrid[3][7] = Bonus.DOUBLE_LETTER;
        bonusGrid[3][11] = Bonus.DOUBLE_WORD;
        bonusGrid[3][14] = Bonus.DOUBLE_LETTER;

        bonusGrid[4][4] = Bonus.DOUBLE_WORD;
        bonusGrid[4][10] = Bonus.DOUBLE_WORD;

        bonusGrid[5][1] = Bonus.TRIPLE_LETTER;
        bonusGrid[5][5] = Bonus.TRIPLE_LETTER;
        bonusGrid[5][9] = Bonus.TRIPLE_LETTER;
        bonusGrid[5][13] = Bonus.TRIPLE_LETTER;

        bonusGrid[6][2] = Bonus.DOUBLE_LETTER;
        bonusGrid[6][6] = Bonus.DOUBLE_LETTER;
        bonusGrid[6][8] = Bonus.DOUBLE_LETTER;
        bonusGrid[6][12] = Bonus.DOUBLE_LETTER;

        bonusGrid[7][0] = Bonus.TRIPLE_WORD;
        bonusGrid[7][3] = Bonus.DOUBLE_LETTER;
        bonusGrid[7][11] = Bonus.DOUBLE_LETTER;
        bonusGrid[7][14] = Bonus.TRIPLE_WORD;

        bonusGrid[8][2] = Bonus.DOUBLE_LETTER;
        bonusGrid[8][6] = Bonus.DOUBLE_LETTER;
        bonusGrid[8][8] = Bonus.DOUBLE_LETTER;
        bonusGrid[8][12] = Bonus.DOUBLE_LETTER;

        bonusGrid[9][1] = Bonus.TRIPLE_LETTER;
        bonusGrid[9][5] = Bonus.TRIPLE_LETTER;
        bonusGrid[9][9] = Bonus.TRIPLE_LETTER;
        bonusGrid[9][13] = Bonus.TRIPLE_LETTER;

        bonusGrid[10][4] = Bonus.DOUBLE_WORD;
        bonusGrid[10][10] = Bonus.DOUBLE_WORD;

        bonusGrid[11][0] = Bonus.DOUBLE_LETTER;
        bonusGrid[11][3] = Bonus.DOUBLE_WORD;
        bonusGrid[11][7] = Bonus.DOUBLE_LETTER;
        bonusGrid[11][11] = Bonus.DOUBLE_WORD;
        bonusGrid[11][14] = Bonus.DOUBLE_LETTER;

        bonusGrid[12][2] = Bonus.DOUBLE_WORD;
        bonusGrid[12][6] = Bonus.DOUBLE_LETTER;
        bonusGrid[12][8] = Bonus.DOUBLE_LETTER;
        bonusGrid[12][12] = Bonus.DOUBLE_WORD;

        bonusGrid[13][1] = Bonus.DOUBLE_WORD;
        bonusGrid[13][5] = Bonus.TRIPLE_LETTER;
        bonusGrid[13][9] = Bonus.TRIPLE_LETTER;
        bonusGrid[13][13] = Bonus.DOUBLE_WORD;

        bonusGrid[14][0] = Bonus.TRIPLE_WORD;
        bonusGrid[14][3] = Bonus.DOUBLE_LETTER;
        bonusGrid[14][7] = Bonus.TRIPLE_WORD;
        bonusGrid[14][11] = Bonus.DOUBLE_LETTER;
        bonusGrid[14][14] = Bonus.TRIPLE_WORD;
    }

    // Method to set up each player's rack of letters
    private void initializePlayerRacks() {
        for (int i = 1; i <= numPlayers; i++) {
            List<Character> rack = new ArrayList<>();
            while (rack.size() < RACK_SIZE && !letterBag.isEmpty()) {
                rack.add(letterBag.remove(0));
            }
            playerRacks.put(i, rack);
        }
    }

    // Adds the actual images as icons for each tile
    private void loadRackForCurrentPlayer() {
        clearRack();
        List<Character> rack = playerRacks.get(currentPlayer);
        for (int i = 0; i < RACK_SIZE; i++) {
            if (i < rack.size()) {
                char c = rack.get(i);
                playerRack[i].setText(String.valueOf(c));
                playerRack[i].setIcon(new ImageIcon(c + ".png"));
            } else {
                playerRack[i].setText("");
                playerRack[i].setIcon(null);
            }
        }
    }

    // Saves the unused tiles for each player (allowing them to access them in their next turn)
    private void saveRackForCurrentPlayer() {
        List<Character> updatedRack = new ArrayList<>();
        for (RoundedButton btn : playerRack) {
            if (!btn.getText().isEmpty()) {
                updatedRack.add(btn.getText().charAt(0));
            }
        }
        playerRacks.put(currentPlayer, updatedRack);
    }

    // Refills each player's rack after their turn (only the empty slots)
    private void refillRackAfterSubmit() {
        List<Character> rack = playerRacks.get(currentPlayer);
        for (RoundedButton usedTile : usedRackTiles) {
            if (rack.size() < RACK_SIZE && !letterBag.isEmpty()) {
                char newLetter = letterBag.remove(0);
                rack.add(newLetter);
                usedTile.setText(String.valueOf(newLetter));
                usedTile.setIcon(new ImageIcon(newLetter + ".png"));
            }
        }
    }
    
    // Method to create buttons easily
    private RoundedButton createControlButton(String text, boolean enabled, ActionListener listener) {
        RoundedButton btn = new RoundedButton(text);
        btn.setFont(new Font("Segoe Script", Font.BOLD, 20));
        btn.setPreferredSize(new Dimension(200, 60));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(60, 60, 60, 200));
        btn.setEnabled(enabled);
        btn.addActionListener(listener);
        return btn;
    }

    // Method that removes highlights from squares after the tile is placed
    private void clearHighlights() {
        for (JLabel[] row : boardLabels) {
            for (JLabel label : row) {
                label.setBorder(null);
            }
        }
    }

    // Method handle putting the tiles on the board
    private void placeTile(int row, int col) {
        for (RoundedButton tile : playerRack) {
            if (!tile.getText().isEmpty() && tile.getText().charAt(0) == selectedTile) {
                tile.setText("");
                tile.setIcon(null);
                usedRackTiles.add(tile);
                break;
            }
        }
        boardLabels[row][col].setText(String.valueOf(selectedTile));
        boardLabels[row][col].setIcon(new ImageIcon(selectedTile + ".png")); // Loads in images from folder
        currentTilesPlaced.add(new Point(row, col));
        validSpots[row][col] = false;
        occupied.add(new Point(row, col));
        updateAdjacentSpots(row, col); // Updates new squares that are now also available
        selectedTile = ' ';
        clearHighlights(); // Removes highlights from squares after the tile is placed
        if (currentTilesPlaced.size() >= 1) resetButton.setEnabled(true); // Enables button only after something is actually placed on the board
        if (currentTilesPlaced.size() >= 1) checkButton.setEnabled(true); // Enables button only after something is actually placed on the board
    }

    // Method to restart turn (put all tiles back in the rack)
    private void resetPlacedTiles() {
        for (Point p : currentTilesPlaced) {
            char c = boardLabels[p.x][p.y].getText().charAt(0);
            for (RoundedButton btn : playerRack) {
                if (btn.getText().isEmpty()) {
                    btn.setText(String.valueOf(c));
                    btn.setIcon(new ImageIcon(c + ".png")); // Refills the rack with images of the letters
                    break;
                }
            }
            boardLabels[p.x][p.y].setText("");
            boardLabels[p.x][p.y].setIcon(null); // Removes the images from the board
            occupied.remove(p);
        }
        currentTilesPlaced.clear();
        // All buttons are unavailable to press (just like before the turn)
        resetButton.setEnabled(false);
        checkButton.setEnabled(false);
        submitButton.setEnabled(false);
        clearTempValidSpots(); // Empties the temporary array for valid squares
        recalculateValidSpots(); // Gets valid spots again (without the ones that became valid due to this turn)
        highlightValidSpots(); // Highlight these newly calculated spots only
    }

    // Method to calulcate scores for each player
    private int calculateScore(Set<String> words, List<Point> newTiles) {
        int totalScore = 0; // Starts off with 0

        for (String word : words) {
            int wordScore = 0;
            int wordMultiplier = 1; // Multiplier for 'Double Word' and 'Triple Word' bonuses
            
            List<Point> wordTiles = findTilesForWord(word);

            for (Point p : wordTiles) {
                char c = boardLabels[p.x][p.y].getText().charAt(0);
                int letterScore = letterValues.getOrDefault(Character.toUpperCase(c), 0);

                if (newTiles.contains(p)) {
                    Bonus bonus = bonusGrid[p.x][p.y];
                    switch (bonus) {
                        case DOUBLE_LETTER -> letterScore *= 2; // Multiplies the letter's score by 2
                        case TRIPLE_LETTER -> letterScore *= 3; // Multiplies the letter's score by 3
                        case DOUBLE_WORD -> wordMultiplier *= 2; // Multiplies the whole word's score by 2
                        case TRIPLE_WORD -> wordMultiplier *= 3; // Multiplies the whole word's score by 3
                    }
                }

                wordScore += letterScore; // Adds each letter's newly calculated score to the word's total score
            }

            totalScore += wordScore * wordMultiplier; // Multiples the entire word's score if needed
        }

        return totalScore;
    }

    private List<Point> findTilesForWord(String word) {
        List<Point> result = new ArrayList<>();

        for (int r = 0; r < 15; r++) {
            for (int c = 0; c < 15; c++) {
                if (!boardLabels[r][c].getText().isEmpty()) {
                    // Check horizontal
                    int cc = c;
                    StringBuilder sb = new StringBuilder();
                    List<Point> temp = new ArrayList<>();

                    while (cc < 15 && !boardLabels[r][cc].getText().isEmpty()) {
                        sb.append(boardLabels[r][cc].getText());
                        temp.add(new Point(r, cc));
                        cc++;
                    }

                    if (sb.toString().equals(word)) return temp;

                    // Check vertical
                    int rr = r;
                    sb.setLength(0);
                    temp.clear();

                    while (rr < 15 && !boardLabels[rr][c].getText().isEmpty()) {
                        sb.append(boardLabels[rr][c].getText());
                        temp.add(new Point(rr, c));
                        rr++;
                    }

                    if (sb.toString().equals(word)) return temp;
                }
            }
        }

        return result; // Should always succeed if word was already validated
    }

    // Method to handle submitting a word
    private void handleSubmit() {
        Set<String> words = new HashSet<>();
        for (Point p : currentTilesPlaced) {
            words.addAll(getHorizontalWord(p.x, p.y)); // Gets all word made horizontally
            words.addAll(getVerticalWord(p.x, p.y)); // Gets all word made vertically
        }

        int score = calculateScore(words, new ArrayList<>(currentTilesPlaced)); // Uses the calculate score method to get the score for these words
        scores[currentPlayer - 1] += score; // Adds this score to the player's own score

        // Updates the players' labels with their new score
        switch (currentPlayer) {
            case 1 -> scoreLabel1.setText("Player 1 Score: " + scores[0]);
            case 2 -> scoreLabel2.setText("Player 2 Score: " + scores[1]);
            case 3 -> scoreLabel3.setText("Player 3 Score: " + scores[2]);
            case 4 -> scoreLabel4.setText("Player 4 Score: " + scores[3]);
        }

        saveRackForCurrentPlayer(); // Saves player's unused tiles
        refillRackAfterSubmit(); // Refills player's used up slots
        
        currentTilesPlaced.clear();
        usedRackTiles.clear();
        selectedTile = ' ';
        firstMove = false;

        // Disables all the buttons for the next player
        resetButton.setEnabled(false);
        checkButton.setEnabled(false);
        submitButton.setEnabled(false);
        
        currentPlayer = (currentPlayer % numPlayers) + 1;
        if (currentPlayer == 1) round++;
        
        turnLabel.setText("Player " + currentPlayer + "'s Turn"); // Updates whose turn it is
        roundLabel.setText("Round " + round); // Updates what round it is
        letterBagLabel.setText("Letter Bag: " + letterBag.size()); // Updates how many letters are left
        
        loadRackForCurrentPlayer();
        if (letterBag.isEmpty()) {  // End of game logic
            showGameOverDialog();
        }
        clearTempValidSpots();
        recalculateValidSpots();
        highlightValidSpots();
    }

    private void showGameOverDialog() {
        int maxScore = -1;
        List<Integer> winners = new ArrayList<>();

        for (int i = 0; i < scores.length; i++) { // for loop to calculate who win (works with any number of players)
            if (scores[i] > maxScore) {
                maxScore = scores[i];
                winners.clear();
                winners.add(i);
            } else if (scores[i] == maxScore) {
                winners.add(i);
            }
        }

        String message;
        if (winners.size() == 1) {
            message = "\n Player " + (winners.get(0) + 1) + " wins with " + maxScore + " points!"; // Message to display who won
        } else { // Message to display there was a tie
            message = "\n It's a tie between players " +
                winners.stream().map(i -> "Player " + (i + 1)).collect(Collectors.joining(", ")) +
                " with " + maxScore + " points!";
        }

        JButton restartButton = new JButton("Restart"); // Option to restart the game
        JButton menuButton = new JButton("Menu"); // Option to return to the menu

        JPanel panel = new JPanel(new BorderLayout(30, 30));
        panel.add(new JLabel("<html><center>" + message.replace("\n", "<br>") + "</center></html>"), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(restartButton);
        buttonPanel.add(menuButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        JDialog dialog = new JDialog(this, "Game Over", true);
        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        restartButton.addActionListener(e -> {  // Restarts the game
            dialog.dispose();
            Scrabble newGame = new Scrabble(numPlayers);
            newGame.setVisible(true);
            dispose();
        });

        menuButton.addActionListener(e -> { // Goes back to the menu
            dialog.dispose();
            new Menu().setVisible(true);
            dispose();
        });

        dialog.setVisible(true);
    }

    // Method to recalculate which squares are valid to place tiles on
    private void recalculateValidSpots() {
        clearTempValidSpots(); // Clears any squares that were temporarily made valid
        if (firstMove) {
            validSpots[firstMoveCenter.x][firstMoveCenter.y] = true;
            tempValidSpots.add(firstMoveCenter);
        } else {
            for (Point p : occupied) {
                updateAdjacentSpots(p.x, p.y);
            }
        }
    }

    // Highlights squares that are valid to place tiles on
    private void highlightValidSpots() {
        for (int r = 0; r < 15; r++) {
            for (int c = 0; c < 15; c++) {
                if (validSpots[r][c]) {
                    boardLabels[r][c].setBorder(BorderFactory.createLineBorder(new Color(19, 68, 70), 4)); // Highlight using a dark green color (same as Menu)
                } else {
                    boardLabels[r][c].setBorder(null); // No highlight if the spots are not valid
                }
            }
        }
    }

    // Method to handle dialog box for when the user checks if their word is valid
    private void handleCheck() {
        if (!validateWords()) {
            JOptionPane.showMessageDialog(this, "Invalid word!"); // If word is not in the dictionary
            resetPlacedTiles(); // Puts all their tiles back in their rack
        } else {
            JOptionPane.showMessageDialog(this, "Valid word!"); // If word is in the dictionary
            submitButton.setEnabled(true); // Allows them to submit this word
        }
    }

    // Method to validate words (feeds the method above)
    private boolean validateWords() {
        if (firstMove && !currentTilesPlaced.contains(firstMoveCenter)) return false;

        Set<String> words = new HashSet<>();
        for (Point p : currentTilesPlaced) {
            words.addAll(getHorizontalWord(p.x, p.y)); // Gets all words made horizontally
            words.addAll(getVerticalWord(p.x, p.y)); // Gets all words made vertically
        }

        if (words.isEmpty()) return false;

        for (String word : words) {
            String upperWord = word.toUpperCase();
            if (upperWord.contains("*")) { // Handles blank tile logic
                if (!hasValidBlankReplacement(upperWord)) return false; // Word is invalid
            } else {
                if (!dictionary.contains(upperWord)) return false; // Word is invalid
            }
        }

        return true; // Word is valid
    }

    // Checks to see if any letter can fit in the blank's spot to make a valid word
    private boolean hasValidBlankReplacement(String wordWithBlanks) {
        return generateAllBlankVariants(wordWithBlanks).stream()
            .anyMatch(variant -> dictionary.contains(variant.toUpperCase()));
    }

    // Generates all possibilities for the blank (feeds method above)
    private List<String> generateAllBlankVariants(String word) {
        List<String> results = new ArrayList<>();
        char[] chars = word.toCharArray();
        backtrackBlank(chars, 0, results);
        return results;
    }

    // Method to help with the blank logic
    private void backtrackBlank(char[] chars, int index, List<String> results) {
        if (index == chars.length) {
            results.add(new String(chars));
            return;
        }

        if (chars[index] == '*') {
            for (char c = 'A'; c <= 'Z'; c++) {
                chars[index] = c;
                backtrackBlank(chars, index + 1, results);
            }
            chars[index] = '*'; // backtrack
        } else {
            backtrackBlank(chars, index + 1, results);
        }
    }

    // Method to check for any words made horizontally
    private List<String> getHorizontalWord(int row, int col) {
        List<String> words = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        int left = col;
        while (left >= 0 && !boardLabels[row][left].getText().isEmpty()) left--;
        left++;
        for (int i = left; i < 15 && !boardLabels[row][i].getText().isEmpty(); i++) {
            sb.append(boardLabels[row][i].getText());
        }
        if (sb.length() > 1) words.add(sb.toString());
        return words;
    }

    // Method to check for any words made horizontally
    private List<String> getVerticalWord(int row, int col) {
        List<String> words = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        int up = row;
        while (up >= 0 && !boardLabels[up][col].getText().isEmpty()) up--;
        up++;
        for (int i = up; i < 15 && !boardLabels[i][col].getText().isEmpty(); i++) {
            sb.append(boardLabels[i][col].getText());
        }
        if (sb.length() > 1) words.add(sb.toString());
        return words;
    }

    // Method to empty out the players entire rack
    private void clearRack() {
        for (RoundedButton tile : playerRack) {
            tile.setText("");
            tile.setIcon(null);
        }
    }

    // Updates all the squares around the one just placed on to indicate that they are valid
    private void updateAdjacentSpots(int row, int col) {
        int[][] dirs = {{-1,0}, {1,0}, {0,-1}, {0,1}}; // To account for all 4 sides of the square
        for (int[] d : dirs) {
            int r = row + d[0], c = col + d[1];
            if (r >= 0 && r < 15 && c >= 0 && c < 15) { // Checks to see if the coordinates are even on the board
                Point adjacent = new Point(r, c);
                if (occupied.contains(adjacent)) continue;
                if (tempValidSpots.contains(adjacent)) continue;
                validSpots[r][c] = true; // Sets the square to valid
                tempValidSpots.add(adjacent);
            }
        }
    }

    // Emtpies out the squares temporarily made valid
    private void clearTempValidSpots() {
        for (Point p : tempValidSpots) {
            validSpots[p.x][p.y] = false;
        }
        tempValidSpots.clear(); // Very self-explanatory
    }

    // Listens for a mouse click - very very essential to program
    private class BoardClickListener extends MouseAdapter {
        int row, col;
        BoardClickListener(int row, int col) {
            this.row = row;
            this.col = col;
        }
        public void mouseClicked(MouseEvent e) {
            if (validSpots[row][col] && selectedTile != ' ') {
                placeTile(row, col);
            }
        }
    }

    // Main method
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Scrabble().setVisible(true));
    }
}