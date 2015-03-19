package cst420.thread.client;

import javax.swing.*;
import java.io.*;
import javax.swing.event.*;
import javax.swing.text.html.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.util.*;
import java.util.Arrays;
import java.text.DecimalFormat;

import java.net.URL;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;


/**
 * Purpose: Class acts a client which takes in user inputs and data, sends requests to the server
 * for data, and displays a collection of data.
 * 
 * @author Brandon Sleater
 * @version November 20, 2014
 */
public class WaypointClient extends WaypointGUI implements ActionListener, ItemListener {

  private static final boolean debugOn = true;
  public String lookupVal;

  public static String serviceURL;
  public JsonRpcRequestViaHttp server;
  public static int id = 0;


  public WaypointClient(String url) {

    try {

      serviceURL = url;

      try {
        server = new JsonRpcRequestViaHttp(new URL(serviceURL));
        distBearInGC.setText("Connected at: " + serviceURL);
      } catch (Exception ex) {
        System.out.println("Malformed URL " + ex.getMessage());
      }

      //Load any currently saved waypoints from the server into the GUI
      loadAllWaypoints();
     
      removeWPButt.addActionListener(this);
      addWPButt.addActionListener(this);
      modWPButt.addActionListener(this);
      getAddrButt.addActionListener(this);
      getLatLonButt.addActionListener(this);
      distBearButtGC.addActionListener(this);
      frWps.addItemListener(this);
      toWps.addItemListener(this);

      this.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
           System.exit(0);
        }
      });

      setVisible(true);
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Exception: " + ex.getMessage());
    }
  }


  private String packageCalcCall(String operation, String params) {

    JSONObject jsonObj = new JSONObject();
  
    jsonObj.put("jsonrpc", "2.0");
    jsonObj.put("method", operation);
    jsonObj.put("id", ++id);
    
    String almost   = jsonObj.toString();
    String toInsert = ",\"params\":"+params;

    String begin = almost.substring(0, almost.length() - 1);
    String end   = almost.substring(almost.length() - 1);
    String ret   = begin + toInsert + end;

    return ret;
  }


  /**
   * Function is used to refresh the dropdown list data. If running with a server
   * that other people manipulate, you would want to do this to get any data they added
   */
  public void loadAllWaypoints() {

    try {

      frWps.removeAllItems();
      toWps.removeAllItems();
  
      String jsonStr = this.packageCalcCall("getNamesFromLibrary", "null");
      String temp    = server.call(jsonStr);

      JSONObject points = new JSONObject(temp);
      
      Boolean isStr = false;

      try {
        String test = points.getString("result");
        isStr = true;
      } catch (JSONException e) { }

      //Ensure its an array
      if (!isStr) {

        JSONArray list = points.getJSONArray("result");

        for (int i = 0; i < list.length(); i++) {

          String value = list.getString(i);

          frWps.addItem(value);
          toWps.addItem(value);
        }

        loadWaypoint(String.valueOf(frWps.getSelectedItem()));
      }
    } catch(Exception ex) {
      System.out.println("RPC Exception Loading Points: " + ex.getMessage());
    }
  }


  /**
   * Function is used to load a single waypoint into the GUI
   */
  public void loadWaypoint(String name) {

    try {

      String params = "[\""+ name +"\"]";
  
      String jsonStr = this.packageCalcCall("getJSONPointFromLibrary", params);
      String temp    = server.call(jsonStr);

      JSONObject point = new JSONObject(temp);

      JSONObject res = point.getJSONObject("result");

      if (res.length() > 0) {

        point = point.getJSONObject("result").getJSONObject(name);

        namIn.setText(name);
        latIn.setText(String.format("%.4f", point.getDouble("lat")));
        lonIn.setText(String.format("%.4f", point.getDouble("lon")));
        eleIn.setText(String.format("%.4f", point.getDouble("ele")));
      }
    } catch(Exception ex) {
      System.out.println("RPC Exception Load Waypoint: " + ex.getMessage());
    }
  }


  public void addWaypoint() {

    String name = namIn.getText();
    String lat  = String.format("%.4f", Double.parseDouble(latIn.getText()));
    String lon  = String.format("%.4f", Double.parseDouble(lonIn.getText()));
    String ele  = String.format("%.4f", Double.parseDouble(eleIn.getText()));

    try {
  
      String params  = "[\"" + name + "\"," + lat + "," + lon + "," + ele + "]";
      String jsonStr = this.packageCalcCall("addWaypoint", params);

      server.call(jsonStr);
      loadAllWaypoints();

      frWps.setSelectedItem(name);
      toWps.setSelectedItem(name);
    } catch(Exception ex) {
      System.out.println("RPC Exception Add Waypoint: " + ex.getMessage());
    }
  }


  public void modifyWaypoint() {

    String name = namIn.getText();
    String lat  = String.format("%.4f", Double.parseDouble(latIn.getText()));
    String lon  = String.format("%.4f", Double.parseDouble(lonIn.getText()));
    String ele  = String.format("%.4f", Double.parseDouble(eleIn.getText()));

    try {
  
      String params  = "[\"" + name + "\"," + lat + "," + lon + "," + ele + "]";
      String jsonStr = this.packageCalcCall("modifyWaypoint", params);
      server.call(jsonStr);

      params  = "null";
      jsonStr = this.packageCalcCall("getActive", params);
      
      String temp       = server.call(jsonStr);
      JSONObject result = new JSONObject(temp);
      String active     = result.getString("result");

      loadAllWaypoints();

      frWps.setSelectedItem(active);
      toWps.setSelectedItem(active);
    } catch(Exception ex) {
      System.out.println("RPC Exception: " + ex.getMessage());
    }
  }


  public void removeWaypoint() {
  
    try {
  
      String params  = "null";
      String jsonStr = this.packageCalcCall("getActive", params);
      String temp    = server.call(jsonStr);

      JSONObject result = new JSONObject(temp);
      String active     = result.getString("result");

      params  = "[\""+ active +"\"]";
      jsonStr = this.packageCalcCall("removeWaypoint", params);
      
      String empty      = server.call(jsonStr);
      result            = new JSONObject(empty);
      Boolean lastEntry = result.getBoolean("result");

      if (lastEntry) {
        latIn.setText("");
        lonIn.setText("");
        eleIn.setText("");
        namIn.setText("");
      }

      loadAllWaypoints();
    } catch(Exception ex) {
      System.out.println("RPC Exception: " + ex.getMessage());
    }
  }


  public void calculateDistGC() {

    try {
  
      String wayFrom = String.valueOf(frWps.getSelectedItem());
      String wayTo   = String.valueOf(toWps.getSelectedItem());

      String params  = "[\"" + wayFrom + "\",\"" + wayTo + "\"]";
      String jsonStr = this.packageCalcCall("calcDistBear", params);
      
      String temp       = server.call(jsonStr);
      JSONObject result = new JSONObject(temp);
      JSONArray calc    = result.getJSONArray("result");

      //Clean up the output, 3 decimals should be fine
      DecimalFormat df = new DecimalFormat("#.####");

      distBearInGC.setText(df.format(calc.getDouble(0)) + " miles at " + df.format(calc.getDouble(1)));
    } catch(Exception ex) {
      System.out.println("RPC Exception: " + ex.getMessage());
    }
  }


  public void debug() {
  
    try {
  
      String jsonStr = this.packageCalcCall("debugLibrary", "null");
      server.call(jsonStr);
    } catch(Exception ex) {
      System.out.println("RPC Exception: " + ex.getMessage());
    }
  }


  public void itemStateChanged(ItemEvent event) {
     
    if (event.getStateChange() == ItemEvent.SELECTED) {
      try { loadWaypoint((String)event.getItem()); } catch (Exception ee) { ee.printStackTrace(); }
    }
  }


  /**
   * Function handles GUI actions that send requests up to the server
   */
  public void actionPerformed(ActionEvent e) {

    if (e.getActionCommand().equals("Add")) {
      addWaypoint();
    } else if (e.getActionCommand().equals("Remove")) {
      removeWaypoint();
    } else if (e.getActionCommand().equals("Modify")) {
      modifyWaypoint();
    } else if (e.getActionCommand().equals("DistanceGC")) {
      calculateDistGC();
    } else if (e.getActionCommand().equals("GetLatLon")) {
      debug();
    } else if (e.getActionCommand().equals("GetAddr")) {
      debug();
    }
  }


  public static void main(String args[]) {

    try {

      String url = "http://localhost:8080";

      if (args.length > 0){
        url = args[0];
      }
            
      WaypointClient cjc = new WaypointClient(url);         
    } catch (Exception e) {
      System.out.println("Error @ Main!");
    }
  }
}
