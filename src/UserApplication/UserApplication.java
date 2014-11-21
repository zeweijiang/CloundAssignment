package UserApplication;
import java.util.ArrayList;
import java.util.HashSet;

import com.database.DB;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;


public class UserApplication {
	DB database;
	ConfigurationBuilder configurationBuilder;
	TwitterStream twitterStream;
	HashSet<String> stream = new HashSet<String>();
	ArrayList<String> keys = new ArrayList<String>();
	String currentText=null;
	String currentLatitude=null;
	String currentLongitude=null;
	String currentTimeStamp=null;
	public ArrayList<String> getStream(){
		return keys;
	}
	public UserApplication(DB database){
		this.database=database;
		configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setOAuthConsumerKey("O1y4263eAK53Y4btf7BEDBWO3")
                .setOAuthConsumerSecret("gdZZygXcHWT72z6StAWnzOQEwKgibtBPgaI8P6uXiNku9VE8pR")
                .setOAuthAccessToken("2846262060-mPn7PZbxlmNSmFc3Qtpm5uKRAVab6t7KUoIQeXZ")
                .setOAuthAccessTokenSecret("fkQLghfGpf7oDZB8BfUAKV49RVzcT0eEdpBLUPR3ApSSM");
        twitterStream = new TwitterStreamFactory(configurationBuilder.build()).getInstance();
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
			resetListener();
			return true;
		}
		return false;
	}
	public boolean startStream(String filter){
		if(stream.contains(filter))
		{
			return false;
		}
		stream.add(filter);
		keys.add(filter);
		resetListener();
        return true;
	}
	public String[] getCurrent(){
		if(currentText!=null&&currentLatitude!=null&&currentLongitude!=null&&currentTimeStamp!=null){
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
	}
	public void resetListener(){
		twitterStream.clearListeners();
		twitterStream.cleanUp();
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
					//System.out.println(currentText=arg0.getText());
					//System.out.println(currentLatitude=String.valueOf(arg0.getGeoLocation().getLatitude()));
					//System.out.println(currentLongitude=String.valueOf(arg0.getGeoLocation().getLatitude()));
					//System.out.println(currentTimeStamp=arg0.getCreatedAt().toString());
					currentText=arg0.getText();
					currentLatitude=String.valueOf(arg0.getGeoLocation().getLatitude());
					currentLongitude=String.valueOf(arg0.getGeoLocation().getLongitude());
					currentTimeStamp=arg0.getCreatedAt().toString();
					ArrayList<String> keys = getKey(stream, arg0.getText());
					for(String key:keys){
						database.fetchPosition(arg0.getId(),key, arg0.getText(),arg0.getGeoLocation().getLatitude(),arg0.getGeoLocation().getLongitude(),arg0.getCreatedAt().toString());	
					}
				}
			}

			@Override
			public void onTrackLimitationNotice(int arg0) {
				// TODO Auto-generated method stub
			}
			private ArrayList<String> getKey(HashSet<String> keySet, String text){
				ArrayList<String> keyList =new ArrayList<String>();
				for(String key: keySet){
					if(text.contains(key)){
						keyList.add(key);
					}
				}
				return keyList;
			}
        });
        FilterQuery tweetFilterQuery = new FilterQuery(); // See 
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
}
