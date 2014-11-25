package Servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.json.*;


public class FetchSentiment {
	public static void fetchSentiment(HttpServletRequest request)
			throws JSONException {
		String receivedData=getPostData(request);
		System.out.println(receivedData);
		if(receivedData.startsWith("{")){
			JSONObject messageAttribute = new JSONObject(receivedData).getJSONObject("MessageAttributes");
			long id = Long.parseLong((String) messageAttribute.getJSONObject("id").getString("Value"));
			double lat = Double.parseDouble((String)messageAttribute.getJSONObject("lat").getString("Value"));
			double lon = Double.parseDouble((String)messageAttribute.getJSONObject("lon").getString("Value"));
			String time = (String) messageAttribute.getJSONObject("time").getString("Value");
			String text = (String) messageAttribute.getJSONObject("text").getString("Value");
			String senti = (String) messageAttribute.getJSONObject("senti").getString("Value");
			MyServlet.database.fetchPosition(id, "dsds", text, lat, lon, time, senti);	
			//ArrayList<String> keys = MyServlet.ua.getKey(text);
			/*for(String key:keys){
				MyServlet.database.fetchPosition(id, key, text, lat, lon, time, senti);	
			}*/
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
}
