/*
 * The ServerGUICanvas Class - Written by Chitescu Denis for the EE402 Module - Assignment 2
 * */

package server;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import robot.Robot;
import robot.RobotHistory;

@SuppressWarnings("serial")

public class ServerGUICanvas extends Canvas implements MouseListener{
	
	//canvas dimensions[pixels]
	public static final int CANVAS_WIDTH = 720;
	public static final int CANVAS_HEIGHT = 720;
		
	
	private boolean showPrevPositions = false; //will determine if previous positions of the robots will be drawn. By default they will now be shown.
	private ServerGUI serverFrontend = null; //a handler to the server GUI
	private ConcurrentMap<Robot, Color> robotsMap = null; //all robots that are connected to the server and have to be managed.
	
 	
	
	public ServerGUICanvas(ServerGUI serverFrontend , ConcurrentMap<Robot, Color> robotsMap) {
		this.setSize(ServerGUICanvas.CANVAS_WIDTH, ServerGUICanvas.CANVAS_HEIGHT);
		this.serverFrontend = serverFrontend;
		this.robotsMap = robotsMap;
		
		this.addMouseListener(this);
	}
	
	
	/**
	 * Each of the connected robots will be drawn according to its cardinal position.
	 * According to @showPrevPositions state value, previous positions will be drawn or not.
	 * */
	public void paint(Graphics g) {
		for( Map.Entry<Robot, Color> entry : robotsMap.entrySet() ) { 
			g.setColor(entry.getValue());
			
			if( entry.getKey().getDirectionAsInt() == Robot.HEADING_NORTH ) {
				this.drawRobotHeadingNorth(g, entry.getKey()); 
			}
			else if( entry.getKey().getDirectionAsInt() == Robot.HEADING_EAST ) {
				this.drawRobotHeadingEast(g, entry.getKey()); 
			}
			else if( entry.getKey().getDirectionAsInt() == Robot.HEADING_SOUTH ) {
				this.drawRobotHeadingSouth(g, entry.getKey()); 
			}
			else if( entry.getKey().getDirectionAsInt() == Robot.HEADING_WEST ) {
				this.drawRobotHeadingWest(g, entry.getKey());
			}
			
			if(this.showPrevPositions == true) {
				this.drawRobotPreviousPositions(g, entry.getKey()); 
			}
		}
		this.serverFrontend.checkForRobotCollisions(this.robotsMap); 
	}
	
	/**
	 * Used when an robot which heading NORTH is (re-)paint.
	 * */
	private void drawRobotHeadingNorth(Graphics g, Robot r) {
		g.drawRect(r.getDrawingPoint().x, r.getDrawingPoint().y, r.getSize(), r.getSize());
		
		g.drawRect(r.getCollisionSafetyMarginCircleDrawingPoint().x, r.getCollisionSafetyMarginCircleDrawingPoint().y,
				r.getCollisionSafetyMarginCircleDiameter(), r.getCollisionSafetyMarginCircleDiameter());
		g.drawOval(r.getCollisionSafetyMarginCircleDrawingPoint().x, r.getCollisionSafetyMarginCircleDrawingPoint().y,
				r.getCollisionSafetyMarginCircleDiameter(), r.getCollisionSafetyMarginCircleDiameter());
		
		Polygon robotBodyCoords = new Polygon();
		robotBodyCoords.addPoint( (r.getDrawingPoint().x + (r.getSize() / 3)),  (r.getDrawingPoint().y + (r.getSize() / 3)) );
		robotBodyCoords.addPoint( (r.getDrawingPoint().x + ((r.getSize() / 3) * 2)) , (r.getDrawingPoint().y + (r.getSize() / 3)) );
		robotBodyCoords.addPoint( (r.getDrawingPoint().x + r.getSize()), (r.getDrawingPoint().y + r.getSize()));
		robotBodyCoords.addPoint( r.getDrawingPoint().x, (r.getDrawingPoint().y + r.getSize()) );
		
		Polygon robotHeadCoords = new Polygon();
		robotHeadCoords.addPoint(r.getDrawingPoint().x , (r.getDrawingPoint().y + (r.getSize() / 3)) );
		robotHeadCoords.addPoint( (r.getDrawingPoint().x + (r.getSize() / 2)) , r.getDrawingPoint().y);
		robotHeadCoords.addPoint( (r.getDrawingPoint().x + r.getSize()) , (r.getDrawingPoint().y + (r.getSize() / 3)) );
		
		g.fillPolygon(robotBodyCoords);
		g.fillPolygon(robotHeadCoords);
	}
	
	
	/**
	 * Used when an robot which heading EAST is (re-)paint.
	 * */
	private void drawRobotHeadingEast(Graphics g, Robot r) {
		g.drawRect(r.getDrawingPoint().x, r.getDrawingPoint().y, r.getSize(), r.getSize());
		
		g.drawRect(r.getCollisionSafetyMarginCircleDrawingPoint().x, r.getCollisionSafetyMarginCircleDrawingPoint().y,
				r.getCollisionSafetyMarginCircleDiameter(), r.getCollisionSafetyMarginCircleDiameter());
		g.drawOval(r.getCollisionSafetyMarginCircleDrawingPoint().x, r.getCollisionSafetyMarginCircleDrawingPoint().y,
				r.getCollisionSafetyMarginCircleDiameter(), r.getCollisionSafetyMarginCircleDiameter());
		
		Polygon robotBodyCoords = new Polygon();
		robotBodyCoords.addPoint( (r.getDrawingPoint().x + ((r.getSize() / 3) * 2)) , (r.getDrawingPoint().y + (r.getSize() / 3)) );
		robotBodyCoords.addPoint( (r.getDrawingPoint().x + ((r.getSize() / 3) * 2)) , (r.getDrawingPoint().y + ((r.getSize() / 3) * 2)) );
		robotBodyCoords.addPoint( r.getDrawingPoint().x, (r.getDrawingPoint().y + r.getSize()) );
		robotBodyCoords.addPoint( r.getDrawingPoint().x , r.getDrawingPoint().y);
		
		
		Polygon robotHeadCoords = new Polygon();
		robotHeadCoords.addPoint( (r.getDrawingPoint().x + ((r.getSize() / 3) * 2)) , r.getDrawingPoint().y );
		robotHeadCoords.addPoint( (r.getDrawingPoint().x + r.getSize()) , (r.getDrawingPoint().y + (r.getSize() / 2)) );
		robotHeadCoords.addPoint( (r.getDrawingPoint().x + ((r.getSize() / 3) * 2)) , (r.getDrawingPoint().y + r.getSize()) );
		
		g.fillPolygon(robotBodyCoords);
		g.fillPolygon(robotHeadCoords);
	}
	
	
	/**
	 * Used when an robot which heading WEST is (re-)paint.
	 * */
	private void drawRobotHeadingWest(Graphics g, Robot r) {
		g.drawRect(r.getDrawingPoint().x, r.getDrawingPoint().y, r.getSize(), r.getSize());
		
		g.drawRect(r.getCollisionSafetyMarginCircleDrawingPoint().x, r.getCollisionSafetyMarginCircleDrawingPoint().y,
				r.getCollisionSafetyMarginCircleDiameter(), r.getCollisionSafetyMarginCircleDiameter());
		g.drawOval(r.getCollisionSafetyMarginCircleDrawingPoint().x, r.getCollisionSafetyMarginCircleDrawingPoint().y,
				r.getCollisionSafetyMarginCircleDiameter(), r.getCollisionSafetyMarginCircleDiameter());
		
		Polygon robotBodyCoords = new Polygon();
		robotBodyCoords.addPoint( (r.getDrawingPoint().x + (r.getSize() / 3)),  (r.getDrawingPoint().y + (r.getSize() / 3)) );
		robotBodyCoords.addPoint( (r.getDrawingPoint().x + r.getSize()) , r.getDrawingPoint().y );
		robotBodyCoords.addPoint( (r.getDrawingPoint().x + r.getSize()), (r.getDrawingPoint().y + r.getSize()) );
		robotBodyCoords.addPoint( (r.getDrawingPoint().x + (r.getSize() / 3)) , ((r.getDrawingPoint().y + (r.getSize() / 3) * 2)) );
		
		Polygon robotHeadCoords = new Polygon();
		robotHeadCoords.addPoint( (r.getDrawingPoint().x + (r.getSize() / 3)) , r.getDrawingPoint().y );
		robotHeadCoords.addPoint( r.getDrawingPoint().x , (r.getDrawingPoint().y + (r.getSize() / 2)) );
		robotHeadCoords.addPoint( (r.getDrawingPoint().x + (r.getSize() / 3)) , (r.getDrawingPoint().y + r.getSize()) );
		
		g.fillPolygon(robotBodyCoords);
		g.fillPolygon(robotHeadCoords);
	}
	
	
	/**
	 * Used when an robot which heading SOUTH is (re-)paint.
	 * */
	private void drawRobotHeadingSouth(Graphics g, Robot r) {
		g.drawRect(r.getDrawingPoint().x, r.getDrawingPoint().y, r.getSize(), r.getSize());
		
		g.drawRect(r.getCollisionSafetyMarginCircleDrawingPoint().x, r.getCollisionSafetyMarginCircleDrawingPoint().y,
				r.getCollisionSafetyMarginCircleDiameter(), r.getCollisionSafetyMarginCircleDiameter());
		g.drawOval(r.getCollisionSafetyMarginCircleDrawingPoint().x, r.getCollisionSafetyMarginCircleDrawingPoint().y,
				r.getCollisionSafetyMarginCircleDiameter(), r.getCollisionSafetyMarginCircleDiameter());
		
		Polygon robotBodyCoords = new Polygon();
		robotBodyCoords.addPoint( r.getDrawingPoint().x ,  r.getDrawingPoint().y );
		robotBodyCoords.addPoint( (r.getDrawingPoint().x + r.getSize()) , r.getDrawingPoint().y );
		robotBodyCoords.addPoint( (r.getDrawingPoint().x + ((r.getSize() / 3) * 2)) , (r.getDrawingPoint().y + ((r.getSize() / 3) * 2)) );
		robotBodyCoords.addPoint( (r.getDrawingPoint().x + (r.getSize() / 3)), (r.getDrawingPoint().y + ((r.getSize() / 3) * 2)) );
		
		Polygon robotHeadCoords = new Polygon();
		robotHeadCoords.addPoint( r.getDrawingPoint().x , (r.getDrawingPoint().y + ((r.getSize() / 3) * 2)) );
		robotHeadCoords.addPoint( (r.getDrawingPoint().x + r.getSize()) , (r.getDrawingPoint().y + ((r.getSize() / 3) * 2)) );
		robotHeadCoords.addPoint( (r.getDrawingPoint().x + (r.getSize() / 2)) , (r.getDrawingPoint().y + r.getSize()) );
		
		g.fillPolygon(robotBodyCoords);
		g.fillPolygon(robotHeadCoords);
	}
	
	
	/**
	 * This methos is called when previous positions must be drawn.
	 * */
	private void drawRobotPreviousPositions(Graphics g, Robot r) {
		RobotHistory[] rh = r.getPositionsHistoryArray();
		Color[] customColors = {Color.red, Color.yellow, Color.green}; 
		Color robotColor = g.getColor();
		
		int lastPrevPosition = 0; //find the last position added to previous positions array.
		for( int i=0; i < rh.length;i++ ) {
			if( rh[i] != null )
				lastPrevPosition++;
		}
		
		int desiredIterationsNo = r.getPositionsHistoryArrayCapacity(); //number of drawn prev-positions will be done according to the number adjusted by user in server-GUI or with default value.
		
		if( lastPrevPosition > 0 ) { //if there is any previous position
			for( int i = lastPrevPosition - 1; i >= 0; i-- ) {//go from the last prev-pos added as many positions as are specified by the @desiredIterationsNo or till the end of array if there are not enough positions in the array as many as user chose.
				int offset = (rh[i].getPosition().x - rh[i].getDrawingPoint().x) / 2;
				if( desiredIterationsNo <=3 ) {
					g.setColor(customColors[desiredIterationsNo-1]);
					g.fillOval(rh[i].getDrawingPoint().x + offset , rh[i].getDrawingPoint().y + offset, offset*2, offset*2); 
				}else {
					g.setColor(robotColor);
					g.fillOval(rh[i].getDrawingPoint().x + offset, rh[i].getDrawingPoint().y + offset, offset*2, offset*2);
				}
				
				desiredIterationsNo--;
				
				if( desiredIterationsNo == 0 ) {
					break;
				}
					
				if( i-1 >= 0 ) {
					g.setColor(robotColor);
					g.drawLine(rh[i].getPosition().x, rh[i].getPosition().y, rh[i-1].getPosition().x, rh[i-1].getPosition().y); 
				}
				
				
			}
			g.setColor(robotColor);
			g.drawLine(rh[lastPrevPosition-1].getPosition().x, rh[lastPrevPosition-1].getPosition().y, r.getPosition().x, r.getPosition().y);
		}
	}
	
	
	/**
	 * Method used when ServerGUI.showOrHidePrevPositions button on server-GUI is clicked
	 * */
	public void setShowPrevPositions() {
		this.showPrevPositions = !this.showPrevPositions;
		this.repaint();
	}
	
	
	/**
	 * When a click is made on the canvas, iterate over all connected robots and verify any of robots is in that position. 
	 * */
	@Override
	public void mouseClicked(MouseEvent e) {
		boolean foundIt = false; //used to break the loop in case that a robot was found in position of clicked point
		for( Map.Entry<Robot, Color> entry : this.robotsMap.entrySet() ) {
			if( ((e.getX() >= entry.getKey().getDrawingPoint().x) && (e.getX() <= (entry.getKey().getDrawingPoint().x + entry.getKey().getSize()))) &&
					((e.getY() >= entry.getKey().getDrawingPoint().y) && (e.getY() <= (entry.getKey().getDrawingPoint().y + entry.getKey().getSize())))	) {
				this.serverFrontend.displayRobotDetails( entry.getKey() );
				foundIt = true;
			}
			else {
				this.serverFrontend.clearTextArea();
			}
			
			if(foundIt == true) {
				break;
			}
		}
		
	}
	
	
	@Override
	public void mousePressed(MouseEvent e) {}
	@Override
	public void mouseReleased(MouseEvent e) {}
	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}
}









