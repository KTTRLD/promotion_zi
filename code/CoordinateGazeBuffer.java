package com.iView.data;

import java.awt.Point;
import java.awt.Toolkit;
import java.util.LinkedList;

import com.iView.data.ObjectLibrary.Eyegesture;

/**
 * 
 * Listenobjekt, das {@link Point} Elemente nach dem Lifo Prinzip hinzufügt und
 * entfernt. Die Tupel entsprechen den POR-Koordinaten auf
 * dem Monitor. Im Zuge eines jeden Hinzufügens wird der komplette in der Liste
 * befindliche Inhalt auf eventuell aufgetretene Augengesten, wie Horizontal-,
 * Vertikalbewegung und Fixierung hin überprüft.
 * 
 * @author Simone Eidam
 *
 */
public class CoordinateGazeBuffer extends LinkedList<Point> {

	/**
	 * Anzahl der {@link Point} Elemente die maximal vom CoordinateBuffer
	 * aufgenommen werden können. Ist abhängig von der Framerate des Eyetrackers
	 * (RED: 60Hz).
	 */
	private static int bufferSize = 6;
	/**
	 * Zeit in ms, die benötigt wird, um den {@link CoordinateGazeBuffer} mit der
	 * {@link #bufferSize} entsprechenden Anzahl an {@link Point} Elementen zu füllen.
	 * Ist abhängig von der Framerate des Eyetrackers (RED: 60Hz).
	 */
	private static double captureTime = 100;
	/**
	 * Bildschirmhöhe des Stimulus Rechners.
	 */
	private int height = Toolkit.getDefaultToolkit().getScreenSize().height;
	/**
	 * Bildschirmbreite des Stimulus Rechners.
	 */
	private int width = Toolkit.getDefaultToolkit().getScreenSize().width;

	/**
	 * letzte geglättete vom Eyetracker stammende x-Koordinate
	 */
	public int lastAddedX;
	/**
	 * letzte geglättete vom Eyetracker stammende y-Koordinate
	 */
	public int lastAddedY;

	private static final long serialVersionUID = 1L;

	/**
	 * Modifizierte {@link LinkedList#add(Object)} Methode, die im Zuge des Hinzufügens den
	 * gesamten CoordinateBuffer Inhalt auf Augengesten hin überprüft.
	 * 
	 * @param _x
	 *            x-Koordinate, die dem Listenobjekt hinzugefügt werden soll.
	 * @param _y
	 *            y-Koordinate, die dem Listenobjekt hinzugefügt werden soll.
	 * @return Eyegesture gibt die erkannte Augengeste als enum zurück.
	 */
	public Eyegesture addTuple(int _x, int _y) {
		// smooth values
		double duration = 0.90; // Wichtung
		lastAddedX = (int) (duration * lastAddedX + (1 - duration) * _x);
		lastAddedY = (int) (duration * lastAddedY + (1 - duration) * _y);
		add(new Point(lastAddedX, lastAddedY));
		
		// Hinzufügen des Tupels am Ende der Liste
		if (size()== bufferSize) {
			clear();
			return Eyegesture.Gaze;
			
		}
		
		return Eyegesture.GazeNull;
		
		
	}


	/**
	 * Gibt die Zeit in ms zurück, die benötigt wird, um den
	 * CoordinateBuffer mit der {@link #bufferSize} entsprechenden
	 * Anzahl an Points zu füllen.
	 * 
	 * @return Zeit in ms, die benötigt wird, um den CoordinateBuffer
	 *         mit der {@link #bufferSize} entsprechenden Anzahl an
	 *         Points zu füllen.
	 */
	public static int getCaptureTime() {
		return (int) captureTime;
	}

	/**
	 * Diese Methode legt durch eine fps Umrechnung die Anzahl der Elemente die
	 * vom CoordinateBuffer aufgenommen werden können fest.
	 * 
	 * @param _captureTime
	 *            Die zur fps Umrechnung benötigte Zeitangabe
	 */
	public static void setCaptureTime(int _captureTime) {
		captureTime = _captureTime;
		try {
			// Umrechnung der captureTime (in ms) in sek, mal 60 Hz (die max.
			// framerate vom Eyetracker)
			double result = (captureTime / 1000.0) * 60.0;
			bufferSize = (int) Math.round(result);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	public int getHeight() {
		return height;
	}


	public void setHeight(int height) {
		this.height = height;
	}


	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}


	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	

	
}
