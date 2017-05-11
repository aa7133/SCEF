package com.att.scef.interfaces;

import java.io.FileInputStream;
import java.util.concurrent.TimeUnit;

import org.jdiameter.api.ApplicationId;
import org.jdiameter.api.IllegalDiameterStateException;
import org.jdiameter.api.InternalException;
import org.jdiameter.api.Mode;
import org.jdiameter.api.OverloadException;
import org.jdiameter.api.RouteException;
import org.jdiameter.api.app.AppAnswerEvent;
import org.jdiameter.api.app.AppRequestEvent;
import org.jdiameter.api.app.AppSession;
import org.jdiameter.api.s6t.ClientS6tSession;
import org.jdiameter.api.s6t.ClientS6tSessionListener;
import org.jdiameter.api.s6t.ServerS6tSession;
import org.jdiameter.api.s6t.events.JConfigurationInformationAnswer;
import org.jdiameter.api.s6t.events.JConfigurationInformationRequest;
import org.jdiameter.api.s6t.events.JNIDDInformationAnswer;
import org.jdiameter.api.s6t.events.JNIDDInformationRequest;
import org.jdiameter.api.s6t.events.JReportingInformationRequest;
import org.jdiameter.common.impl.app.s6t.S6tSessionFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.scef.utils.TBase;

public abstract class S6tAbstractClient extends TBase implements ClientS6tSessionListener {
	private final Logger logger = LoggerFactory.getLogger(S6tAbstractClient.class);
	private ClientS6tSession clientS6tSession;
	protected S6tSessionFactoryImpl s6tSessionFactory;
	private static final long VENDOR_ID = 10415;
	private static final long AUTH_APPLICATION_ID = 16777345;

	public void init(FileInputStream configStream, String clientID){
		try {
			super.init(configStream, clientID, ApplicationId.createByAuthAppId(VENDOR_ID, AUTH_APPLICATION_ID));
		    this.s6tSessionFactory = new S6tSessionFactoryImpl(this.sessionFactory);
		    this.sessionFactory.registerAppFacory(ServerS6tSession.class, s6tSessionFactory);
			this.sessionFactory.registerAppFacory(ClientS6tSession.class, s6tSessionFactory);

			s6tSessionFactory.setClientSessionListener(this);

			this.clientS6tSession = this.sessionFactory.getNewAppSession(
					this.sessionFactory.getSessionId("S6t"), getApplicationId(), ClientS6tSession.class,
					(Object[]) null);
		} catch (InternalException e) {
			e.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();
		}
	    finally {
	      try {
	        configStream.close();
	      }
	      catch (Exception e) {
	        e.printStackTrace();
	      }
	    }
	}
	
	 public void start() throws IllegalDiameterStateException, InternalException {
	    stack.start();
	  }

	  public void start(Mode mode, long timeOut, TimeUnit timeUnit) throws IllegalDiameterStateException, InternalException {
	    stack.start(mode, timeOut, timeUnit);
	  }


	@Override
	public void doOtherEvent(AppSession session, AppRequestEvent request, AppAnswerEvent answer)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		logger.error("Received \"S6t Other\" event, request[" + request + "], answer[" + answer + "], on session["
				+ session + "]");
	}

	@Override
	public void doConfigurationInformationAnswerEvent(ClientS6tSession session,
			JConfigurationInformationRequest request, JConfigurationInformationAnswer answer)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		logger.error("Received \"S6t CIA\" event, request[" + request + "], answer[" + answer + "], on session["
				+ session + "]");
	}

	@Override
	public void doReportingInformationRequestEvent(ClientS6tSession session, JReportingInformationRequest request)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		logger.error("Received \"S6t RIR\" event, request[" + request + "], on session[" + session + "]");
	}

	@Override
	public void doNIDDInformationAnswerEvent(ClientS6tSession session, JNIDDInformationRequest request,
			JNIDDInformationAnswer answer)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		logger.error("Received \"S6t NIA\" event, request[" + request + "], answer[" + answer + "], on session["
				+ session + "]");
	}

	public String getSessionId() {
		return this.clientS6tSession.getSessionId();
	}

	public ClientS6tSession getSession() {
		return this.clientS6tSession;
	}

	public S6tSessionFactoryImpl getS6tSessionFactory() {
		return s6tSessionFactory;
	}
}
