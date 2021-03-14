package com.iView.client.roomba;

import com.iView.gui.StatusBar;
import com.iView.network.roomba.ConnectionRoombaObject;

public abstract class ARoomba {

	/** turns on/off various debugging messages */
	public boolean debug = true;

	/** distance between wheels on the roomba, in millimeters */
	public static final int wheelbase = 258;
	/** mm/deg is circumference distance divided by 360 degrees */
	public static final float millimetersPerDegree = (float) (wheelbase * Math.PI / 360.0);
	/** mm/rad is a circumference distance divied by two pi */
	public static final float millimetersPerRadian = (float) (wheelbase / 2);

	/** default speed for movement operations if speed isn't specified */
	public static float defaultSpeed = (float) 200.0;

	private float maxSpeed;

	/** default update time in ms for auto sensors update */
	public static final int defaultSensorsUpdateTime = 200;

	/** current mode, if known */
	int mode;

	/** current speed for movement operations that don't take a speed */
	public float currentSpeed = defaultSpeed;

	/** connected to a serial port or not, not necessarily to roomba */
	boolean connected = false;

	public ARoomba() {
		connected = false;
		mode = MODE_UNKNOWN;
	}

	/**
	 * Connect to a {@link} ConnectionRoombaObject
	 * 
	 * @return true on successful connect, false otherwise
	 */
	public abstract boolean connect(ConnectionRoombaObject roombaConnection);

	/**
	 * Disconnect from a port, clean up any memory in use
	 */
	public abstract void disconnect();

	/**
	 * Send given byte array to Roomba.
	 * 
	 * @param bytes
	 *            byte array of ROI commands to send
	 * @return true on successful send
	 */
	public abstract boolean send(byte[] bytes);

	/**
	 * Send a single byte to the Roomba (defined as int because of java signed
	 * bytes)
	 * 
	 * @param b
	 *            byte of an ROI command to send
	 * @return true on successful send
	 */
	public abstract boolean send(int b);

	/**
	 * Put Roomba in safe mode. As opposed to full mode. Safe mode is the
	 * preferred working state when playing with the Roomba as it provides some
	 * measure of autonomous self-preservation if it encounters a cliff or is
	 * picked up If that happens it goes into passive mode and must be
	 * 'reset()'.
	 * 
	 * @see #reset()
	 */
	public void startup() {
		logmsg("startup");
		currentSpeed = defaultSpeed;
		start();
		safe();
	}

	/**
	 * Reset Roomba after a fault. This takes it out of whatever mode it was in
	 * and puts it into safe mode.
	 * 
	 * @see #startup()
	 * @see #updateSensors()
	 */
	public void reset() {
		logmsg("reset");
		stop();
		startup();
	}

	/** Send START command */
	public void start() {
		logmsg("start");
		mode = MODE_PASSIVE;
		send(START);
	}

	/** Send SAFE command */
	public void safe() {
		logmsg("safe");
		mode = MODE_SAFE;
		send(SAFE);
	}

	/** Send FULL command */
	public void full() {
		logmsg("full");
		mode = MODE_FULL;
		send(FULL);
	}

	/**
	 * Power off the Roomba. Once powered off, the only way to wake it is via
	 * wakeup() (if implemented) or via a physically pressing the Power button
	 * 
	 * @see #wakeup()
	 */
	public void powerOff() {
		logmsg("powerOff");
		mode = MODE_UNKNOWN;
		send(POWER);
	}

	/** Send the SPOT command */
	public void spot() {
		logmsg("spot");
		mode = MODE_PASSIVE;
		send(SPOT);
	}

	/** Send the CLEAN command */
	public void clean() {
		logmsg("clean");
		mode = MODE_PASSIVE;
		send(CLEAN);
	}

	/** Send the max command */
	public void max() {
		logmsg("max");
		mode = MODE_PASSIVE;
		send(MAX);
	}

	//
	// basic functions
	//
	/**
	 * A simple pause function. Makes the thread block with Thread.sleep()
	 * 
	 * @param millis
	 *            number of milliseconds to wait
	 */
	public void pause(int millis) {
		try {
			Thread.sleep(millis);
		} catch (Exception e) {
		}
	}

	//
	// higher-level functions
	//

	/**
	 * Stop Rooomba's motion. Sends drive(0,0)
	 */
	public void stop() {
		drive(0, 0);
	}


	/** Set speed for movement commands */
	public void setCurrentSpeed(int s) {
		currentSpeed = Math.abs(s);
	}

	/** Get speed for movement commands */
	public float getCurrentSpeed() {
		return currentSpeed;
	}

	/**
	 * Go straight at the current speed for a specified distance. Positive
	 * distance moves forward, negative distance moves backward. This method
	 * blocks until the action is finished.
	 * 
	 * @param distance
	 *            distance in millimeters, positive or negative
	 */
	public void goStraight(int distance) {
		float pausetime = Math.abs(distance / currentSpeed); // mm/(mm/sec) =
																// sec
		if (distance > 0)
			goStraightAt(currentSpeed);
		else
			goStraightAt(-currentSpeed);
		pause((int) (pausetime * 1000));
		stop();
	}

	/**
	 * @param distance
	 *            distance in millimeters, positive
	 */
	public void goForward(int distance) {
		if (distance < 0)
			return;
		goStraight(distance);
	}

	/**
	 * @param distance
	 *            distance in millimeters, positive
	 */
	public void goBackward(int distance) {
		if (distance < 0)
			return;
		goStraight(-distance);
	}

	/**
	 *
	 */
	public void turnLeft() {
		turn(129);
	}

	public void turnRight() {
		turn(-129);
	}

	public void turn(int radius) {
		drive((int) currentSpeed, radius);
	}

	/**
	 * Spin right or spin left a particular number of degrees
	 * 
	 * @param angle
	 *            angle in degrees, positive to spin left, negative to spin
	 *            right
	 */
	public void spin(float angle) {
		if (angle > 0)
			spinLeft(angle);
		else if (angle < 0)
			spinRight(-angle);
	}

	/**
	 * Spin right the current speed for a specified angle
	 * 
	 * @param angle
	 *            angle in degrees, positive
	 */
	public void spinRight(float angle) {
		if (angle < 0)
			return;
		float pausetime = Math.abs(millimetersPerDegree * angle / currentSpeed);
		spinRightAt(currentSpeed);
		pause((int) (pausetime * 1000));
		stop();
	}

	/**
	 * Spin left a specified angle at a specified speed
	 * 
	 * @param angle
	 *            angle in degrees, positive
	 */
	public void spinLeft(float angle) {
		if (angle < 0)
			return;
		// float pausetime =
		float pausetime = Math.abs(millimetersPerDegree * angle / currentSpeed);
		spinLeftAt(currentSpeed);
		pause((int) (pausetime * 1000));
		stop();
	}

	/**
	 * Spin in place anti-clockwise, at the current speed
	 */
	public void spinLeft() {
		spinLeftAt(currentSpeed);
	}

	/**
	 * Spin in place clockwise, at the current speed
	 */
	public void spinRight() {
		spinRightAt(currentSpeed);
	}

	/**
	 * Spin in place anti-clockwise, at the current speed.
	 * 
	 * @param aspeed
	 *            speed to spin at
	 */
	public void spinLeftAt(float aspeed) {
		drive(Math.abs((int) aspeed), 0x0001);
	}

	/**
	 * Spin in place clockwise, at the current speed.
	 * 
	 * @param aspeed
	 *            speed to spin at, positive
	 */
	public void spinRightAt(float aspeed) {
		drive(Math.abs((int) aspeed), 0xffff);
	}

	//
	// mid-level movement, no blocking, parameterized by speed, not distance
	//

	/**
	 * Go straight at a specified speed. Positive is forward, negative is
	 * backward
	 * 
	 * @param velocity
	 *            velocity of motion in mm/sec
	 */
	public void goStraightAt(float velocity) {
		if (velocity > 500)
			velocity = 500;
		if (velocity < -500)
			velocity = -500;
		drive((int) velocity, 0x8000);
	}

	/**
	 * Go forward the current (positive) speed
	 */
	public void goForward() {
		goStraightAt(Math.abs(currentSpeed));
	}

	/**
	 * Go backward at the current (negative) speed
	 */
	public void goBackward() {
		goStraightAt(-Math.abs(currentSpeed));
	}

	/**
	 * Go forward at a specified speed
	 */
	public void goForwardAt(float aspeed) {
		if (aspeed < 0)
			return;
		goStraightAt(aspeed);
	}

	/**
	 * Go backward at a specified speed
	 */
	public void goBackwardAt(float aspeed) {
		if (aspeed < 0)
			return;
		goStraightAt(-aspeed);
	}

	//
	// low-level movement and action
	//

	/**
	 * Move the Roomba via the low-level velocity + radius method. Low-level
	 * command.
	 * 
	 * @param velocity
	 *            speed in millimeters/second, positive forward, negative
	 *            backward
	 * @param radius
	 *            radius of turn in millimeters
	 */
	public void drive(int velocity, int radius) {
		byte cmd[] = { (byte) DRIVE, (byte) (velocity >>> 8), (byte) (velocity & 0xff), (byte) (radius >>> 8), (byte) (radius & 0xff) };
		send(cmd);
	}


	/**
	 * Move the Roomba via the speed of the two wheels
	 * 
	 * @param velocityRightWheel
	 *            speed in millimeters/second, positive forward, negative
	 *            backward
	 * @param velocityLeftWheel
	 *            radius of turn in millimeters
	 */
	public void driveDirect(int velocityRightWheel, int velocityLeftWheel) {
		byte cmd[] = { (byte) DRIVEWHEELS, (byte) (velocityRightWheel >>> 8), (byte) (velocityRightWheel & 0xff),
				(byte) (velocityLeftWheel >>> 8), (byte) (velocityLeftWheel & 0xff) };
		logmsg("drive direct: " + hex(cmd[0]) + "," + hex(cmd[1]) + "," + hex(cmd[2]) + "," + hex(cmd[3]) + ","
				+ hex(cmd[4]));
		send(cmd);
	}

	/**
	 * Returns current connected state. It's up to subclasses to ensure this
	 * variable is correct.
	 * 
	 * @return current connected state
	 */
	public boolean connected() {
		return connected;
	}

	/** current ROI mode RoombaComm thinks the Roomba is in */
	public int mode() {
		return mode;
	}

	/** mode as String */
	public String modeAsString() {
		String s = null;
		switch (mode) {
		case MODE_UNKNOWN:
			s = "unknown";
			break;
		case MODE_PASSIVE:
			s = "passive";
			break;
		case MODE_SAFE:
			s = "safe";
			break;
		case MODE_FULL:
			s = "full";
			break;
		}
		return s;
	}

	// possible modes
	public static final int MODE_UNKNOWN = 0;
	public static final int MODE_PASSIVE = 1;
	public static final int MODE_SAFE = 2;
	public static final int MODE_FULL = 3;

	// Roomba ROI opcodes
	// these should all be bytes, but Java bytes are signed
	public static final int START = 128; // 0
	public static final int BAUD = 129; // 1
	public static final int CONTROL = 130; // 0
	public static final int SAFE = 131; // 0
	public static final int FULL = 132; // 0
	public static final int POWER = 133; // 0
	public static final int SPOT = 134; // 0
	public static final int CLEAN = 135; // 0
	public static final int MAX = 136; // 0
	public static final int DRIVE = 137; // 4
	public static final int MOTORS = 138; // 1
	public static final int LEDS = 139; // 3
	public static final int SONG = 140; // 2N+2
	public static final int PLAY = 141; // 1
	public static final int SENSORS = 142; // 1
	public static final int DOCK = 143; // 0
	public static final int PWMMOTORS = 144; // 3
	public static final int DRIVEWHEELS = 145; // 4
	public static final int DRIVEPWM = 146; // 4
	public static final int STREAM = 148; // N+1
	public static final int QUERYLIST = 149; // N+1
	public static final int STOPSTARTSTREAM = 150; // 1
	public static final int SCHEDULINGLEDS = 162; // 2
	public static final int DIGITLEDSRAW = 163; // 4
	public static final int DIGITLEDSASCII = 164; // 4
	public static final int BUTTONSCMD = 165; // 1
	public static final int SCHEDULE = 167; // n
	public static final int SETDAYTIME = 168; // 3

	//
	// utility methods
	//

	/**
	 *
	 */
	static public final short toShort(byte hi, byte lo) {
		return (short) ((hi << 8) | (lo & 0xff));
	}

	/**
	 *
	 */
	static public final int toUnsignedShort(byte hi, byte lo) {
		return (int) (hi & 0xff) << 8 | lo & 0xff;
	}

	public String hex(byte b) {
		return Integer.toHexString(b & 0xff);
	}

	public String hex(int i) {
		return Integer.toHexString(i);
	}

	public String binary(int i) {
		return Integer.toBinaryString(i);
	}

	/**
	 * just a little debug
	 */
	public void logmsg(String msg) {
		if (debug)
			System.err.println("Roomba (" + System.currentTimeMillis() + "):" + msg);
		StatusBar.getInstance().setText("Roomba (" + System.currentTimeMillis() + "):" + msg);
	}

	/**
	 * @return the maxSpeed
	 */
	public float getMaxSpeed() {
		return maxSpeed;
	}

	/**
	 * @param maxSpeed
	 *            the maxSpeed to set
	 */
	public void setMaxSpeed(int maxSpeed) {
		if (maxSpeed >= 200) {
			defaultSpeed = 200;
			return;
		}
		if (maxSpeed == 0) {return;
		}
		defaultSpeed = Math.abs(maxSpeed);
	}

}
