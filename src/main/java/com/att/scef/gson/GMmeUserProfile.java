package com.att.scef.gson;

public class GMmeUserProfile {
  GMonitoringEventConfig[] monitoringEvents;
  int active;
  
  public GMonitoringEventConfig[] getMonitoringEvents() {
    return monitoringEvents;
  }
  public void setMonitoringEvents(GMonitoringEventConfig[] monitoringEvents) {
    this.monitoringEvents = monitoringEvents;
  }
  public int getActive() {
    return active;
  }
  public void setActive(int active) {
    this.active = active;
  }
}
