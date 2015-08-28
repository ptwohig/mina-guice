package org.apache.mina.guice;

import javax.inject.Named;

import org.apache.mina.core.filterchain.IoFilter;

import com.google.inject.binder.LinkedBindingBuilder;
import org.apache.mina.core.filterchain.IoFilterChain;

/**
 * Used to name filters as they are added to the {@link IoFilterChain}.
 * 
 * @author "Patrick Twohig" patrick@namazustudios.com
 *
 */
public interface FilterBinding {

	/**
	 * Indicates the name of the filter.  This is not to be confused
	 * with using the {@link Named} annotation. The name is strictly
	 * used within the context of the filter chain.
	 *
	 * @see {@link IoFilterChain#addLast(String, IoFilter)}
	 * 
	 * @param name the name, to be passed to {@link IoFilterChain#addLast(String, IoFilter)}
	 *
	 * @return an instance of {@link LinkedBindingBuilder}
	 */
	LinkedBindingBuilder<IoFilter> named(final String name);

}
