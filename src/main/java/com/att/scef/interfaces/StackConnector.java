package com.att.scef.interfaces;

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


public class StackConnector extends StackImpl {
	protected final Logger logger = LoggerFactory.getLogger(StackConnector.class);
	
	public StackConnector() {
		super();
	}

	public void init(String configFile, NetworkReqListener networkReqListener,
			EventListener<Request, Answer> eventListener, String dooer, ApplicationId... appIds) throws Exception {
		this.init(new XMLConfiguration(configFile), networkReqListener, eventListener, dooer, appIds);
	}

	public void init(Configuration config, NetworkReqListener networkReqListener,
			EventListener<Request, Answer> eventListener, String identifier, ApplicationId... appIds) throws Exception {
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
