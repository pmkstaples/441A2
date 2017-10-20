/*
 *
 * Worker class
 */

/* Known issues: 
 * -Does not check for all proper header files, JUST
 *  focuses on the GET request.
 * -When closing a web browser that has connected to the server, the server
 *  takes a few moments to time out with Error: null
 */

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.net.Socket;
import java.lang.StringBuilder;
import java.util.concurrent.TimeUnit;
import java.util.Date;

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
	}
	catch (IOException e) {
	    System.out.println("Error: " + e.getMessage());
	}
    }
    
    /* Default run method, uses the Socket conn to receive, parse and reply to properly formed HTTP GET
     * requests. 
     * @author Paul Staples paul.staples@ucalgary.ca
     */
    
    public void run(){
	
	System.out.println("Connection Thread Opened");
	
    	try{
	    request = input.readLine();
	    parser = request.split(" ");
	    /* Breaks the string up based on whitespace, checking if it's a properly formed
	     * GET based on there being 3 parts, the GET, object, and protocol version.
	     * Outputs a properly formed HTTP response with correct code.
	     */
	    
	    if(parser.length != 3 || !parser[0].equals("GET") || 
	       !(parser[2].equals("HTTP/1.1") || parser[2].equals("HTTP/1.0"))){
		
		Date d = new Date();
		
		response = "HTTP/1.1 400 Bad Request\r\n"  +
		    "Date: " + d + "\r\n" +
		    "Server: " + SERVER_NAME + "\r\n" +
		    "Connection: close" + "\r\n" +
		    "\r\n";;
		
		output.writeBytes(response);
		output.flush();
		output.close();
		input.close();
	    }
	    
	    else{
		
		/* Needed to do something with the object name given by the GET request
		 * as it was not working with the beginning '/' using TELNET. Quick 
		 * removal of this character if it exists before converting to a
		 * proper Path
		 */
		
		StringBuilder sb = new StringBuilder(parser[1]);
		if(sb.charAt(0) == '/')
		    sb.deleteCharAt(0);
		String tmp = sb.toString();
		Path path = Paths.get(tmp);
		
		
		/* Checks to see if the file actually exists, otherwise writes out
		 * a proper 404 Not Found response
		 */
		
		if(!Files.exists(path)){

		    Date d = new Date();
		    
		    response = "HTTP/1.1 404 Bad File Not Found\r\n"  +
			"Date: " + d + "\r\n" +
			"Server: " + SERVER_NAME + "\r\n" +
			"Connection: close\r\n" +
			"\r\n";;
		    
		    output.writeBytes(response);
		    output.flush();
		    output.close();
		    input.close();
		}
		else{
		    
		    /* Because I used nio.file.Files instead of File, getting the last modified
		     * required a few additional steps, named a FileTime object into an actual long
		     */
		    
		    FileTime fTime = Files.getLastModifiedTime(path);
		    long time = fTime.to(TimeUnit.MILLISECONDS);
		    Date d = new Date(time);
		    Date curr = new Date();
		    String type = Files.probeContentType(path);
		    byte[] two = Files.readAllBytes(path);
		    int len = two.length;
		    
		    response = "HTTP/1.1 200 OK\r\n" +
			"Date: " + curr + "\r\n" +
			"Server: " + SERVER_NAME + "\r\n" +
			"Last-Modified: " + d + "\r\n" +
			"Content-Length: " + len + "\r\n" +
			"Content-Type: " + type + "\r\n" +
			"Connection: close\r\n" +
			"\r\n";
		    
		    byte[] one = response.getBytes();
		    byte[] outputBytes = new byte[one.length + two.length];
		    
		    System.arraycopy(one, 0, outputBytes, 0, one.length);
		    System.arraycopy(two, 0, outputBytes, one.length, two.length);
		    
		    output.write(outputBytes);
		    output.flush();
		    output.close();
		    input.close();
        	}
	    }
	    conn.close();
	}
	catch(Exception e){
	    System.out.println("Error: " + e.getMessage());
	    try{
		conn.close();
	    }
	    catch(Exception m){
	    }
	}
    }
}

