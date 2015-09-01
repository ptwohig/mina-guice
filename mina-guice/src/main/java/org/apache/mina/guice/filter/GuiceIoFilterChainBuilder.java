package org.apache.mina.guice.filter;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.filterchain.IoFilterChainBuilder;

import com.google.inject.Provider;
import org.apache.mina.guice.FilterNameBindingBuilder;
import org.apache.mina.guice.MinaModule;

/**
 * This type is automatically configured by the {@link MinaModule}. It will
 * build the {@link IoFilterChain} based on Guice's configured bindings. For
 * each invocation of the method
 * {@link GuiceIoFilterChainBuilder#buildFilterChain(IoFilterChain)} each
 * {@link Provider} will called exactly once to build the chain.
 *
 * Filters are inserted in the filter chain in the order in which they are bound and are
 * named according to the value passed to {@link FilterNameBindingBuilder#named(String)}.
 *
 * @author "Patrick Twohig" patrick@namazustudios.com
 * 
 */
@Singleton
public class GuiceIoFilterChainBuilder implements IoFilterChainBuilder {

	@Inject
	private Injector injector;

	@Inject
	@Named(MinaModule.ORIGINAL_FILTER_SEQUENCE)
	private List<String> filterNames;

	@Override
	public void buildFilterChain(final IoFilterChain chain) throws Exception {

		for (final String filterName : filterNames) {
			final Key<IoFilter> ioFilterKey = Key.get(IoFilter.class, Names.named(filterName));
			final IoFilter ioFilter = injector.getInstance(ioFilterKey);
			chain.addLast(filterName, ioFilter);
		}

	}

}
