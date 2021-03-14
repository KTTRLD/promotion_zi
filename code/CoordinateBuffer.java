package com.iView.data;

import java.awt.Point;
import java.awt.Toolkit;
import java.util.Iterator;
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
public class CoordinateBuffer extends LinkedList<Point> {

	
	/**
	 * Anzahl der {@link Point} Elemente die maximal vom CoordinateBuffer
	 * aufgenommen werden können. Ist abhängig von der Framerate des Eyetrackers
	 * (RED: 60Hz).
	 */
	private static int bufferSize = 60;
	/**
	 * Zeit in ms, die benötigt wird, um den {@link CoordinateBuffer} mit der
	 * {@link #bufferSize} entsprechenden Anzahl an {@link Point} Elementen zu füllen.
	 * Ist abhängig von der Framerate des Eyetrackers (RED: 60Hz).
	 */
	private static double captureTime = 1000;
	/**
	 * Bildschirmhöhe des Stimulus Rechners.
	 */
	private int height = Toolkit.getDefaultToolkit().getScreenSize().height;
	/**
	 * Bildschirmbreite des Stimulus Rechners.
	 */
	private int width = Toolkit.getDefaultToolkit().getScreenSize().width;
	/**
	 * Streuungsmaß (in Pixel), das festlegt, ab wann eine Fixierung nicht
	 * mehr erkannt werden soll.
	 */
	private static int maxDispersion = 200;
	/**
	 * Größe (in Pixel) der Auslenkung, welche von einer vertikalen/horizontalen
	 * gedachten Linie auf dem Bildschirm des Stimulus Rechners zugelassen wird,
	 * um noch als horizontale/vertikale Augengeste erkannt zu werden.
	 */
	private static int maxDisplacement = 200;
	/**
	 * Anzahl der Nullwerte, die maximal Eintreffen können, um den Nullwert
	 * Datenstrom noch als Lidschluss zu erkennen. Treffen mehr Nullwerte ein
	 * (z.B. im Falle des kompletten Wegschauens des Users) kann kein
	 * Lidschlussevent mehr ausgelöst werden. Wird aus der Framerate des
	 * Eyetrackers(RED: 60Hz) und der vom User festgelegten
	 * {@link #blinktimeMax} berechnet.
	 */
	private static int blinkBufferMax = 50;
	/**
	 * Anzahl der Nullwerte, die minimal Eintreffen müssen, um den Nullwert
	 * Datenstrom noch als Lidschluss zu erkennen. Treffen weniger Nullwerte ein
	 * (z.B. im Falle eines normalen schnellen Lidschlusses des Users) wird noch
	 * kein Lidschlussevent ausgelöst. Wird aus der Framerate des Eyetrackers(RED:
	 * 60Hz) und der vom User festgelegten {@link #blinktimeMin} berechnet.
	 */
	private static int blinkBufferMin = 15;
	/**
	 * Obere Schranke der Lidschlussdauer in ms.
	 */
	private static double blinktimeMax = 500;
	/**
	 * Untere Schranke der Lidschlussdauer in ms.
	 */
	private static double blinktimeMin = 250;
	/**
	 * Maximaler Abstand zwischen den x-Werten aller im CoordinateBuffer
	 * befindlichen {@link Point} Elemente
	 */
	private int maxDistanzeX = 0;
	/**
	 * Maximaler Abstand zwischen den y-Werten aller im CoordinateBuffer
	 * befindlichen {@link Point} Elemente
	 */
	private int maxDistanzeY = 0;
	/**
	 * Maximalster x-Wert aller im CoordinateBuffer befindlichen
	 * {@link Point} Elemente
	 */
	private int maxX = 0;
	/**
	 * Maximalster y-Wert aller im CoordinateBuffer befindlichen
	 * {@link Point} Elemente
	 */
	private int maxY = 0;
	/**
	 * Minimalster x-Wert aller im CoordinateBuffer befindlichen
	 * {@link Point} Elemente
	 */
	private int minX = Integer.MAX_VALUE;
	/**
	 * Minimalster y-Wert aller im CoordinateBuffer befindlichen
	 * {@link Point} Elemente
	 */
	private int minY = Integer.MAX_VALUE;
	/**
	 * Zähler von Nullwerten.
	 */
	private int zeroCounter = 0;
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
		// Wenn Nullwerte kommen, wird die Suche nach einem Lidschlussevent über
		// die erfüllte Bedingung
		// zeroCounter > 0 aktiviert
		if (_x == 0 && _y == 0) {
			zeroCounter++;
			// Keine weitere Analyse wenn nur Nullwerte kommen
			return Eyegesture.Null;
		}
		// alle Koordinaten > 0 werden nun analysiert
		// sobald die Liste ihre festgelegte Kapazität erreicht hat, wird das
		// erste Element entfernt
		if (size() > (bufferSize - 1)) {
			super.pop();
		}
		// smooth values
		double duration = 0.90; // Wichtung
		lastAddedX = (int) (duration * lastAddedX + (1 - duration) * _x);
		lastAddedY = (int) (duration * lastAddedY + (1 - duration) * _y);
		// Hinzufügen des Tupels am Ende der Liste
		super.add(new Point(lastAddedX, lastAddedY));
		
		LinkedList<Point> gazeList = new LinkedList<Point>();
		gazeList.add(new Point(lastAddedX, lastAddedY));
		

		// bei Eintreffen der ersten Koordinate != 0 kann erst ein Lidschlussevent
		// ausgelöst werden
		if ((zeroCounter > blinkBufferMin) && (zeroCounter < blinkBufferMax)) {
			zeroCounter = 0;
			return Eyegesture.Blink;
		} else
			zeroCounter = 0;

		// Erneutes Suchen nach maxX,maxY,minX,minY
		Iterator<Point> iter = listIterator();
		try {
			while (iter.hasNext()) {
				Point t = iter.next();
				if (maxX < t.x)
					maxX = t.x;
				if (maxY < t.y)
					maxY = t.y;
				if (minX > t.x)
					minX = t.x;
				if (minY > t.y)
					minY = t.y;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Erneute Berechnung von maxDistanzeX und maxDistanzeY
		maxDistanzeX = Math.abs(maxX - minX);
		maxDistanzeY = Math.abs(maxY - minY);
		// Erkennen der horizontalen Augenbewegung
		// die max. Distanz zwischen den y-Werten muss kleiner sein, als
		// maxDisplacement
		if (maxDistanzeY < maxDisplacement
		// die max. Distanz der x-Werte muss größer sein als die width minus
		// maxDisplacement
				&& maxDistanzeX > width - maxDisplacement) {
			// Löschen aller Elemente nach erkannter horizontaler Augenbewegung
			reset();
			clear();
			return Eyegesture.HorizontalMove;
		}
		// Erkennen der vertikalen Augenbewegung
		// die max. Distanz zwischen den x-Werten muss kleiner sein, als
		// maxDisplacement
		if (maxDistanzeX < maxDisplacement
		// die max. Distanz der y-Werte muss größer sein als height minus
		// maxDisplacement
				&& maxDistanzeY > height - maxDisplacement) {
			// Löschen aller Elemente nach erkannter vertikaler Augenbewegung
			reset();
			clear();
			return Eyegesture.VerticalMove;
		}
		// Berechnung der aktuellen Dispersion
		// Stellt Fixierung dar, wenn result gegen null get
		int result = Math.abs(maxX - minX) + Math.abs(maxY - minY);

		// Erkennen der Fixierung nur im Falle der maximal erreichten Kapazität
		// der Liste,
		// da sonst zu früh ein Fixierungsevent ausgelöst werden könnte
		if (result < maxDispersion  && size() == bufferSize) {
			reset();
			clear();
			return Eyegesture.Fixation;
		}
		reset();
		
		
		
		// Keine Augengeste ist erkannt worden
		return Eyegesture.Null;
	}

	/**
	 * Setzt die Parameter maxX,maxY,minX und minY zurück.
	 */
	private void reset() {

		minX = Integer.MAX_VALUE;
		minY = Integer.MAX_VALUE;
		maxX = 0;
		maxY = 0;
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

	/**
	 * 
	 * Gibt die maximale Auslenkung (in Pixel) zurück, welche von einer
	 * vertikalen/horizontalen gedachten Linie auf dem Bildschirm des
	 * Stimulus Rechners zugelassen wird, um noch als horizontale/vertikale
	 * Augengeste erkannt zu werden.
	 * 
	 * @return maxDisplacement Maximale Auslenkung (in Pixel), welche
	 *         von einer vertikalen/horizontalen gedachten Linie auf dem
	 *         Bildschirm des Stimulus Rechners zugelassen wird, um noch als
	 *         horizontale/vertikale Augengeste erkannt zu werden.
	 */
	public static int getMaxDisplacement() {
		return maxDisplacement;
	}

	/**
	 * Legt die Auslenkung fest, die der Augenbewegung Spielraum bei
	 * horizontalen/vertikalen Bewegungen lässt, um Augengesten noch als
	 * Horizontal- oder Vertikalbewegungen zu erkennen. Je kleiner, desto weniger
	 * Spielraum wird gegeben und desto exakt horizontal/vertikal müssen diese
	 * Augengesten erfolgen.
	 * 
	 * @param _maxDisplacement
	 *            Wert der Auslenkung, welche von einer vertikalen/horizontalen
	 *            gedachten Linie auf dem Monitor zugelassen wird, um noch als
	 *            horizontale/vertikale Augengeste erkannt zu werden.
	 */
	public static void setMaxDisplacement(int _maxDisplacement) {
		maxDisplacement = _maxDisplacement;
	}

	/**
	 * Gibt die maximale Dispersion (in Pixel) zurück, die festlegt ab wann eine
	 * Fixierung nicht mehr erkannt werden soll.
	 * 
	 * @return Streuungsmaß (in Pixel), das festlegt ab wann eine Fixierung
	 *         nicht mehr erkannt werden soll.
	 */
	public static int getMaxDispersion() {
		return maxDispersion;
	}

	/**
	 * Legt die maximale Dispersion fest, wodurch Augenbewegungen, die eine
	 * größere Dispersion zur Folge haben, nicht als Fixierung erkannt werden.
	 * Eine Fixierung ist gegeben, wenn die Dispersion unterhalb dieser Schranke
	 * ist und gegen Null geht.
	 * 
	 * @param _maxDispersion
	 *            Streuungsmaß, das festlegt, ab wann eine Fixierung nicht
	 *            mehr erkannt werden soll.
	 */
	public static void setMaxDispersion(int _maxDispersion) {
		maxDispersion = _maxDispersion;
	}

	/**
	 * Gibt die minimale Lidschlussdauer in ms zurück, die angibt bis wann ein
	 * Lidschluss ein Event auslöst.
	 * 
	 * @return Maximale Lidschlussdauer in ms. Ein längerer Lidschluss kann kein
	 *         Event mehr auslösen.
	 */
	public static int getBlinktimeMax() {
		return (int) blinktimeMax;
	}

	/**
	 * Gibt die minimale Lidschlussdauer in ms zurück, die angibt ab wann ein
	 * Lidschluss ein Event auslöst.
	 * 
	 * @return Minimale Lidschlussdauer in ms. Ein kürzerer Lidschluss kann kein
	 *         Event auslösen.
	 */
	public static int getBlinktimeMin() {
		return (int) blinktimeMin;
	}

	/**
	 * Berechnet aus der neuen maximalen Lidschlussdauer die Anzahl der Nullwerte
	 * (siehe {@link #blinkBufferMax}), die in dieser Zeit eintreffen sollten.
	 * Ergibt sich aus der framerate des Eyetrackers (RED:60Hz) und der
	 * übergebenen maximalen Lidschlussdauer.
	 * 
	 * @param _blinktime
	 *            Die vom User festgelegte neue Lidschlussdauer in ms, die angibt,
	 *            ab wann ein Lidschluss nicht mehr ein Event auslösen
	 *            soll.
	 */
	public static void setBlinktimeMax(int _blinktime) {
		blinktimeMax = _blinktime;
		// 60Hz mal neuer Lidschlussdauer in ms dividiert durch 1000ms ergibt die
		// Anzahl der Koordinaten,
		// die in dieser Zeit vom Client empfangen werden
		double result = (60.0 * blinktimeMax) / 1000.0;
		blinkBufferMax = (int) Math.round(result);
	}

	/**
	 * Berechnet aus der neuen minimalen Lidschlussdauer die Anzahl der Nullwerte
	 * (siehe {@link #blinkBufferMin}), die in dieser Zeit eintreffen sollten.
	 * Ergibt sich aus der framerate des Eyetrackers (RED:60Hz) und der
	 * übergebenen minimalen Lidschlussdauer.
	 * 
	 * @param _blinktime
	 *            Die vom User festgelegte neue Lidschlussdauer in ms, die angibt,
	 *            ab wann ein Lidschluss bereits ein Event auslösen soll.
	 */
	public static void setBlinktimeMin(int _blinktime) {
		blinktimeMin = _blinktime;
		// 60Hz mal neuer Lidschlussdauer in ms dividiert durch 1000ms ergibt die
		// Anzahl der Koordinaten,
		// die in dieser Zeit vom Client empfangen werden
		double result = (60.0 * blinktimeMin) / 1000.0;
		blinkBufferMin = (int) Math.round(result);
	}

	
}
