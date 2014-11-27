package Servlet;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.json.*;


public class FetchSentiment {
	public static void fetchSentiment(HttpServletRequest request){
		String receivedData=getPostData(request);
		//System.out.println(receivedData);
		if(receivedData.startsWith("{")){
			JSONObject jo = new JSONObject(receivedData);
			JSONObject messageAttribute = null;
			String confirm=null;
			try{
				messageAttribute = jo.getJSONObject("MessageAttributes");
			}catch(JSONException je){
				confirm = jo.getString("SubscribeURL");
				try {
					System.out.println("url "+confirm);
					sendMessage(confirm);
					return;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
			}
			long id = Long.parseLong((String) messageAttribute.getJSONObject("id").getString("Value"));
			double lat = Double.parseDouble((String)messageAttribute.getJSONObject("lat").getString("Value"));
			double lon = Double.parseDouble((String)messageAttribute.getJSONObject("lon").getString("Value"));
			String time = (String) messageAttribute.getJSONObject("time").getString("Value");
			String text = (String) jo.get("Message");
			String senti = (String) messageAttribute.getJSONObject("senti").getString("Value");
			//System.out.println(text);
			//MyServlet.database.fetchPosition(id, "dsds", text, lat, lon, time, senti);
			if("error".equals(senti)){
				senti="0";
			}
			ArrayList<String> keys = MyServlet.ua.getKey(text);
			for(String key:keys){
				MyServlet.database.fetchPosition(id, key, text, lat, lon, time, senti);
				//System.out.println("get one");
			}
		}
	}
	public static String getPostData(HttpServletRequest req) {
	    StringBuilder sb = new StringBuilder();
	    try {
	        BufferedReader reader = req.getReader();
	        reader.mark(10000);

	        String line;
	        do {
	            line = reader.readLine();
	            sb.append(line).append("\n");
	        } while (line != null);
	        reader.reset();
	        // do NOT close the reader here, or you won't be able to get the post data twice
	    } catch(IOException e) {
	        System.out.println("getPostData couldn't.. get the post data");  // This has happened if the request's reader is closed    
	    }

	    return sb.toString();
	}
	public static void sendMessage(String text) throws IOException{
		String url = "http://160.39.132.196:8080/elasticbeanstalk-sampleapp/Worker";
		//String url= "http://160.39.132.196:8080/elasticbeanstalk-sampleapp/Worker";
		URL obj = new URL(url);
		URLConnection con = obj.openConnection();

		// add reuqest header

		((HttpURLConnection) con).setRequestMethod("GET");
		con.setRequestProperty("User-Agent", "Mozilla/5.0");
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(text);
		wr.flush();
		wr.close();
		int responseCode = ((HttpURLConnection) con).getResponseCode();
		System.out.println(responseCode);
		
	}
}
