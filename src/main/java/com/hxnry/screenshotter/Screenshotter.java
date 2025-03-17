package com.hxnry.screenshotter;

import com.hxnry.screenshotter.ui.ScreenshotterFrame;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class Screenshotter {

	public static void main(String[] args) throws AWTException {
		System.setProperty("screenshotter.home", System.getProperty("user.home") + "/Screenshotter/");
		initExceptionHandler();
		new ScreenshotterFrame();
	}

	static void initExceptionHandler() {
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				error(t, e);
			}
		});
	}

	public static void error(Thread t, Throwable e) {
		try {
			e.printStackTrace();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos, true, "UTF-8"));
			JOptionPane.showMessageDialog(null, t.getName() + ": " + baos.toString("UTF-8"), "Screenshotter Error!", JOptionPane.ERROR_MESSAGE);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}

	public static void error(Throwable e) {
		error(Thread.currentThread(), e);
	}
}