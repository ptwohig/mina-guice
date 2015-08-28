package org.apache.mina.guice.filter;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

import com.google.inject.Injector;

@Singleton
public class GuiceProtocolCodecFactory implements ProtocolCodecFactory {

	@Inject
	private Injector injector;

	@Override
	public ProtocolEncoder getEncoder(IoSession session) throws Exception {
		return injector.getInstance(ProtocolEncoder.class);
	}

	@Override
	public ProtocolDecoder getDecoder(IoSession session) throws Exception {
		return injector.getInstance(ProtocolDecoder.class);
	}

}