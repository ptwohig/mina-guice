package org.apache.mina.guice;

import java.lang.reflect.Method;

import javax.inject.Provider;

import com.google.inject.Module;
import com.google.inject.TypeLiteral;


/**
 * Contains some static methods useful for finding bindings in in an injector.
 * Generally, you shouldn't use this directly.  It's useful for those who
 * wish to extend the MINA Guice bindings for other purposes.
 * 
 * @author "Patrick Twohig" patrick@namazustudios.com
 *
 */
class TypeUtil {

	private TypeUtil() {}

	public static <T> boolean isLiteralAssignableFrom(final Class<T> cls, final TypeLiteral<?> t) {
		if (Provider.class.isAssignableFrom(t.getRawType())) {
			try {
				return cls.isAssignableFrom(t.getReturnType(t.getRawType().getDeclaredMethod("get")).getRawType());
			} catch (NoSuchMethodException ex) {
				throw new MinaGuiceException(ex);
			}
		} else if (Module.class.isAssignableFrom(t.getRawType())) {

			for (final Method method : t.getRawType().getDeclaredMethods()) {
				if (cls.isAssignableFrom(t.getReturnType(method).getRawType())) {
					return true;
				}
			}

			return false;

		} else {
			return cls.isAssignableFrom(t.getRawType());
		}
	}

}
