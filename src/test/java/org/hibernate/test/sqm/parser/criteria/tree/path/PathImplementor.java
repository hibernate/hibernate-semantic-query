/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree.path;

import javax.persistence.criteria.Path;
import javax.persistence.metamodel.Attribute;

import org.hibernate.test.sqm.parser.criteria.tree.PathSource;
import org.hibernate.test.sqm.parser.criteria.tree.expression.ExpressionImplementor;

/**
 * Implementation contract for the JPA {@link Path} interface.
 *
 * @author Steve Ebersole
 */
public interface PathImplementor<X>
		extends ExpressionImplementor<X>, org.hibernate.sqm.parser.criteria.spi.path.PathImplementor<X>, PathSource<X> {
	/**
	 * Retrieve reference to the attribute this path represents.
	 *
	 * @return The metamodel attribute.
	 */
	Attribute<?, ?> getAttribute();

	/**
	 * Defines handling for the JPA 2.1 TREAT down-casting feature.
	 *
	 * @param treatAsType The type to treat the path as.
	 * @param <T> The parameterized type representation of treatAsType.
	 *
	 * @return The properly typed view of this path.
	 */
	<T extends X> PathImplementor<T> treatAs(Class<T> treatAsType);
}
