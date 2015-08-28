package org.apache.mina.guice.filter;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;

import com.google.inject.Injector;

/**
 * Defers to the {@link Injector} to get the {@link ProtcocolEndoer} and the
 * {@link ProtocolDecoder} instances. 
 * 
 * @author patricktwohig
 *
 */
@Singleton
public class GuiceProtocolCodecFilter extends ProtocolCodecFilter {

	@Inject
	public GuiceProtocolCodecFilter(final Injector injector) {
		super(injector.getInstance(GuiceProtocolCodecFactory.class));
	}

}
