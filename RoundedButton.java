package FPT;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class RoundedButton extends JButton {
    private int arc = 40;
    private Color borderColor = Color.WHITE;
    private Color hoverBorderColor = new Color(19, 68, 70);
    private Color textColor = Color.WHITE;
    private Color hoverTextColor = new Color(19, 68, 70);
    private boolean hover = false;

    public RoundedButton(String text) {
        super(text);
        setFocusPainted(false);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setForeground(textColor);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                setForeground(hoverTextColor);
                repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                setForeground(textColor);
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(2));
        g2.setColor(hover ? hoverBorderColor : borderColor);
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);
        g2.dispose();
    }
}