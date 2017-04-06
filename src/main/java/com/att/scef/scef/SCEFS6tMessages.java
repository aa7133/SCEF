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
import org.jdiameter.common.impl.app.s6t.JConfigurationInformationRequestImpl;
import org.jdiameter.common.impl.app.s6t.JNIDDInformationRequestImpl;
import org.jdiameter.common.impl.app.s6t.JReportingInformationAnswerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SCEFS6tMessages implements IS6tMessageFactory {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());


	@Override
	public JConfigurationInformationRequest createConfigurationInformationRequest(Request request) {
		return new JConfigurationInformationRequestImpl(request);
	}

	@Override
	public JConfigurationInformationAnswer createConfigurationInformationAnswer(Answer answer) {
		if (logger.isErrorEnabled()) {
			logger.error("S6t Configure-Information-Answer (CIA) shuld not be called in this state");
		}
		return null;
	}

	@Override
	public JReportingInformationRequest createReportingInformationRequest(Request request) {
		if (logger.isErrorEnabled()) {
			logger.error("S6t Reporting-Information-Request (RIR) shuld not be called in this state");
		}
		return null;
	}

	@Override
	public JReportingInformationAnswer createReportingInformationAnswer(Answer answer) {
		return new JReportingInformationAnswerImpl(answer);
	}

	@Override
	public JNIDDInformationRequest createNIDDInformationRequest(Request request) {
		return new JNIDDInformationRequestImpl(request);
	}

	@Override
	public JNIDDInformationAnswer createNIDDInformationAnswer(Answer answer) {
		if (logger.isErrorEnabled()) {
			logger.error("S6t NIDD-Information-Answer (NIA) shuld not be called in this state");
		}
		return null;
	}

	@Override
	public long getApplicationId() {
		return 16777345;
	}


}
