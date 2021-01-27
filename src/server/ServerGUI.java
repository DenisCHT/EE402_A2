/*
 * The ServerGUI Class - Written by Chitescu Denis for the EE402 Module - Assignment 2
 * */

package server;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Scrollbar;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import robot.Robot;
import robot.RobotHistory;

@SuppressWarnings("serial")

public class ServerGUI extends Frame implements ActionListener, AdjustmentListener{
	
	private Button showOrHidePrevPositions; // by pressing this button, robot previous positions will be shown
	private Label previousPosNoLabel;
	private TextField previousPosNoTF;
	private Scrollbar previousPosNoScrollbar; //for adjusting number of previous positions to be shown
	private TextArea robotInfoTA; //area where robot details will be shown when a robot is clicked
	
	
	private ServerGUICanvas canvas = null; //canvas have to be created when server GUI starts
	private ConcurrentMap<Robot, Color> robotsMap = null; //each connected robot will be stored in this map using a tread-safe data structure.
	private HashMap<String, ConnectionHandler> conHandlers = null; //retaining connection handler of every robot(base on the name of robot).Later used when server has something to sent to client.
	
	public ServerGUI() {
		super("Server-GUI");
		this.setLayout(new BorderLayout());
		
		this.robotsMap = new ConcurrentHashMap<Robot, Color>();
		
		this.conHandlers = new HashMap<String, ConnectionHandler>();
		
		this.robotInfoTA = new TextArea("Details about robot will be displayed here. \nSelect one!\n", 10,50, TextArea.SCROLLBARS_BOTH);
		this.robotInfoTA.setEditable(false); 
		
		Panel commandButtonsPanel = new Panel(new FlowLayout());
		
		this.showOrHidePrevPositions = new Button("Show/Hide History");
		this.showOrHidePrevPositions.addActionListener(this);
		this.previousPosNoLabel = new Label("History Points");
		this.previousPosNoTF = new TextField(""+Robot.POSITIONS_HISTORY_ARRAY_MIN_CAPACITY,1);
		this.previousPosNoScrollbar = new Scrollbar(Scrollbar.HORIZONTAL, Robot.POSITIONS_HISTORY_ARRAY_MIN_CAPACITY, 10, 
														Robot.POSITIONS_HISTORY_ARRAY_MIN_CAPACITY, Robot.POSITIONS_HISTORY_ARRAY_MAX_CAPACITY + 10);
		this.previousPosNoScrollbar.addAdjustmentListener(this); 
		
		
		commandButtonsPanel.add(this.previousPosNoLabel);
		commandButtonsPanel.add(this.previousPosNoTF);
		commandButtonsPanel.add(this.previousPosNoScrollbar);
		commandButtonsPanel.add(this.showOrHidePrevPositions);
		
		
		this.canvas = new ServerGUICanvas(this, this.robotsMap);
		
		this.add(commandButtonsPanel, BorderLayout.NORTH);
		this.add(robotInfoTA, BorderLayout.EAST);
		this.add(canvas,BorderLayout.CENTER);
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				closeServerProperly();
				dispose();
			}
		});
		
		this.pack();
		this.setVisible(true);
	}
	
	
	/*
	 * When a robot is connecting to the server, a random color will be assigned to it keeping the same color for an robot as long is connected.  
	 * */
	private void addRobot(Robot r) {		
		Color customColor = new  Color((float)Math.random(), (float)Math.random(), (float)Math.random()); 
		this.robotsMap.put(r, customColor);
		this.canvas.repaint();
	}
	
	public void updateGUICanvas() {
		this.canvas.repaint();
	}
	
	
	/**
	 * Assumption: Each robot has a unique name - each robot can be identified through it's name.
	 * When this function is called, if robot received as parameter(@r) it is already present on the server, it's details will be updated. Otherwise
	 * 	robot will be added to the server.
	 * */
	public void updateServerRobotProperties(Robot r) {
		boolean isPresent=false; //check if an robot is already connected to the server or not. 
		
		for( Map.Entry<Robot, Color> entry : this.robotsMap.entrySet() ) {
			if( entry.getKey().getName().equals(r.getName()) ) {
				entry.getKey().updateRobotStates(r); 
				this.canvas.repaint();
			
				isPresent = true;
			}
		}
	
		if(isPresent == false) {
			this.addRobot(r); 
		}
		
	}
	
	/**
	 * This method is used for debugging purpose - printing robot details in console
	 * */
	public void displayRobotDetails(Robot r) {
		RobotHistory[] prevPositions = r.getPositionsHistoryArray();
		
		this.robotInfoTA.setText("\nRobot Details:" +
		"\n\tname: " + r.getName() + "\n" +
		"\n\ttimeOfUpdate: " + r.getTimeOfUpdate() + "\n" +
	   	 "\n\trole: " + r.getRole() + "\n" +
	   	 "\n\tstatus: " + r.getStatusAsString() + "\n" +
	   	 "\n\tvelocity: " + r.getVelocity() + "\n" +
	   	 "\n\tsize: " + r.getSize() + "\n" +
	   	 "\n\tsafety margin: " + r.getCollisionSafetyMarginCircleDiameter() + "\n" +
	   	 "\n\tposition(X,Y): " + "(" + r.getPosition().x +", " + r.getPosition().y + ")\n" +
	   	 "\n\tdirection: " + r.getDirectionAsString() + "\n" +
	   	 "\n\tprevious positions: ");
	   	 for(int i=0; i< r.getPositionsHistoryArrayCapacity(); i++) {
	   		 if( prevPositions[i] != null ) {
	   			 this.robotInfoTA.append("\n\t\t(X= " + prevPositions[i].getPosition().x + ", Y= " + prevPositions[i].getPosition().y + ")   At:" +
	   					 prevPositions[i].getTimeOfUpdate().toString() );
	   		 }
	   	 }
		
	}
	
	/**
	 * This method is used when clicks are made on canvas and at that position is no robot.
	 * */
	public void clearTextArea() {
		this.robotInfoTA.setText("Details about robot will be displayed here. \nSelect one!\n");
	}
	
	
	/** 
	 * When @showOrHidePrevPosotions button is press, it will change @ServerGUICanvas.showPrevPositions from false to true and vice-versa.
	 * This action will determine if the previous positions of the robots will be drawn or not.
	 * */
	@Override
	public void actionPerformed(ActionEvent e) {
		if( e.getSource().equals(showOrHidePrevPositions) ) {
			this.canvas.setShowPrevPositions();
		}	
	}
	
	
	/**
	 * When user will adjust the number of previous positions to be  displayed, each robot will update @positionsHistoryArrayCapacityToDisplay state.
	 * @positionsHistoryArrayCapacityToDisplay is later used to determine how many of previous positions to be displayed.
	 * */
	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		if( e.getSource().equals(this.previousPosNoScrollbar) ) {
			this.previousPosNoTF.setText(""+ this.previousPosNoScrollbar.getValue()); 
			
			for( Map.Entry<Robot, Color> entry : this.robotsMap.entrySet() ) {
				entry.getKey().setPositionsHistoryArrayCapacity(this.previousPosNoScrollbar.getValue()); 
			}
			
		}
	}
	
	
	
	/**
	 * Robot collisions will be checked for each 2 robots by checking if there is an overlapping of 2 robots. 
	 * For this purpose, the size of the robot will be 'extended' according to collisionSafetyMargin size (X+collisionsafetyMarginRadius, Y+collisionsafetyMarginRadius).
	 * */
	public void checkForRobotCollisions(ConcurrentMap<Robot, Color> robotsMap) {
		
		Robot[] robotsArray = new Robot[robotsMap.size()];
		
		int robotsArrayIndex = 0;
		for( Map.Entry<Robot, Color> entry : robotsMap.entrySet() ) {
			robotsArray[robotsArrayIndex] = entry.getKey();
			robotsArrayIndex++;
		}
		
		int collisionSafetyCircleRadius_A=0;
		int collisionSafetyCircleRadius_B=0;
		
		for( int i=0; i<robotsArrayIndex; i++ ) {
			collisionSafetyCircleRadius_A = (int)Math.ceil( robotsArray[i].getCollisionSafetyMarginCircleDiameter() / 2);
			for( int j=i; j<robotsArrayIndex; j++ ) {
				if( !(robotsArray[i].getName().equals(robotsArray[j].getName())) ) {
					collisionSafetyCircleRadius_B = (int)Math.ceil( robotsArray[j].getCollisionSafetyMarginCircleDiameter() / 2);
					if( ((collisionSafetyCircleRadius_A + collisionSafetyCircleRadius_B) > Math.abs(robotsArray[j].getPosition().x - robotsArray[i].getPosition().x)) &&
							((collisionSafetyCircleRadius_A + collisionSafetyCircleRadius_B) > Math.abs(robotsArray[j].getPosition().y - robotsArray[i].getPosition().y)) ) {
						this.showCollision(robotsArray[j], robotsArray[i]);
						robotsArray[i].setStatus(Robot.STATUS_COLLIDED);
						robotsArray[j].setStatus(Robot.STATUS_COLLIDED);
						this.conHandlers.get(robotsArray[i].getName()).send(robotsArray[i]);
						this.conHandlers.get(robotsArray[j].getName()).send(robotsArray[j]);
					}else {
						this.clearTextArea();
					}
				}
			}
		}
	}
	
	/**
	 * This method is used to print on server-GUI text area a message with the name of the colliding robots
	 * */
	public void showCollision(Robot r1, Robot r2) {
		this.robotInfoTA.setText("Collision between: " + r1.getName() + " and " + r2.getName());
	}
	
	
	/**
	 * This function is called when the client side has been closed. I will delete the robot associated with that client that has been closed.
	 * */
	public void deleteRobotFromServer(String robotName) {
		for( Map.Entry<Robot, Color> entry : this.robotsMap.entrySet() ) {
			if( entry.getKey().getName().equals(robotName) ) {
				this.robotsMap.remove(entry.getKey(),entry.getValue());
				break;
			}
		}
		
		for( Map.Entry<String, ConnectionHandler> entry : this.conHandlers.entrySet() ) {
			if( entry.getKey().equals(robotName) ) {
				this.robotsMap.remove(entry.getKey(),entry.getValue());
				break;
			}
		}
	}
	
	
	
	public void addConHandlerToServerGUILink(String robotName, ConnectionHandler ch) {
		this.conHandlers.put(robotName, ch);
	}
	
	private void closeServerProperly() {
		for( Map.Entry<String, ConnectionHandler> entry : this.conHandlers.entrySet() ) {
			entry.getValue().closeSocket();
		}
		
	}
	
//	public static void main(String[] args) { main method used at server-GUI design step, see how it looks without starting the server each time.
//		new ServerGUI();
//	}

	

}
