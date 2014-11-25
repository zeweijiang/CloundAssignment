package com.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.sql.DataSource;

import com.beans.TweetInfo;
public class DB
{
	String userid=null;
	String password=null;
    String jdbcUrl ="jdbc:mysql://tweetmaop.cvdk3xvzipiw.us-east-1.rds.amazonaws.com:3306/rome";
    static Connection conn;
	private DataSource dataSource;
	private String databaseName;
	private Statement stmt;
	private Query query;
	public void connect() throws SQLException{
		try {
			Class.forName ("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		userid = "zhangluoma";
	    password = "88522712";
	    conn = DriverManager.getConnection (jdbcUrl,userid,password);
    	stmt=conn.createStatement();
    	query=new Query(conn);
    	stmt.close();
    }
	/*
	public void connect(){
		try {
			conn = dataSource.getConnection();
			stmt = conn.createStatement();
			if(databaseName!=null){
			stmt.execute("use "+databaseName);
			query=new Query(conn);
			stmt.close();
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
 
		}
	}*/
	public void close(){
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {}
		}
	}
	public ArrayList<String> test(){
		try {
			ArrayList<String> result = new ArrayList<String>();
			ResultSet rset = stmt.executeQuery("show tables");
			while(rset.next()){
				result.add(rset.getString(1));
			}
			return result;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	public void fetchPosition(long id, String key, String Text, double Lat, double Long, String time,String senti){
		query.insert(id, key, Text, Lat, Long, time,senti);
	}
	public void readPosition(HashMap<String,HashMap<Long, TweetInfo>> list, String key){
		query.read(list, key);
	}
	public ArrayList<TweetInfo> getAfter(String key, long index){
		return query.getAfter(key, index);
	}
	public ArrayList<String> filterList(ArrayList<Integer> numbers, ArrayList<String> addition){
		ArrayList<String> result = new ArrayList<String>();
		HashSet<String> tmp = new HashSet<String>();
		tmp.addAll(query.filterList());
		tmp.addAll(addition);
		for(String tmpString:tmp){
			result.add(tmpString);
		}
		numbers.clear();
		for(int i=0;i<result.size();i++){
			numbers.add(query.getNumber(result.get(i)));
		}
		return result;
	}
	public void setLimit(int limit){
		query.setLimit(limit);
	}
	public void delete(String key){
		query.delete(key);
	}
	//check if the current key exist in the database
	public boolean checkFilterExist(String key){
		if(query.filterList().contains(key)){
			return true;
		}else{
			return false;
		}
	}
	public HashMap<Long, TweetInfo> getTwitterList(String key){
		HashMap<String,HashMap<Long, TweetInfo>> list=new HashMap<String,HashMap<Long,TweetInfo>>();
		readPosition(list, key);
		return list.get(key);
	}
}
