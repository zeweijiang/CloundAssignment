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
	public ArrayList<String> getStream(){
		return keys;
	}
	public UserApplication(DB database){
		this.database=database;
		configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setOAuthConsumerKey("G2kMaZD9heWD17KB6xGAS3PbG")
                .setOAuthConsumerSecret("GsURAVuUnDMc5wbCNpviTwDNnjhg4xa6RlE7wwLu1vuh5FO19M")
                .setOAuthAccessToken("2841491153-eSVzpe4XQME5RiPwybFKq0aksHH7lM78iQrip1t")
                .setOAuthAccessTokenSecret("01bSaYy596m65JwlSyArBX7n6Yp12V0YLzuFbDUV1vrF7");
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
					System.out.println(arg0.getText());
					System.out.println(arg0.getGeoLocation());
					System.out.println(arg0.getCreatedAt());
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
