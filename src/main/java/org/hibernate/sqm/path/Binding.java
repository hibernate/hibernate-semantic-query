/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.path;

import org.hibernate.sqm.domain.Bindable;
import org.hibernate.sqm.query.expression.SqmExpression;

/**
 * Very similar in concept to Bindable, but here represents a specific binding
 * of a Bindable.  At the highest level might be one of:<ul>
 *     <li>{@link AttributeBinding}</li>
 *     <li>{@link AttributeBindingSource}</li>
 * </ul>
 *
 * @author Steve Ebersole
 */
public interface Binding extends SqmExpression {
	/**
	 * Obtain the Bindable referenced by this FromElement.
	 *
	 * @return The bound type (Bindable)
	 */
	Bindable getBoundModelType();

	/**
	 * Obtain a loggable representation of this path expression, ideally back to
	 * its source form.
	 *
	 * @return
	 */
	String asLoggableText();

	FromElementBinding getBoundFromElementBinding();
}
