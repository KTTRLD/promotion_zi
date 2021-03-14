package com.iView.command;

import com.iView.client.roomba.ARoomba;
import com.iView.data.ObjectLibrary.CommandAction;

public class Command {

	private float rightWheelRatio;
	private float leftWheelRatio;
	private float speedAngleSpeedRatio;

	private float rightWheel;
	private float leftWheel;
	private float angle;
	
	private final int DEFAULTANGLE = 15;
	private final int DEFAULTDISTANCE = 50;

	private ARoomba mobileRoboter;
	private CommandAction commandAction;
	private boolean test = true;
	private long timeStamp;
	private float angleSpeed;
	private final float ANGLERATIO=(float) 0.4;

	public Command(CommandAction command) {
		this.commandAction = command;
	}

	public void setRatio(double rightWheelRatio, double leftWheelRatio) {
		this.setRightWheelRatio((float) rightWheelRatio);
		this.setLeftWheelRatio((float) leftWheelRatio);
	}

	/**
	 * @return the rightWheel Velocity
	 */
	public float getRightWheel() {
		return rightWheel;
	}

	/**
	 * @param rightWheel
	 *            the rightWheel Velocity to set
	 */
	public void setRightWheel(float rightWheel) {
		this.rightWheel = rightWheel;
	}

	/**
	 * @return the leftWheel Velocity
	 */
	public float getLeftWheel() {
		return leftWheel;
	}

	/**
	 * @param leftWheel
	 *            the leftWheel Velocity to set
	 */
	public void setLeftWheel(float leftWheel) {
		this.leftWheel = leftWheel;
	}

	/**
	 * @return the command
	 */
	public CommandAction getCommandAction() {
		return commandAction;
	}

	/**
	 * @param command
	 *            the command to set
	 */
	public void setCommandAction(CommandAction command) {
		this.commandAction = command;
	}

	/**
	 * Get called from a Navigator
	 */
	public void perform() {

		switch (commandAction) {
		case Forward:
			if (test) {
				mobileRoboter.goForward();
			}
			System.out.println(this.toString());
			break;
		case ForwardDistance:
			if (test) {
				mobileRoboter.goForward(DEFAULTDISTANCE);
			}
			System.out.println(this.toString());
			break;
		case ForwardSpeed:
			if (test) {
				// Calculate the ratio of Screen to Roomba speed
				calculateSpeed();
				mobileRoboter.goForwardAt(speedAngleSpeedRatio);
			}
			System.out.println(this.toString());
			break;
		case Backward:
			if (test) {
				mobileRoboter.goBackward();
			}
			System.out.println(this.toString());
			break;
		case BackwardDistance:
			if (test) {
				mobileRoboter.goBackward(DEFAULTDISTANCE);
			}
			System.out.println(this.toString());
			break;
		case BackwardSpeed:
			if (test) {
				// Calculate the ratio of screen to Roomba speed
				calculateSpeed();
				mobileRoboter.goBackwardAt(speedAngleSpeedRatio);
			}
			System.out.println(this.toString());
		case SpinLeft:
			if (test) {
				mobileRoboter.spinLeft();
			}
			System.out.println(this.toString());
			break;
		case SpinLeftAngle:
			if (test) {
				mobileRoboter.spinLeft(DEFAULTANGLE);
			}
			System.out.println(this.toString());
			break;
		case SpinLeftAt:
			if (test) {
				calculateSpeed();
				mobileRoboter.spinLeftAt(angleSpeed);
			}
			System.out.println(this.toString());
			break;
		case SpinRight:
			if (test) {
				mobileRoboter.spinRight();
			}
			System.out.println(this.toString());
			break;
		case SpinRightAngle:
			if (test) {
				mobileRoboter.spinRight(DEFAULTANGLE);
			}
			System.out.println(this.toString());
			break;
		
		case SpinRightAt:
			if (test) {
				calculateSpeed();
				mobileRoboter.spinRightAt(angleSpeed);
			}
			System.out.println(this.toString());
			break;
		case DriveDirect:
			if (test) {
				calculateSpeed();
				mobileRoboter.driveDirect((int) rightWheel, (int) leftWheel);
			}
//			calculateSpeed();
			System.out.println(this.toString());
			break;
		case Stop:
			if (test) {
				mobileRoboter.stop();
			}
			System.out.println(this.toString());
			break;
		case Spin:
			if (test) {
				calculateSpeed();
				mobileRoboter.spin(angle);
			}
			System.out.println(this.toString());
			break;
		default:
			break;
		}

	}

	private void calculateSpeed() {

		float defaultSpeed = ARoomba.defaultSpeed;
		angleSpeed = (float) (defaultSpeed * ANGLERATIO);

		leftWheel = (leftWheelRatio * defaultSpeed);
		rightWheel = (rightWheelRatio * defaultSpeed);

	}

	/**
	 * A mobileRoboter need to be set befor perform() methode will be call
	 * 
	 * @see perfom
	 * @param mobileRoboter
	 */
	public void setMobileRomoter(ARoomba mobileRoboter) {
		if (mobileRoboter != null) {
			this.mobileRoboter = mobileRoboter;
		}
	}

	/**
	 * @return the angle
	 */
	public float getAngle() {
		return angle;
	}

	/**
	 * @param angle
	 *            the angle to set
	 */
	public void setAngle(int angle) {
		this.angle = angle;
	}

	@Override
	public String toString() {
		return commandAction.toString();
	}

	public void setSpeed(int speed) {

		this.speedAngleSpeedRatio = speed;

	}

	/**
	 * @return the timeStamp
	 */
	public long getTimeStamp() {
		return timeStamp;
	}

	/**
	 * @param timeStamp
	 *            the timeStamp to set
	 */
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * @return the rightWheelRatio
	 */
	public float getRightWheelRatio() {
		return rightWheelRatio;
	}

	/**
	 * @param rightWheelRatio
	 *            the rightWheelRatio to set
	 */
	public void setRightWheelRatio(float rightWheelRatio) {
		this.rightWheelRatio = rightWheelRatio;
	}

	/**
	 * @return the leftWheelRatio
	 */
	public float getLeftWheelRatio() {
		return leftWheelRatio;
	}

	/**
	 * @param leftWheelRatio
	 *            the leftWheelRatio to set
	 */
	public void setLeftWheelRatio(float leftWheelRatio) {
		this.leftWheelRatio = leftWheelRatio;
	}

}
