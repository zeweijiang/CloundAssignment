package UserApplication;
import java.util.ArrayList;
import java.util.HashSet;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.database.DB;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;


public class UserApplication {
	DB database;
	ConfigurationBuilder configurationBuilder;
	TwitterStream twitterStream;
	public HashSet<String> stream = new HashSet<String>();
	ArrayList<String> keys = new ArrayList<String>();
	AmazonSQSClient sqs =new AmazonSQSClient(new ProfileCredentialsProvider("default")
	.getCredentials());
	String myQueueUrl="https://sqs.us-east-1.amazonaws.com/668249848517/Cloud";
	//String currentText=null;
	//String currentLatitude=null;
	//String currentLongitude=null;
	//String currentTimeStamp=null;
	FilterQuery tweetFilterQuery;
	public ArrayList<String> getStream(){
		return keys;
	}
	public boolean isSearching(String key){
		if(stream.contains(key)){
			return true;
		}else{
			return false;
		}
	}
	public UserApplication(DB database){
		this.database=database;
		configurationBuilder = new ConfigurationBuilder();
        /*configurationBuilder.setOAuthConsumerKey("O1y4263eAK53Y4btf7BEDBWO3")
                .setOAuthConsumerSecret("gdZZygXcHWT72z6StAWnzOQEwKgibtBPgaI8P6uXiNku9VE8pR")
                .setOAuthAccessToken("2846262060-mPn7PZbxlmNSmFc3Qtpm5uKRAVab6t7KUoIQeXZ")
                .setOAuthAccessTokenSecret("fkQLghfGpf7oDZB8BfUAKV49RVzcT0eEdpBLUPR3ApSSM");*/
        configurationBuilder.setOAuthConsumerKey("G2kMaZD9heWD17KB6xGAS3PbG")
        .setOAuthConsumerSecret("GsURAVuUnDMc5wbCNpviTwDNnjhg4xa6RlE7wwLu1vuh5FO19M")
        .setOAuthAccessToken("2841491153-eSVzpe4XQME5RiPwybFKq0aksHH7lM78iQrip1t")
        .setOAuthAccessTokenSecret("01bSaYy596m65JwlSyArBX7n6Yp12V0YLzuFbDUV1vrF7");
        twitterStream = new TwitterStreamFactory(configurationBuilder.build()).getInstance();
        initialStream();
	}
	public boolean endStream(String key){
		if(stream.contains(key)){
			stream.remove(key);
			for(int i=0;i<keys.size();i++){
				if(keys.get(i).equals(key)){
					keys.remove(i);
					break;
				}
			}
			return true;
		}
		return false;
	}
	public void initialStream(){
		setListener();
		tweetFilterQuery = new FilterQuery();
	}
	public boolean changeFilter(String filter){
		System.out.println("*******start*******");
		if(stream.contains(filter))
		{
			return false;
		}
		stream.add(filter);
		keys.add(filter);
		resetFilter();
        return true;
	}
	/*
	public String[] getCurrent(String key){
		if(currentText!=null&&currentLatitude!=null&&currentLongitude!=null&&currentTimeStamp!=null&&currentText.contains(key)){
			String[] tmps= new String[4];
			tmps[0]= currentText;
			tmps[1]=currentLatitude;
			tmps[2]=currentLongitude;
			tmps[3]=currentTimeStamp;
			currentText=null;
			currentLatitude=null;
			currentLongitude=null;
			currentTimeStamp=null;
			return tmps;
		}else{
			return null;
		}
	}*/
	public void resetFilter(){
        String[] f = new String[stream.size()];
        int i=0;
        for(String s:stream){
        	f[i]=" "+s+" ";
        	i++;
        }
        tweetFilterQuery.track(f); // OR on keywords
        /*tweetFilterQuery.locations(new double[][]{new double[]{-126.562500,30.448674},
                        new double[]{-61.171875,44.087585
                        }}); */// See https://dev.twitter.com/docs/streaming-apis/parameters#locations for proper location doc. 
        //Note that not all tweets have location metadata set.
        tweetFilterQuery.language(new String[]{"en"}); // Note that language does not work properly on Norwegian tweets 
        twitterStream.filter(tweetFilterQuery);
	}
	public void setListener(){
        twitterStream.addListener(new StatusListener(){

			@Override
			public void onException(Exception arg0) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onDeletionNotice(StatusDeletionNotice arg0) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onScrubGeo(long arg0, long arg1) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onStallWarning(StallWarning arg0) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onStatus(Status arg0) {
				// TODO Auto-generated method stub
				if(arg0.getGeoLocation()!=null){
					//System.out.println("asdfasdfasdfasfd");
					//arg0.getPlace().getGeometryCoordinates()
					//System.out.println("text="+arg0.getText());
					//System.out.println(currentText=arg0.getText());
					//System.out.println(currentLatitude=String.valueOf(arg0.getGeoLocation().getLatitude()));
					//System.out.println(currentLongitude=String.valueOf(arg0.getGeoLocation().getLatitude()));
					//System.out.println(currentTimeStamp=arg0.getCreatedAt().toString());
					//System.out.println("text="+arg0.getText());
					//currentText=arg0.getText();
					//currentLatitude=String.valueOf(arg0.getGeoLocation().getLatitude());
					//currentLongitude=String.valueOf(arg0.getGeoLocation().getLongitude());					
					//System.out.println(currentLatitude=String.valueOf(arg0.getPlace().getGeometryCoordinates()[0][0].getLatitude()));
					//System.out.println(currentLongitude=String.valueOf(arg0.getPlace().getGeometryCoordinates()[0][0].getLongitude()));
					//currentTimeStamp=arg0.getCreatedAt().toString();
					//System.out.println("text="+arg0.getText());
					JSONObject jo = new JSONObject();
					try {
						jo.put("id", String.valueOf(arg0.getId()));
						jo.put("text", arg0.getText());
						jo.put("lat", String.valueOf(arg0.getGeoLocation().getLatitude()));
						jo.put("lon",String.valueOf( arg0.getGeoLocation().getLongitude()));
						jo.put("time", arg0.getCreatedAt());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					SendMessageResult smr=sqs.sendMessage(new SendMessageRequest().withQueueUrl(myQueueUrl)
							.withMessageBody(jo.toString()));
					//System.out.println(smr.getMessageId());
					
				}
			}

			@Override
			public void onTrackLimitationNotice(int arg0) {
				// TODO Auto-generated method stub
			}
        });
	}
	public ArrayList<String> getKey(String text){
		//System.out.println("keySet="+keySet);
		ArrayList<String> keyList =new ArrayList<String>();
		for(String key: stream){
			if(text.contains(key)){
				keyList.add(key);
			}
		}
		return keyList;
	}
}
