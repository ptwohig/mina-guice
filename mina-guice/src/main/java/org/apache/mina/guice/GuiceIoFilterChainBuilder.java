package org.apache.mina.guice;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.filterchain.IoFilterChainBuilder;

import com.google.inject.Provider;

/**
 * This type is automatically configured by the {@link MinaModule}. It will
 * build the {@link IoFilterChain} based on Guice's configured bindings. For
 * each invocation of the method
 * {@link GuiceIoFilterChainBuilder#buildFilterChain(IoFilterChain)} each
 * {@link Provider} will called exactly once to build the chain. Filters are
 * inserted in the filter chain in the order in which they are bound and are
 * named according to the usage of the {@link Named} annotation.
 * 
 * This does not actually insert filters directly.  Rather this inserts
 * the filters into a separate chain and then uses that to dispatch the Guice 
 * managed filters.
 * 
 * @author patricktwohig
 * 
 */
@Singleton
class GuiceIoFilterChainBuilder implements IoFilterChainBuilder {


	@Inject
	private	Map<String, Provider<IoFilter>> filters;

	@Override
	public void buildFilterChain(final IoFilterChain chain) throws Exception {

		for (final Map.Entry<String, Provider<IoFilter>> entry : filters.entrySet()) {
			chain.addLast(entry.getKey(), entry.getValue().get());
		}

	}

}
