/*
 * The RobotHistory Class - Written by Chitescu Denis for the EE402 Module - Assignment 2
 *
*/

package robot;

import java.io.Serializable;
import java.awt.Point;
import java.util.Date;


/**
 * RobotHistory objects will store robot position at a certain time.
 * This types of objects will be used in storing previous positions of an robot and the time at robot was in that position.
 * */

@SuppressWarnings("serial")
public class RobotHistory implements Serializable {
	private Point drawingPoint = null;
	private Point position = null;
	private Date timeOfUpdate = null;

	public RobotHistory(Point drawingPoint, Point position, Date timeOfUpdate) {
		this.position = position;
		this.timeOfUpdate = timeOfUpdate;
		this.drawingPoint = drawingPoint;
	}
	
	public Point getDrawingPoint() { return this.drawingPoint; }
	
	public Point getPosition() {return this.position; }
	
	public Date getTimeOfUpdate() { return this.timeOfUpdate; }
}
