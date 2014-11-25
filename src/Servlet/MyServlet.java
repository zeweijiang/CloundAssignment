package Servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import UserApplication.UserApplication;

import org.json.*;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.beans.TweetInfo;
import com.database.DB;
/**
 * Servlet implementation class MyServlet
 */
public class MyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    public static DB database = new DB();
	public static UserApplication ua;
	HashMap<String,HashMap<Long, TweetInfo>> wholeList = new HashMap<String,HashMap<Long, TweetInfo>>();
	ArrayList<String> filterList;
	ArrayList<Integer> numberInFilterList = new ArrayList<Integer>();
	String currentKey;
	int currentLimit=30;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MyServlet() {
        super();
        try {
			database.connect();
			ua = new UserApplication(database);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        // TODO Auto-generated constructor stub
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.service(request, response);
		try {
			processRequest(request,response);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*System.out.println(request.getParameter("sub"));*/
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	protected void processRequest(HttpServletRequest request,
			HttpServletResponse response) throws JSONException {
		//String a = request.getParameter("x-amz-sns-message-type");
		//System.out.println("!!!!");
		/*Enumeration<String> a = request.getParameterNames();
		while(a.hasMoreElements()){
			System.out.println(a.nextElement());
		};*/
		FetchSentiment.fetchSentiment(request);
		String keys = request.getParameter("keys");
		filterList = database.filterList(numberInFilterList, ua.getStream());
		String filter = request.getParameter("filter");
		//String startStream = request.getParameter("inputKey");
		String limit = request.getParameter("limit");
		String act = request.getParameter("act");
		if(act!=null&&act.equals("delete")&&filter!=null){
			currentKey=null;
			database.delete(filter);
		}
		if(limit!=null){
			database.setLimit(Integer.parseInt(limit));
			currentLimit=Integer.parseInt(limit);
		}
		if(keys!=null){
			ua.endStream(keys);
		}
		/*if(startStream!=null){
			ua.startStream(startStream);
		}*/
		if(filter!=null){
			currentKey = filter;
		}
		wholeList.clear();
		database.readPosition(wholeList, currentKey);
		request.setAttribute("wholeList",wholeList);
		request.setAttribute("filterKey",currentKey);
		request.setAttribute("filterList",filterList);
		request.setAttribute("number",numberInFilterList);
		request.setAttribute("keys",ua.getStream());
		request.setAttribute("limit",currentLimit);
		try {
			request.getRequestDispatcher("index2.jsp").forward(request, response);
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
