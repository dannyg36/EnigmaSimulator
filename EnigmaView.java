/*
 * File: EnigmaView.java
 * ---------------------
 * This module implements the class EnigmaView, which manages the
 * graphics for the Enigma simulator.
 */

package edu.willamette.cs1.enigma;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;

public class EnigmaView extends JFrame {

    public EnigmaView(EnigmaModel model) {
        super("Enigma");
        this.model = model;
        canvas = new EnigmaCanvas(model);
        setLayout(new BorderLayout());
        add(canvas, BorderLayout.CENTER);
        pack();
        setVisible(true);
    }

    public void update() {
        canvas.repaint();
    }

/* Private instance variables */

    private EnigmaModel model;
    private EnigmaCanvas canvas;

}

class EnigmaCanvas extends JComponent implements MouseListener {

    public EnigmaCanvas(EnigmaModel model) {
        this.model = model;
        started = false;
        setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
        createKeys();
        createLamps();
        createRotors();
        addMouseListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        try {
            Image topView = ImageIO.read(new File("images/EnigmaTopView.png"));
            g.drawImage(topView, 0, 0, this);
        } catch (IOException ex) {
            throw new RuntimeException("Missing EnigmaTopView.png image file");
        }
        drawKeys((Graphics2D) g);
        drawLamps((Graphics2D) g);
        drawRotors((Graphics2D) g);
    }

    private void createKeys() {
        keys = new HashMap<String,Ellipse2D>();
        for (int i = 0; i < 26; i++) {
            String ch = Character.toString((char) ('A' + i));
            double x = KEY_LOCATIONS[i].getX();
            double y = KEY_LOCATIONS[i].getY();
            double r = KEY_RADIUS;
            Ellipse2D oval = new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r);
            keys.put(ch, oval);
        }
    }

    private void drawKeys(Graphics2D g) {
        g.setStroke(new BasicStroke(KEY_BORDER));
        g.setFont(Font.decode(KEY_FONT));
        FontMetrics fm = g.getFontMetrics();
        for (int i = 0; i < 26; i++) {
            String ch = Character.toString((char) ('A' + i));
            Ellipse2D oval = keys.get(ch);
            g.setColor(Color.decode(KEY_BGCOLOR));
            g.fill(oval);
            g.setColor(Color.decode(KEY_BORDER_COLOR));
            g.draw(oval);
            if (started && model.isKeyDown(ch)) {
                g.setColor(Color.decode(KEY_DOWN_COLOR));
            } else {
                g.setColor(Color.decode(KEY_UP_COLOR));
            }
            int w = fm.stringWidth(ch);
            int x = (int) Math.round(oval.getX() + oval.getWidth() / 2);
            int y = (int) Math.round(oval.getY() + oval.getHeight() / 2);
            g.drawString(ch, x - w / 2,
                         y + fm.getAscent() / 2 + KEY_LABEL_DY);
        }
    }

    private void createLamps() {
        lamps = new HashMap<String,Ellipse2D>();
        for (int i = 0; i < 26; i++) {
            String ch = Character.toString((char) ('A' + i));
            double x = LAMP_LOCATIONS[i].getX();
            double y = LAMP_LOCATIONS[i].getY();
            double r = LAMP_RADIUS;
            Ellipse2D oval = new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r);
            lamps.put(ch, oval);
        }
    }

    private void drawLamps(Graphics2D g) {
        g.setStroke(new BasicStroke(LAMP_BORDER));
        g.setFont(Font.decode(LAMP_FONT));
        FontMetrics fm = g.getFontMetrics();
        for (int i = 0; i < 26; i++) {
            String ch = Character.toString((char) ('A' + i));
            Ellipse2D oval = lamps.get(ch);
            g.setColor(Color.decode(LAMP_BGCOLOR));
            g.fill(oval);
            g.setColor(Color.decode(LAMP_BORDER_COLOR));
            g.draw(oval);
            if (started && model.isLampOn(ch)) {
                g.setColor(Color.decode(LAMP_ON_COLOR));
            } else {
                g.setColor(Color.decode(LAMP_OFF_COLOR));
            }
            int w = fm.stringWidth(ch);
            int x = (int) Math.round(oval.getX() + oval.getWidth() / 2);
            int y = (int) Math.round(oval.getY() + oval.getHeight() / 2);
            g.drawString(ch, x - w / 2,
                             y + fm.getAscent() / 2 + LAMP_LABEL_DY);
        }
    }

    private void createRotors() {
        rotors = new ArrayList<Rectangle2D>();
        for (int i = 0; i < ROTOR_LOCATIONS.length; i++) {
            double x = ROTOR_LOCATIONS[i].getX();
            double y = ROTOR_LOCATIONS[i].getY();
            double w = ROTOR_WIDTH;
            double h = ROTOR_HEIGHT;
            Rectangle2D rect = new Rectangle2D.Double(x - w / 2, y - h / 2,
                                                      w, h);
            rotors.add(rect);
        }
    }

    private void drawRotors(Graphics2D g) {
        g.setFont(Font.decode(ROTOR_FONT));
        FontMetrics fm = g.getFontMetrics();
        for (int i = 0; i < ROTOR_LOCATIONS.length; i++) {
            Rectangle2D rect = rotors.get(i);
            g.setColor(Color.decode(ROTOR_BGCOLOR));
            g.fill(rect);
            g.setColor(Color.decode(ROTOR_COLOR));
            String s = (started) ? model.getRotorLetter(i) : "A";
            int w = fm.stringWidth(s);
            int x = (int) Math.round(rect.getX() + rect.getWidth() / 2);
            int y = (int) Math.round(rect.getY() + rect.getHeight() / 2);
            g.drawString(s, x - w / 2,
                         y + fm.getAscent() / 2 + ROTOR_LABEL_DY);
        }
    }

/* MouseListener */

    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        for (int i = 0; i < ROTOR_LOCATIONS.length; i++) {
            Rectangle2D rect = rotors.get(i);
            if (rect.contains(x, y)) {
                model.rotorClicked(i);
                return;
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        /* Empty */
    }

    @Override
    public void mouseExited(MouseEvent e) {
        /* Empty */
    }

    @Override
    public void mousePressed(MouseEvent e) {
        started = true;
        int x = e.getX();
        int y = e.getY();
        lastKey = null;
        for (int i = 0; i < 26; i++) {
            String ch = Character.toString((char) ('A' + i));
            Ellipse2D oval = keys.get(ch);
            if (oval.contains(x, y)) {
                model.keyPressed(ch);
                lastKey = ch;
                return;
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (lastKey != null) {
            model.keyReleased(lastKey);
        }
    }

/* Constants */

    public static final int CANVAS_WIDTH = 818;
    public static final int CANVAS_HEIGHT = 694;

    public static final String ROTOR_BGCOLOR = "#BBAA77";
    public static final String ROTOR_COLOR = "#000000";
    public static final String ROTOR_FONT = "Helvetica Neue-24";
    public static final int ROTOR_FRAME_HEIGHT = 100;
    public static final int ROTOR_FRAME_WIDTH = 40;
    public static final int ROTOR_HEIGHT = 26;
    public static final int ROTOR_WIDTH = 24;
    public static final int ROTOR_LABEL_DY = -3;


    public static final Point[] ROTOR_LOCATIONS = {
        new Point(244, 94),
        new Point(329, 94),
        new Point(412, 94)
    };

    public static final String KEY_BGCOLOR = "#666666";
    public static final String KEY_BORDER_COLOR = "#CCCCCC";
    public static final String KEY_DOWN_COLOR = "#CC3333";
    public static final String KEY_UP_COLOR = "#CCCCCC";
    public static final String KEY_FONT = "Helvetica Neue-Bold-28";
    public static final int KEY_BORDER = 3;
    public static final int KEY_RADIUS = 24;
    public static final int KEY_LABEL_DY = -3;

    public static final Point[] KEY_LOCATIONS = {
        new Point(140, 566),
        new Point(471, 640),
        new Point(319, 639),
        new Point(294, 567),
        new Point(268, 495),
        new Point(371, 567),
        new Point(448, 567),
        new Point(523, 567),
        new Point(650, 496),
        new Point(598, 567),
        new Point(674, 567),
        new Point(699, 641),
        new Point(624, 641),
        new Point(547, 640),
        new Point(725, 497),
        new Point( 92, 639),
        new Point(115, 494),
        new Point(345, 495),
        new Point(217, 566),
        new Point(420, 496),
        new Point(574, 496),
        new Point(395, 639),
        new Point(192, 494),
        new Point(242, 639),
        new Point(168, 639),
        new Point(497, 496)
    };

    public static final String LAMP_BGCOLOR = "#333333";
    public static final String LAMP_BORDER_COLOR = "#111111";
    public static final String LAMP_OFF_COLOR = "#666666";
    public static final String LAMP_ON_COLOR = "#FFFF99";
    public static final String LAMP_FONT = "Helvetica Neue-Bold-24";
    public static final int LAMP_BORDER = 1;
    public static final int LAMP_LABEL_DY = -3;
    public static final int LAMP_RADIUS = 23;

    public static final Point[] LAMP_LOCATIONS = {
        new Point(144, 332),
        new Point(472, 403),
        new Point(321, 402),
        new Point(296, 333),
        new Point(272, 265),
        new Point(372, 333),
        new Point(448, 334),
        new Point(524, 334),
        new Point(650, 266),
        new Point(600, 335),
        new Point(676, 335),
        new Point(700, 403),
        new Point(624, 403),
        new Point(549, 403),
        new Point(725, 267),
        new Point( 94, 401),
        new Point(121, 264),
        new Point(347, 265),
        new Point(220, 332),
        new Point(423, 265),
        new Point(574, 266),
        new Point(397, 402),
        new Point(197, 264),
        new Point(246, 402),
        new Point(170, 401),
        new Point(499, 265)
    };

/* Private instance variables */

    private ArrayList<Rectangle2D> rotors;
    private EnigmaModel model;
    private HashMap<String,Ellipse2D> keys;
    private HashMap<String,Ellipse2D> lamps;
    private String lastKey;
    private boolean started;

}
