/* 
 * The ServerApp(ThreadedServer) Class - Written by Derek Molloy for the EE402 Module
 * See: ee402.eeng.dcu.ie
 */

package server;


import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;


public class ServerApp{
	
private static int portNumber = 5000;
    

    public static void main(String args[]) {
        
    	 // Once server is up, server-GUI is started as well
        ServerGUI serverFrontend = new ServerGUI();
        
    	
    	boolean listening = true;
        ServerSocket serverSocket = null;
        
        
        // Set up the Server Socket
        try 
        {
            serverSocket = new ServerSocket(portNumber);
            System.out.println("New Server has started listening on port: " + portNumber );
        } 
        catch (IOException e) 
        {
            System.out.println("Cannot listen on port: " + portNumber + ", Exception: " + e); 
            System.exit(1);
        }
        
        // Server is now listening for connections or would not get to this point
        while (listening) // almost infinite loop - loop once for each client request
        {
            Socket clientSocket = null;
            try{
                System.out.println("**. Listening for a connection...");
                clientSocket = serverSocket.accept();
                System.out.println("00. <- Accepted socket connection from a client: ");
                System.out.println("    <- with address: " + clientSocket.getInetAddress().toString());
                System.out.println("    <- and port number: " + clientSocket.getPort());
            } 
            catch (IOException e){
                System.out.println("XX. Accept failed: " + portNumber + e);
                listening = false;   // end the loop - stop listening for further client requests
            }   
            
        	ConnectionHandler con = new ConnectionHandler(clientSocket, serverFrontend);
        	con.start(); 
            
            System.out.println("02A. -- Communication with client: " + clientSocket.getInetAddress().toString() + "has started !");
        }
        // Server is no longer listening for client connections - time to shut down.
        try 
        {
            System.out.println("04. -- Closing down the server socket gracefully.");
            serverSocket.close();
        } 
        catch (IOException e) 
        {
            System.err.println("XX. Could not close server socket. " + e.getMessage());
        }
    }
}