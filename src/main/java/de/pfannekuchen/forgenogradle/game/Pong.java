package de.pfannekuchen.forgenogradle.game;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;

/**
 * Seriously, what do you expect? Wait for the slow process of getting Forge ready? Nop, we play Pong in the meantime.
 * @author Pancake
 */
public class Pong extends JFrame implements KeyListener {
	
	/**
	 * Prepares the window by setting the size and some window hints
	 */
	public Pong() {
		// get display height
		int height = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getHeight();
		// calculate width 16:9
		int width = (int) (height/9f*16f);
		setSize(width, height);
		// hide title bar
		setUndecorated(true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setVisible(true);
		addKeyListener(this);
		// start game thread
		new Thread(() -> {
			try {
				while (true) {
					// wait a frame
					repaint();
					Thread.sleep(8);
					// moving
					if (isWPressed) y1 -= 0.005;
					if (isSPressed) y1 += 0.005;
					// ball moving
					if (rol) ballx += 0.0025;
					else ballx -= 0.0025;
					if (uod) bally += 0.0025/9*16;
					else bally -= 0.0025/9*16;
					
					// top bottom collision
					double ballsize = ballpixelsize*6;
					if (bally >= 1-ballsize) uod = false;
					if (bally <= ballsize) uod = true;
					
					// box collision. flawed but who cares
					if (ballx < 0.06 && bally > y1 && bally < y1+size) rol = true;
					if (ballx > 0.95-0.05 && bally > y2 && bally < y2+size) rol = false;
					
					// move box 2
					if (ballx > 0.5 && rol) {
						if (bally > y2+size/2) y2+=0.005;
						else y2-=0.005;
					}
					
					// clamp positions
					if (y1 > 1-size) y1 = 1-size;
					else if (y1 < 0) y1 = 0;
					if (y2 > 1-size) y2 = 1-size;
					else if (y2 < 0) y2 = 0;
					
					
					// map collision
					if (ballx >= 1-ballsize) score1++;
					if (ballx <= 0) score2++;
					if (ballx >= 1-ballsize || ballx <= 0) {
						ballx = 0.5;
						bally = 0.5;
						continue;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}
	
	// Positions of the boxes in percent
	double y1 = 0.1;
	double y2 = 0.4;
	// Size of the box
	double size = 0.22;
	// Ball position
	double ballx = 0.5;
	double bally = 0.5;
	// Ball motion
	boolean rol = true;
	boolean uod = true;
	// Ball "texture"
	double ballpixelsize = 0.006;
	int[][] texture = new int[][] {
		{0, 1, 1, 1, 1, 0},
		{1, 1, 1, 1, 1, 1},
		{1, 1, 1, 1, 1, 1},
		{1, 1, 1, 1, 1, 1},
		{1, 1, 1, 1, 1, 1},
		{0, 1, 1, 1, 1, 0},
		
	};
	
	// scores of the game
	int score1 = 0;
	int score2 = 0;
	
	/**
	 * Renders Pong
	 */
	@Override
	public void paint(Graphics gr) {
		// double buffered image
		BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		g.setColor(Color.black);
		g.fillRect(0, 0, getWidth(), getHeight());
		// Render left and right boxes
		g.setColor(Color.white);
		g.fillRect((int) (getWidth()/2-(getWidth()*0.45)), (int) (y1*getHeight()), (int) (0.015*getWidth()), (int) (size*getHeight()));
		g.fillRect((int) (getWidth()/2+(getWidth()*0.435)), (int) (y2*getHeight()), (int) (0.015*getWidth()), (int) (size*getHeight()));
		// Score
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setFont(new Font(Font.MONOSPACED, 2, 32));
		FontMetrics m = g.getFontMetrics();
		g.drawString(score1 + " - " + score2, (int) (0.5*getWidth())-m.stringWidth(score1 + " - " + score2)/2, (int) (0.05*getHeight()));
		// Render Ball
		int size = (int) (getWidth()*ballpixelsize);
		for (int x = 0; x < 6; x++) {
			for (int y = 0; y < 6; y++) {
				if (texture[x][y] == 1) g.fillRect((int) (getWidth()*ballx) + x*size, (int) (getHeight()*bally) + y*size, size, size);
			}
		}
		// Text Background
		g.setColor(Color.green);
		int y = (int) (m.getHeight()*1.2);
		List<String> clone = new ArrayList<>(strings);
		Collections.reverse(clone);
		if (clone.size() > 40) clone = clone.subList(0, 40);
		for (String string : clone) {
			g.drawString(string.replace('\t', ' ').replaceAll("  ", ""), 15, y);
			y += m.getHeight()*1.2;
			//if (y > m.getHeight()) break;
		}
		gr.drawImage(img, 0, 0, null);
	}
	
	private static ArrayList<String> strings = new ArrayList<>();
	
	public static void runPong() {
		new Pong();
		System.setOut(new PrintStream(System.out) {
			@Override
			public void println(String x) {
				strings.add(x);
				super.println(x);
			}
		});
		System.setErr(new PrintStream(System.err) {
			@Override
			public void println(String x) {
				if (x != null) strings.add(x);
				super.println(x);
			}
		});
	}

	@Override public void keyTyped(KeyEvent e) {}

	boolean isWPressed = false;
	boolean isSPressed = false;
	
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_W || e.getKeyChar() == KeyEvent.VK_KP_UP) isWPressed = true;
		if (e.getKeyCode() == KeyEvent.VK_S || e.getKeyChar() == KeyEvent.VK_KP_DOWN) isSPressed = true;
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_W || e.getKeyChar() == KeyEvent.VK_KP_UP) isWPressed = false;
		if (e.getKeyCode() == KeyEvent.VK_S || e.getKeyChar() == KeyEvent.VK_KP_DOWN) isSPressed = false;
	}
	
}
