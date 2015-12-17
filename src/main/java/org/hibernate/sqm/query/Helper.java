/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query;

import org.hibernate.sqm.domain.Bindable;
import org.hibernate.sqm.domain.ManagedType;

/**
 * @author Steve Ebersole
 */
public class Helper {
	public static <T> T firstNonNull(T v1, T v2) {
		return v1 != null ? v1 : v2;
	}

	public static <T> T firstNonNull(T... values) {
		if ( values != null ) {
			for ( T value : values ) {
				if ( value != null ) {
					return value;
				}
			}
		}

		return null;
	}

	private Helper() {
	}

	public static ManagedType determineManagedType(Bindable bindableModelDescriptor) {
		if ( bindableModelDescriptor.getBoundType() instanceof ManagedType ) {
			return (ManagedType) bindableModelDescriptor.getBoundType();
		}

		return null;
	}
}
