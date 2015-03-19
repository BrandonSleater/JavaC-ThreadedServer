package cst420.thread.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * Purpose: Class acts a client GUI interface for displaying and
 * manipulating a waypoint application
 * 
 * @author Brandon Sleater
 * @version November 20, 2014
 */
public class WaypointGUI extends JFrame {

  /**
  * frWps is the JComboBox in the upper left of the waypoint browser.
  */
  protected JComboBox frWps;

  /**
   * toWps is the JComboBox just above the Distance and Bearing JButton
   * in the waypoint browser.
   */
  protected JComboBox toWps;

  /**
   * latIn is the JTextField labeled lat in the waypoint browser.
   */
  protected JTextField latIn;

  /**
   * lonIn is the JTextField labeled lon in the waypoint browser.
   */
  protected JTextField lonIn;

  /**
   * eleIn is the JTextField labeled ele in the waypoint browser.
   */
  protected JTextField eleIn;

  /**
   * namIn is the JTextField labeled name in the waypoint browser.
   */
  protected JTextField namIn;

  /**
   * distBearIn is the JTextField to the right of the Distance and Bearing
   * button in the waypoint browser. The field is for displaying the
   * distance and bearing between from and to waypoints.
   */
  protected JTextField distBearInGC;

  /**
   * addrIn is the JTextArea to the right of addr label. Its for entering
   * and displaying a waypoint's address.
   */
  protected JTextArea addrIn;

  /**
   * removeWPButt is the JButton just below the to waypoint drop-down.
   * When the user clicks Remove Waypoint, the waypoint named in the 
   * namIn JTextField should be removed from the server.
   */
  protected JButton removeWPButt;

  /**
   * addWPButt is the JButton labeled Add Waypoint.
   * When the user clicks Add Waypoint, the current values of the fields on
   * the right of the GUI are used to create and register a new waypoint
   * with the server
   */
  protected JButton addWPButt;

  /**
   * modWPButt is the JButton labeled Modify Waypoint
   * When the user clicks Modify Waypoint, the fields on the right side
   * of the GUI are used modify an existing waypoint. The name of a Waypoint
   * cannot be modified.
   */
  protected JButton modWPButt;

  /**
   * getAddrButt is the JButton labeled Get Addr for lat/lon.
   * This button will be used in a later assignment.
   * When the user clicks this button, the client uses a web service to
   * obtain the street address of the specified lat/lon.
   */
  protected JButton getAddrButt;

  /**
   * getLatLonButt is the JButton labeled Get lat/lon for Addr.
   * This button will be used in a later assignment.
   * When the user clicks this button, the client uses a web service to
   * obtain the latitude and longitude of the address specified in the
   * address text area.
   */
  protected JButton getLatLonButt;

  /**
   * distBearButtGC is the JButton bottom button.
   * When the user clicks Distance and Bearing, the direction and distance 
   * between the from waypoint and the to waypoint should be displayed
   * in the distBearIn text field.
   */
  protected JButton distBearButtGC;

  private JLabel latLab, lonLab, eleLab, nameLab, addrLab, fromLab, toLab;


  public WaypointGUI() {

    Toolkit tk = Toolkit.getDefaultToolkit();
    
    getContentPane().setLayout(null);
    setSize(500,390);

    frWps = new JComboBox();
    frWps.setBounds(40,10,160,25);
    getContentPane().add(frWps);
    
    fromLab = new JLabel("from");
    fromLab.setBounds(10, 10, 30, 25);
    getContentPane().add(fromLab);

    toWps = new JComboBox();
    toWps.setBounds(40,45,160,25);
    getContentPane().add(toWps);
    
    toLab = new JLabel("to");
    toLab.setBounds(10, 45, 30, 25);
    getContentPane().add(toLab);

    removeWPButt = new JButton("Remove Waypoint");
    removeWPButt.setBounds(40, 80, 135, 25);
    removeWPButt.setActionCommand("Remove");
    getContentPane().add(removeWPButt);

    addWPButt = new JButton("Add Waypoint");
    addWPButt.setBounds(60, 115, 115, 25);
    addWPButt.setActionCommand("Add");
    getContentPane().add(addWPButt);

    modWPButt = new JButton("Modify Waypoint");
    modWPButt.setBounds(40, 150, 135, 25);
    modWPButt.setActionCommand("Modify");
    getContentPane().add(modWPButt);

    getAddrButt = new JButton("Get Addr for lat/lon");
    getAddrButt.setBounds(20, 185, 180, 25);
    getAddrButt.setActionCommand("GetAddr");
    getContentPane().add(getAddrButt);

    getLatLonButt = new JButton("Get lat/lon for Addr");
    getLatLonButt.setBounds(20, 220, 180, 25);
    getLatLonButt.setActionCommand("GetLatLon");
    getContentPane().add(getLatLonButt);

    distBearButtGC = new JButton("Distance and Bearing");
    distBearButtGC.setBounds(20, 290, 180, 25);
    distBearButtGC.setActionCommand("DistanceGC");
    getContentPane().add(distBearButtGC);

    latIn = new JTextField();
    latIn.setBounds(250, 10, 230, 25);
    getContentPane().add(latIn);

    latLab = new JLabel("lat");
    latLab.setBounds(225, 10, 25, 25);
    getContentPane().add(latLab);

    lonIn = new JTextField();
    lonIn.setBounds(250, 45, 230, 25);
    getContentPane().add(lonIn);

    lonLab = new JLabel("lon");
    lonLab.setBounds(225, 45, 25, 25);
    getContentPane().add(lonLab);

    eleIn = new JTextField();
    eleIn.setBounds(250, 80, 230, 25);
    getContentPane().add(eleIn);

    eleLab = new JLabel("ele");
    eleLab.setBounds(225, 80, 25, 25);
    getContentPane().add(eleLab);

    namIn = new JTextField();
    namIn.setBounds(250, 115, 230, 25);
    getContentPane().add(namIn);

    nameLab = new JLabel("name");
    nameLab.setBounds(210, 115, 35, 25);
    getContentPane().add(nameLab);

    addrIn = new JTextArea();
    addrIn.setBounds(250, 150, 230, 70);
    getContentPane().add(addrIn);

    addrLab = new JLabel("addr");
    addrLab.setBounds(210, 150, 35, 25);
    getContentPane().add(addrLab);

    distBearInGC = new JTextField("dist/bearing (gc)");
    distBearInGC.setBounds(225, 290, 255, 25);
    getContentPane().add(distBearInGC);

    setVisible(true);
  }
}
