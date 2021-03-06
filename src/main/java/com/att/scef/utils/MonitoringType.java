package com.att.scef.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adi Enzel on 3/23/17.
 *
 * @author <a href="mailto:aa7133@att.com"> Adi Enzel </a>
 */
public class MonitoringType {
  public static final int LOSS_OF_CONNECTIVITY = 0;
  public static final int UE_REACHABILITY = 1;
  public static final int LOCATION_REPORTING = 2;
  public static final int CHANGE_OF_IMSI_IMEI_ASSOCIATION = 3;
  public static final int ROAMING_STATUS = 4;
  public static final int COMMUNICATION_FAILURE = 5;
  public static final int AVAILABILITY_AFTER_DDN_FAILURE = 6;
  public static final int NUMBER_OF_UES_PRESENT_IN_A_GEOGRAPHICAL_AREA = 7;
  
  public static final int  UE_REACHABILITY_REACHABLE_FOR_DATA = 1;
  public static final int  UE_REACHABILITY_REACHABLE_FOR_SMS = 0;
  
  public static final int  CAUSE_TYPE_RADIO_NETWORK_LAYER = 0;
  public static final int  CAUSE_TYPE_TRANSPORT_LAYER = 1;
  public static final int  CAUSE_TYPE_NAS = 2;
  public static final int  CAUSE_TYPE_PROTOCOL = 3;
  public static final int  CAUSE_TYPE_MISCELLANEOUS = 4;
 
  
  private static final int LOSS_OF_CONNECTIVITY_FLAG = 1;
  private static final int UE_REACHABILITY_FLAG = 2;
  private static final int LOCATION_REPORTING_FLAG = 4;
  private static final int CHANGE_OF_IMSI_IMEI_ASSOCIATION_FLAG = 8;
  private static final int ROAMING_STATUS_FLAG = 16;
  private static final int COMMUNICATION_FAILURE_FLAG = 32;
  private static final int AVAILABILITY_AFTER_DDN_FAILURE_FLAG = 64;
  private static final int NUMBER_OF_UES_PRESENT_IN_A_GEOGRAPHICAL_AREA_FLAG = 128;
  
  public static int getNewMonitoringMap(int oldMap, int newMap) {
    int val = (oldMap ^ newMap);
    val = val & newMap;
    return val;
  }

  public static int getDeletedMonitoringMap(int oldMap, int newMap) {
    int val = (oldMap ^ newMap);
    val = val & oldMap;
    return val;
  }
  
  public static int getRemaindMonitoringMap(int oldMap, int newMap) {
    int val = (oldMap & newMap);
    return val;
  }

  public static int getnextMonitoringMap(int oldMap, int newMap) {
	  return getNewMonitoringMap(oldMap, newMap) | getRemaindMonitoringMap(oldMap, newMap);
  }
  
  public static List<Integer> getNewMonitoringTypeList(int oldMap, int newMap) {
	  return getMonitorinTypeList(getNewMonitoringMap(oldMap, newMap));
  }
  
  public static List<Integer> getDeletedMonitoringTypeList(int oldMap, int newMap) {
	  return getMonitorinTypeList(getDeletedMonitoringMap(oldMap, newMap));
  }

  public static List<Integer>  getRemaindMonitoringTypeList(int oldMap, int newMap) {
	  return getMonitorinTypeList(getRemaindMonitoringMap(oldMap, newMap));
  }

  public static List<Integer>  getnextMonitoringTypeList(int oldMap, int newMap) {
	  return getMonitorinTypeList(getnextMonitoringMap(oldMap, newMap));
  }
  
  public static List<Integer> getMonitorinTypeList(int m) {
	  List<Integer> l = new ArrayList<Integer>();
	  
	  if ((m & MonitoringType.LOSS_OF_CONNECTIVITY_FLAG) != 0) {
		  l.add(MonitoringType.LOSS_OF_CONNECTIVITY);
	  }
	  if ((m & MonitoringType.UE_REACHABILITY_FLAG) != 0) {
		  l.add(MonitoringType.UE_REACHABILITY);
	  }
	  if ((m & MonitoringType.LOCATION_REPORTING_FLAG) != 0) {
		  l.add(MonitoringType.LOCATION_REPORTING);
	  }
	  if ((m & MonitoringType.CHANGE_OF_IMSI_IMEI_ASSOCIATION_FLAG) != 0) {
		  l.add(MonitoringType.CHANGE_OF_IMSI_IMEI_ASSOCIATION);
	  }
	  if ((m & MonitoringType.ROAMING_STATUS_FLAG) != 0) {
		  l.add(MonitoringType.ROAMING_STATUS);
	  }
	  if ((m & MonitoringType.COMMUNICATION_FAILURE_FLAG) != 0) {
		  l.add(MonitoringType.COMMUNICATION_FAILURE);
	  }
	  if ((m & MonitoringType.AVAILABILITY_AFTER_DDN_FAILURE_FLAG) != 0) {
		  l.add(MonitoringType.AVAILABILITY_AFTER_DDN_FAILURE);
	  }
	  if ((m & MonitoringType.NUMBER_OF_UES_PRESENT_IN_A_GEOGRAPHICAL_AREA_FLAG) != 0) {
		  l.add(MonitoringType.NUMBER_OF_UES_PRESENT_IN_A_GEOGRAPHICAL_AREA);
	  }
	  
	  return l;

  }
  
  public static String getMonitoringText(int monitoringType) {
    switch (monitoringType) {
    case LOSS_OF_CONNECTIVITY:
      return "LOSS_OF_CONNECTIVITY";
    case UE_REACHABILITY:
      return "UE_REACHABILITY";
    case LOCATION_REPORTING:
      return "LOCATION_REPORTING";
    case CHANGE_OF_IMSI_IMEI_ASSOCIATION:
      return "CHANGE_OF_IMSI_IMEI_ASSOCIATION";
    case ROAMING_STATUS:
      return "ROAMING_STATUS";
    case COMMUNICATION_FAILURE:
      return "COMMUNICATION_FAILURE";
    case AVAILABILITY_AFTER_DDN_FAILURE:
      return "AVAILABILITY_AFTER_DDN_FAILURE";
    case NUMBER_OF_UES_PRESENT_IN_A_GEOGRAPHICAL_AREA:
      return "NUMBER_OF_UES_PRESENT_IN_A_GEOGRAPHICAL_AREA";
    default:
      return null;
    }
  }
  
}
