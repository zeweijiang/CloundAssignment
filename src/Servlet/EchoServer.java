package Servlet;

import java.io.IOException;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
 
/** 
 * @ServerEndpoint gives the relative name for the end point
 * This will be accessed via ws://localhost:8080/EchoChamber/echo
 * Where "localhost" is the address of the host,
 * "EchoChamber" is the name of the package
 * and "echo" is the address to access this class from the server
 */
@ServerEndpoint("/echo") 
public class EchoServer {
    /**
     * @OnOpen allows us to intercept the creation of a new session.
     * The session class allows us to send data to the user.
     * In the method onOpen, we'll let the user know that the handshake was 
     * successful.
     */
	Session s;
	boolean startTransmit=false;
    @OnOpen
    public void onOpen(Session session){
        System.out.println(session.getId() + " has opened a connection"); 
        try {
            session.getBasicRemote().sendText("Connection Established");
            s=session;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    class Reply extends Thread{
    	String key;
    	public Reply(String key){
    		this.key=key;
    	}
    	public void run(){
    		MyServlet.ua.startStream(key);
    		while(startTransmit){
    			String[] tmp= new String[4];
    			tmp=MyServlet.ua.getCurrent();
    			//System.out.println("!!!!");
    			if(tmp!=null){
    				System.out.println("dsds");
    				try {
    					System.out.println(tmp[0]);
						s.getBasicRemote().sendText(tmp[0]);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    			}
    			try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}
    }
    /**
     * When a user sends a message to the server, this method will intercept the message
     * and allow us to react to it. For now the message is read as a String.
     */
    @OnMessage
    public void onMessage(String message, Session session){
    	startTransmit=true;
    	System.out.println(message);
    	Reply r = new Reply(message);
    	r.start();
        //System.out.println("Message from " + session.getId() + ": " + message);
        /*try {
            session.getBasicRemote().sendText(message);
        } catch (IOException ex) {
            ex.printStackTrace();
        }*/
    }
 
    /**
     * The user closes the connection.
     * 
     * Note: you can't send messages to the client from this method
     */
    @OnClose
    public void onClose(Session session){
        System.out.println("Session " +session.getId()+" has ended");
        startTransmit=false;
    }
}