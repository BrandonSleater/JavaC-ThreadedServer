#include "WaypointGUI.cpp"
#include "waypointstubclient.h"

#include <FL/Fl.H>
#include <FL/Fl_Window.H>
#include <FL/Fl_Button.H>
#include <FL/Fl_Output.H>
#include <FL/Fl_Text_Display.H>
#include <FL/Fl_Text_Buffer.H>
#include <FL/Fl_Input_Choice.H>
#include <FL/Fl_Multiline_Input.H>
#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <jsonrpc/rpc.h>

/**
 * Purpose: Class acts a client object. Receives events from the GUI
 *          and communicates with the serverObj to push back data to the GUI
 * 
 * @author Brandon Sleater
 * @version November 20, 2014
 */

using namespace std;
using namespace jsonrpc;

class WaypointClient : public WaypointGUI {

  /**
   * Makes a call to the serverObj object to get the current batch of 
   * saved waypoints. It then will rebuild the dropdown names for both 
   * from and to. If there are no waypoints, it will display placeholders
   *
   * @params (WaypointClient * anInstance, bool isEmpty)
   *    -anInstance: pointer to client object
   *    -isEmpty: flag to determine the label value (if waypoint or placeholder)
   * @return void
   */
  static void rebuildDropdowns(WaypointClient * anInstance, bool isEmpty) {
    
    waypointstubClient server(new HttpClient(hostID));
    Json::Value names;

    Fl_Input_Choice * fromDD = anInstance->frWps;
    Fl_Input_Choice * toDD   = anInstance->toWps;

    fromDD->clear();
    toDD->clear();

    fromDD->add("From Waypoint");
    toDD->add("To Waypoint");

    //Get all the waypoints currently on the serverObj member
    try {
      names = server.getNamesFromLibrary();
    } catch (JsonRpcException e) { cout << e.what() << endl; }

    for (int i = 0; i < names.size(); ++i) {

      string temp = names[i].asString();

      fromDD->add(temp.c_str());
      toDD->add(temp.c_str());
    }

    if (!isEmpty) {

      string nextPoint;

      //Get the last loaded point
      try {
        nextPoint = server.getActive();
      } catch (JsonRpcException e) { cout << e.what() << endl; }

      loadPoint(anInstance, nextPoint);

      fromDD->value(nextPoint.c_str());
      toDD->value(nextPoint.c_str());
    } else {

      //Clear out the text fields
      loadPoint(anInstance);

      //Set the original placeholders
      fromDD->value(fromDD->menubutton()->menu()[0].label());
      toDD->value(toDD->menubutton()->menu()[0].label());
    } 
  }


  /**
   * Remove a waypoint. It will make sure that the user is 
   * removing an actual waypoint, in which case, it will send 
   * a request to the serverObj object to remove it. It then determines
   * if there are any waypoints left for repopulating the dropdowns correctly
   *
   * @params (Fl_Widget * w, void * userdata)
   *    -w: pointer to GUI object
   *    -userdata: pointer to client object
   * @return void
   */
  static void clickedRemoveWP(Fl_Widget * w, void * userdata) {
    
    waypointstubClient server(new HttpClient(hostID));
    WaypointClient * anInstance = (WaypointClient *)userdata;

    Fl_Input_Choice * fromDD = anInstance->frWps;
    Fl_Input_Choice * toDD   = anInstance->toWps;

    string fromDDVal = fromDD->value();
    string toDDVal   = toDD->value();

    if (fromDDVal != "From Waypoint" && toDDVal != "To Waypoint") {

      string point;
      string response;
      bool isEmpty = false;
      
      //Instead of trying to determine which dropdown was selected, just
      //get the last point we loaded
      try {

        response = server.removeWaypoint(point);
        point    = server.getActive();

        isEmpty = (response == "false") ? false : true;
      } catch (JsonRpcException e) { cout << e.what() << endl; }

      rebuildDropdowns(anInstance, isEmpty);
    } else {

      //Empty the text fields
      loadPoint(anInstance);  
    }
  }

  
  /**
   * Add a waypoint. Will act like an actual user application 
   * and recontact the serverObj to rebuild the dropdowns
   * "incase other users have added data"
   *
   * @params (Fl_Widget * w, void * userdata)
   *    -w: pointer to GUI object
   *    -userdata: pointer to client object
   * @return void
   */
  static void clickedAddWP(Fl_Widget * w, void * userdata) {
    
    waypointstubClient server(new HttpClient(hostID));

    WaypointClient * anInstance = (WaypointClient *)userdata;

    Fl_Input_Choice * fromWPChoice = anInstance->frWps;
    Fl_Input_Choice * toWPChoice   = anInstance->toWps;

    Fl_Input * theLat  = anInstance->latIn;
    Fl_Input * theLon  = anInstance->lonIn;
    Fl_Input * theEle  = anInstance->eleIn;
    Fl_Input * theName = anInstance->nameIn;
    
    string name(theName->value());
    string lat(theLat->value());
    string lon(theLon->value());
    string ele(theEle->value());

    double latDub = atof(lat.c_str());
    double lonDub = atof(lon.c_str());
    double eleDub = atof(ele.c_str());

    try {
      server.addWaypoint(name, latDub, lonDub, eleDub);
    } catch (JsonRpcException e) { cout << e.what() << endl; }

    //Pass false because we know the serverObj member is not empty
    rebuildDropdowns(anInstance, false);
  }


  /**
   * Modify a waypoint. In case the user
   * changes the name, we rebuild the dropdown
   *
   * @params (Fl_Widget * w, void * userdata)
   *    -w: pointer to GUI object
   *    -userdata: pointer to client object
   * @return void
   */
  static void clickedModifyWP(Fl_Widget * w, void * userdata) {
    
    waypointstubClient server(new HttpClient(hostID));
    WaypointClient * anInstance = (WaypointClient *)userdata;

    Fl_Input_Choice * fromWPChoice = anInstance->frWps;
    Fl_Input_Choice * toWPChoice   = anInstance->toWps;

    Fl_Input * theLat  = anInstance->latIn;
    Fl_Input * theLon  = anInstance->lonIn;
    Fl_Input * theEle  = anInstance->eleIn;
    Fl_Input * theName = anInstance->nameIn;
    
    string name(theName->value());
    string lat(theLat->value());
    string lon(theLon->value());
    string ele(theEle->value());

    double latDub = atof(lat.c_str());
    double lonDub = atof(lon.c_str());
    double eleDub = atof(ele.c_str());

    try {
      server.modifyWaypoint(name, latDub, lonDub, eleDub);
    } catch (JsonRpcException e) { cout << e.what() << endl; }

    rebuildDropdowns(anInstance, false);
  }


  /**
   * Grab data from both active waypoints
   * and calculate their distance and bearing
   *
   * @params (Fl_Widget * w, void * userdata)
   *    -w: pointer to GUI object
   *    -userdata: pointer to client object
   * @return void
   */
  static void clickedDistBearWP(Fl_Widget * w, void * userdata) {

    waypointstubClient server(new HttpClient(hostID));
    Json::Value calcVals;
    char result[40] = {}, dist[10], bear[10];

    WaypointClient * anInstance = (WaypointClient *)userdata;

    Fl_Input * distBearWP = anInstance->distBearIn;

    Fl_Input_Choice * fromWPChoice = anInstance->frWps;
    Fl_Input_Choice * toWPChoice   = anInstance->toWps;

    string fromWP(fromWPChoice->value());
    string toWP(toWPChoice->value());

    try {
      calcVals = server.calcDistBear(fromWP, toWP);
    } catch (JsonRpcException e) { cout << e.what() << endl; }

    sprintf(dist, "%2.2f", calcVals[0u].asDouble());
    sprintf(bear, "%2.2f", calcVals[1u].asDouble());

    strcat(result, dist);
    strcat(result, " miles at ");
    strcat(result, bear);
    strcat(result, " degrees");

    distBearWP->value(result);
  }


  /**
   * Used as middle function to determine
   * which dropdown was selected. Sends frWps flag
   *
   * @params (Fl_Widget * w, void * userdata)
   *    -w: pointer to GUI object
   *    -userdata: pointer to client object
   * @return void
   */
  static void selectedFrom(Fl_Widget * w, void * userdata) {
    selectedPoint(w, userdata, 0);
  }


  /**
   * Used as middle function to determine
   * which dropdown was selected. Sends toWps flag
   *
   * @params (Fl_Widget * w, void * userdata)
   *    -w: pointer to GUI object
   *    -userdata: pointer to client object
   * @return void
   */
  static void selectedTo(Fl_Widget * w, void * userdata) {
    selectedPoint(w, userdata, 1);
  }


  /**
   * Used as middle function to load a waypoint
   *
   * @params (Fl_Widget * w, void * userdata)
   *    -w: pointer to GUI object
   *    -userdata: pointer to client object
   *    -whichOne: 0 => fromWps, 1 => toWps
   * @return void
   */
  static void selectedPoint(Fl_Widget * w, void * userdata, int whichOne) {

    waypointstubClient server(new HttpClient(hostID));
    WaypointClient * anInstance = (WaypointClient *)userdata;

    Fl_Input_Choice * click = whichOne ? anInstance->toWps : anInstance->frWps;

    string selected(click->value());

    //Make sure they clicked a waypoint
    //else get the last loaded point and load it
    //(should mimic the current loaded point from not changing if placeholder is selected)
    if (selected != "From Waypoint" && selected != "To Waypoint") {
      loadPoint(anInstance, selected);
    } else {
      try {
        click->value(server.getActive().c_str());
      } catch (JsonRpcException e) { cout << e.what() << endl; }
    }
  }


  /**
   * Used to get the the values of a 
   * waypoint name to output onto the GUI
   *
   * @params (Fl_Widget * w, void * userdata)
   *    -anInstance: pointer to client object
   *    -nameArg: waypoint to load. If nothing 
   *              passed, sets textfields empty
   * @return void
   */
  static void loadPoint(WaypointClient * anInstance, string nameArg = "") {

    waypointstubClient server(new HttpClient(hostID));

    Fl_Input * theLat  = anInstance->latIn;
    Fl_Input * theLon  = anInstance->lonIn;
    Fl_Input * theEle  = anInstance->eleIn;
    Fl_Input * theName = anInstance->nameIn;

    if (!nameArg.empty()) {

      Json::Value point;

      try {
        point  = server.getJSONPointFromLibrary(nameArg);
      } catch (JsonRpcException e) { cout << e.what() << endl; }

      //Format our waypoint values (4 decimal places)
      char lat[10], lon[10], ele[10];

      sprintf(lat, "%4.4f", point[nameArg]["lat"].asDouble());
      sprintf(lon, "%4.4f", point[nameArg]["lon"].asDouble());
      sprintf(ele, "%4.4f", point[nameArg]["ele"].asDouble());

      theLat->value(lat);
      theLon->value(lon);
      theEle->value(ele);
      theName->value(nameArg.c_str());
    } else {

      //Clear 'em
      theLat->value("");
      theLon->value("");
      theEle->value("");
      theName->value("");
    }
  }


  /**
   * Used to display parameters and values
   * at current points throughout functions
   *
   * @params (string message)
   *    -message: concatenated text and numbers 
   * @return void
   */
  static void debug(string message) {
    if (debugOn) cout << message << endl;
  }


  /**
   * Used as a debug to display the current
   * contents of the waypoint library
   *
   * @params (Fl_Widget * w, void * userdata)
   *    -w: pointer to GUI object
   *    -userdata: pointer to client object
   * @return void
   */
  static void clickedGetAddrWP(Fl_Widget * w, void * userdata) {
    
    waypointstubClient server(new HttpClient(hostID));
    
    try {
      server.debugLibrary();
    } catch (JsonRpcException e) { cout << e.what() << endl; }
  }


  /**
   * Terminate the application
   *
   * @params (Fl_Widget * w, void * userdata)
   *    -w: pointer to GUI object
   *    -userdata: pointer to client object
   * @return void
   */
  static void clickedX(Fl_Widget * w, void * userdata) {
    exit(1);
  }


  public:
    static const bool debugOn = false;
    static string hostID;

    WaypointClient(string host, const char * name = 0) : WaypointGUI(name) {

      //Save the hostID
      hostID = host;

      waypointstubClient server(new HttpClient(hostID));

      bool isEmpty = true;
      string active;

      try {
        cout << "Connected to: " << server.serviceInfo() << endl;
        active = server.getActive();
        isEmpty = (active == "null") ? true : false;
      } catch (JsonRpcException e) { cout << e.what() << endl; }

      rebuildDropdowns(this, isEmpty);

      distBearButt->callback(clickedDistBearWP, (void *)this);
      removeWPButt->callback(clickedRemoveWP, (void *)this);
      addWPButt->callback(clickedAddWP, (void *)this);
      modWPButt->callback(clickedModifyWP, (void *)this);
      frWps->callback(selectedFrom, (void *)this);
      toWps->callback(selectedTo, (void *)this);

      //Debug
      getAddrButt->callback(clickedGetAddrWP);

      //Exit
      callback(clickedX);
    }
};

string WaypointClient::hostID;

int main(int argc, char * argv[]) {

  string host = "http://127.0.0.1:8080";

  if (argc > 1) {
    host = string(argv[1]);
  }
  
  WaypointClient wc(host, "Waypoint Browser");

  return (Fl::run());
}
