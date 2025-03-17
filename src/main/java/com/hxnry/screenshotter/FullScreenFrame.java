package com.hxnry.screenshotter;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Random;

public class FullScreenFrame extends JFrame {

	public static final String NAME = "Screenshotter" + File.separator + "images" + File.separator;
	public static final OperatingSystem OPERATINGSYSTEM;
	public static final String HOME_DIRECTORY;
	public static final String UNIX_HOME_DIRECTORY;
	private static final long serialVersionUID = 1L;
	private BufferedImage img;
	private Rectangle rect;

	public enum OperatingSystem {
		WINDOWS, MAC, LINUX, UNIDENTIFIED
	}

	static {
		final String operatingsystem = System.getProperty("os.name");
		final String home = System.getProperty("user.home");
		UNIX_HOME_DIRECTORY = home == null ? "~" : home;
		OPERATINGSYSTEM = operatingsystem.contains("Windows") ? OperatingSystem.WINDOWS : operatingsystem.contains("Mac") ? OperatingSystem.MAC : operatingsystem.contains("Linux") ? OperatingSystem.LINUX : OperatingSystem.UNIDENTIFIED;
		HOME_DIRECTORY = OPERATINGSYSTEM == OperatingSystem.WINDOWS ? FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath() + File.separator + NAME : UNIX_HOME_DIRECTORY + File.separator + NAME;
		makeHomeDirs();
	}

	public FullScreenFrame(final Rectangle rect, final BufferedImage img) {
		this.rect = rect;
		this.img = img;
		init();
	}

	private void init() {
		this.setLocationRelativeTo(null);
		this.setUndecorated(true);
		this.setLocation(rect.getLocation());
		setCursor(Cursor.getDefaultCursor());
		JPanel contentPanel = new JPanel() {

			private static final long serialVersionUID = 1L;

			@Override
			public void paintComponent(Graphics g) {
				g.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null);
				int x = currentPoint.x;
				int y = currentPoint.y;

				g.setColor(new Color(220, 220, 220, 220));
				g.drawLine(x, 0, x, img.getHeight());
				g.drawLine(0, y, img.getWidth(), y);

				if (isPressed) {
					Rectangle rect = new Rectangle(startPoint);
					rect.add(currentPoint);
					g.drawRect(rect.x, rect.y, rect.width, rect.height);
					g.setColor(new Color(0, 0, 0, 65));
					g.fillRect(rect.x + 1, rect.y + 1, rect.width - 1, rect.height - 1);
					final String leftBotString = rect.width + ", " + rect.height;
					final int stringWidth = g.getFontMetrics().stringWidth(leftBotString);
					g.drawString(leftBotString, rect.x + rect.width - stringWidth - 4, rect.y + rect.height - 4);
					g.setColor(new Color(220, 220, 220));
					g.drawString(leftBotString, rect.x + rect.width - stringWidth - 4 - 1, rect.y + rect.height - 4 - 1);
				}
			}
		};
		getContentPane().add(contentPanel);
		//this.setContentPane(contentPanel);
		this.setSize(img.getWidth(), img.getHeight());
		this.setVisible(true);
		setupKeyListener();
		setupMouseListener();
	}

	private boolean isPressed = false;
	private Point startPoint = null;
	private Point currentPoint = new Point(0, 0);

	private Rectangle cutRect = null;

	private void finishCut() throws IOException {
		final BufferedImage cut = img.getSubimage(cutRect.x, cutRect.y, cutRect.width, cutRect.height);
		DateFormat formatter = new SimpleDateFormat("MM dd yyyy");
		Date now = Date.from(Instant.now());
		String formattedTime = formatter.format(now);
		String homeDirectory = System.getProperty("screenshotter.home");
		File f = new File((homeDirectory + "Screenshotter-" + formattedTime + ".png").replaceAll(" ", "-"));
		if (f.exists()) {
			f.createNewFile();
			String name = f.getName();
			int indexOfPng = f.getName().indexOf(".png");
			String extractedName = homeDirectory + name.substring(0, indexOfPng);
			f = new File(extractedName + "-" + generateRandomText(5) + ".png");
		}
		ImageIO.write(cut, "png", f);
		System.out.println("Your screenshot has been saved! -> " + f.getAbsolutePath());
	}

	public static String generateRandomText(int length) {
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 ";
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			sb.append(characters.charAt(random.nextInt(characters.length())));
		}
		return sb.toString();
	}

	private static void makeHomeDirs() {
		File file = new File(HOME_DIRECTORY);
		if(!file.exists()) {
			file.mkdirs();
			System.out.println("Home directory has been created @ " + file.getAbsolutePath());
		}
	}

	private void setupMouseListener() {
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				e.consume();
				repaint();
				isPressed = false;
				if (startPoint == null)
					return;
				Rectangle rectangle = new Rectangle(startPoint);
				rectangle.add(new Point(e.getX(), e.getY()));
				dispose();
				cutRect = rectangle;
				new Thread(() -> {
                    try {
						makeHomeDirs();
                        finishCut();
                    } catch (Exception e1) {
                        Screenshotter.error(e1);
                    }
                }).start();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				repaint();
				if (e.getButton() == MouseEvent.BUTTON1) {
					isPressed = true;
					startPoint = new Point(e.getX(), e.getY());
				} else if (e.getButton() == MouseEvent.BUTTON3) {
					e.consume();
					dispose();
				}
			}
		});

		this.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent arg0) {
				repaint();
				currentPoint = arg0.getPoint();
			}

			@Override
			public void mouseMoved(MouseEvent arg0) {
				repaint();
				currentPoint = arg0.getPoint();
			}

		});
	}

	private void setupKeyListener() {
		this.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
					dispose();
				}
			}
		});
	}

}