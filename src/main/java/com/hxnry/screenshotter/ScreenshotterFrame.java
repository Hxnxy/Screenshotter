package com.hxnry.screenshotter;

import com.hxnry.screenshotter.ui.lnf.CustomFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class ScreenshotterFrame extends CustomFrame {

	private static final long serialVersionUID = 1L;
	private Point mouseDownCompCoords = null;
	private Point mouseDownFrameCoords = null;
	private Image cameraImage;
	private Image cameraImageHovered;
	private Robot robot;

	public ScreenshotterFrame() throws AWTException {
		robot = new Robot();
		init();
	}

	private void init() {
		cameraImage = getImage("src/main/resources/camera.png");
		cameraImageHovered = getImage("src/main/resources/camera_hover.png");
		this.setIconImage(cameraImage);
		this.setTitle("Screenshotter");
		final JPanel newContentPane = new JPanel() {

			private static final long serialVersionUID = 1L;

			int CAMERA_W = 200;
			int CAMERA_H = 150;

			public void paintComponent(Graphics g) {
				g.fillRect(0, 0, CAMERA_W, CAMERA_H);
				g.setColor(new Color(100, 100, 100));
				g.drawRect(0, 0, CAMERA_W, CAMERA_H);
				if (centerHovered) {
					g.setColor(new Color(59, 59, 59));
					g.drawImage(cameraImageHovered, 0, 0, CAMERA_W, CAMERA_H, null);
				} else {
					g.setColor(new Color(140, 139, 139));
					g.drawImage(cameraImage, 0, 0, CAMERA_W, CAMERA_H, null);
				}
			}
		};
		final GraphicsConfiguration gc = this.getGraphicsConfiguration();
		final Rectangle bounds = gc.getBounds();
		int x = (int) (bounds.x + bounds.width * 0.75);
		int y = (int) (bounds.y + bounds.height * 0.25);
		this.setLocation(x, y);
		getContentPane().add(newContentPane);
		//this.setUndecorated(true);
		getContentPane().setBackground(new Color(220, 220, 240));
		this.setAlwaysOnTop(true);
		this.setMinimumSize(new Dimension(210, 185));
		this.setMaximumSize(new Dimension(210, 185));
		setupDragFrameListeners();
		this.setVisible(true);
	}

	private void startScreenshot() {
		final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final GraphicsDevice[] devices = env.getScreenDevices();
		int maxHeight = 0;
		int width = 0;
		for (GraphicsDevice q : devices) {
			final GraphicsConfiguration[] conf = q.getConfigurations();
			for (GraphicsConfiguration gc : conf) {
				final Rectangle bounds = gc.getBounds();
				int height = bounds.height;
				if (height > maxHeight) {
					maxHeight = height;
				}
				width += bounds.width;
			}
		}
		final Rectangle fullScreenBounds = new Rectangle(width, maxHeight);
		final BufferedImage screenShot = robot.createScreenCapture(fullScreenBounds);
		new FullScreenFrame(fullScreenBounds, screenShot);
	}

	private boolean centerHovered = false;

	private void setupDragFrameListeners() {
		MouseListener clickingListener = new MouseAdapter() {

			@Override
			public void mouseExited(MouseEvent e) {
				centerHovered = false;
				repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				mouseDownCompCoords = null;
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() != MouseEvent.BUTTON1) {
					return;
				}
				if (e.isConsumed()) {
					return;
				}
				final Point p = e.getPoint();
				if (p.x > getWidth() - 14 && p.x < getWidth() - 7 && p.y < 20) {
					dispose();
				}

				if (p.y < 20) {
					mouseDownCompCoords = e.getLocationOnScreen();
					mouseDownFrameCoords = getLocation();
				} else {
					startScreenshot();
				}
				e.consume();
			}
		};
		MouseMotionListener draggingListener = new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if (mouseDownCompCoords == null) {
					return;
				}
				Point currCoords = e.getLocationOnScreen();
				Point currentLocation = mouseDownFrameCoords;
				setLocation(currentLocation.x + (currCoords.x - mouseDownCompCoords.x), currentLocation.y + (currCoords.y - mouseDownCompCoords.y));
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				Point p = e.getPoint();
				centerHovered = p.y > 20;
				repaint();
			}
		};

		this.addMouseListener(clickingListener);
		this.addMouseMotionListener(draggingListener);
	}

	public static boolean isRunningFromJar() {
		return Screenshotter.class.getResource("Screenshotter.class").toString().startsWith("jar:");
	}

	public static Image getImage(final String resource) {
		try {
			if (isRunningFromJar()) {
				System.out.println("RUNS FROM JAR");
				InputStream str = Screenshotter.class.getClassLoader().getResourceAsStream(resource);
				Image img = ImageIO.read(str);
				str.close();
				return img;
			} else {
				System.out.println("NOT FROM JAR");
				InputStream str = new FileInputStream(new File(resource));
				Image img = ImageIO.read(str);
				str.close();
				return img;
			}
		} catch (final Exception ignored) {
			ignored.printStackTrace();
		}
		return null;
	}
}