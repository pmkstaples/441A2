/**
 * WebServer Class
 * 
 */

import java.util.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.util.concurrent.*;

@SuppressWarnings("unused")
public class WebServer extends Thread {

    private static int THREAD_POOL = 8;
    
    private int servPort;
    private volatile boolean listen = false;
    private ServerSocket socket;
    private Socket incoming;
    ThreadPoolExecutor exec = (ThreadPoolExecutor)Executors.newFixedThreadPool(THREAD_POOL);
    
    /**
     * Default constructor to initialize the web server
     * 
     * @param port 	The server port at which the web server listens > 1024
     * 
     */
    public WebServer(int port) {
    	if(port > 1024 && port < 65536)
    		servPort = port;
    	else{
    		System.out.println("Invalid port number, must be between 1024 and 65536");
    		System.exit(0);
    	}
    }

	
    /**
     * The main loop of the web server
     *   Opens a server socket at the specified server port
     *   Remains in listening mode until shutdown signal
     * 
     */
    public void run() {
    	try{
    		socket = new ServerSocket(servPort);
    		socket.setSoTimeout(1000);
    	}
    	catch(Exception e){
    		System.out.println("Exception thrown: " + e.getMessage());
    	}
	
    	while(!listen){
    		try{
    			incoming = socket.accept();
    			//	incoming = new Socket("people.ucalgary.ca", 80);
    			Worker work = new Worker(incoming);
    			exec.execute(work);
    			System.out.println("past worker execute");
    		}
    		catch(Exception e){
    		}
    	}
    }
    
    
    /**
     * Signals the server to shutdown.
     *
     */
    public void shutdown() {
    	listen = true;
    	try{
    		socket.close();
    		exec.shutdown();
    	}
    	catch(Exception e){
    	}
    }
    
    
    /**
     * A simple driver.
     */
    public static void main(String[] args) {
	int serverPort = 2225;
	
	// parse command line args
	if (args.length == 1) {
	    serverPort = Integer.parseInt(args[0]);
	}
	
	if (args.length >= 2) {
	    System.out.println("wrong number of arguments");
	    System.out.println("usage: WebServer <port>");
	    System.exit(0);
	}
	
	System.out.println("starting the server on port " + serverPort);
	
	WebServer server = new WebServer(serverPort);
	
	server.start();
	System.out.println("server started. Type \"quit\" to stop");
	System.out.println(".....................................");
	
	Scanner keyboard = new Scanner(System.in);
	while ( !keyboard.next().equals("quit") );
	
	System.out.println();
	System.out.println("shutting down the server...");
	server.shutdown();
	keyboard.close();
	System.out.println("server stopped");
    }
}
