package com.att.scef.utils;

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
import org.jdiameter.api.EventListener;
import org.jdiameter.api.Message;
import org.jdiameter.api.NetworkReqListener;
import org.jdiameter.api.Request;
import org.jdiameter.api.ResultCode;
import org.jdiameter.api.Stack;
import org.jdiameter.api.app.AppSession;
import org.jdiameter.api.app.StateChangeListener;
import org.jdiameter.client.api.ISessionFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 */
public abstract class TBase implements EventListener<Request, Answer>, NetworkReqListener, StateChangeListener<AppSession> {
  private final Logger logger = LoggerFactory.getLogger(TBase.class);

  protected boolean passed = true;

  // ------- those actually should come from conf... but..
  protected static final String clientHost = "127.0.0.1";
  protected static final String clientPort = "13868";
  protected static final String clientURI = "aaa://" + clientHost + ":" + clientPort;

  protected static final String serverHost = "127.0.0.1";
  protected static final String serverHost2 = "127.0.0.2";
  protected static final String serverPortNode1 = "4868";
  protected static final String serverPortNode2 = "4968";
  protected static final String serverURINode1 = "aaa://" + serverHost + ":" + serverPortNode1;
  protected static final String serverURINode2 = "aaa://" + serverHost2 + ":" + serverPortNode2;

  protected static final String serverRealm = "server.mobicents.org";
  protected static final String clientRealm = "client.mobicents.org";

  protected StackCreator stack;
  protected ISessionFactory sessionFactory;

  protected ApplicationId applicationId;

  public void init(InputStream configStream, String clientID, ApplicationId appId) throws Exception {
    this.applicationId = appId;
    stack = new StackCreator();
    stack.init(configStream, this, this, clientID, true, appId); // lets always pass
    this.sessionFactory = (ISessionFactory) this.stack.getSessionFactory();
  }

  public void updateAnswer(Message req, Message ans, int resultCode) {
    AvpSet reqSet = req.getAvps();
    
    Avp originHost = reqSet.getAvp(Avp.ORIGIN_HOST);
    Avp originRealm = reqSet.getAvp(Avp.ORIGIN_REALM);

    AvpSet set = ans.getAvps();

    set.insertAvp(0, Avp.SESSION_ID, req.getSessionId(), false);
    
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

  public boolean isPassed() {
    return passed;
  }

  public ApplicationId getApplicationId() {
    return applicationId;
  }

  protected String getClientURI() {
    return clientURI;
  }

  protected String getServerRealmName() {
    return serverRealm;
  }

  protected String getClientRealmName() {
    return clientRealm;
  }

  public Stack getStack() {
    return this.stack;
  }

  public ISessionFactory getSessionFactory() {
    return sessionFactory;
  }
  
  /**
   * @return
   */
  protected String getServerURI() {
    return serverURINode1;
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
