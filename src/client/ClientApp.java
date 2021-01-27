/* 
 * The Client Class - Written by Derek Molloy for the EE402 Module
 * See: ee402.eeng.dcu.ie
 */


/*
 * The code for this class is provided by professor Derek Molloy.
 * I slightly modified/built on top of the original codebase, to suit assignment 2 purpose.
 * Original code base can be see by request.
 */

package client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import java.net.Socket;

import java.util.Timer;
import java.util.TimerTask;

import robot.Robot;


public class ClientApp{
	
	public static final int DEFAULT_UPDATE_RATE = 30;
	private int updateRate = DEFAULT_UPDATE_RATE; //an update will be send, to the server, every 30 seconds
	private boolean updateRateChanged = false; //if user set another update rate from GUI, this will become true
	private int cronometer = 0; // counting seconds - used to determine when update about robot to be send to the server
	private final int portNumber = 5000;
	private Socket socket = null;
	private ObjectOutputStream os = null;
	private ObjectInputStream is = null;
	private ClientGUI clientAppFrontend= null;
	private Robot robot = null; //this robot object will be send to the server
	
	private TimerTask timerTask = null; 
	private Timer timer = null;

	
	public ClientApp(String serverIP, String robotName) {
		if (!connectToServer(serverIP)) {
    		System.out.println("XX. Failed to open socket connection to: " + serverIP);            
    	}else {
    		this.robot = new Robot(robotName);
    		this.clientAppFrontend = new ClientGUI(this, this.robot);
    		this.send(this.robot); 
    		this.updatePeriodically(); 
    		while(readRobotObject()) {}
    	}
	}
	
    private boolean connectToServer(String serverIP) {
    	try { // open a new socket to the server 
    		this.socket = new Socket(serverIP,portNumber);
    		this.os = new ObjectOutputStream(this.socket.getOutputStream());
    		this.is = new ObjectInputStream(this.socket.getInputStream());
    		
    		System.out.println("00. -> Connected to Server:" + this.socket.getInetAddress() 
    				+ " on port: " + this.socket.getPort());
    		System.out.println("    -> from local address: " + this.socket.getLocalAddress() 
    				+ " and port: " + this.socket.getLocalPort());
    	} 
        catch (Exception e) {
        	System.out.println("XX. Failed to Connect to the Server at port: " + portNumber);
        	System.out.println("    Exception: " + e.toString());	
        	return false;
        }
		return true;
    }
    
    
    //method to send a generic object
    private void send(Object o) {
		try {
		    System.out.println("02. -> Sending an object...");
		    os.writeObject(o);
		    os.flush();
		    os.reset();
		    
		    if( this.updateRateChanged ) {
		    	updateRate = clientAppFrontend.getUpdateRate();//new selected update rate will have effect from next cycle
		    	this.updateRateChanged = false;
		    }
		    
		    System.out.println("Message sent at:" + java.time.LocalDateTime.now().toString());
		  
		} 
	    catch (Exception e) {
		    System.out.println("XX. Exception Occurred on Sending: " +  e.getMessage());
		    e.printStackTrace();
		    this.closeClientServerCommunication();
	    }
    }
	
    
    /**
     * Method used to send an update to the server is to user take no action in time specified by updateRate parameter.
     * */
    public void updatePeriodically() {
    	timerTask = new TimerTask() {
    		public void run() {
    			clientAppFrontend.showTimeUntilUpdateSend(updateRate - cronometer);
    			if(cronometer == updateRate) {
    				robot.setStatus(Robot.STATUS_PASSIVE); // if periodically update was sent, means the object did not move from previous update
    				clientAppFrontend.showRobotStatus(robot.getStatusAsString());
    				send(robot);
    				cronometer = 0; //reset the cronometer
    				clientAppFrontend.showTimeUntilUpdateSend(updateRate - cronometer);
    			}
    			cronometer++;
    		}
    	};
    	timer = new Timer();
    	timer.scheduleAtFixedRate(timerTask, 1000, 1000);
	}
    
    /**
     * Method used when the user moved/changed robot heading the robot will be send to the server immediately.
     * */
    public void updateInstantly() {
    	send(robot);
    	this.cronometer = 0;
    }
    
    public void updateRateChanged() { 
    	this.updateRateChanged = true;
    }
    
    public ClientGUI getClientGUIInstance() {
    	return this.clientAppFrontend;
    }
    
    
    /**
     * Method used to continuous listening for server messages.(Robot object)
     * Every time server will update the robot object, it will immediately send it to the client.
     * Client will get the updates made by server and process them. 
     * */
    private boolean readRobotObject() {
    	Robot robotCreatedOnServerByDeserialization = null;
	    
    	try {
    		System.out.println("03. -- About to receive an object...");
    		robotCreatedOnServerByDeserialization = (Robot)is.readObject();
    		System.out.println("04. <- Object received...");
    	}catch(Exception e) {
    		System.out.println("XX. Exception Occurred on Receiving:" + e.toString());
    		this.closeClientServerCommunication();
    		return false;
    	}
    	
    	this.robot.updateRobotStates(robotCreatedOnServerByDeserialization);
	    this.clientAppFrontend.showRobotStatus(this.robot.getStatusAsString()); 
	    this.clientAppFrontend.showTimeAtMessageSuccessfullySent(this.robot.getTimeOfUpdate());
	    
	    System.out.println("Last time a message was successfully sent at: " + this.robot.getTimeOfUpdate().toString());
    	
	    return true;
    }
    
    public void closeClientServerCommunication() {
    	try {
    		this.os.close();
    		this.is.close();
			this.socket.close();
			this.timerTask.cancel(); // if client<->server communication goes down, no updates, about robot, will be sent anymore
		    this.timer.cancel();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    
	public static void main(String[] args)  {
		System.out.println("**. Java Client Application - EE402 OOP Module, DCU");
    	if(args.length==2){
    		 new ClientApp(args[0], args[1]);
    		 
    	}
    	else
    	{
    		System.out.println("Error: you must provide the address of the server");
    		System.out.println("Usage is:  java Client x.x.x.x  (e.g. java Client 192.168.7.2)");
    		System.out.println("      or:  java Client hostname (e.g. java Client localhost)");
    	}    
    	System.out.println("**. End of Application.");
	}

	
}