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
public interface FilterNameBindingBuilder {

	/**
	 * Indicates the name of the filter.  The filter will be bound using the {@link Named}
	 * annotation with the given name.  The name will also be used to determine the name
	 * of the filter when added to the {@link IoFilterChain}.
	 *
	 * @see {@link IoFilterChain#addLast(String, IoFilter)}
	 * @see {@link Named#value()}
	 *
	 * @param name the name of the filter.
	 *
	 * @return an instance of {@link LinkedBindingBuilder}
	 */
	FilterSequenceBindingBuilder named(final String name);

}
