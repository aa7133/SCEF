package com.att.scef.scef;

import org.jdiameter.api.Answer;
import org.jdiameter.api.Request;
import org.jdiameter.api.t6a.events.JConfigurationInformationAnswer;
import org.jdiameter.api.t6a.events.JConfigurationInformationRequest;
import org.jdiameter.api.t6a.events.JConnectionManagementAnswer;
import org.jdiameter.api.t6a.events.JConnectionManagementRequest;
import org.jdiameter.api.t6a.events.JMO_DataAnswer;
import org.jdiameter.api.t6a.events.JMO_DataRequest;
import org.jdiameter.api.t6a.events.JMT_DataAnswer;
import org.jdiameter.api.t6a.events.JMT_DataRequest;
import org.jdiameter.api.t6a.events.JReportingInformationAnswer;
import org.jdiameter.api.t6a.events.JReportingInformationRequest;
import org.jdiameter.common.api.app.t6a.IT6aMessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SCEFT6aMessages implements IT6aMessageFactory {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public JConfigurationInformationRequest createConfigurationInformationRequest(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JConfigurationInformationAnswer createConfigurationInformationAnswer(Answer answer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JReportingInformationRequest createReportingInformationRequest(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JReportingInformationAnswer createReportingInformationAnswer(Answer answer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JConnectionManagementRequest createConnectionManagementRequest(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JConnectionManagementAnswer createConnectionManagementAnswer(Answer answer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JMO_DataRequest createMO_DataRequest(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JMO_DataAnswer createMO_DataAnswer(Answer answer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JMT_DataRequest createMT_DataRequest(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JMT_DataAnswer createMT_DataAnswer(Answer answer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getApplicationId() {
		return 16777346;
	}

}
