package Servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.beans.TweetInfo;
 
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
	Reply r;
    @OnOpen
    public void onOpen(Session session){
        System.out.println(session.getId() + " has opened a connection"); 
        //session.getBasicRemote().sendText("Connection Established");
    }
    class Reply extends Thread{
    	long index=0;//search for the tweets with id larger than index.
    	String key;
    	Session s;
    	boolean startTransmit=true;
    	public Reply(Session s,String key){
    		this.key=key;
    		this.s=s;
    	}
    	public void end(){
    		startTransmit=false;
    	}
    	public void run(){
    		/*if(MyServlet.database.checkFilterExist(key)){
    			Collection<TweetInfo> list = MyServlet.database.getTwitterList(key).values();
    			for(TweetInfo t:list){
					try {
	    				s.getBasicRemote().sendText(t.getText());
						s.getBasicRemote().sendText(String.valueOf(t.getLatitude()));
						s.getBasicRemote().sendText(String.valueOf(t.getLongitude()));
						s.getBasicRemote().sendText(t.getTime());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    			}
    		}*/
    		if(!MyServlet.ua.isSearching(key)){
    			MyServlet.ua.changeFilter(key);
    		}
    		while(startTransmit){
    			for(TweetInfo t:MyServlet.database.getAfter(key, index)){
    				try {
						s.getBasicRemote().sendText(t.getText());
						s.getBasicRemote().sendText(String.valueOf(t.getLatitude()));
						s.getBasicRemote().sendText(String.valueOf(t.getLongitude()));
						s.getBasicRemote().sendText(t.getTime());
						s.getBasicRemote().sendText(t.getSenti());
						index=t.getId();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    			}
    			try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    		//MyServlet.ua.endStream(key);
    	}
    }
    /**
     * When a user sends a message to the server, this method will intercept the message
     * and allow us to react to it. For now the message is read as a String.
     */
    @OnMessage
    public void onMessage(String message, Session session){
    	if(!message.equals("----stop")){
    	if(r!=null){
    		r.end();
    	}
    	System.out.println(message);
    	r = new Reply(session, message);
    	r.start();
        //System.out.println("Message from " + session.getId() + ": " + message);
        /*try {
            session.getBasicRemote().sendText(message);
        } catch (IOException ex) {
            ex.printStackTrace();
        }*/
    	}else{
    		//r.end();
    	}
    }
 
    /**
     * The user closes the connection.
     * 
     * Note: you can't send messages to the client from this method
     */
    @OnClose
    public void onClose(Session session){
        System.out.println("Session " +session.getId()+" has ended");
        if(r!=null){
    		r.end();
    	}
    }
}
