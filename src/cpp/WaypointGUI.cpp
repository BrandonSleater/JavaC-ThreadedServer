#include <FL/Fl.H>
#include <FL/Fl_Window.H>
#include <FL/Fl_Button.H>
#include <FL/Fl_Output.H>
#include <FL/Fl_Text_Display.H>
#include <FL/Fl_Text_Buffer.H>
#include <FL/Fl_Input_Choice.H>
#include <FL/Fl_Multiline_Input.H>
#include <stdio.h>
#include <iostream>
#include <stdlib.h>

/**
 * Purpose: Class acts a GUI for manipulating waypoints
 * 
 * @author Brandon Sleater
 * @version November 20, 2014
 */

using namespace std;

class WaypointGUI : public Fl_Window {
  
  protected:
    //frWps is the Fl_Input_Choice in the upper left of the waypoint browser.
    Fl_Input_Choice * frWps;

    //toWps is the Fl_Input_Choice just below the from waypoint drop-down list in the waypoint browser.
    Fl_Input_Choice * toWps;

    //latIn is the Fl_Input labeled lat in the waypoint browser.
    Fl_Input * latIn;

    //lonIn is the Fl_Input labeled lon in the waypoint browser.
    Fl_Input * lonIn;

    //eleIn is the Fl_Input labeled ele in the waypoint browser.
    Fl_Input * eleIn;

    //nameIn is the Fl_Input labeled name in the waypoint browser.
    Fl_Input * nameIn;

    //distBearIn is the Fl_Input to the right of the Distance and Bearing
    //button in the waypoint browser. The field is for displaying the
    //distance and bearing between from and to waypoints.
    Fl_Input * distBearIn;
   
    //addrIn is the Fl_Multiline_Input to the right of addr label.
    //Its for entering and displaying a waypoint's address.
    Fl_Multiline_Input * addrIn;

    //removeWPButt is the Fl_Button just below the to waypoint drop-down.
    //When the user clicks Remove Waypoint, the waypoint selected in
    //frWps from waypoint drop-down list should be removed from the server.
    Fl_Button * removeWPButt;

    //addWPButt is the Fl_Button labeled Add Waypoint.
    //When the user clicks Add Waypoint, the current values of the fields on
    //the right of the GUI are used to create and register a new waypoint
    //with the server
    Fl_Button * addWPButt;

    //modWPButt is the Fl_Button labeled Modify Waypoint
    //When the user clicks Modify Waypoint, the fields on the right side
    //of the GUI are used modify an existing waypoint. The name of a Waypoint
    //cannot be modified.
    Fl_Button * modWPButt;

    //getAddrButt is the Fl_Button labeled Get Addr for lat/lon.
    //This button will be used in a later assignment.
    //When the user clicks this button, the client uses a web service to
    //obtain the street address of the specified lat/lon.
    Fl_Button * getAddrButt;

    //distBearButt is the Fl_Button bottom button.
    //When the user clicks Distance and Bearing, the direction and distance 
    //between the from waypoint and the to waypoint should be displayed
    //in the distBearIn text field.
    Fl_Button * distBearButt;
    Fl_Text_Display * aLab;
    Fl_Text_Buffer * buff;

  public:
    WaypointGUI(const char * name = 0) : Fl_Window(520, 300, name) {

      begin();

      frWps = new Fl_Input_Choice(40, 10, 200, 25, "from");
      frWps->add("From Waypoint");
      frWps->value("From Waypoint");

      toWps = new Fl_Input_Choice(40, 45, 200, 25, "to");
      toWps->add("To Waypoint");
      toWps->value("From Waypoint");

      latIn      = new Fl_Input(270, 10, 230, 25,"lat");
      lonIn      = new Fl_Input(270, 45, 230, 25,"lon");
      eleIn      = new Fl_Input(270, 80, 230, 25,"ele");
      nameIn     = new Fl_Input(270, 115, 230, 25,"name");
      addrIn     = new Fl_Multiline_Input(270, 150, 230, 70,"addr");
      distBearIn = new Fl_Input(225, 260, 255, 25);

      removeWPButt  = new Fl_Button(55, 80, 130, 25, "Remove Waypoint");
      addWPButt     = new Fl_Button(55, 115, 130, 25, "Add Waypoint");
      modWPButt     = new Fl_Button(55, 150, 130, 25, "Modify Waypoint");
      getAddrButt   = new Fl_Button(20, 185, 180, 25, "Get Addr for lat/lon");
      distBearButt  = new Fl_Button(20, 260, 180, 25, "Distance and Bearing");

      end();
      show();
    }
};
