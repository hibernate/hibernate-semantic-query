/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query;

import org.hibernate.sqm.domain.Type;

/**
 * Describes a parameter declared in the query.
 *
 * @author Steve Ebersole
 */
public interface Parameter {
	/**
	 * If this represents a named parameter, return that parameter name;
	 * otherwise return {@code null}.
	 *
	 * @return The parameter name, or {@code null} if not a named parameter
	 */
	String getName();

	/**
	 * If this represents a positional parameter, return that parameter position;
	 * otherwise return {@code null}.
	 *
	 * @return The parameter position
	 */
	Integer getPosition();

	/**
	 * Can a collection/array of values be bound to this parameter?
	 * <P/>
	 * This is allowed in very limited contexts within the query:<ol>
	 *     <li>as the value of an IN predicate if the only value is a single param</li>
	 *     <li>(in non-strict JPA mode) as the final vararg to a function</li>
	 * </ol>
	 *
	 * @return {@code true} if binding collection/array of values is allowed
	 * for this parameter; {@code false} otherwise.
	 */
	boolean allowMultiValuedBinding();

	/**
	 * Based on the context it is declared, what is the anticipated Type for
	 * bind values?
	 * <p/>
	 * NOTE: If {@link #allowMultiValuedBinding()} is true, this will indicate
	 * the Type of the individual values.
	 *
	 * @return The anticipated Type.
	 */
	Type getAnticipatedType();
}
