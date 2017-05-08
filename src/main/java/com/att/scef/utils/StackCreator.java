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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Set;

import org.jdiameter.api.Answer;
import org.jdiameter.api.ApplicationId;
import org.jdiameter.api.Configuration;
import org.jdiameter.api.EventListener;
import org.jdiameter.api.Network;
import org.jdiameter.api.NetworkReqListener;
import org.jdiameter.api.Request;
import org.jdiameter.server.impl.StackImpl;
import org.jdiameter.server.impl.helpers.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 */
public class StackCreator extends StackImpl {
  private final Logger logger = LoggerFactory.getLogger(StackCreator.class);

  public StackCreator() {
    super();
  }

  public StackCreator(InputStream streamConfig, NetworkReqListener networkReqListener,
      EventListener<Request, Answer> eventListener, String dooer, Boolean isServer, ApplicationId... appIds)
      throws Exception {
    init(
        isServer ? new XMLConfiguration(streamConfig)
            : new org.jdiameter.client.impl.helpers.XMLConfiguration(streamConfig),
        networkReqListener, eventListener, dooer, isServer, appIds);
  }

  public StackCreator(String stringConfig, NetworkReqListener networkReqListener,
      EventListener<Request, Answer> eventListener, String dooer, Boolean isServer, ApplicationId... appIds)
      throws Exception {
    init(
        isServer ? new XMLConfiguration(new ByteArrayInputStream(stringConfig.getBytes()))
            : new org.jdiameter.client.impl.helpers.XMLConfiguration(new ByteArrayInputStream(stringConfig.getBytes())),
        networkReqListener, eventListener, dooer, isServer, appIds);
  }

  public void init(String stringConfig, NetworkReqListener networkReqListener,
      EventListener<Request, Answer> eventListener, String dooer, Boolean isServer, ApplicationId... appIds)
      throws Exception {
    this.init(
        isServer ? new XMLConfiguration(new ByteArrayInputStream(stringConfig.getBytes()))
            : new org.jdiameter.client.impl.helpers.XMLConfiguration(new ByteArrayInputStream(stringConfig.getBytes())),
        networkReqListener, eventListener, dooer, isServer, appIds);
  }

  public void init(InputStream streamConfig, NetworkReqListener networkReqListener,
      EventListener<Request, Answer> eventListener, String dooer, Boolean isServer, ApplicationId... appIds)
      throws Exception {
    this.init(
        isServer ? new XMLConfiguration(streamConfig)
            : new org.jdiameter.client.impl.helpers.XMLConfiguration(streamConfig),
        networkReqListener, eventListener, dooer, isServer, appIds);
  }

  public void init(Configuration config, NetworkReqListener networkReqListener, EventListener<Request, Answer> eventListener,
      String identifier, Boolean isServer, ApplicationId... appIds) throws Exception {
    // local one
    try {
      this.init(config);

      // Let it stabilize...
      Thread.sleep(500);

      Network network = unwrap(Network.class);

      if (appIds != null) {

        for (ApplicationId appId : appIds) {
          if (logger.isInfoEnabled()) {
            logger.info("Diameter " + identifier + " :: Adding Listener for [" + appId + "].");
          }
          network.addNetworkReqListener(networkReqListener, appId);
        }

        if (logger.isInfoEnabled()) {
          logger.info("Diameter " + identifier + " :: Supporting " + appIds.length + " applications.");
        }
      } else {
        Set<ApplicationId> stackAppIds = getMetaData().getLocalPeer().getCommonApplications();

        for (ApplicationId appId : stackAppIds) {
          if (logger.isInfoEnabled()) {
            logger.info("Diameter " + identifier + " :: Adding Listener for [" + appId + "].");
          }
          network.addNetworkReqListener(networkReqListener, appId);
        }

        if (logger.isInfoEnabled()) {
          logger.info("Diameter " + identifier + " :: Supporting " + stackAppIds.size() + " applications.");
        }
      }
    } catch (Exception e) {
      logger.error("Failure creating stack '" + identifier + "'", e);
    }
  }

}
