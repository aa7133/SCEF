package com.att.scef.scef;

import org.jdiameter.api.Answer;
import org.jdiameter.api.Request;
import org.jdiameter.api.s6t.events.JConfigurationInformationAnswer;
import org.jdiameter.api.s6t.events.JConfigurationInformationRequest;
import org.jdiameter.api.s6t.events.JNIDDInformationAnswer;
import org.jdiameter.api.s6t.events.JNIDDInformationRequest;
import org.jdiameter.api.s6t.events.JReportingInformationAnswer;
import org.jdiameter.api.s6t.events.JReportingInformationRequest;
import org.jdiameter.common.api.app.s6t.IS6tMessageFactory;

public final class SCEFS6tMessages implements IS6tMessageFactory {

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
	public JNIDDInformationRequest createNIDDInformationRequest(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JNIDDInformationAnswer createNIDDInformationAnswer(Answer answer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getApplicationId() {
		return 16777345;
	}


}
