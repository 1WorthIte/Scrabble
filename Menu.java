/*
Name: Mahad Khurram
Date: 16 Jun 2025
Description:

This program is a digital recreation of the popular board game Scrabble! where players use tiles with little letters
on them to make words on a 15 x 15 board. Each letter has a certain value, and bonuses are spread across the board.
This code allows the user to submit only if their word is part of the official Scrabble dictionary, using a text
file containing almost 300k words and a file reader. Supports 2-4 players, with the option to specify this in the
Menu before the game begins. From a dropdown in the actual game, 4 buttons are available: Help (this leads to an
official Scrabble help page), Restart (to start a brand new game), Menu (to go back to the Menu), and Quit (to
exit the program). During the game, players can place tiles on the board and use the 'reset' button if they'd
like to restart their button. The number of letter tiles left in the letter bag are always visible, and the game
ends whenever these tiles run out (whoevever has more points wins). Each player's score is always visible as
well, as well as what round the game is on. The game will always highlight which squares are available to place
tiles on according to the official scrabble rules. The program is user-friendly and all files heavily commented
throughout. I couldn't be happier to be making this as my last ever coding project in high school. It's most
definitely been quite the journey. Until next time! Farewell Mr. Bawa :)
*/

package FPT; // The folder containing all the other files in the project

// Import statements
import javax.swing.*;
import java.awt.*;
import java.net.URI;

public class Menu extends JFrame {
    private int playerCount = 2; // Default number of players
    private JLabel playerCountLabel; // Displays the number of players between the + and -

    public Menu() {
        setTitle("Scrabble Menu");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1024, 700); // Size of window
        setResizable(false);
        setLocationRelativeTo(null);

        // Custom panel to paint background image
        JPanel bgPanel = new JPanel() {
            Image bg = new ImageIcon("scrabbleMenu.png").getImage(); // Image for the menu background
            @Override
            protected void paintComponent(Graphics g) { // Uses paint component for easy resizing
                super.paintComponent(g); 
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            }
        };
        bgPanel.setLayout(null);

        // Title
        JLabel title = new JLabel("Scrabble"); // Displays name of game as the title
        title.setFont(new Font("Segoe Script", Font.BOLD, 48));
        title.setForeground(Color.WHITE); // Color of title text
        title.setBounds(325, 50, 370, 60); // Position of the title
        title.setHorizontalAlignment(SwingConstants.CENTER);
        bgPanel.add(title);

        // Buttons to be displayed on the right side of the screen
        int btnWidth = 300, btnHeight = 80, btnX = 570, btnY = 215, btnGap = 20; // 
        JButton playBtn = makeMenuButton("Play", btnX, btnY + 0 * (btnHeight + btnGap), btnWidth, btnHeight); // Begins the game
        JButton helpBtn = makeMenuButton("Help", btnX, btnY + 1 * (btnHeight + btnGap), btnWidth, btnHeight); // Pops up an online help page for Scrabble
        JButton quitBtn = makeMenuButton("Quit", btnX, btnY + 2 * (btnHeight + btnGap), btnWidth, btnHeight); // Ends the program

        bgPanel.add(playBtn);
        bgPanel.add(helpBtn);
        bgPanel.add(quitBtn);

        // Button actions
        playBtn.addActionListener(e -> {
            Scrabble x = new Scrabble(playerCount); // Pass actual player count to game constructor
            x.setVisible(true);
            dispose(); // Closes current instance
        });
        helpBtn.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URI("https://www.scrabblepages.com/scrabble/rules/")); // Link that the help button goes to
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Could not open help page."); // Error message
            }
        });
        quitBtn.addActionListener(e -> System.exit(0));

        // Player count selector
        JLabel playerLabel = new JLabel("Num of Players:");
        playerLabel.setFont(new Font("Segoe Script", Font.BOLD, 32));
        playerLabel.setForeground(Color.WHITE);
        playerLabel.setBounds(185, 250, 400, 40);
        bgPanel.add(playerLabel);

        playerCountLabel = new JLabel(String.valueOf(playerCount));
        playerCountLabel.setFont(new Font("Segoe Script", Font.BOLD, 48));
        playerCountLabel.setForeground(Color.WHITE);
        playerCountLabel.setBounds(286, 355, 60, 40);
        playerCountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        bgPanel.add(playerCountLabel);

        JButton minusBtn = makeNumButton("-", 160, 330); // To reduce number of players
        JButton plusBtn = makeNumButton("+", 370, 330); // To increase number of players

        minusBtn.addActionListener(e -> {
            if (playerCount > 2) { // Minimum number of players (2)
                playerCount--;
                playerCountLabel.setText(String.valueOf(playerCount));
            }
        });

        plusBtn.addActionListener(e -> {
            if (playerCount < 4) { // Minimum number of players (2)
                playerCount++;
                playerCountLabel.setText(String.valueOf(playerCount));
            }
        });

        bgPanel.add(minusBtn);
        bgPanel.add(plusBtn);

        setContentPane(bgPanel);
    }

    // Method to make a button easily
    private JButton makeMenuButton(String text, int x, int y, int w, int h) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.WHITE);
        btn.setBounds(x, y, w, h);
        btn.setFont(new Font("Segoe Script", Font.BOLD, 32));
        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE); // White text
        btn.setOpaque(false);
        btn.setContentAreaFilled(false); // Transparent background
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2, true)); // White border
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { // Checks for mouse hovering over button
                btn.setContentAreaFilled(true);
                btn.setForeground(new Color(19, 68, 70)); // Dark green text
                btn.setBorder(BorderFactory.createLineBorder(new Color(19, 68, 70), 2, true)); // Dark green border
            }

            public void mouseExited(java.awt.event.MouseEvent evt) { // Checks for mouse leaving button
                btn.setContentAreaFilled(false);
                btn.setForeground(Color.WHITE); // White text
                btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2, true));
            }
        });
        return btn;
    }

    // Method to make a button easily (+ and - buttons only)
    private JButton makeNumButton(String symbol, int x, int y) {
        JButton btn = new JButton(symbol);
        btn.setBounds(x, y, 100, 100);
        btn.setFont(new Font("Arial", Font.BOLD, 70));
        btn.setForeground(Color.WHITE); // White text
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false); // Transparent background
        btn.setOpaque(false);
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2, true)); // White border
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { // Checks for mouse hovering over button
                btn.setContentAreaFilled(true);
                btn.setForeground(new Color(19, 68, 70));  // Dark green text
                btn.setBorder(BorderFactory.createLineBorder(new Color(19, 68, 70), 2, true));  // Dark green border
            }

            public void mouseExited(java.awt.event.MouseEvent evt) { // Checks for mouse leaving button
                btn.setContentAreaFilled(false);
                btn.setForeground(Color.WHITE); // White text
                btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2, true));
            }
        });

        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2, true));
        btn.setBackground(Color.WHITE);
        btn.setBorderPainted(true);
        btn.setFocusable(false);
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2, true));
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        return btn;
    }

    // Main method
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Menu().setVisible(true));
    }
}
