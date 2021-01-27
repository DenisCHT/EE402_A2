/* 
 * The Connection Handler Class - Written by Derek Molloy for the EE402 Module
 * See: ee402.eeng.dcu.ie
 */

/*
 * The code for this class is provided by professor Derek Molloy.
 * I slightly modified/built on top of the original codebase, to suit assignment 2 purpose.
 * Original code base can be see by request.
 */

package server;

import java.net.Socket;
import java.util.Date;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import robot.Robot;
import robot.RobotHistory;
import utils.DateTimeService;

public class ConnectionHandler extends Thread {
	
	private Socket clientSocket = null;             // Client socket object
    private ObjectInputStream is = null;            // Input stream
    private ObjectOutputStream os = null;           // Output stream
    private DateTimeService theDateService;
    
    private ServerGUI serverFrontend = null;
    private Robot robot = null;
    
    // The constructor for the connection handler
    public ConnectionHandler(Socket clientSocket, ServerGUI serverFrontend) {
        this.clientSocket = clientSocket;
        this.serverFrontend = serverFrontend;
    }

    // Will eventually be the thread execution method - can't pass the exception back
    public void run() {
         try {
            this.is = new ObjectInputStream(clientSocket.getInputStream());
            this.os = new ObjectOutputStream(clientSocket.getOutputStream());
            while (this.readRobotObject()) {}
            this.serverFrontend.deleteRobotFromServer(this.robot.getName()); // when a client-server connection is closed, delete that object from server
            this.serverFrontend.updateGUICanvas(); // repaint canvas withoud that object
            System.out.println("02B. -- Finished communicating with client:" + clientSocket.getInetAddress().toString());
         } 
         catch (IOException e) 
         {
            System.out.println("XX. There was a problem with the Input/Output Communication:");
            e.printStackTrace();
         }
    }

    // Receive and process incoming string commands from client socket 
    private boolean readRobotObject() {
        try {
            this.robot = (Robot) is.readObject();
        } 
        catch (Exception e){    // catch a general exception
            this.closeSocket();
            return false;
        }
        System.out.println("01. <- Received an Robot object from the client (" + this.robot.getName() + ").");
        
        // At this point there is a valid Robot object
        
        this.serverFrontend.updateServerRobotProperties(this.robot); //when an Robot object is received from Client, update it's states in case the client did some changes.

        this.serverFrontend.addConHandlerToServerGUILink(this.robot.getName(), this); // obtain a handler to the thread that manage Robot object during client-server communication/
        
        this.printRobotDetails(this.robot);//for debugging purpose - see info about received Robot object in console

        
        Date serverDateTime = this.getCurrentDateAndTime();
      
        this.robot.setTimeOfUpdate(serverDateTime);// when the Robot object is received, update Robot.timeOfUpdate state and send robot back to client. 
        this.send(this.robot); //send robot back to client with updated state(s).
           
        return true;
    }

    // Use our custom DateTimeService Class to get the date and time
    private Date getCurrentDateAndTime() {    // use the date service to get the date
        theDateService = new DateTimeService(); // for having date and time updated every time when send it to client
    	Date currentDateTime = theDateService.getDateAndTime();
        return currentDateTime;
    }

    // Send a generic object back to the client 
    public void send(Object o) {
        try {
            System.out.println("02. -> Sending (" + o +") to the client.");
            this.os.writeObject(o);
            this.os.flush();
            this.os.reset();
        } 
        catch (Exception e) {
            System.out.println("XX." + e.getStackTrace());
        }
    }
    
    
    // Close the client socket 
    public void closeSocket() { //gracefully close the socket connection
        try {
            this.os.close();
            this.is.close();
            this.clientSocket.close();
        } 
        catch (Exception e) {
            System.out.println("XX. " + e.getStackTrace());
        }
    }
    
    //debuggin purpose method - Print details about received robot object in the console
    public void printRobotDetails(Robot r) {
    	RobotHistory[] prevPositions = r.getPositionsHistoryArray();
    	 System.out.println("Received Robot object details:\n" +
 		"\tname: " + r.getName() + "\n" +
 		"\ttimeOfUpdate: " + r.getTimeOfUpdate() + "\n" +
    	 "\trole: " + r.getRole() + "\n" +
    	 "\tstatus: " + r.getStatusAsString() + "\n" +
    	 "\tvelocity: " + r.getVelocity() + "\n" +
    	 "\tsize: " + r.getSize() + "\n" +
    	 "\tsafety margin: " + r.getCollisionSafetyMarginCircleDiameter() + "\n" +
    	 "\tposition(X,Y): " + "(" + r.getPosition().x +", " + r.getPosition().y + ")\n" +
    	 "\tdirection: " + r.getDirectionAsString() + "\n" +
    	 "\tprevious positions: ");
    	 for(int i=0; i<r.getPositionsHistoryArrayCapacity(); i++) {
    		 if( prevPositions[i] != null ) {
    			 System.out.println("\t\t(X= " + prevPositions[i].getPosition().x + ", Y= " + prevPositions[i].getPosition().y + ")   At:" +
    					 prevPositions[i].getTimeOfUpdate().toString());
    		 }
    	 }
    }

}
