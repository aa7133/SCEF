package com.att.scef.hss;

import org.jdiameter.api.Answer;
import org.jdiameter.api.Request;
import org.jdiameter.api.s6t.events.JConfigurationInformationAnswer;
import org.jdiameter.api.s6t.events.JConfigurationInformationRequest;
import org.jdiameter.api.s6t.events.JNIDDInformationAnswer;
import org.jdiameter.api.s6t.events.JNIDDInformationRequest;
import org.jdiameter.api.s6t.events.JReportingInformationAnswer;
import org.jdiameter.api.s6t.events.JReportingInformationRequest;
import org.jdiameter.common.api.app.s6t.IS6tMessageFactory;
import org.jdiameter.common.impl.app.s6t.JConfigurationInformationAnswerImpl;
import org.jdiameter.common.impl.app.s6t.JNIDDInformationAnswerImpl;
import org.jdiameter.common.impl.app.s6t.JReportingInformationRequestImpl;

public class HssS6tMessages implements IS6tMessageFactory {

	@Override
	public JConfigurationInformationAnswer createConfigurationInformationAnswer(Answer answer) {
		return new JConfigurationInformationAnswerImpl(answer);
	}

	@Override
	public JConfigurationInformationRequest createConfigurationInformationRequest(Request request) {
		return null;
	}

	@Override
	public JNIDDInformationAnswer createNIDDInformationAnswer(Answer answer) {
		return new JNIDDInformationAnswerImpl(answer);
	}

	@Override
	public JNIDDInformationRequest createNIDDInformationRequest(Request request) {
		return null;
	}

	@Override
	public JReportingInformationAnswer createReportingInformationAnswer(Answer answer) {
		return null;
	}

	@Override
	public JReportingInformationRequest createReportingInformationRequest(Request request) {
		return new JReportingInformationRequestImpl(request);
	}

	@Override
	public long getApplicationId() {
		// TODO Auto-generated method stub
		return 16777345;
	}

}
