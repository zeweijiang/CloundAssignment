package com.database;

import java.sql.*;
import java.util.*;

import com.beans.*;
public class Query {
	private Connection conn;
	private int limit=20;
	public Query(Connection conn){
		this.conn = conn;
	}
	public void insert(long id, String key, String text, double latitude, double longitude, String time){
		try {
			Statement stmt = conn.createStatement();
			StringBuffer sb = new StringBuffer();
			for(int i=0;i<text.length();i++){
				if(text.charAt(i)=='\n'){
					continue;
				}
				if(text.charAt(i)=='\''||text.charAt(i)=='\"'||text.charAt(i)=='\\'){
					sb.append("\\");
				}
				sb.append(text.charAt(i));
			}
			stmt.execute("insert into tweet values("+id+",'"+key+"','"+sb.toString()+"','"+latitude+"','"+longitude+"','"+time+"')");
			ResultSet rset = stmt.executeQuery("select count(*) from tweet where key_word='"+key+"'");
			int number=0;
			if(rset.next()){
				number=Integer.parseInt(rset.getString(1));
			}
			if(number>limit){
				stmt.execute("delete from tweet where key_word='"+key+"'"+" order by tweet_id asc limit "+(number-limit));
			}
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void read(HashMap<String,HashMap<Long, TweetInfo>> list, String key){
		HashMap<Long, TweetInfo> tweetList = list.get(key);
		if(tweetList==null){
			list.put(key, new HashMap<Long, TweetInfo>());
			tweetList=list.get(key);
		}
		try {
			Statement stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery("select * from tweet where key_word='"+key+"'");
			while(rset.next()){
				Long id = Long.parseLong(rset.getString(1));
				if(tweetList.get(id)==null){
					TweetInfo tmp = new TweetInfo();
					Double lati = Double.parseDouble(rset.getString(4));
					Double longti =  Double.parseDouble(rset.getString(5));
					if(lati!=null && longti!=null){
						tmp.setAll(id,rset.getString(2), rset.getString(3), lati, longti, rset.getString(6));
						tweetList.put(id,tmp);
					}
				}
			}
			rset.close();
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public ArrayList<String> filterList(){
		ArrayList<String> filter = new ArrayList<String>();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery("select distinct key_word from tweet");
			while(rset.next()){
				filter.add(rset.getString(1));
			}
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return filter;
	}
	public int getNumber(String key){
		int number=0;
		ResultSet rset;
		try {
			Statement stmt = conn.createStatement();
			rset = stmt.executeQuery("select count(*) from tweet where key_word='"+key+"'");
			if(rset.next()){
				number=Integer.parseInt(rset.getString(1));
			}
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return number;
	}
	public void setLimit(int limit){
		this.limit=limit;
	}
	public ArrayList<TweetInfo> getAfter(String key, long index){
		ArrayList<TweetInfo> result = new ArrayList<TweetInfo>();
		ResultSet rset;
		try{
			Statement stmt = conn.createStatement();
			rset = stmt.executeQuery("select * from tweet where key_word='"+key+"' and tweet_id>"+index);
			while(rset.next()){
				Long id = Long.parseLong(rset.getString(1));
					TweetInfo tmp = new TweetInfo();
					Double lati = Double.parseDouble(rset.getString(4));
					Double longti =  Double.parseDouble(rset.getString(5));
					if(lati!=null && longti!=null){
						tmp.setAll(id,rset.getString(2), rset.getString(3), lati, longti, rset.getString(6));
						result.add(tmp);
					}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	public void delete(String key){
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("delete from tweet where key_word='"+key+"'");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
