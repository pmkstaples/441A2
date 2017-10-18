/*
 *
 * Worker class
 */

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Worker implements Runnable{

	private static String SERVER_NAME = "PMKServer";
		
    private Socket conn;
    private BufferedReader input;
    private DataOutputStream output;
    private String request = "";
    private String response = "";
    private String[] parser;
    
  /* Default constructor, creates the connection to parse out, as well
     * as setting up input and output streams.
     * 
     * @param {Socket} in  A socket with which to send and receive from.
     * 
     * @author Paul Staples paul.staples@ucalgary.ca
     */
    
    public Worker(Socket in){
    	conn = in;
    	try {
			input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			output = new DataOutputStream(conn.getOutputStream());
		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
		}
    }

    /* Default run method, uses the Socket conn to receive, parse and reply to properly formed HTTP GET
     * requests. 
     * @author Paul Staples paul.staples@ucalgary.ca
     */
    
	public void run(){

		System.out.println("Worker is running");
	
    	try{
    		request = input.readLine();
    		
    		parser = request.split(" ");
    		
    		if(parser.length != 3 || !parser[0].equals("GET") || 
    				!(parser[2].equals("HTTP/1.1") && parser[2].equals("HTTP/1.0"))){
    			response = "HTTP/1.1 400 Bad Request\r\n"  + "Date: \r\n" + "Server: " + SERVER_NAME + "Connection: close\r\n" + "\r\n\r\n";;
    			System.out.println(response);
    			output.writeChars(response);
    		}
    		
    		conn.close();
    	}
    	catch(Exception e){
    	}
    }


}