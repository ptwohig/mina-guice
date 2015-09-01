package org.apache.mina.guice.filter;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;

import com.google.inject.Injector;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * Defers to the {@link Injector} to get the {@link ProtocolEncoder} and the
 * {@link ProtocolDecoder} instances. 
 * 
 * @author patricktwohig
 *
 */
@Singleton
public class InjectProtocolCodecFilter extends ProtocolCodecFilter {

	@Inject
	public InjectProtocolCodecFilter(final InjectProtocolCodecFactory guiceProtocolCodecFactory) {
		super(guiceProtocolCodecFactory);
	}

}
