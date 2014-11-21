<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="java.util.*" import="com.beans.TweetInfo"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<% HashMap<String, HashMap<Integer, TweetInfo>> wholeList;
	   wholeList = (HashMap<String, HashMap<Integer, TweetInfo>>)request.getAttribute("wholeList");
	   String filterKey = (String)request.getAttribute("filterKey");
	   int size=0;
	   ArrayList<TweetInfo> listAfterFilter = new ArrayList<TweetInfo>();
	   if(wholeList!=null && filterKey!=null && wholeList.get(filterKey)!=null){
	   		size = wholeList.get(filterKey).size();
	   		for(TweetInfo tweet:wholeList.get(filterKey).values()){
	   			listAfterFilter.add(tweet);
	   		}
	   }
	%>
	<script type="text/javascript" 
     src="http://maps.google.com/maps/api/js?  
                   key=AIzaSyDGOiekhlovikxS8oKxObYbQlrY7GZf1QE&sensor=false">
	</script>
	<script src="https://maps.googleapis.com/maps/api/js?v=3.exp&libraries=visualization"></script>
	<script type="text/javascript" src="http://google-maps-utility-library-v3.googlecode.com/svn/tags/markerclusterer/1.0/src/markerclusterer.js"></script>
	<title>Home</title>
</head>
<body onload="GetMap()">
<!--  <div>
    <input type="text" id="messageinput"/>
</div>
<div>
    <button type="button" onclick="openSocket();" >Open</button>
    <button type="button" onclick="send();" >Send</button>
    <button type="button" onclick="closeSocket();" >Close</button>
</div> -->
<!-- Server responses get written here -->
<div id="messages"></div>

<!-- Script to utilise the WebSocket -->
<script type="text/javascript">
               
    var webSocket;
    var messages = document.getElementById("messages");
    var currentState=0;
    var stop=<%=new String("stop").hashCode()%>;
    var readpoint=[];
	var map2;
	var map;
	var heatmap;
	var pointarray;
	var taxiData=[];
	var marker=[];
	var infowindow=[];
	function initialPoints(){
		<%
		if(listAfterFilter.size()!=0){
			for(int index=0;index<size;index++){
				%>
				taxiData[<%=index%>]=new google.maps.LatLng(<%=listAfterFilter.get(index).getLatitude()%>,<%=listAfterFilter.get(index).getLongitude()%>);
				marker[<%=index%>] = new google.maps.Marker({
					map: map,
					position: new google.maps.LatLng(<%=listAfterFilter.get(index).getLatitude()%>,<%=listAfterFilter.get(index).getLongitude()%>)
					});
				infowindow[<%=index%>] = new google.maps.InfoWindow();
				  infowindow[<%=index%>].setContent('<%=listAfterFilter.get(index).getTime()%> : '+'<%=listAfterFilter.get(index).getQueryText()%>');
				  google.maps.event.addListener(marker[<%=index%>], 'click', function() {
					  infowindow[<%=index%>].open(map, marker[<%=index%>]);
				  });
				<%
			}
		}
		%>
		var pointArray = new google.maps.MVCArray(taxiData);
		heatmap = new google.maps.visualization.HeatmapLayer({
		    data: pointArray
		  });
		var mcOptions = {gridSize: 50, maxZoom: 15};
		//mc = new MarkerClusterer(map);
		var mc = new MarkerClusterer(map,marker,mcOptions);
		heatmap.setMap(map2);
	}
	function GetMap(){
		mainMap();
		heatMap();
		initialPoints();
		openSocket();
	}
	function mainMap(){
		var latlng= new google.maps.LatLng(0,0);
		var myOptions={
				zoom: 1,
				center: latlng,
				mapTypeId: google.maps.MapTypeId.ROADMAP
		};
		var container = document.getElementById("mapContainer");
		map = new google.maps.Map(container,myOptions);
	}
	function heatMap(){
		var latlng= new google.maps.LatLng(0,0);
		var myOptions={
				zoom: 1,
				center: latlng,
				mapTypeId: google.maps.MapTypeId.SATELLITE
		};
		var container = document.getElementById("mapContainer2");
		map2 = new google.maps.Map(container,myOptions);
	}
	
	
	   function openSocket(){
	        // Ensures only one connection is open at a time
	        if(webSocket !== undefined && webSocket.readyState !== WebSocket.CLOSED){
	           writeResponse("WebSocket is already opened.");
	            return;
	        }
	        // Create a new instance of the websocket
	        webSocket = new WebSocket("ws://localhost:8080/CloudTwitterMap/echo");
	        /**
	         * Binds functions to the listeners for the websocket.
	         */
	        webSocket.onopen = function(event){
	            // For reasons I can't determine, onopen gets called twice
	            // and the first time event.data is undefined.
	            // Leave a comment if you know the answer.
	            if(event.data === undefined)
	                return;

	            writeResponse(event.data);
	        };

	        webSocket.onmessage = function(event){
	            //writeResponse(event.data);
	            if(currentState=0){
	            	currentState=1;
	            	readpoint[0]=event.data;
	            }else if(currentState==1){
	            	currentState=2;
	            	readpoint[1]=event.data;
	            }else if(currentState=2){
	            	currentState=3;
	            	readpoint[2]=event.data;
	            }else if(currentState=3){
	            	currentState=0;
	            	readpoint[3]=event.data;
	            	
	            }
	        };

	        webSocket.onclose = function(event){
	            writeResponse("Connection closed");
	        };
	    }
	   
	    /**
	     * Sends the value of the text input to the server
	     */
	    function send(){
	        var text = document.getElementById("messageinput").value;
	        webSocket.send(text);
	    }
	   
	    function closeSocket(){
	        webSocket.close();
	    }

	    function writeResponse(text){
	        messages.innerHTML += "<br/>" + text;
	    }
	   
		function buttonSubmitFunction(){
			var text = document.getElementById("wordtobesearch").value;
			if (text!=""){
				webSocket.send(text);
				document.getElementById("buttonSubmit").disabled=true;
				document.getElementById("stopSubmit").disabled=false;
			}
		}
		
		function stopSubmitFunction(){
			webSocket.send("stop");
			document.getElementById("buttonSubmit").disabled=false;
			document.getElementById("stopSubmit").disabled=true;
		}
	
</script>
<% 
	ArrayList<String> keyList=(ArrayList<String>)request.getAttribute("keys");
	ArrayList<String> filterList=(ArrayList<String>)request.getAttribute("filterList");
	ArrayList<Integer> numbers = (ArrayList<Integer>)request.getAttribute("number");
	int limit = (Integer)request.getAttribute("limit");
%>
<table>
<tr>
<td>
<form action="MyServlet" method="get">
Key Words<br>
<input id="wordtobesearch" type="text" >
<input id="buttonSubmit" type="button" onclick="buttonSubmitFunction()" value="search for key word!"/>
<input id="stopSubmit" type="button" onclick="stopSubmitFunction()" value="stop updating"/>
</form>
<script type="text/javascript">document.getElementById("stopSubmit").disabled=true;</script>
<form>
Number Limit(can only affect current searching)<br>
<input type="text" name="limit" value=<%=limit%>>
<input type="submit" value="OK">
</form>
Current Searching<form action="MyServlet" method="get">
<select id="keys" name="keys">
	<% 
		for(int i=0;i<keyList.size();i++){
	%>
	<option value="<%=keyList.get(i)%>"><%=keyList.get(i)%></option>
	<% 
		}
	%>
</select>
<input type="submit" name="stop" value="stop">
</form>
Filter Selection<form action="MyServlet" method="get">
<select id="filter" name="filter">
	<% 
		for(int i=0;i<filterList.size();i++){
	%>
	<option value="<%=filterList.get(i)%>" <% if(filterList.get(i).equals(filterKey)){%>selected="selected"<%}%>><%=filterList.get(i)%></option>
	<% 
		}
	%>
</select>
<input type="submit" name="act" value="filter">
<input type="submit" name="act" value="delete"><br>
</form>
</td>
<td valign="top">
<div id="mapContainer" style="width:500px;height:500px">
</div>
</td>
<td valign="top">
<div id="mapContainer2" style="width:500px;height:500px">
</div>
</td>
</tr>
</table>

</body>
</html>
