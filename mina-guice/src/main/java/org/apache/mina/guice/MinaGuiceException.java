package org.apache.mina.guice;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

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

	private final List<Throwable> causes;

	public MinaGuiceException() {
		causes = new ImmutableList.Builder<Throwable>().build();
	}

	public MinaGuiceException(String message, Throwable cause) {
		super(message, cause);

		causes = cause == null ?
			new ImmutableList.Builder<Throwable>().build() :
			new ImmutableList.Builder<Throwable>().add(cause).build();

	}

	public MinaGuiceException(String message) {
		super(message);
		causes = new ImmutableList.Builder<Throwable>().build();
	}

	public MinaGuiceException(Throwable cause) {
		super(cause);

		causes = cause == null ?
				new ImmutableList.Builder<Throwable>().build() :
				new ImmutableList.Builder<Throwable>().add(cause).build();

	}

	public MinaGuiceException(final Collection<? extends Throwable> causes) {

		super(causes.toString(), causes.isEmpty() ? null : causes.iterator().next());

		final Collection<? extends Throwable> filtered = 
			Collections2.filter(causes, new Predicate<Throwable>() {

				@Override
				public boolean apply(final Throwable input) {
					return input != null;
				}

		});

		this.causes = new ImmutableList.Builder<Throwable>().addAll(filtered).build();

	}

	@Override
	public String toString() {
		return "GuiceIoFilterLifeCycleException [causes=" + causes + "]";
	}

	@Override
	public void printStackTrace(PrintStream stream) {

		stream.println("Multiple causes: ");

		int count = 1;
		for (Throwable th : causes) {
			stream.println(String.format("Cause %d: ", ++count));
			th.printStackTrace(stream);
		}

	}

	@Override
	public void printStackTrace(PrintWriter writer) {

		writer.println("Multiple causes: ");

		int count = 1;
		for (Throwable th : causes) {
			writer.println(String.format("Cause %d: ", ++count));
			th.printStackTrace(writer);
		}

	}

}
