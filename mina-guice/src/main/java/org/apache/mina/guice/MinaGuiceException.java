package org.apache.mina.guice;

/**
 * An exception type useful for collecting multiple causes of failure into a
 * single exception.  This type has the methods overridden to print all cuases
 * when logging.  Additionally, this provides access to the underlying list
 * of causes as an immutable list.
 * 
 * @author "Patrick Twohig" patrick@namazustudios.com
 *
 */
public class MinaGuiceException extends RuntimeException {

	private static final long serialVersionUID = -4490627315197654753L;

    public MinaGuiceException(Throwable cause) {
        super(cause);
    }

    public MinaGuiceException(String message, Throwable cause) {
		super(message, cause);
	}

}
