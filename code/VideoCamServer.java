package com.iView.video;

import java.awt.image.BufferedImage;
import java.util.Observable;

import javax.swing.JOptionPane;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

import com.iView.gui.InformationFrame;
import com.iView.network.cam.ConnectionRoombaCamObject;

public class VideoCamServer extends Observable implements Runnable {

	private FFmpegFrameGrabber videoAdapter;
	private boolean connect;

	public VideoCamServer(ConnectionRoombaCamObject cam) {
		setConnection(cam);
	}

	public boolean setConnection(ConnectionRoombaCamObject connection) {
		connect = false;
		if (connection == null) {
			InformationFrame.showInformationFrame(
					"Keine Verbindungsangaben zur Kamera", "Bitte geben sie eine IP-Adresse an");
			return false;
		} else {
			try {
				System.out.println("Connection to Video-Cam: " + connection.getHost()+ " Port: " + connection.getPortRoombaCam());
String mainStreamUrl = connection.getMainURL();

// Create the VideoAdapter used to load the video file
				videoAdapter = new FFmpegFrameGrabber(mainStreamUrl);
				videoAdapter.setFormat("mjpeg");
				// Request a video frame
				// Start the thread for requesting the video frames
				videoAdapter.start();
				connect = true;
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null,
						"Kamera nicht zu erreichen, bitte erneut Verbindung wechseln");
			}
		}
		return connect;
	}
	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {while (videoAdapter.grabFrame() != null && connect) {
BufferedImage img = new Java2DFrameConverter().convert(videoAdapter.grabFrame());

					if (img != null) {
						setChanged();
						notifyObservers(img);
					}
				}
			} catch (Exception e) {
				// e.printStackTrace();
			}
		}
	}
	public void stop() {

		try {
			videoAdapter.stop();
			connect = false;
			System.out.println("client stopt");
		} catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
			e.printStackTrace();
		}

	}
}
