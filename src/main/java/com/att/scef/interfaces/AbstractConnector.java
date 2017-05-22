package com.att.scef.interfaces;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jdiameter.api.Answer;
import org.jdiameter.api.ApplicationId;
import org.jdiameter.api.Configuration;
import org.jdiameter.api.EventListener;
import org.jdiameter.api.IllegalDiameterStateException;
import org.jdiameter.api.InternalException;
import org.jdiameter.api.Mode;
import org.jdiameter.api.NetworkReqListener;
import org.jdiameter.api.Request;
import org.jdiameter.api.app.AppSession;
import org.jdiameter.api.app.StateChangeListener;
import org.jdiameter.client.api.ISessionFactory;
import org.jdiameter.server.impl.helpers.Parameters;
import org.jdiameter.server.impl.helpers.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractConnector implements EventListener<Request, Answer>, NetworkReqListener, StateChangeListener<AppSession>{
	protected final Logger logger = LoggerFactory.getLogger(AbstractConnector.class);
	
	private StackConnector stack;
	private ApplicationId applicationId;
	
	private long vendorId;
	private long authApplicationId;
	private long acctApplicationId;
	
	private ISessionFactory sessionFactory;
	private Configuration config;
	// may move to Set to allow uniqueness
	Set<ApplicationId> ids = null;
	Set<String> relmNames = null;
	
	public AbstractConnector() {
		this.stack = new StackConnector();
	}

	public void init(String configFile, String clientID, ApplicationId appId) throws Exception {
		this.applicationId = appId;
		this.vendorId = this.applicationId.getVendorId();
		this.authApplicationId = this.applicationId.getAuthAppId();
		this.acctApplicationId = this.applicationId.getAcctAppId();
		this.ids = new HashSet<ApplicationId>();
		this.relmNames = new HashSet<String>();

		this.config = new XMLConfiguration(configFile);
		for (Configuration r : config.getChildren(Parameters.RealmTable.ordinal())) {
			Configuration[] rEntry = r.getChildren(Parameters.RealmEntry.ordinal()); 
			for (Configuration e : rEntry) {
				String relmName = e.getStringValue(Parameters.RealmName.ordinal(), "no value");
				Configuration[] app = e.getChildren(Parameters.ApplicationId.ordinal());
				for (Configuration a : app) {
					long v = a.getLongValue(Parameters.VendorId.ordinal(), -1);
					long au = a.getLongValue(Parameters.AuthApplId.ordinal(), -1);
					long ac = a.getLongValue(Parameters.AcctApplId.ordinal(), -1);
					if (v == this.vendorId && au == this.authApplicationId && ac == this.acctApplicationId) {
						ids.add(ApplicationId.createByAuthAppId(v, au));
						relmNames.add(relmName);
					}
				}
			}
		}
		
		this.stack = new StackConnector();
		

		this.stack.init(configFile, this, this, clientID, ids.toArray(new ApplicationId[ids.size()])); // lets always
		//this.stack.start(Mode.ANY_PEER, 10, TimeUnit.SECONDS);
		
		this.sessionFactory = (ISessionFactory) this.stack.getSessionFactory();
	}
	
	public StackConnector getStack() {
		return stack;
	}

	public ApplicationId getApplicationId() {
		return applicationId;
	}
	
	public ISessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void start() throws IllegalDiameterStateException, InternalException {
		getStack().start();
	}

	public void start(Mode mode, long timeOut, TimeUnit timeUnit)
			throws IllegalDiameterStateException, InternalException {
		getStack().start(mode, timeOut, timeUnit);
	}

	public void stop(long timeOut, TimeUnit timeUnit, int disconnectCause)
			throws IllegalDiameterStateException, InternalException {
		getStack().stop(timeOut, timeUnit, disconnectCause);
	}

	public void stop(int disconnectCause) {
		getStack().stop(disconnectCause);
	}



	@Override
	public void stateChanged(Enum oldState, Enum newState) {
	}

	@Override
	public void stateChanged(AppSession source, Enum oldState, Enum newState) {
	}

	@Override
	public Answer processRequest(Request request) {
	    logger.error("processRequest - Received \"Request\" event, request[" + request + "]");
		return null;
	}

	@Override
	public void receivedSuccessMessage(Request request, Answer answer) {
		logger.error("receivedSuccessMessage -Received \"SuccessMessage\" event, request[" + request + "], answer[" + answer + "]");
	}

	@Override
	public void timeoutExpired(Request request) {
	    logger.error("timeoutExpired - Received \"Timoeout\" event, request[" + request + "]");
	}

}
