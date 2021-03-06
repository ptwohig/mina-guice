package org.apache.mina.guice;

import com.google.inject.binder.LinkedBindingBuilder;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.guice.filter.GuiceIoFilterChainBuilder;

/**
 * Used to control the sequence of the filters as they are added to the
 * {@link GuiceIoFilterChainBuilder}.
 *
 * Created by patricktwohig on 8/31/15.
 */
public interface FilterSequenceBindingBuilder<FilterT extends IoFilter> {

    /**
     * Places the filter at the beginning such that when the {@link IoFilterChain} is built,
     * it will be placed at the beginning.  This will move any existing filters further
     * in the chain.
     *
     * @return AnnotatedBindingBuilder see the EDSL examples at {@link com.google.inject.Binder}
     */
    LinkedBindingBuilder<FilterT> atBeginningOfChain();

    /**
     * Places the filter after the filter with the given mame.
     *
     * @param filterName the filter name
     * @return AnnotatedBindingBuilder see the EDSL examples at {@link com.google.inject.Binder}
     * @throws IllegalArgumentException if the filter with the given name does not exist.
     *
     */
    LinkedBindingBuilder<FilterT> after(String filterName);

    /**
     * Places the filter before the filter with the given name.
     *
     * @param filterName the filter name.
     * @return AnnotatedBindingBuilder see the EDSL examples at {@link com.google.inject.Binder}
     * @throws IllegalArgumentException if the filter with the given name does not exist.
     *
     */
    LinkedBindingBuilder<FilterT> before(String filterName);

    /**
     * Places filter at the end of the chain after any previously defined filters.
     * @return AnnotatedBindingBuilder see the EDSL examples at {@link com.google.inject.Binder}
     */
    LinkedBindingBuilder<FilterT> atAndOfFilterChain();

}
