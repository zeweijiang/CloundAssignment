<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="java.util.*" import="com.beans.TweetInfo"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<% 
	   String filterKey = (String)request.getAttribute("filterKey");
	%>
	<script type="text/javascript" 
     src="http://maps.google.com/maps/api/js?  
                   key=AIzaSyDGOiekhlovikxS8oKxObYbQlrY7GZf1QE&sensor=false">
	</script>
	<script src="https://maps.googleapis.com/maps/api/js?v=3.exp&libraries=visualization"></script>
	<script type="text/javascript" src="http://google-maps-utility-library-v3.googlecode.com/svn/tags/markerclusterer/1.0/src/markerclusterer.js"></script>
	<script type="text/javascript" src="https://www.google.com/jsapi"></script>
    <script type="text/javascript" src="https://www.google.com/jsapi?autoload={'modules':[{'name':'visualization','version':'1','packages':['corechart'],'language':'ru'}]}"></script>
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
<div id="senti"></div>
<!-- Script to utilise the WebSocket -->
<script type="text/javascript">
               
    var webSocket;
    var messages = document.getElementById("messages");
    var currentState=0;
    var stop=<%=new String("stop").hashCode()%>;
    var readpoint=[];
    var map3;
	var map2;
	var map;
	var heatmap;
	var positiveMap;
	var negativeMap;
	var pointArray;
	var pointArray2;
	var pointArray3;
	var taxiData=[];
	var taxiData2=[];
	var taxiData3=[];
	var marker=[];
	var infowindow=[];
	var mc;
	var overall=0.0;
	var totalsenti=0.0;
	var drawChartCount=0;
	var drawChartRate=10;//Repaint when received 10 new data
	var lineChartData = [['Sentiment Range', 'Count'],
			          	 ['[-1.0, -0.8)',  0],
			             ['[-0.8, -0.6)',  0],
			             ['[-0.6, -0.4)',  0],
			             ['[-0.4, -0.2)',  0],
			             ['[-0.2, -0)',  0],
			             ['[0, 0.2)',  0],
			             ['[0.2, 0.4)',  0],
			             ['[0.4, 0.6)',  0],
			             ['[0.6, 0.8)',  0],
			             ['[0.8, 1]',  0]
			            ];
	function changeGradient() {
		  var gradient = [
		    'rgba(255, 0, 0, 0)',
		    'rgba(255, 0, 0, 1)',
		    'rgba(255, 0, 0, 2)',
		    'rgba(255, 0, 0, 3)'
		  ]
		  positiveMap.set('gradient', heatmap.get('gradient') ? null : gradient);
		  var gradient2 = [
			'rgba(0, 0, 255, 0)',
			'rgba(0, 0, 255, 1)',
			'rgba(0, 0, 255, 2)',
			'rgba(0, 0, 255, 3)'
		      		  ]
		      		  negativeMap.set('gradient', heatmap.get('gradient') ? null : gradient2);
		}
	function initialPoints(){
		var mcOptions = {gridSize: 50, maxZoom: 15};
		//var mc = new MarkerClusterer(map);
		mc = new MarkerClusterer(map,marker,mcOptions);
	}
	function clearMarker() {
		for (var i = 0; i < markers.length; i++) {
			marker[i].setMap(null);
		}
		marker=[];
	}
	function GetMap(){
		mainMap();
		heatMap();
		sentimentMap();
		changeGradient();
		drawChart();
		initialPoints();
		openSocket();
		//clearMarker();
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
		pointArray = new google.maps.MVCArray(taxiData);
		heatmap = new google.maps.visualization.HeatmapLayer({
		    data: pointArray
		  });
		heatmap.setMap(map2);
	}
	function sentimentMap(){
		var latlng= new google.maps.LatLng(0,0);
		var myOptions={
				zoom: 1,
				center: latlng,
				mapTypeId: google.maps.MapTypeId.SATELLITE
		};
		var container = document.getElementById("mapContainer3");
		map3 = new google.maps.Map(container,myOptions);
		pointArray2 = new google.maps.MVCArray(taxiData2);
		positiveMap = new google.maps.visualization.HeatmapLayer({
		    data: pointArray2
		  });
		positiveMap.setMap(map3);
		pointArray3 = new google.maps.MVCArray(taxiData3);
		negativeMap = new google.maps.visualization.HeatmapLayer({
		    data: pointArray3
		  });
		negativeMap.setMap(map3);
	}
	
	function drawChart() {
        var data = google.visualization.arrayToDataTable(lineChartData);
        var options = {
          title: 'Sentiment Count V.S. Sentiment Score'
        };

        var lineChart = new google.visualization.LineChart(document.getElementById('chart_div'));

        lineChart.draw(data, options);
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
	            if(currentState==0){
	            	currentState=1;
	            	readpoint[0]=event.data;
	            }else if(currentState==1){
	            	currentState=2;
	            	readpoint[1]=event.data;
	            }else if(currentState==2){
	            	currentState=3;
	            	readpoint[2]=event.data;
	            }else if(currentState==3){
	            	currentState=4;
	            	readpoint[3]=event.data;
	            }else if (currentState==4){
	            	overall=overall+1;
	            	//totalsenti=parseFloat(totalsenti)+parseFloat(event.data);
	            	//writeResponse(event.data*100);
	            	currentState=0;
	            	readpoint[4]=event.data;
	            	var tmpa=new google.maps.LatLng(readpoint[1],readpoint[2]);
	            	var tmpb = new google.maps.Marker({
						map: map,
						position: tmpa});
	           		mc.addMarker(tmpb,false);
	            	
	            	var tmpc = new google.maps.InfoWindow();
	            	tmpc.setContent('sentiment: '+readpoint[4]+' | '+readpoint[3]+' : '+readpoint[0]);
					  google.maps.event.addListener(tmpb, 'click', function() {
						  tmpc.open(map, tmpb);
					  });
					var number=0;
					if(event.data>0){
						number=Math.ceil((event.data+1)*5);
						for(var i=0;i<number;i++){
							var lat = (parseFloat(readpoint[1])+parseFloat(Math.random()/100));
							var lon = (parseFloat(readpoint[2])+parseFloat(Math.random()/100));
							writeResponse(lat);
							pointArray2.push(new google.maps.LatLng(lat,lon));	
						}
					}
					if(event.data<0){
						number=Math.ceil((-event.data+1)*5);
						for(var i=0;i<number;i++){
							var lat = (parseFloat(readpoint[1])+parseFloat(Math.random()/100));
							var lon = (parseFloat(readpoint[2])+parseFloat(Math.random()/100));
							pointArray3.push(new google.maps.LatLng(lat,lon));	
						}
					}
					pointArray.push(new google.maps.LatLng(readpoint[1],readpoint[2]));
	            	
					if(readpoint[4]>=-1 && readpoint[4]<-0.8) lineChartData[1][1]++;
					else if(readpoint[4]>=-0.8 && readpoint[4]<-0.6) lineChartData[2][1]++;
					else if(readpoint[4]>=-0.6 && readpoint[4]<-0.4) lineChartData[3][1]++;
					else if(readpoint[4]>=-0.4 && readpoint[4]<-0.2) lineChartData[4][1]++;
					else if(readpoint[4]>=-0.2 && readpoint[4]<0) lineChartData[5][1]++;
					else if(readpoint[4]>=0 && readpoint[4]<0.2) lineChartData[6][1]++;
					else if(readpoint[4]>=0.2 && readpoint[4]<0.4) lineChartData[7][1]++;
					else if(readpoint[4]>=0.4 && readpoint[4]<0.6) lineChartData[8][1]++;
					else if(readpoint[4]>=0.6 && readpoint[4]<0.8) lineChartData[9][1]++;
					else lineChartData[10][1]++;
					if(drawChartCount%drawChartRate == 0) drawChart();
					drawChartCount++;
					
	            	/*var pointArray = new google.maps.MVCArray(tmpa);
	        		heatmap = new google.maps.visualization.HeatmapLayer({
	        		    data: pointArray
	        		  });*/
	        		//var mcOptions = {gridSize: 50, maxZoom: 15};
	        		//mc = new MarkerClusterer(map);
	        		//mc.add
	        		//heatmap.setMap(map2);	
	            	
					//writeResponse(overall);
	            	
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
	        messages.innerHTML =  text;
	    }
	   
		function buttonSubmitFunction(){
			infowindow=[];taxiData=[];
			var i=0;
			for(;i<marker.length;i++){
    			marker[i].setMap(null);
    		}
			marker=[];
			mainMap();
			//heatMap();
			var mcOptions = {gridSize: 50, maxZoom: 15};
			mc = new MarkerClusterer(map,marker,mcOptions);
			var text = document.getElementById("wordtobesearch").value;
			var option = document.createElement("option");
			option.text = text;
			document.getElementById("filter").add(option);
			if (text!=""){
				webSocket.send(text);
				document.getElementById("buttonSubmit").disabled=true;
				document.getElementById("stopSubmit").disabled=false;
			}
		}
		function filterFunction(){
			infowindow=[];taxiData=[];
			var i=0;
			for(;i<marker.length;i++){
    			marker[i].setMap(null);
    		}
			marker=[];
			mainMap();
			//heatMap();
			pointArray.clear();
			pointArray2.clear();
			var mcOptions = {gridSize: 50, maxZoom: 15};
			mc = new MarkerClusterer(map,marker,mcOptions);
			var text = document.getElementById("filter").value;
			if (text!=""){
				webSocket.send(text);
			}
			overall=0.0;
			totalsenti=0.0;
		}
		function stopSubmitFunction(){
			webSocket.send("----stop");
			infowindow=[];taxiData=[];marker=[];
			document.getElementById("buttonSubmit").disabled=false;
			document.getElementById("stopSubmit").disabled=true;
		}
	
</script>
<%
	ArrayList<String> filterList=(ArrayList<String>)request.getAttribute("filterList");
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
Filter Selection<form action="MyServlet" method="get">
<select id="filter" name="filter">
	<%
		for(int i=0;i<filterList.size();i++){
	%>
	<option value="<%=filterList.get(i)%>"<%if(filterList.get(i).equals(filterKey)){%>selected="selected"<%}%>><%=filterList.get(i)%></option>
	<%
		}
	%>
</select>
<input type="button" onclick="filterFunction()" value="filter">
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
</td>
<td valign="top">
<div id="mapContainer3" style="width:500px;height:500px">
</div>
</td>
</tr>
</table>
<div id="chart_div" style="width:900px;height:500px"></div>
</body>
</html>
