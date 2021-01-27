/*
 * The ClientGUI Class - Written by Chitescu Denis for the EE402 Module - Assignment 2 
 */

package client;

import java.awt.Button;
import java.awt.Scrollbar;
import java.awt.TextField;
import java.awt.Label;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;


import java.awt.event.ActionListener;
import java.awt.event.AdjustmentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.AdjustmentEvent;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import java.util.Date;

import robot.Robot;



@SuppressWarnings("serial")
public class ClientGUI extends Frame implements ActionListener, AdjustmentListener{
	
	private Button moveForwardButton, moveBackwardButton, rotateLeftButton, rotateRightButton; //buttons for robot movement
	private Scrollbar robotSpeedScrollbar, updateRateScrollbar, collisionSafetyMarginScrollbar; //scrollbars for parameters that can be adjusted by client
	private TextField robotSpeedTF, robotStatusTF, timeUntilUpdateSendTF, timeAtMessageSuccessfullySentTF, updateRateTF, collisionSafetyMarginTF; // textfields for display relevant info on client gui
	private Label robotSpeedLabel, robotStatusLabel, timeUntilUpdateSendLabel, timeAtMessageSuccessfullySentLabel,
					updateRateLabel, collisionSafetyMarginLabel; //labeling each component on client-GUI offering a hint of what that is.
	
	private ClientApp clientAppBackend = null;
	private Robot robot = null; 
	
	public ClientGUI( ClientApp clientBackend, Robot robot ) {
		super("Client-GUI");
		this.setLayout(new BorderLayout());
	
		this.clientAppBackend = clientBackend;
		this.robot = robot;
		
		//Panels are created for clinet-GUI design
		Panel robotControlsPanel = new Panel(new GridLayout(5,1)); 
		Panel connectionDetailsPanel = new Panel(new GridLayout(4,1));
	
		
		Panel upCommandPanel = new Panel(new FlowLayout());
		Panel rotationCommandsPanel = new Panel(new FlowLayout());
		Panel downCommandPanel = new Panel(new FlowLayout());
		Panel fineControlsCommandPanel = new Panel(new FlowLayout()); 
		Panel connectionStatusPanel = new Panel(new FlowLayout());
		Panel connectionUpdatePanel = new Panel(new FlowLayout());
		Panel connectionMessagingPanel = new Panel(new FlowLayout());
		
		
		this.moveForwardButton = new Button("up");
		this.moveForwardButton.addActionListener(this);
		this.moveBackwardButton = new Button("down");
		this.moveBackwardButton.addActionListener(this);
		this.rotateLeftButton = new Button("left");
		this.rotateLeftButton.addActionListener(this);
		this.rotateRightButton = new Button("right");
		this.rotateRightButton.addActionListener(this);
		
		this.robotSpeedLabel = new Label("Speed:");
		this.robotSpeedTF = new TextField(""+Robot.DEFAULT_VELOCITY,1);
		this.robotSpeedTF.setEditable(false);
		this.robotSpeedScrollbar = new Scrollbar(Scrollbar.HORIZONTAL, Robot.DEFAULT_VELOCITY,10, 1, 20); 
		this.robotSpeedScrollbar.addAdjustmentListener(this);
		
		
		this.updateRateLabel = new Label("Updating rate:");
		this.updateRateTF = new TextField("" + ClientApp.DEFAULT_UPDATE_RATE ,1);
		this.updateRateTF.setEditable(false);
		this.updateRateScrollbar = new Scrollbar(Scrollbar.HORIZONTAL, ClientApp.DEFAULT_UPDATE_RATE, 10, 0, 70);
		this.updateRateScrollbar.addAdjustmentListener(this);
		
		this.robotStatusLabel = new Label("Robot status:");
		this.robotStatusTF = new TextField(""+this.robot.getStatusAsString(), 12);
		this.robotStatusTF.setEditable(false);

		this.timeUntilUpdateSendLabel = new Label("Update will be send in:");
		this.timeUntilUpdateSendTF = new TextField(""+ClientApp.DEFAULT_UPDATE_RATE, 2);
		this.timeUntilUpdateSendTF.setEditable(false);
		
		this.timeAtMessageSuccessfullySentLabel = new Label("Last message successfuly sent at:");
		this.timeAtMessageSuccessfullySentTF = new TextField("Nothing sent yet...", 15);
		this.timeAtMessageSuccessfullySentTF.setEditable(false);
		
		this.collisionSafetyMarginLabel = new Label("Collision Safety Margin:");
		this.collisionSafetyMarginTF = new TextField("" + this.robot.getCollisionSafetyMarginCircleDiameter(), 2);
		this.collisionSafetyMarginTF.setEditable(false);
		this.collisionSafetyMarginScrollbar = new Scrollbar(Scrollbar.HORIZONTAL, this.robot.getCollisionSafetyMarginCircleDiameter(), 10, 
				this.robot.getCollisionSafetyMarginCircleDiameter(), Robot.MAX_SAFETY_CIRCLE_DIAMETER + 10);
		this.collisionSafetyMarginScrollbar.addAdjustmentListener(this);
		
		upCommandPanel.add(moveForwardButton);
		downCommandPanel.add(moveBackwardButton);
		rotationCommandsPanel.add(rotateLeftButton);
		rotationCommandsPanel.add(rotateRightButton);
		
		
		fineControlsCommandPanel.add(robotSpeedLabel);
		fineControlsCommandPanel.add(robotSpeedTF);
		fineControlsCommandPanel.add(robotSpeedScrollbar);
		/**
		 * safety margin - will be a circle, around robot, with diameter of half of diagonal of the square from where robot shape is carved.
		 * Min diameter will be (SQRT_2 * Robot.ROBOT_MIN_SIZE) while max possible diameter will be (SQRT_2 * Robot.ROBOT_MIN_SIZE)
		 * */
		fineControlsCommandPanel.add(this.collisionSafetyMarginLabel);
		fineControlsCommandPanel.add(this.collisionSafetyMarginTF);
		fineControlsCommandPanel.add(this.collisionSafetyMarginScrollbar);
		fineControlsCommandPanel.add(updateRateLabel);
		fineControlsCommandPanel.add(updateRateTF);
		fineControlsCommandPanel.add(updateRateScrollbar);
		
		
		connectionStatusPanel.add(robotStatusLabel);
		connectionStatusPanel.add(robotStatusTF);
		connectionUpdatePanel.add(timeUntilUpdateSendLabel);
		connectionUpdatePanel.add(timeUntilUpdateSendTF);
		connectionMessagingPanel.add(timeAtMessageSuccessfullySentLabel);
		connectionMessagingPanel.add(timeAtMessageSuccessfullySentTF);
		
		robotControlsPanel.add(upCommandPanel);
		robotControlsPanel.add(rotationCommandsPanel);
		robotControlsPanel.add(downCommandPanel); 
		robotControlsPanel.add(fineControlsCommandPanel);
		
		connectionDetailsPanel.add(connectionStatusPanel);
		connectionDetailsPanel.add(connectionUpdatePanel);
		connectionDetailsPanel.add(connectionMessagingPanel);
		
		
		
		this.add(connectionDetailsPanel,BorderLayout.NORTH);
		this.add(robotControlsPanel, BorderLayout.SOUTH);
		
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				clientAppBackend.closeClientServerCommunication();
				dispose();
			}
		});
		
		this.pack();
		this.setVisible(true);
	}
	
	
	/**
	 * when one of client-GUI buttons is pressed, relevant action have to be performed
	 * */
	@Override
	public void actionPerformed(ActionEvent e) {
		if( e.getSource().equals(moveForwardButton) ) {
			if( this.robot.moveForward(robot.getVelocity()) == true ) { //when an robot was successfully moved means it is active
				this.robot.setStatus(Robot.STATUS_ACTIVE);
				this.showRobotStatus(this.robot.getStatusAsString()); 
			}
			else if( this.robot.moveForward(robot.getVelocity()) == false ) {
				this.robot.setStatus(Robot.STATUS_STUCK);
				this.showRobotStatus(this.robot.getStatusAsString()); 
			} 
			this.clientAppBackend.updateInstantly(); //send info to the server regarding new robot position and orientation
		}
		else if( e.getSource().equals(moveBackwardButton) ) {
			if( this.robot.moveBackward(robot.getVelocity()) == true ) { //when an robot was successfully moved means it is active
				this.robot.setStatus(Robot.STATUS_ACTIVE);
				this.showRobotStatus(this.robot.getStatusAsString()); 
			}
			else if( this.robot.moveBackward(robot.getVelocity()) == false ) {
				this.robot.setStatus(Robot.STATUS_STUCK);
				this.showRobotStatus(this.robot.getStatusAsString()); 
			}  
			this.clientAppBackend.updateInstantly();//send info to the server regarding new robot position and orientation
		} 
		else if( e.getSource().equals(rotateLeftButton) ) {
			this.robot.rotateLeft();
			this.clientAppBackend.updateInstantly();//send info to the server regarding new robot position and orientation
		}
		else if( e.getSource().equals(rotateRightButton) ) {
			this.robot.rotateRight();
			this.clientAppBackend.updateInstantly();//send info to the server regarding new robot position and orientation
		}
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		if( e.getSource().equals(robotSpeedScrollbar) ) {
			this.robotSpeedTF.setText(""+this.robotSpeedScrollbar.getValue());
			robot.setVelocity(this.robotSpeedScrollbar.getValue());
		}
		else if( e.getSource().equals(updateRateScrollbar) ) {
			this.updateRateTF.setText(""+this.updateRateScrollbar.getValue());
			this.clientAppBackend.updateRateChanged();
		}
		else if( e.getSource().equals(this.collisionSafetyMarginScrollbar) ) {
			this.collisionSafetyMarginTF.setText(""+this.collisionSafetyMarginScrollbar.getValue()); 
			this.robot.setCollisionSafetyMarginCircleDiameter(this.collisionSafetyMarginScrollbar.getValue()); 
			this.clientAppBackend.updateInstantly();
		}
	}
	
	/**
	 * Method used to display second, till an automatically update will be send, on client GUI.
	 * */
	public void showTimeUntilUpdateSend(int seconds) {
		this.timeUntilUpdateSendTF.setText("" + seconds);
	}
	
	/**
	 * Method used to display on client-GUI when client successfully communicate with server, sending and receiving an Robot object.
	 * */
	public void showTimeAtMessageSuccessfullySent(Date d) {
		this.timeAtMessageSuccessfullySentTF.setText(""+d.toString());
	}
	
	/**
	 * Method used to set the status value shown in client GUI
	 * */
	public void showRobotStatus(String status) {
		this.robotStatusTF.setText(status);
	}
	
	
	/**
	 * Method used to determine if user changed the default update rate - default is 30 seconds
	 * */
	public int getUpdateRate() {
		return this.updateRateScrollbar.getValue();
	}

}







