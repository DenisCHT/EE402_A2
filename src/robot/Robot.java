/*
 * The Robot Class - Written by Chitescu Denis for the EE402 Module - Assignment 2
 * 
 */

package robot;

import java.util.Date;
import java.util.Random;
import server.ServerGUICanvas;
import java.awt.Point;

import java.io.*;


/**
 * Robot object will hold all info the server and client need to communicate each other.
 * Each robot must have a unique name.
*/
@SuppressWarnings("serial")
public class Robot implements Serializable{
	
	/**
	 * Robot can be oriented in one of the following cardinal directions:
	 * */
	public static final int HEADING_NORTH = 0;
	public static final int HEADING_EAST = 1;
	public static final int HEADING_SOUTH = 2;
	public static final int HEADING_WEST = 3;
	
	
	
	public static final int STATUS_ACTIVE = 0; //robot will be considerate active if change its position faster than periodically updates are sent.
	public static final int STATUS_PASSIVE = 1;//robot will be considerate PASSIVE if periodically update was sent and robot didn't change its position
	public static final int STATUS_COLLIDED = 2;// if robot collided with another robot
	public static final int STATUS_HIBERNATE = 3; //assigned at robot object instantiation - before being sent to the Server 
	public static final int STATUS_STUCK = 4; // if robot cannot move because of canvas dimensions
	
	/**
	 * I am gonna use the velocity of a robot to determine how many positions to move it by a single move command.
	 * I considerate to by a robot position a space on canvas with the same size of the object.
	 * */
	public static final int DEFAULT_VELOCITY = 1;//will determine how many positions a robot will be moved.
	
	//configurable robot dimensions range[pixels]
	public static final int ROBOT_MIN_SIZE = 15; //min value for @sideLegth state
	public static final int ROBOT_MAX_SIZE = 65; //max value for @sideLength state
	
	/**
	 * @SQRT_2 - used in computing diameter of safety-margin circle around robot. Hardcoded for faster computations in case of many robots.
	 * 
	 * @MAX_SAFETY_CIRCLE_DIAMETER - the maximum diameter of the circle surrounding the object(collision safety margin). 
	 * */
	public static final double SQRT_2 = 1.41421356237; 
	public static final int MAX_SAFETY_CIRCLE_DIAMETER = (int)Math.ceil(Robot.ROBOT_MAX_SIZE * SQRT_2) * 2;
	
	
	public static final int POSITIONS_HISTORY_ARRAY_MIN_CAPACITY = 3; //minimum number of previous positions that will be retained
	public static final int POSITIONS_HISTORY_ARRAY_MAX_CAPACITY = 10; //maximum number of previous positions that will be retained
	
	/**
	 * Assuming that the canvas, on server side, is ServerGUICanvas.CANVAS_WIDTH x ServerGUICanvas.CANVAS_HEIGHT size and 
	 * biggest robot allowed have ROBOT_MAX_SIZE x ROBOT_MAX_SIZE size, I have to determine  maximum x and y coords 
	 * from where canvas can draw and objects will still fit on canvas
	 * */
	private int xCanvasThreshold, yCanvasThreshold;
	
	/**
	 * difference from robot center till head edge of the robot.
	 *		- robot center = robot position(coordinates)
	 * */
	private int headOffset; 
	private int positionsHistoryArrayCapacityToDisplay; //number of previous positions that will be displayed, between *_MIN_CAPACITY and *_MAX_CAPACITY. Parameter adjustable from server GUI.
	private int positionsHistoryArrayIndex = 0; //used for managing positionsHistoryArray
	private Point prevPosition = null; // used for storing the actual position before moving. Later used to verify if object moved after last command or not. Part of robot prev positions. 
	private Point prevDrawingPoint = null; //used for storing the actual drawing point before moving. Part robot previous positions.
	private Random rand = null;
	private Point drawingPoint = null; // coords from where canvas will draw the square from which robot shape will be carved
	private int collisionSafetyMarginCircleDiameter; //the diameter of the circle circumscribed to the robot
	private Point collisionSafetyMarginCircleDrawingPoint = null; //coords from where the circle surrounding the robot will be drawn
	private String[] rolesPool = {"policeman", "firefighter", "doctor", "engineer", "driver", "accountant", "chef", "plumber"}; //each robot will have one of this roles
	private RobotHistory[] positionsHistoryArray;
	
	
	//Robot main properties:
	/**
	 * @param sideLength
	 * Robot will look like an arrow carved out from a square with length of the sides = sideLength.
	 * Randomly generated at robot object instantiation.
	 * */
	private int sideLength;
	private String name; //name of the robot - set from command-line arguments
	private int direction; //direction of the robot[North,South,East,West] - randomly selected 
	private String role; //randomly selected, from rolesPool, at robot object instantiation
	private Point position; //is center of square from which robot shape is carved. Will be interpreted as center point of the robot
	private int velocity; //set by client-GUI at object creation. 
	private int status; //status of the robot - by default is STATUS_HIBERNATE,at robot object instantiation. Updated later by server
	private Date timeOfUpdate; //null at robot object instantiation - current date will be assigned when robot arrive to the Server
	
	
	

	public Robot(String name) {
		this.rand = new Random();
		
		this.name = name;
		this.velocity = Robot.DEFAULT_VELOCITY;
		this.status = Robot.STATUS_HIBERNATE;
		this.timeOfUpdate = null;
		this.positionsHistoryArray = new RobotHistory[Robot.POSITIONS_HISTORY_ARRAY_MAX_CAPACITY];
		this.positionsHistoryArrayCapacityToDisplay = Robot.POSITIONS_HISTORY_ARRAY_MIN_CAPACITY;
		
		this.init();
	}
	
	/**
	 * getter methods for each of robot states
	 * */
	public String getName() { return this.name; }
	
	public String getRole() {return this.role; }
	
	public int getSize() { return this.sideLength; }
	
	public int getCollisionSafetyMarginCircleDiameter() { return this.collisionSafetyMarginCircleDiameter; }
	
	public int getDirectionAsInt() { return this.direction; }
	
	public Point getPosition() { return this.position; }
	
	public int getVelocity() {return this.velocity; }
	
	public Date getTimeOfUpdate() { return this.timeOfUpdate; }
	
	public Point getDrawingPoint() { return this.drawingPoint; }
	
	public RobotHistory[] getPositionsHistoryArray() { return this.positionsHistoryArray; }
	
	public int getStatusAsInt() { return this.status; }
	
	public Point getCollisionSafetyMarginCircleDrawingPoint() { return this.collisionSafetyMarginCircleDrawingPoint; }
	
	public int getPositionsHistoryArrayCapacity() { return this.positionsHistoryArrayCapacityToDisplay; }
	
	public String getDirectionAsString() {
		String directionRes = null;
		if( this.direction == HEADING_NORTH ) {
			directionRes = "NORTH";
		}
		else if( this.direction == HEADING_EAST ) {
			directionRes = "EAST";
		}
		else if( this.direction == HEADING_SOUTH ) {
			directionRes = "SOUTH";
		}
		else if( this.direction == HEADING_WEST ) {
			directionRes = "WEST";
		}
		return directionRes;
	}
	
	public String getStatusAsString() { 
		String statusRes = null;
		if ( this.status == STATUS_ACTIVE ) {
			statusRes = "ACTIVE";
		}
		else if ( this.status == STATUS_PASSIVE ) {
			statusRes = "PASSIVE";
		}
		else if ( this.status == STATUS_COLLIDED ) {
			statusRes = "COLLIDED";
		}
		else if ( this.status == STATUS_HIBERNATE ) {
			statusRes = "HIBERNATE";
		}
		else if( this.status == STATUS_STUCK ) {
			statusRes = "STUCK";
		}
		return statusRes;
	}
	

	
	
	/**
	 * setter methods only for those states that have to be change outside of this class
	 * */
	public void setPosition(int x, int y) { this.position.x = x; this.position.y = y; }
	
	public void setVelocity(int velocity) { this.velocity = velocity; }
	
	public void setTimeOfUpdate(Date timeOfUpdate) { this.timeOfUpdate = timeOfUpdate; }
	
	public void setStatus(int status) { this.status = status; }
	
	public void setPositionsHistoryArrayCapacity( int positionsHistoryArrayCapacity ) { this.positionsHistoryArrayCapacityToDisplay = positionsHistoryArrayCapacity; }
	
	public void setCollisionSafetyMarginCircleDiameter(int diameter) { 

		this.collisionSafetyMarginCircleDrawingPoint.x = collisionSafetyMarginCircleDrawingPoint.x - ( diameter - this.collisionSafetyMarginCircleDiameter ) ;	
		this.collisionSafetyMarginCircleDrawingPoint.y = collisionSafetyMarginCircleDrawingPoint.y - ( diameter - this.collisionSafetyMarginCircleDiameter ) ;
	
		this.collisionSafetyMarginCircleDiameter = diameter + (diameter - this.collisionSafetyMarginCircleDiameter);
	}
	
	
	/**
	 * default initialization of robot states
	 * */
	private void init() {
		this.sideLength = this.rand.nextInt((ROBOT_MAX_SIZE - ROBOT_MIN_SIZE)+1) + ROBOT_MIN_SIZE; 
		this.collisionSafetyMarginCircleDiameter = getCollisionSafetyCircleMinDiameter();
		this.xCanvasThreshold = ServerGUICanvas.CANVAS_WIDTH - this.sideLength;
		this.yCanvasThreshold = ServerGUICanvas.CANVAS_HEIGHT - this.sideLength;
		this.drawingPoint = new Point(this.rand.nextInt(xCanvasThreshold), this.rand.nextInt(yCanvasThreshold));
		this.position = new Point( (this.drawingPoint.x + sideLength/2) , (this.drawingPoint.y + sideLength/2) );
		
		this.collisionSafetyMarginCircleDrawingPoint = new Point( (this.drawingPoint.x - ((this.getCollisionSafetyCircleMinDiameter()-(this.sideLength)) / 2)), 
																	(this.drawingPoint.y - ((this.getCollisionSafetyCircleMinDiameter()-(this.sideLength)) / 2)) );
		
		this.headOffset = this.sideLength / 2;
		this.direction = this.rand.nextInt(4); 
		this.role = this.rolesPool[this.rand.nextInt(rolesPool.length)];
		
	}
	
	
	/**
	 * @velocity - it is the speed adjustable from client GUI.
	 * Speed represent how many positions the robot will be moved for a single button push(forward button). 
	 * Drawing point and prevPosition will be updated too.
	 * */
	public boolean moveForward(int velocity) {
		int step = velocity*sideLength;
		this.prevPosition = new Point(this.position.x, this.position.y);
		this.prevDrawingPoint = new Point(this.drawingPoint.x, this.drawingPoint.y);
		if( (this.direction == Robot.HEADING_NORTH) && (((this.position.y - this.headOffset) - step) >= 0) ) {
			this.position.y -= step;
			this.drawingPoint.y -= step; 
			this.collisionSafetyMarginCircleDrawingPoint.y -= step;
		}
		else if( (this.direction == Robot.HEADING_EAST) && (((this.position.x + this.headOffset) + step) <= ServerGUICanvas.CANVAS_WIDTH) ) {
			this.position.x += step;
			this.drawingPoint.x += step;
			this.collisionSafetyMarginCircleDrawingPoint.x += step;
		}
		else if( (this.direction == Robot.HEADING_SOUTH) && (((this.position.y + this.headOffset) + step) <= ServerGUICanvas.CANVAS_HEIGHT) ) {
			this.position.y += step;
			this.drawingPoint.y += step;
			this.collisionSafetyMarginCircleDrawingPoint.y +=step;
		}
		else if( this.direction == Robot.HEADING_WEST && (((this.position.x - this.headOffset) - step) >= 0) ) {
			this.position.x -= step; 
			this.drawingPoint.x -= step;
			this.collisionSafetyMarginCircleDrawingPoint.x -= step;
		}
		return this.addRobotPositionHistory(this.prevDrawingPoint, this.prevPosition, this.timeOfUpdate); //before moving, store current position
	}
	
	
	/**
	 * @velocity - it is the speed adjustable from client GUI.
	 * Velocity represent how many positions the robot will be moved for a single button push(backward button).
	 * 		- a position has the same size as the robot.
	 * By moving backward, robot head direction will not be changed.
	 * Drawing point and prevPosition will be updated too.
	 * */
	public boolean moveBackward(int velocity) { 
		int step = velocity*sideLength;
		this.prevPosition = new Point(this.position.x, this.position.y);
		this.prevDrawingPoint = new Point(this.drawingPoint.x, this.drawingPoint.y);
		if( (this.direction == Robot.HEADING_NORTH) && (((this.position.y + this.headOffset) + step) <= ServerGUICanvas.CANVAS_HEIGHT) ) {
			this.position.y += step;
			this.drawingPoint.y += step;
			this.collisionSafetyMarginCircleDrawingPoint.y += step;
		}
		else if( (this.direction == Robot.HEADING_EAST) && (((this.position.x - this.headOffset) - step) >= 0) ) {
			this.position.x -= step;
			this.drawingPoint.x -= step;
			this.collisionSafetyMarginCircleDrawingPoint.x -= step;
		}
		else if( (this.direction == Robot.HEADING_SOUTH) && (((this.position.y - this.headOffset) - step) >= 0) ) {
			this.position.y -= step;
			this.drawingPoint.y -= step;
			this.collisionSafetyMarginCircleDrawingPoint.y -= step;
		}
		else if( this.direction == Robot.HEADING_WEST && (((this.position.x + this.headOffset) + step) <= ServerGUICanvas.CANVAS_WIDTH) ) {
			this.position.x += step;
			this.drawingPoint.x += step;
			this.collisionSafetyMarginCircleDrawingPoint.x += step;
		}
		return this.addRobotPositionHistory(this.prevDrawingPoint, this.prevPosition, this.timeOfUpdate); //before moving, store current position
	}

	
	/**
	 * By rotating robot to the left, its cardinal coordinate will be changed ( moved counter-clock wise )
	 * */
	public void rotateLeft() {
		if( this.direction == Robot.HEADING_NORTH ) {
			this.direction = Robot.HEADING_WEST;
		}
		else if( this.direction == Robot.HEADING_WEST ) {
			this.direction = Robot.HEADING_SOUTH;
		}
		else if( this.direction == Robot.HEADING_SOUTH ) {
			this.direction = Robot.HEADING_EAST;
		}
		else if( this.direction == Robot.HEADING_EAST ) {
			this.direction = Robot.HEADING_NORTH;
		}
	}
	
	/**
	 * By rotating robot to the right, its cardinal coordinate will be changed ( moved clock wise )
	 * */
	public void rotateRight() {
		if( this.direction == Robot.HEADING_NORTH ) {
			this.direction = Robot.HEADING_EAST;
		}
		else if( this.direction == Robot.HEADING_EAST ) {
			this.direction = Robot.HEADING_SOUTH;
		}
		else if( this.direction == Robot.HEADING_SOUTH ) { 
			this.direction = Robot.HEADING_WEST;
		}
		else if( this.direction == Robot.HEADING_WEST ) {
			this.direction = Robot.HEADING_NORTH;
		}
	}
	
	
	
	/**
	 * This robot states will be updated according to @newRobot states.
	 * Method used when client-server communicate to each other.
	 */
	public void updateRobotStates(Robot newRobot) {
		this.sideLength = newRobot.getSize();
		this.name = newRobot.getName(); 
		this.direction = newRobot.getDirectionAsInt();  
		this.role = newRobot.getRole(); 
		this.position = newRobot.getPosition(); 
		this.velocity = newRobot.getVelocity(); 
		this.status = newRobot.getStatusAsInt(); 
		this.timeOfUpdate= newRobot.getTimeOfUpdate(); 
		this.collisionSafetyMarginCircleDiameter = newRobot.getCollisionSafetyMarginCircleDiameter(); 
		this.positionsHistoryArray = newRobot.getPositionsHistoryArray();
		this.drawingPoint = newRobot.getDrawingPoint();
		this.collisionSafetyMarginCircleDrawingPoint = newRobot.getCollisionSafetyMarginCircleDrawingPoint();
	}
	
	
		
	
	
	/**
	 * This method will be used in keeping robot previous positions and the time when it was there.
	 * The method will indicate when object has moved.
	 * */
	private boolean addRobotPositionHistory(Point dp, Point p, Date d) {
		
		if( this.position.x != p.x || this.position.y != p.y ) {		
		
			RobotHistory rh = new RobotHistory(dp, p,d);
			
			if( this.positionsHistoryArrayIndex < Robot.POSITIONS_HISTORY_ARRAY_MAX_CAPACITY ) {
				this.positionsHistoryArray[this.positionsHistoryArrayIndex] = rh;
				this.positionsHistoryArrayIndex++;
			}
			else {
				
				for(int i=0; i<Robot.POSITIONS_HISTORY_ARRAY_MAX_CAPACITY - 1; i++) {
					this.positionsHistoryArray[i] = this.positionsHistoryArray[i+1];
				}
	
				this.positionsHistoryArray[this.positionsHistoryArrayIndex - 1] = rh;
			}
			return true;
		}
		return false;
	}
	
	
	/**
	 * the minimum diameter for the circle around the object(collision safety margin)
	 * */
	private int getCollisionSafetyCircleMinDiameter() {
		return (int)Math.ceil(this.sideLength * SQRT_2);
	}
	
}








