/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.select;

import org.hibernate.sqm.domain.Type;

/**
 * @author Steve Ebersole
 */
public interface DynamicInstantiationTarget<T> {
	enum Nature {
		CLASS,
		MAP,
		LIST
	}

	/**
	 * Retrieves the enum describing the nature of this target.
	 *
	 * @return The nature of this target.
	 */
	Nature getNature();

	/**
	 * For {@link Nature#CLASS} this will return the descriptor for the Class to be instantiated.  For
	 * {@link Nature#MAP} and {@link Nature#LIST} this will return the descriptor for {@code Map.class}
	 * and {@code List.class} respectively.
	 *
	 * @return The type to be instantiated.
	 */
	Type getTargetType();
}
