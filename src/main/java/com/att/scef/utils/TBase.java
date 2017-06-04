package com.att.scef.utils;

import static org.jdiameter.client.impl.helpers.Parameters.OwnDiameterURI;
import static org.jdiameter.client.impl.helpers.Parameters.OwnRealm;
import static org.jdiameter.client.impl.helpers.Parameters.PeerTable;
import static org.jdiameter.client.impl.helpers.Parameters.PeerName;
import static org.jdiameter.client.impl.helpers.Parameters.RealmEntry;
import static org.jdiameter.client.impl.helpers.Parameters.RealmTable;
import static org.jdiameter.server.impl.helpers.Parameters.RealmName;

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual
 * contributors as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */
import java.io.InputStream;

import org.jdiameter.api.Answer;
import org.jdiameter.api.ApplicationId;
import org.jdiameter.api.Avp;
import org.jdiameter.api.AvpSet;
import org.jdiameter.api.Configuration;
import org.jdiameter.api.EventListener;
import org.jdiameter.api.Message;
import org.jdiameter.api.NetworkReqListener;
import org.jdiameter.api.Request;
import org.jdiameter.api.ResultCode;
import org.jdiameter.api.Stack;
import org.jdiameter.api.app.AppSession;
import org.jdiameter.api.app.StateChangeListener;
import org.jdiameter.client.api.ISessionFactory;
import org.jdiameter.common.impl.statistic.StatisticManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public abstract class TBase implements EventListener<Request, Answer>, NetworkReqListener, StateChangeListener<AppSession> {
  private final Logger logger = LoggerFactory.getLogger(TBase.class);


  protected StackCreator stack;
  protected ISessionFactory sessionFactory;
  private String remoteRealm = null;
  private String[] peers = null;
  private String destinationHost;
  private StatisticManagerImpl statistics;
  protected ApplicationId applicationId;

  public void init(InputStream configStream, String clientID, ApplicationId appId) throws Exception {
    this.applicationId = appId;
    stack = new StackCreator();
    stack.init(configStream, this, this, clientID, true, appId); // lets always pass
    this.sessionFactory = (ISessionFactory) this.stack.getSessionFactory();
    Configuration config = stack.getMetaData().getConfiguration();
    this.statistics = new StatisticManagerImpl(config);
    Configuration[] realmTable = config.getChildren(RealmTable.ordinal());
    for (Configuration t : realmTable) {
      for (Configuration e : t.getChildren(RealmEntry.ordinal())) {
        remoteRealm = e.getStringValue(RealmName.ordinal(), "");
        logger.info("Realm = " + remoteRealm);          
      }
    }
    Configuration[] peersConfig = config.getChildren(PeerTable.ordinal());
    peers = new String[peersConfig.length];
    for (int i = 0; i < peersConfig.length; i++) {
      peers[i] = peersConfig[i].getStringValue(PeerName.ordinal(), null);
      logger.info("Peer = " + peers[i]);
    }
    destinationHost = peers[0];
  }

  public StatisticManagerImpl getStatisticsManager() {
    return this.statistics;
  }

  public void updateAnswer(Message req, Message ans, int resultCode) {
    AvpSet reqSet = req.getAvps();
    
    Avp originHost = reqSet.getAvp(Avp.ORIGIN_HOST);
    Avp originRealm = reqSet.getAvp(Avp.ORIGIN_REALM);

    AvpSet set = ans.getAvps();

    //set.insertAvp(0, Avp.SESSION_ID, req.getSessionId(), false);
    
    set.addAvp(originHost);
    set.addAvp(originRealm);
    
    set.addAvp(Avp.AUTH_SESSION_STATE, 1);
    
    ans.setProxiable(true);
    ans.setRequest(false);
    if (resultCode != ResultCode.SUCCESS) {
      ans.setError(true);
        AvpSet experimentalResult = set.addGroupedAvp(Avp.EXPERIMENTAL_RESULT, 0, true, false);
        experimentalResult.addAvp(Avp.VENDOR_ID, 0);
        experimentalResult.addAvp(Avp.EXPERIMENTAL_RESULT_CODE, resultCode);
    }
    AvpSet vendorSpecificApplicationId = set.addGroupedAvp(Avp.VENDOR_SPECIFIC_APPLICATION_ID, 0, false, false);
    vendorSpecificApplicationId.addAvp(Avp.VENDOR_ID, this.getApplicationId().getVendorId(), true);
    vendorSpecificApplicationId.addAvp(Avp.AUTH_APPLICATION_ID, this.getApplicationId().getAuthAppId(), true);
  }

  // ----------- helper
  /*
  public Request createRequest(AppSession session, int code) {
    Request r = session.getSessions().get(0).createRequest(code, getApplicationId(), this.getRemoteRealm());

    AvpSet reqSet = r.getAvps();
    AvpSet vendorSpecificApplicationId = reqSet.addGroupedAvp(Avp.VENDOR_SPECIFIC_APPLICATION_ID, 0, false, false);
    // 1* [ Vendor-Id ]
    vendorSpecificApplicationId.addAvp(Avp.VENDOR_ID, getApplicationId().getVendorId(), true);
    // 0*1{ Auth-Application-Id }
    vendorSpecificApplicationId.addAvp(Avp.AUTH_APPLICATION_ID, getApplicationId().getAuthAppId(), true);
    // 0*1{ Acct-Application-Id }
    // { Auth-Session-State }
    reqSet.addAvp(Avp.AUTH_SESSION_STATE, 1); // no session maintiand
    // { Origin-Host }
    reqSet.removeAvp(Avp.ORIGIN_HOST);
    reqSet.addAvp(Avp.ORIGIN_HOST, this.getStack().getMetaData().getConfiguration()
        .getStringValue(OwnDiameterURI.ordinal(), (String) OwnDiameterURI.defValue()), true);

    return r;
  }  
*/
  protected Request createRequest(AppSession session, int code) {
    Request r = session.getSessions().get(0).createRequest(code, getApplicationId(), this.getRemoteRealm());

    AvpSet reqSet = r.getAvps();
    Avp vsa = reqSet.getAvp(Avp.VENDOR_SPECIFIC_APPLICATION_ID);
    if (vsa == null) {
      AvpSet vendorSpecificApplicationId = reqSet.addGroupedAvp(Avp.VENDOR_SPECIFIC_APPLICATION_ID, 0, false, false);
      // 1* [ Vendor-Id ]
      vendorSpecificApplicationId.addAvp(Avp.VENDOR_ID, getApplicationId().getVendorId(), true);
      // 0*1{ Auth-Application-Id }
      vendorSpecificApplicationId.addAvp(Avp.AUTH_APPLICATION_ID, getApplicationId().getAuthAppId(), true);
    }
    // 0*1{ Acct-Application-Id }
    // { Auth-Session-State }
    reqSet.addAvp(Avp.AUTH_SESSION_STATE, 1);
    // { Origin-Host }
    reqSet.removeAvp(Avp.ORIGIN_HOST);
    reqSet.addAvp(Avp.ORIGIN_HOST, this.getStack().getMetaData().getConfiguration()
        .getStringValue(OwnDiameterURI.ordinal(), (String) OwnDiameterURI.defValue()), true);
    return r;
  }

  protected Request createRequest(AppSession session, int code, String msisdn, String bearerIdentification) {
    Request r = session.getSessions().get(0).createRequest(code, getApplicationId(), this.getRemoteRealm());

    AvpSet reqSet = r.getAvps();
    
    AvpSet vspec = reqSet.removeAvp(Avp.VENDOR_SPECIFIC_APPLICATION_ID);
    AvpSet destRelm = reqSet.removeAvp(Avp.DESTINATION_REALM);
    AvpSet origRelm = reqSet.removeAvp(Avp.ORIGIN_REALM);
    
    
    AvpSet userIdentifier = reqSet.addGroupedAvp(Avp.USER_IDENTIFIER, getApplicationId().getVendorId(), true, false);

    if (msisdn != null && msisdn.length() != 0) {
      userIdentifier.addAvp(Avp.MSISDN, BCDStringConverter.toBCD(msisdn), getApplicationId().getVendorId(), true,
          false);
    }

    reqSet.addAvp(Avp.BEARER_IDENTIFIER, bearerIdentification, true, true, true); //(Avp.BEARER_IDENTIFIER, bearerIdentification, true);
    
    if (vspec != null) {
      reqSet.addAvp(vspec);
    }
    else {
      AvpSet vendorSpecificApplicationId = reqSet.addGroupedAvp(Avp.VENDOR_SPECIFIC_APPLICATION_ID, 0, false, false);
      // 1* [ Vendor-Id ]
      vendorSpecificApplicationId.addAvp(Avp.VENDOR_ID, getApplicationId().getVendorId(), true);
      // 0*1{ Auth-Application-Id }
      vendorSpecificApplicationId.addAvp(Avp.AUTH_APPLICATION_ID, getApplicationId().getAuthAppId(), true);
      // 0*1{ Acct-Application-Id }
      
    }
    reqSet.addAvp(destRelm);
    reqSet.addAvp(origRelm);
    
    // { Auth-Session-State }
    reqSet.addAvp(Avp.AUTH_SESSION_STATE, 1);
    // { Origin-Host }
    reqSet.removeAvp(Avp.ORIGIN_HOST);
    reqSet.addAvp(Avp.ORIGIN_HOST, this.getStack().getMetaData().getConfiguration()
        .getStringValue(OwnDiameterURI.ordinal(), (String) OwnDiameterURI.defValue()), true);
    return r;
  }

  public String getRemoteRealm() {
    return remoteRealm;
  }

  public ApplicationId getApplicationId() {
    return applicationId;
  }
  
  public String getFirstPeerFromList() {
    return this.peers[0];
  }
  
  public String getDestinationHost() {
    return this.destinationHost;
  }
  
  public Stack getStack() {
    return this.stack;
  }

  public ISessionFactory getSessionFactory() {
    return sessionFactory;
  }
  
  public void checkConfig() {
    Configuration config = this.getStack().getMetaData().getConfiguration();
    Configuration[] realmTable = config.getChildren(RealmTable.ordinal());
    for (Configuration rt : realmTable) {
      for (Configuration e : rt.getChildren(RealmEntry.ordinal())) {
        logger.info("To Realm = " + e.getStringValue(RealmName.ordinal(), ""));          
      }
    }
    Configuration[] peersConfig = config.getChildren(PeerTable.ordinal());
    for (int i = 0; i < peersConfig.length; i++) {
      logger.info("To Peer = " + peersConfig[i].getStringValue(PeerName.ordinal(), null));
    }
    logger.info("Local URI = " + config.getStringValue(OwnDiameterURI.ordinal(), (String) OwnDiameterURI.defValue()));
    logger.info("Local Realm = " + config.getStringValue(OwnRealm.ordinal(), (String) OwnRealm.defValue()));
  }

  
  // --------- Default Implementation
  // --------- Depending on class it is overridden or by default makes test fail.
  @Override
  public void receivedSuccessMessage(Request request, Answer answer) {
    logger.error("Received \"SuccessMessage\" event, request[" + request + "], answer[" + answer + "]");
  }

  @Override
  public void timeoutExpired(Request request) {
    logger.error("Received \"Timoeout\" event, request[" + request + "]");
  }

  @Override
  public Answer processRequest(Request request) {
    logger.error("Received \"Request\" event, request[" + request + "]");
    return null;
  }

  // --- State Changes --------------------------------------------------------
  @Override
  public void stateChanged(Enum oldState, Enum newState) {
    // NOP
  }

  @Override
  public void stateChanged(AppSession source, Enum oldState, Enum newState) {
    // NOP
  }

}
