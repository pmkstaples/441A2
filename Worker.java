/*
 *
 * Worker class
 */

import java.net.Socket;

public class Worker implements Runnable{

    Socket conn;

    /* Default constructor, creates the connection to parse
     * 
     * @param {Socket} in  A socket with which to send and receive from
     * 
     * @author Paul Staples paul.staples@ucalgary.ca
     */
    
    public Worker(Socket in){
    	conn = in;
    }

    /* Default run method, uses the Socket conn to receive, parse and reply to probably formed HTTP GET
     * requests. 
     * @author Paul Staples paul.staples@ucalgary.ca
     */
    
    public void run(){

    System.out.println("Worker is running");
	
    	try{
    		conn.close();
    	}
    	catch(Exception e){
    	}
    }


}