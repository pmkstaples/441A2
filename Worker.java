/*
 *
 * Worker class
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
	
	System.out.println("Worker is running");
	
    	try{
	    request = input.readLine();
	    parser = request.split(" ");
	    
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
		
	    StringBuilder sb = new StringBuilder(parser[1]);
	    if(sb.charAt(0) == '/')
		sb.deleteCharAt(0);
	    String tmp = sb.toString();
	    Path path = Paths.get(tmp);
		
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
	}
    }
}
