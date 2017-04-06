package com.att.scef.hss;

import org.jdiameter.api.Answer;
import org.jdiameter.api.Request;
import org.jdiameter.api.s6a.events.JAuthenticationInformationAnswer;
import org.jdiameter.api.s6a.events.JAuthenticationInformationRequest;
import org.jdiameter.api.s6a.events.JCancelLocationAnswer;
import org.jdiameter.api.s6a.events.JCancelLocationRequest;
import org.jdiameter.api.s6a.events.JDeleteSubscriberDataAnswer;
import org.jdiameter.api.s6a.events.JDeleteSubscriberDataRequest;
import org.jdiameter.api.s6a.events.JInsertSubscriberDataAnswer;
import org.jdiameter.api.s6a.events.JInsertSubscriberDataRequest;
import org.jdiameter.api.s6a.events.JNotifyAnswer;
import org.jdiameter.api.s6a.events.JNotifyRequest;
import org.jdiameter.api.s6a.events.JPurgeUEAnswer;
import org.jdiameter.api.s6a.events.JPurgeUERequest;
import org.jdiameter.api.s6a.events.JResetAnswer;
import org.jdiameter.api.s6a.events.JResetRequest;
import org.jdiameter.api.s6a.events.JUpdateLocationAnswer;
import org.jdiameter.api.s6a.events.JUpdateLocationRequest;
import org.jdiameter.common.api.app.s6a.IS6aMessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HssS6aMessages implements IS6aMessageFactory {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public JUpdateLocationRequest createUpdateLocationRequest(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JUpdateLocationAnswer createUpdateLocationAnswer(Answer answer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JCancelLocationRequest createCancelLocationRequest(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JCancelLocationAnswer createCancelLocationAnswer(Answer answer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JAuthenticationInformationRequest createAuthenticationInformationRequest(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JAuthenticationInformationAnswer createAuthenticationInformationAnswer(Answer answer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JInsertSubscriberDataRequest createInsertSubscriberDataRequest(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JInsertSubscriberDataAnswer createInsertSubscriberDataAnswer(Answer answer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JDeleteSubscriberDataRequest createDeleteSubscriberDataRequest(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JDeleteSubscriberDataAnswer createDeleteSubscriberDataAnswer(Answer answer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JPurgeUERequest createPurgeUERequest(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JPurgeUEAnswer createPurgeUEAnswer(Answer answer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JResetRequest createResetRequest(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JResetAnswer createResetAnswer(Answer answer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JNotifyRequest createNotifyRequest(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JNotifyAnswer createNotifyAnswer(Answer answer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getApplicationId() {
		// TODO Auto-generated method stub
		return 16777308;
	}

}
