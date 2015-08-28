package org.apache.mina.guice;

import javax.inject.Named;

import org.apache.mina.core.filterchain.IoFilter;

import com.google.inject.binder.LinkedBindingBuilder;

/**
 * Used to named filters.
 * 
 * @author patricktwohig
 *
 */
public interface FilterBinding {

	/**
	 * Indicates the name of the filter.  This is not to be confused
	 * with using the {@link Named} annotation. The name is strictly
	 * used within the 
	 * 
	 * @param name
	 * @return
	 */
	LinkedBindingBuilder<IoFilter> named(final String name);

}
