package org.apache.mina.guice.filter;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

import com.google.inject.Injector;

/**
 * Defers to a set of injected {@link Provider} instances to get the instances of
 * {@link ProtocolEncoder} and {@link ProtocolDecoder}.
 */
@Singleton
public class InjectProtocolCodecFactory implements ProtocolCodecFactory {

	@Inject
	private Provider<ProtocolEncoder> protocolEncoderProvider;

	@Inject
	private Provider<ProtocolDecoder> protocolDecoderProvider;

	@Override
	public ProtocolEncoder getEncoder(IoSession session) throws Exception {
		return protocolEncoderProvider.get();
	}

	@Override
	public ProtocolDecoder getDecoder(IoSession session) throws Exception {
		return protocolDecoderProvider.get();
	}

}
