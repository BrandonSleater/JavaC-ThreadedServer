package cst420.thread.server;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.lang.reflect.*;

import org.json.JSONObject;
import org.json.JSONArray;

/**
 * Purpose: Class is ran at compile and handles taking in
 *          connections to the server and starting new threads
 * 
 * @author Brandon Sleater
 * @version November 20, 2014
 */
public class WaypointServer {

  public static void main(String[] args) throws IOException {
    
    //Default port
    String regPort = "8080";

    if (args.length >= 1) {
      regPort = args[0];
    }

    //Setup the server connection
    ServerSocket server = new ServerSocket(Integer.parseInt(regPort));
    
    System.out.println("Connected at port: " + regPort);

    while (true) {

      //Connect client to server
      Socket socket = server.accept();      
      Thread thread = new Server(socket);
      thread.start();
    }
  }
}


/**
 * Purpose: Class acts a waypoint managemeet server. 
 *          It takes in http json requests, parses them and 
 *          returns http json responses
 * 
 * @author Brandon Sleater
 * @version November 20, 2014
 */
class Server extends Thread {

  private Socket socket;

  //Counter for json responses
  private static int jsonid = 0;

  public static Map<String, Map<String, Double>> waypointList = new LinkedHashMap<String, Map<String, Double>>();

  //Holds the key name of the waypoint data currently being viewed
  public static String lastLookup = "";

  public final static boolean debugOn = false;
  
  public final static int STATUTE    = 0;
  public final static int NAUTICAL   = 1;
  public final static int KMETER     = 2;
  public final static double radiusE = 6371;


  /**
   * Constructor - Save client connection
   */
  public Server(Socket socket) {
    this.socket = socket;
  }


  /****************
   * BEGIN HANDLER - (Move to another file eventually)
   ***************/


  /**
   * Thread method that is automatically called, handle our client
   */
  public void run() {

    //Parse input/output
    try {
      Thread.sleep(100);
      postResponse();
    } catch (IOException ex) {
      System.out.println("Can't parse input or send a response!" + ex.getMessage());
    } catch (InterruptedException ie) {
      System.out.println("Can't sleep thread! " + ie.getMessage());
    } finally {
      closeSocket();
    }
  }


  /**
   * Handle the POST
   */
  public void postResponse() throws IOException {
    
    OutputStream out = null;
    InputStream in   = null;

    int bufferSize = 0;
    String json    = "";

    //Getters
    bufferSize = socket.getReceiveBufferSize();
    out        = socket.getOutputStream();
    in         = socket.getInputStream();

    //Container for request
    byte[] bytes = new byte[bufferSize];
  
    //Build the request
    for (int count = in.read(bytes); in.available() > 0; count = in.read(bytes)) { }

    //Convert it to a viewable message (this is the http request from client)
    String request = new String(bytes, "UTF-8");

    //Call the server method from the client request
    json = parseJSON(request);

    //Send back a message (this is the http response from server)
    String data = buildResponse(json.toString());
    out.write(data.getBytes());

    //Close up the resources
    out.flush();
    out.close();
    in.close();
  }


  /**
   * Close the connection with the client
   */
  public void closeSocket() {

    try {
      if (socket != null) socket.close();
    } catch (IOException e) {
      System.out.println("Error closing socket: " + e.getMessage());
    }
  }


  /**
   * Setup the http response header/content
   */
  public String buildResponse(String json) {

    String response = "";
    String newline  = System.getProperty("line.separator");

    int len = (int) json.length();

    response += "HTTP/1.1 200 OK" + newline;
    response += "Content-Length: " + len + newline;
    response += "Content-Type: application/json" + newline;
    response += newline;
    response += json + newline;

    return response;
  }


  /**
   * Parse through a client request to get the json 
   * message and handle it
   */
  public String parseJSON(String json) {

    String response = "";

    //Sometimes its in different places, so we'll search for the json notation {}
    final Pattern pattern = Pattern.compile("\\{(.*?)\\}");
    final Matcher matcher = pattern.matcher(json);

    //If we found json in the request
    if (matcher.find()) {

      String str     = "{" + matcher.group(1) + "}";
      JSONObject obj = new JSONObject(str);
      String method  = obj.getString("method");

      //No matter if null or not, we do a json array for consistency
      JSONArray params = (obj.isNull("params")) ? new JSONArray() : obj.getJSONArray("params");

      //Value returned from server function
      response = callFunction(method, params);
    }

    return response;
  }


  /**
   * Reflection intermediary to handle client request
   * to call a server function and setup a json result
   */
  protected String callFunction(String method, JSONArray params) {

    String json = "";
  
    try {

      Method func;
      Object value = null;

      if (params.length() > 0) {
                
        func = getClass().getDeclaredMethod(method, JSONArray.class);
        value = func.invoke(this, params);
      } else {
        
        func = getClass().getDeclaredMethod(method);
        value = func.invoke(this);
      }

      json = (String) value;
    } catch (Exception e) {
      e.printStackTrace();
    }

    return json;
  }


  /**************
   * END HANDLER
   **************/


  /**
   * Waypoint data view setter
   */
  public void setActive(String name) {
    lastLookup = name;
  }


  /**
   * Waypoint data view getter
   */
  public String getActive() {
    return setupJSONResult(lastLookup);
  }


  /**
   * Waypoint data view getter
   */
  public String getLibrarySize() {
    return setupJSONResult(waypointList.size());
  }


  /**
   * Basic message to send back server name to client
   */
  public String serviceInfo() {
    return setupJSONResult("WebServer");
  }


  /**
   * Function takes in the name of the waypoint and returns its data
   * Returns JSONObject(string => double)
   */
  public String getJSONPointFromLibrary(JSONArray arr) {

    JSONObject obj = new JSONObject();
    String name    = arr.getString(0);

    obj.put(name, waypointList.get(name));

    setActive(name);

    return setupJSONResult(obj);
  }


  /**
   * Function builds the names for the dropdown list of all waypoints
   */
  public String getNamesFromLibrary() {

    JSONArray arr = new JSONArray();

    if (waypointList.size() > 0) {
      
      for (String wayName : waypointList.keySet()) {
        arr.put(wayName);
      }
    }
    
    return setupJSONResult(arr);
  }


  /**
   * Add a users waypoint inputs into the list
   */
  public String addWaypoint(JSONArray params) {

    Map<String, Double> meta = new HashMap<String, Double>();

    String name = params.getString(0);    
    Double lat  = params.getDouble(1);
    Double lon  = params.getDouble(2);
    Double ele  = params.getDouble(3);

    meta.put("lat", lat);
    meta.put("lon", lon);
    meta.put("ele", ele);

    waypointList.put(name, meta);

    setActive(name);

    return setupJSONResult("passed");
  }


  /**
   * Pretty self explanatory on this one..
   */
  public String removeWaypoint(JSONArray params) {
  
    boolean lastEntry = false;
    String name       = params.getString(0);

    waypointList.remove(name);

    if (waypointList.size() == 0) {
      lastEntry = true;
    } else {

      //Set a random point as the new active
      ArrayList<String> temp = new ArrayList<String>(waypointList.keySet());
      setActive(temp.get(0));
    }

    return setupJSONResult(String.valueOf(lastEntry));
  }


  /**
   * Function will modify an existing waypoint. If the name hasn't changed, 
   * it will just replace it values. If the name did change, it will be recreated
   * and adding to the bottom of the list.
   */
  public String modifyWaypoint(JSONArray params) {

    Map<String, Double> meta = new HashMap<String, Double>();

    String oldName = getActive();
    String newName = params.getString(0);
    
    Double lat = params.getDouble(1);
    Double lon = params.getDouble(2);
    Double ele = params.getDouble(3);

    if (!oldName.equals(newName)) {

      JSONArray arr = new JSONArray();
      removeWaypoint(arr.put(oldName));
    }

    meta.put("lat", lat);
    meta.put("lon", lon);
    meta.put("ele", ele);

    waypointList.put(newName, meta);
    setActive(newName);

    return setupJSONResult("passed");
  }


  /**
   * Function takes 2 lookup names and performs GC calculations on them
   */
  public String calcDistBear(JSONArray params) {

    JSONArray arr = new JSONArray();

    String wayFrom = params.getString(0);
    String wayTo   = params.getString(1);

    Map<String, Double> fromPoint = waypointList.get(wayFrom);
    Map<String, Double> toPoint   = waypointList.get(wayTo);

    arr.put(distanceGCTo(fromPoint, toPoint, STATUTE));
    arr.put(bearingGCInitTo(fromPoint, toPoint));

    return setupJSONResult(arr);
  }


  /**
   * GC distance calculation
   */
  public double distanceGCTo(Map<String, Double> wayFrom, Map<String, Double> wayTo, int scale) {

    double ret       = 0.0;
    double dlatRad   = Math.toRadians(wayTo.get("lat") - wayFrom.get("lat"));
    double dlonRad   = Math.toRadians(wayTo.get("lon") - wayFrom.get("lon"));
    double latOrgRad = Math.toRadians(wayFrom.get("lat"));
    double formula   = Math.sin(dlatRad/2) * Math.sin(dlatRad/2) + Math.sin(dlonRad/2) * Math.sin(dlonRad/2) * Math.cos(latOrgRad) * Math.cos(Math.toRadians(wayTo.get("lat")));
    
    ret = radiusE * (2 * Math.atan2(Math.sqrt(formula), Math.sqrt(1 - formula)));
    
    //Ret is in kilometers. Switch to either Statute or Nautical?
    switch(scale) {
      case STATUTE:
        ret = ret * 0.62137119;
        break;
    
      case NAUTICAL:
        ret = ret * 0.5399568;
        break;
    }

    return ret;
  }


  /**
   * GC bearing calculation
   */
  public double bearingGCInitTo(Map<String, Double> wayFrom, Map<String, Double> wayTo) {

    double ret       = 0.0;
    double dlonRad   = Math.toRadians(wayTo.get("lon") - wayFrom.get("lon"));
    double latOrgRad = Math.toRadians(wayFrom.get("lat"));

    double y = Math.sin(dlonRad) * Math.cos(Math.toRadians(wayTo.get("lat")));
    double x = Math.cos(latOrgRad) * Math.sin(Math.toRadians(wayTo.get("lat"))) - Math.sin(latOrgRad) * Math.cos(Math.toRadians(wayTo.get("lat"))) * Math.cos(dlonRad);
    
    ret = Math.toDegrees(Math.atan2(y, x));
    ret = (ret + 360.0) % 360.0;
    
    return ret;
  }


  /**
   * Always have an idea of what is inside my server waypoint list
   */
  public String debugLibrary() {

    System.out.println("\n===============");
    System.out.println("STARTING DEBUG:");
    System.out.println("MAP SIZE = " + waypointList.size());
    System.out.println("CURRENT ACTIVE = " + lastLookup);

    String newLine = System.getProperty("line.separator");

    for (Map.Entry<String, Map<String, Double>> wayName : waypointList.entrySet()) {
      
      System.out.println(wayName.getKey() + newLine + "---------------");

      for (Map.Entry<String, Double> meta : wayName.getValue().entrySet()) {
        System.out.println(meta.getKey() + " = " + meta.getValue());
      }
    }

    System.out.println("===============\n");

    return setupJSONResult("passed");
  }


  /**
   * Passing a number
   */
  public String setupJSONResult(int value) {

    JSONObject obj = new JSONObject();

    obj.put("result", value);
    obj.put("jsonrpc", "2.0");
    obj.put("id", this.jsonid++);

    return obj.toString();
  }


  /**
   * Passing a string
   */
  public String setupJSONResult(String value) {

    JSONObject obj = new JSONObject();

    obj.put("result", value);
    obj.put("jsonrpc", "2.0");
    obj.put("id", this.jsonid++);

    return obj.toString();
  }


  /**
   * Passing an array
   */ 
  public String setupJSONResult(JSONArray value) {

    JSONObject obj = new JSONObject();

    obj.put("result", value);
    obj.put("jsonrpc", "2.0");
    obj.put("id", this.jsonid++);

    return obj.toString();
  }


  /**
   * Passing an object
   */ 
  public String setupJSONResult(JSONObject value) {

    JSONObject obj = new JSONObject();

    obj.put("result", value);
    obj.put("jsonrpc", "2.0");
    obj.put("id", this.jsonid++);

    return obj.toString();
  }
}
