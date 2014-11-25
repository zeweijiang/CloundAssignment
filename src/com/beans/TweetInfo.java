package com.beans;

public class TweetInfo {
	private long id;
	private String key;
	private String text;
	private double latitude;
	private double longitude;
	private String time;
	private String senti;
	private String queryText;
	public long getId(){
		return id;
	}
	public String getKey(){
		return key;
	}
	public String getSenti(){
		return senti;
	}
	public String getText(){
		return text;
	}
	public double getLatitude(){
		return latitude;
	}
	public double getLongitude(){
		return longitude;
	}
	public String getTime(){
		return time;
	}
	public String getQueryText(){
		return queryText;
	}
	public void setId(long id){
		this.id=id;
	}
	public void setSenti(String senti){
		this.senti=senti;
	}
	public void setKey(String key){
		this.key=key;
	}
	public void setText(String text){
		this.text=text;
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<text.length();i++){
			if(text.charAt(i)=='\''||text.charAt(i)=='\"'||text.charAt(i)=='\\'){
				sb.append("\\");
			}
			sb.append(text.charAt(i));
		}
		this.queryText=sb.toString();
	}
	public void setLatitude(double latitude){
		this.latitude=latitude;
	}
	public void setLongitude(double longitude){
		this.longitude=longitude;
	}
	public void setTime(String time){
		this.time=time;
	}
	public void setAll(long id, String key, String text, double latitude, double longitude, String time,String senti){
		setId(id);
		setKey(key);
		setText(text);
		setLatitude(latitude);
		setLongitude(longitude);
		setTime(time);
		setSenti(senti);
	}
}

